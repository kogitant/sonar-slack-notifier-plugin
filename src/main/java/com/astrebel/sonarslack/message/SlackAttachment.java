package com.astrebel.sonarslack.message;

public class SlackAttachment {
    private String title;
    private SlackAttachmentType type;
    private String reasons;

    public SlackAttachment(SlackAttachmentType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReasons() {
        return reasons;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }

    public SlackAttachmentType getType() {
        return type;
    }

    public String getFormattedReasons() {
        if (reasons != null){
            return "- " + reasons.replaceAll(",", "\\\\n-");
        }
        return "";
    }

    public enum SlackAttachmentType {
        WARNING, DANGER;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }
}
