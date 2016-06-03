package com.astrebel.sonarslack;

import com.astrebel.sonarslack.message.SlackMessage;
import com.astrebel.sonarslack.notification.SlackNotificationChannel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sonar.api.config.Settings;
import org.sonar.api.notifications.Notification;

import static org.junit.Assert.assertEquals;

public class SlackNotificationChannelTest {

    private static final String NOTIFICATION_TYPE = "new-issues";

    private SlackNotificationChannel channel;

    private Settings settings;
    private SlackClient slackClient;

    @Before
    public void setUp() {
        settings = Mockito.mock(Settings.class);
        slackClient = Mockito.mock(SlackClient.class);

        Mockito.when(settings.getString(SlackNotifierPlugin.SLACK_HOOK)).thenReturn("test.hook");
        Mockito.when(slackClient.toString()).thenReturn("slackClient");

        channel = new SlackNotificationChannel(slackClient, settings);
    }

    @Test
    public void testNotification() {
        Notification notification = new Notification(NOTIFICATION_TYPE);
        notification.setDefaultMessage("This is the default message");

        channel.deliver(notification, Mockito.anyString());

        ArgumentCaptor<SlackMessage> message = ArgumentCaptor.forClass(SlackMessage.class);
        Mockito.verify(slackClient).send(Mockito.anyString(), message.capture());

        assertEquals("This is the default message", message.getValue().getShortText());
    }

    @Test
    public void testNotificationWithChannel() {
        Mockito.when(settings.getString(SlackNotifierPlugin.SLACK_CHANNEL)).thenReturn("test.channel");

        Notification notification = new Notification(NOTIFICATION_TYPE);
        notification.setDefaultMessage("This is the test channel message");

        channel.deliver(notification, Mockito.anyString());

        ArgumentCaptor<SlackMessage> message = ArgumentCaptor.forClass(SlackMessage.class);
        Mockito.verify(slackClient).send(Mockito.anyString(), message.capture());

        assertEquals("This is the test channel message", message.getValue().getShortText());
        assertEquals("test.channel", message.getValue().getChannel());
    }

    @Test
    public void testNotificationNoDefaultMessage() {
        Notification notification = new Notification(NOTIFICATION_TYPE);

        channel.deliver(notification, Mockito.anyString());

        Mockito.verify(slackClient, Mockito.never()).send(Mockito.anyString(), Mockito.any(SlackMessage.class));
    }

    @Test
    public void testNotificationNoHook() {
        Mockito.when(settings.getString(SlackNotifierPlugin.SLACK_HOOK)).thenReturn(null);
        Notification notification = new Notification(NOTIFICATION_TYPE);

        channel.deliver(notification, Mockito.anyString());

        Mockito.verify(slackClient, Mockito.never()).send(Mockito.anyString(), Mockito.any(SlackMessage.class));
    }
}