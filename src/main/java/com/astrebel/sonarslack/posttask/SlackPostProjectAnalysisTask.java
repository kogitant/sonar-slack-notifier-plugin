package com.astrebel.sonarslack.posttask;

import com.astrebel.sonarslack.SlackClient;
import com.astrebel.sonarslack.SlackNotifierPlugin;
import com.astrebel.sonarslack.message.SlackMessage;
import com.astrebel.sonarslack.notification.SlackNotificationChannel;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Created by 616286 on 3.6.2016.
 */
public class SlackPostProjectAnalysisTask implements PostProjectAnalysisTask {

    private static final Logger LOG = Loggers.get(SlackNotificationChannel.class);

    private SlackClient slackClient;
    private Settings settings;

    public SlackPostProjectAnalysisTask(SlackClient slackClient, Settings settings) {
        LOG.info("Constructor called");
        this.slackClient = slackClient;
        this.settings = settings;
    }

    @Override
    public void finished(ProjectAnalysis analysis) {
        String hook = settings.getString(SlackNotifierPlugin.SLACK_HOOK);
        String channel = settings.getString(SlackNotifierPlugin.SLACK_CHANNEL);
        String slackUser = settings.getString(SlackNotifierPlugin.SLACK_SLACKUSER);

        if (hook == null) {
            LOG.info("No slack webhook configured...");
            return;
        }

        LOG.info("New analysis: " + analysis.toString());

        String defaultMessage = analysis.getProject().getName() + " analyzed";

        SlackMessage message = new SlackMessage(defaultMessage, slackUser);
        message.setChannel(channel);
        message.setShortText(defaultMessage);
        slackClient.send(hook, message);
    }
}
