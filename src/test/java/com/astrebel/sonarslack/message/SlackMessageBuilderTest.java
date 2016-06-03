package com.astrebel.sonarslack.message;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * https://api.slack.com/docs/attachments#message_formatting
 * https://api.slack.com/docs/attachments
 */
public class SlackMessageBuilderTest {

    private SlackMessageBuilder messageBuilder;

    @Before
    public void setUp() {
        messageBuilder = new SlackMessageBuilder();
    }

    @Test
    public void testBuildSimpleMessage() {
        SlackMessage message = new SlackMessage(null,"Sonar","url");
        message.setShortText("This is a test");

        String result = messageBuilder.build(message);
        assertEquals("Wrong message result", "{\"username\":\"Sonar\",\"text\":\"This is a test\"}", result);
    }

    @Test
    public void testBuildAttachmentMessage() {
        SlackMessage message = new SlackMessage(null,"Sonar","url");
        message.setShortText("This is a test");
        SlackAttachment attachment = new SlackAttachment();
        attachment.addReason("This is a test alert");
        message.setAttachment(attachment);

        String result = messageBuilder.build(message);

        String expected = "{\"username\":\"Sonar\",\"text\":\"This is a test\",\"attachments\":["
                + "{\"text\":\"- This is a test alert\\n\",\"fallback\": \"Reasons: This is a test alert, \"}]}";
        assertEquals("Wrong message result", expected, result);
    }

    @Test
    public void testBuildAttachmentMessageMultipleReasons() {
        SlackMessage message = new SlackMessage(null,"Sonar","url");
        message.setShortText("This is a test");
        SlackAttachment attachment = new SlackAttachment();
        attachment.addReason("This is a test alert");
        attachment.addReason("This is another test alert");
        message.setAttachment(attachment);

        String result = messageBuilder.build(message);

        String expected = "{\"username\":\"Sonar\",\"text\":\"This is a test\",\"attachments\":["
                + "{\"text\":\"- This is a test alert\\n- This is another test alert\\n\",\"fallback\": \"Reasons: This is a test alert, This is another test alert, \"}]}";
        assertEquals("Wrong message result", expected, result);
    }

    @Test
    public void testBuildMessageWithChannel() {
        SlackMessage message = new SlackMessage("channel","Sonar","url");
        message.setShortText("This is a test");
        message.setChannel("TestChannel");

        String result = messageBuilder.build(message);
        assertEquals("Wrong message result", "{\"channel\":\"TestChannel\",\"username\":\"Sonar\",\"text\":\"This is a test\"}", result);
    }

    @Test
    public void testBuildMessageWithUser() {
        SlackMessage message = new SlackMessage(null,"Sonar","url");
        message.setShortText("This is a test");
        message.setSlackUser("TestUser");

        String result = messageBuilder.build(message);
        assertEquals("Wrong message result", "{\"username\":\"TestUser\",\"text\":\"This is a test\"}", result);
    }
}