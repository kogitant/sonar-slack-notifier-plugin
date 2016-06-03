package com.astrebel.sonarslack;

import com.astrebel.sonarslack.message.SlackAttachment;
import com.astrebel.sonarslack.message.SlackAttachment.SlackAttachmentType;
import com.astrebel.sonarslack.message.SlackMessage;
import org.sonar.api.config.Settings;
import org.sonar.api.notifications.Notification;
import org.sonar.api.notifications.NotificationChannel;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class SlackNotificationChannel extends NotificationChannel {
    private static final Logger LOG = Loggers.get(SlackNotificationChannel.class);

    private SlackClient slackClient;
    private Settings settings;

    public SlackNotificationChannel(SlackClient slackClient, Settings settings) {
        this.slackClient = slackClient;
        this.settings = settings;
    }

    @Override
    public void deliver(Notification notification, String user) {
        String hook = settings.getString(SlackNotifierPlugin.SLACK_HOOK);
        String channel = settings.getString(SlackNotifierPlugin.SLACK_CHANNEL);
        String slackUser = settings.getString(SlackNotifierPlugin.SLACK_SLACKUSER);

        if (hook == null) {
            return;
        }

        LOG.info("New notification: " + notification.toString());

        String defaultMessage = notification.getFieldValue("default_message");
        if (defaultMessage != null) {

            SlackMessage message = new SlackMessage(defaultMessage, slackUser);
            message.setChannel(channel);

            if (!"alerts".equals(notification.getType())) {
                String alertLevel = notification.getFieldValue("alertLevel");
                String alertName = notification.getFieldValue("alertName");
                String alertText = notification.getFieldValue("alertText");

                SlackAttachmentType type = SlackAttachmentType.WARNING;
                if ("ERROR".equalsIgnoreCase(alertLevel)) {
                    type = SlackAttachmentType.DANGER;
                }

                SlackAttachment attachment = new SlackAttachment(type);
                attachment.setTitle(alertName);
                attachment.setReasons(alertText);

                message.setAttachment(attachment);
            }

            slackClient.send(hook, message);
        }
    }

}
