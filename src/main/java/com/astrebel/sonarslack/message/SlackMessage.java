package com.astrebel.sonarslack.message;

public class SlackMessage {

    private String channel;
    private String slackUser;
    private String webHookUrl;
    private String shortText;
    private SlackAttachment attachment;

    public SlackMessage(String channel, String slackUser, String webHookUrl) {
        this.channel = channel;
        this.slackUser = slackUser;
        this.webHookUrl = webHookUrl;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSlackUser() {
        return slackUser;
    }

    public void setSlackUser(String slackUser) {
        this.slackUser = slackUser;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public SlackAttachment getAttachment() {
        return attachment;
    }

    public void setAttachment(SlackAttachment attachment) {
        this.attachment = attachment;
    }

    public String getWebHookUrl() {
        return webHookUrl;
    }

    public void setWebHookUrl(String webHookUrl) {
        this.webHookUrl = webHookUrl;
    }
}
