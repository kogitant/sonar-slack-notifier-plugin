package com.koant.sonar.slacknotifier.extension.task;

import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.CHANNEL;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.CONFIG;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.ENABLED;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.INCLUDE_BRANCH;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.PROJECT;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.QG_FAIL_ONLY;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.USER;
import static com.koant.sonar.slacknotifier.extension.task.Analyses.PROJECT_KEY;
import static java.lang.String.format;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.sonar.api.ce.posttask.Branch;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.i18n.I18n;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

/**
 * Created by 616286 on 3.6.2016.
 * Modified by poznachowski
 */
public class SlackPostProjectAnalysisTaskTest {

    private static final String HOOK = "hook";

    private CaptorPostProjectAnalysisTask postProjectAnalysisTask;
    private SlackPostProjectAnalysisTask task;
    private Slack slackClient;
    private Settings settings;
    private Configuration configuration;

    private I18n i18n;

    @Before
    public void before() throws IOException {
        postProjectAnalysisTask = new CaptorPostProjectAnalysisTask();
        this.settings = new MapSettings();
        settings.setProperty(ENABLED.property(), "true");
        settings.setProperty(SlackNotifierProp.HOOK.property(), HOOK);
        settings.setProperty(CHANNEL.property(), "channel");
        settings.setProperty(USER.property(), "user");
        settings.setProperty(CONFIG.property(), PROJECT_KEY);
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + PROJECT.property(), PROJECT_KEY);
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + CHANNEL.property(), "#random");
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + QG_FAIL_ONLY.property(), "false");
        settings.setProperty("sonar.core.serverBaseURL", "http://your.sonar.com/");
        this.configuration = new ConfigurationBridge(settings);
        slackClient = Mockito.mock(Slack.class);
        WebhookResponse webhookResponse = WebhookResponse.builder().code(200).build();
        when(slackClient.send(anyString(), any(Payload.class))).thenReturn(webhookResponse);
        i18n = Mockito.mock(I18n.class);
        Mockito.when(i18n.message(Matchers.any(Locale.class), anyString(), anyString())).thenAnswer(
            (Answer<String>) invocation -> (String) invocation.getArguments()[2]);
        task = new SlackPostProjectAnalysisTask(slackClient, configuration, i18n);
    }

    @Test
    public void shouldCall() throws Exception {
        Analyses.simple(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verify(slackClient, times(1)).send(eq(HOOK), any(Payload.class));
    }

    @Test
    public void shouldSkipIfPluginDisabled() {
        settings.setProperty(ENABLED.property(), "false");
        Analyses.simple(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verifyZeroInteractions(slackClient);
    }

    @Test
    public void shouldSkipIfNoConfigFound() {
        Analyses.simpleDifferentKey(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verifyZeroInteractions(slackClient);
    }

    @Test
    public void shouldSkipIfReportFailedQualityGateButOk() {
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + QG_FAIL_ONLY.property(), "true");
        Analyses.simple(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verifyZeroInteractions(slackClient);
    }

    @Test
    public void shouldIncludeBranchWhenEnabledAndPresent() throws IOException {
        String branchName = RandomStringUtils.random(13);
        settings.setProperty(INCLUDE_BRANCH.property(), "true");
        Analyses.withBranch(postProjectAnalysisTask, newBranch(false, branchName));
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        ArgumentCaptor<Payload> arg = ArgumentCaptor.forClass(Payload.class);
        Mockito.verify(slackClient, times(1)).send(anyString(), arg.capture());
        Assert.assertTrue(arg.getValue().getText().contains(format("analyzed for branch [%s]", branchName)));
    }

    @Test
    public void shouldNotIncludeBranchWhenDisabled() throws IOException {

        settings.setProperty(INCLUDE_BRANCH.property(), "false");
        Analyses.withBranch(postProjectAnalysisTask, newBranch(false, "branchName"));
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        ArgumentCaptor<Payload> arg = ArgumentCaptor.forClass(Payload.class);
        Mockito.verify(slackClient, times(1)).send(anyString(), arg.capture());
        Assert.assertFalse(arg.getValue().getText().contains("branch"));
    }

    @Test
    public void shouldNotIncludeMainBranch() throws IOException {

        settings.setProperty(INCLUDE_BRANCH.property(), "true");
        Analyses.withBranch(postProjectAnalysisTask, newBranch(true, "branchName"));
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        ArgumentCaptor<Payload> arg = ArgumentCaptor.forClass(Payload.class);
        Mockito.verify(slackClient, times(1)).send(anyString(), arg.capture());
        Assert.assertFalse(arg.getValue().getText().contains("branch"));
    }


    private static Branch newBranch(boolean main, String name) {

        return new Branch() {

            @Override
            public boolean isMain() {

                return main;
            }

            @Override
            public Optional<String> getName() {

                return Optional.of(name);
            }

            @Override
            public Branch.Type getType() {

                return Type.SHORT;
            }
        };
    }
}
