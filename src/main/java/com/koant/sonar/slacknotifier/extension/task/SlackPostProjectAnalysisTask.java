package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.koant.sonar.slacknotifier.common.component.AbstractSlackNotifyingComponent;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Settings;
import org.sonar.api.i18n.I18n;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;

/**
 * Created by 616286 on 3.6.2016.
 */
public class SlackPostProjectAnalysisTask extends AbstractSlackNotifyingComponent implements PostProjectAnalysisTask {

    private static final Logger LOG = Loggers.get(SlackPostProjectAnalysisTask.class);

    private final I18n i18n;
    private final Settings settings;
    private final Slack slackClient;

    public SlackPostProjectAnalysisTask(Settings settings, I18n i18n) {
        this(Slack.getInstance(), settings, i18n);
    }

    public SlackPostProjectAnalysisTask(Slack slackClient, Settings settings, I18n i18n) {
        super(settings);
        this.slackClient = slackClient;
        this.settings = settings;
        this.i18n = i18n;
    }

    @Override
    public void finished(ProjectAnalysis analysis) {
        refreshSettings();

        LOG.info("analysis.getScannerContext().getProperties()=[{}}", analysis.getScannerContext().getProperties());
        String projectKey = analysis.getProject().getKey();
        if (!taskEnabled(projectKey)) {
            return;
        }
        LOG.info("Slack notification will be sent: " + analysis.toString());

        Payload payload = new ProjectAnalysisPayloadBuilder(i18n, analysis)
            .projectUrl(projectUrl(projectKey))
            .channel(getSlackChannel(projectKey))
            .username(getSlackUser())
            .build();

        try {
            // See https://github.com/seratch/jslack
            WebhookResponse response = slackClient.send(getSlackIncomingWebhookUrl(), payload);
            if (!Integer.valueOf(200).equals(response.getCode())) {
                LOG.error("Failed to post to slack, response is [{}]", response);
            }
        } catch (IOException e) {
            LOG.error("Failed to send slack message", e);
        }
    }

    private String projectUrl(String projectKey) {
        return getSonarServerUrl() + "overview?id=" + projectKey;
    }



}
