package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.api.webhook.Payload;
import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.i18n.I18n;

import java.io.IOException;
import java.util.Locale;

import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.*;
import static com.koant.sonar.slacknotifier.extension.task.Analyses.PROJECT_KEY;
import static org.mockito.Mockito.*;

/**
 * Created by 616286 on 3.6.2016.
 * Modified by poznachowski
 */
public class SlackPostProjectAnalysisTaskTest {

    private static final String HOOK          = "http://hook";
    private static final String DIFFERENT_KEY = "different:key";

    private CaptorPostProjectAnalysisTask postProjectAnalysisTask;

    private SlackPostProjectAnalysisTask task;

    private SlackHttpClient httpClient;

    private Settings settings;
    private I18n     i18n;

    @Before
    public void before() throws IOException {
        this.postProjectAnalysisTask = new CaptorPostProjectAnalysisTask();
        this.settings = new MapSettings();
        this.settings.setProperty(ENABLED.property(), "true");
        this.settings.setProperty(SlackNotifierProp.HOOK.property(), HOOK);
        this.settings.setProperty(CHANNEL.property(), "channel");
        this.settings.setProperty(USER.property(), "user");
        this.settings.setProperty(ICON_URL.property(), "");
        this.settings.setProperty(PROXY_IP.property(), "127.0.0.1");
        this.settings.setProperty(PROXY_PORT.property(), "8080");
        this.settings.setProperty(PROXY_PROTOCOL.property(), "http");
        this.settings.setProperty(DEFAULT_CHANNEL.property(), "general");
        this.settings.setProperty(CONFIG.property(), PROJECT_KEY);
        this.settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + PROJECT_REGEXP.property(), PROJECT_KEY);
        this.settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + CHANNEL.property(), "#random");
        this.settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + QG_FAIL_ONLY.property(), "false");
        this.settings.setProperty("sonar.core.serverBaseURL", "http://your.sonar.com/");
        this.httpClient = mock(SlackHttpClient.class);
        this.i18n = mock(I18n.class);
        when(this.i18n.message(ArgumentMatchers.any(Locale.class), anyString(), anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[2];
            }
        });

        this.task = new SlackPostProjectAnalysisTask(this.httpClient, new ConfigurationBridge(this.settings), this.i18n);
    }

    @Test
    public void shouldCall() throws Exception {
        Analyses.simple(this.postProjectAnalysisTask);
        when(this.httpClient.invokeSlackIncomingWebhook(ArgumentMatchers.eq(HOOK), isA(Payload.class))).thenReturn(true);
        this.task.finished(context(this.postProjectAnalysisTask.getProjectAnalysis()));
        verify(this.httpClient, times(1)).invokeSlackIncomingWebhook(ArgumentMatchers.eq(HOOK), isA(Payload.class));
    }

    private PostProjectAnalysisTask.Context context(PostProjectAnalysisTask.ProjectAnalysis projectAnalysis) {
        return new PostProjectAnalysisTask.Context() {
            @Override
            public PostProjectAnalysisTask.ProjectAnalysis getProjectAnalysis() {
                return projectAnalysis;
            }

            @Override
            public PostProjectAnalysisTask.LogStatistics getLogStatistics() {
                return null;
            }
        };
    }

    @Test
    public void shouldSkipIfPluginDisabled() throws Exception {
        this.settings.setProperty(ENABLED.property(), "false");
        Analyses.simple(this.postProjectAnalysisTask);
        this.task.finished(context(this.postProjectAnalysisTask.getProjectAnalysis()));
        verifyNoInteractions(this.httpClient);
    }

    @Test
    public void shouldSkipIfReportFailedQualityGateButOk() throws Exception {
        this.settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + QG_FAIL_ONLY.property(), "true");
        Analyses.simple(this.postProjectAnalysisTask);
        this.task.finished(context(this.postProjectAnalysisTask.getProjectAnalysis()));
        verifyNoInteractions(this.httpClient);
    }
}
