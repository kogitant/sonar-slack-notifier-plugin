package com.koant.sonar.slacknotifier.extension.task;

import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.i18n.I18n;

import java.io.IOException;
import java.util.Locale;

import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.*;
import static com.koant.sonar.slacknotifier.extension.task.Analyses.PROJECT_KEY;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Created by 616286 on 3.6.2016.
 * Modified by poznachowski
 */
public class SlackPostProjectAnalysisTaskTest {

    private static final String HOOK = "hook";
    private static final String DIFFERENT_KEY = "different:key";

    private CaptorPostProjectAnalysisTask postProjectAnalysisTask;

    private SlackPostProjectAnalysisTask task;

    private CloseableHttpClient httpClient;

    private Settings settings;
    private I18n i18n;
    private HttpPost httpRequest;

    @Before
    public void before() throws IOException {
        postProjectAnalysisTask = new CaptorPostProjectAnalysisTask();
        settings = new MapSettings();
        settings.setProperty(ENABLED.property(), "true");
        settings.setProperty(SlackNotifierProp.HOOK.property(), HOOK);
        settings.setProperty(CHANNEL.property(), "channel");
        settings.setProperty(USER.property(), "user");
        settings.setProperty(ICON_URL.property(), "");
        settings.setProperty(PROXY_IP.property(), "127.0.0.1");
        settings.setProperty(PROXY_PORT.property(), "8080");
        settings.setProperty(PROXY_PROTOCOL.property(), "http");
        settings.setProperty(CONFIG.property(), PROJECT_KEY);
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + PROJECT.property(), PROJECT_KEY);
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + CHANNEL.property(), "#random");
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + QG_FAIL_ONLY.property(), "false");
        settings.setProperty("sonar.core.serverBaseURL", "http://your.sonar.com/");
        httpClient = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        httpRequest = Mockito.mock(HttpPost.class);

        when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!"));
        when(httpResponse.getEntity()).thenReturn(entity);
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

        i18n = Mockito.mock(I18n.class);
        Mockito.when(i18n.message(Matchers.any(Locale.class), anyString(), anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[2];
            }
        });

        task = new SlackPostProjectAnalysisTask(httpClient, new ConfigurationBridge(settings), i18n);
    }

    @Test
    public void shouldCall() throws Exception {
        Analyses.simple(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verify(httpClient, times(1)).execute(any(HttpPost.class));
    }

    @Test
    public void shouldSkipIfPluginDisabled() throws Exception {
        settings.setProperty(ENABLED.property(), "false");
        Analyses.simple(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verifyZeroInteractions(httpClient);
    }

    @Test
    public void shouldSkipIfNoConfigFound() throws Exception {
        Analyses.simpleDifferentKey(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verifyZeroInteractions(httpClient);
    }

    @Test
    public void shouldSkipIfReportFailedQualityGateButOk() throws Exception {
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + QG_FAIL_ONLY.property(), "true");
        Analyses.simple(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verifyZeroInteractions(httpClient);
    }
}
