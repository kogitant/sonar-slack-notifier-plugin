package com.astrebel.sonarslack.message;

public class SlackMessageBuilder {

    public String build(SlackMessage message) {
        StringBuilder builder = new StringBuilder();

        builder.append("{");
        if (message.getChannel() != null) {
            builder.append("\"channel\":\"");
            builder.append(message.getChannel());
            builder.append("\",");
        }
        builder.append("\"username\":\"");
        builder.append(message.getSlackUser());
        builder.append("\",");
        builder.append("\"text\":\"");
        builder.append(message.getShortText().replace("\n", "").replace("\r", ""));
        builder.append("\"");

        if (message.getAttachment() != null) {
            builder.append(",\"attachments\":[");
            builder.append(buildAttachment(message.getAttachment()));
            builder.append("]");
        }
        builder.append("}");

        return builder.toString();
    }

    private String buildAttachment(SlackAttachment attachment) {
        return "{\"text\":\"*" + attachment.getTitle() + "*\\n*Reason:*\\n" + attachment.getReasons() + "\",\"color\":\"" + attachment.getType()
                + "\",\"mrkdwn_in\": [\"text\"]}";
    }
}
