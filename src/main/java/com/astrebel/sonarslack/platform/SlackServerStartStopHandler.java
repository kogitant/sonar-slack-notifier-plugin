package com.astrebel.sonarslack.platform;

import com.astrebel.sonarslack.SlackClient;
import com.astrebel.sonarslack.SlackNotifierPlugin;
import com.astrebel.sonarslack.message.SlackMessage;
import com.astrebel.sonarslack.notification.SlackNotificationChannel;
import org.sonar.api.config.Settings;
import org.sonar.api.platform.Server;
import org.sonar.api.platform.ServerStartHandler;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Created by 616286 on 3.6.2016.
 */
public class SlackServerStartStopHandler implements ServerStartHandler {
    private static final Logger LOG = Loggers.get(SlackNotificationChannel.class);

    private SlackClient slackClient;
    private Settings settings;

    public SlackServerStartStopHandler(SlackClient slackClient, Settings settings) {
        LOG.info("Constructor called");
        this.slackClient = slackClient;
        this.settings = settings;
    }

    @Override
    public void onServerStart(Server server) {
        sendMessage(server.getPublicRootUrl() + " started");
    }

    private void sendMessage(String m) {
        String hook = settings.getString(SlackNotifierPlugin.SLACK_HOOK);
        String channel = settings.getString(SlackNotifierPlugin.SLACK_CHANNEL);
        String slackUser = settings.getString(SlackNotifierPlugin.SLACK_SLACKUSER);
        if (hook == null) {
            LOG.info("No slack webhook configured...");
            return;
        }
        SlackMessage message = new SlackMessage(m, slackUser);
        message.setChannel(channel);
        slackClient.send(hook, message);
    }
}
