package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;
import com.koant.sonar.slacknotifier.common.component.AbstractSlackNotifyingComponent;
import com.koant.sonar.slacknotifier.common.component.ProjectConfig;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Settings;
import org.sonar.api.i18n.I18n;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.util.Optional;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.assertj.core.util.Strings;

/**
 * Created by 616286 on 3.6.2016.
 * Modified by gmilosavljevic
 */
public class SlackPostProjectAnalysisTask extends AbstractSlackNotifyingComponent implements PostProjectAnalysisTask {    

    private static final Logger LOG = Loggers.get(SlackPostProjectAnalysisTask.class);

    private final I18n i18n;
    private final CloseableHttpClient httpClient;

    public SlackPostProjectAnalysisTask(Settings settings, I18n i18n) {
        this(HttpClientBuilder.create().build(), settings, i18n);
    }
    
    public SlackPostProjectAnalysisTask(CloseableHttpClient httpClient, Settings settings, I18n i18n) {
        super(settings);
        this.i18n = i18n;
        this.httpClient = httpClient;
    }

    @Override
    public void finished(ProjectAnalysis analysis) {
        refreshSettings();
        if (!isPluginEnabled()) {
            LOG.info("Slack notifier plugin disabled, skipping. Settings are [{}]", logRelevantSettings());
            return;
        }
        LOG.info("Analysis ScannerContext: [{}]", analysis.getScannerContext().getProperties());
        String projectKey = analysis.getProject().getKey();

        Optional<ProjectConfig> projectConfigOptional = getProjectConfig(projectKey);
        if (!projectConfigOptional.isPresent()) {
            return;
        }

        ProjectConfig projectConfig = projectConfigOptional.get();
        if (shouldSkipSendingNotification(projectConfig, analysis.getQualityGate())) {
            return;
        }

        LOG.info("Slack notification will be sent: " + analysis.toString());

        Payload payload = ProjectAnalysisPayloadBuilder.of(analysis)
                .i18n(i18n)
                .projectConfig(projectConfig)
                .projectUrl(projectUrl(projectKey))
                .username(getSlackUser())
                .iconUrl(getIconUrl())
                .build();
        
        Gson gson = GsonFactory.createSnakeCase();
        String payloadJson = gson.toJson(payload);
        
        try {
            HttpPost request = new HttpPost(getSlackIncomingWebhookUrl());
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            
            RequestConfig config = RequestConfig.copy(RequestConfig.DEFAULT)
                    .setConnectTimeout(5000)
                    .setSocketTimeout(5000)
                    .setProxy(getHttpProxy())
                    .setConnectionRequestTimeout(5000)
                    .build();
            request.setConfig(config);            
            request.setEntity(new StringEntity(payloadJson));
            
            LOG.info("Request configuration: [uri=" + request.getURI().toASCIIString() + ", config=" + config.toString() + ", payload=" + payloadJson);
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                LOG.info("Slack HTTP response status: " + response.getStatusLine().toString());
                LOG.info("Slack HTTP response body: " + EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            LOG.error("Failed to send slack message", e);
        }
    }

    private String projectUrl(String projectKey) {
        return getSonarServerUrl() + "dashboard?id=" + projectKey;
    }

    private HttpHost getHttpProxy() {
        if (Strings.isNullOrEmpty(getProxyIP())) {
            return null;
        }
        
        return new HttpHost(getProxyIP(), getProxyPort(), getProxyProtocol());
    }
}
