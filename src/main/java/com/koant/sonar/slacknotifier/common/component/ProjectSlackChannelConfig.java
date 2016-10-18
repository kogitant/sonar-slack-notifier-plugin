package com.koant.sonar.slacknotifier.common.component;

import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.sonar.api.config.Settings;

/**
 * Created by ak on 17/10/16.
 */
class ProjectSlackChannelConfig {
    private final String projectKey;
    private final String slackChannel;

    public ProjectSlackChannelConfig(String projectKey, String slackChannel) {
        this.projectKey = projectKey;
        this.slackChannel = slackChannel;
    }

    /**
     * Cloning constructor
     *
     * @param c
     */
    public ProjectSlackChannelConfig(ProjectSlackChannelConfig c) {
        this.projectKey = c.getProjectKey();
        this.slackChannel = c.getSlackChannel();
    }

    static ProjectSlackChannelConfig create(Settings settings, String configurationId) {

        String configurationPrefix = SlackNotifierProp.CHANNELS.property() + "." + configurationId + ".";

        String projectKey = settings.getString(configurationPrefix + SlackNotifierProp.PROJECT.property());
        String slackChannel = settings.getString(configurationPrefix + SlackNotifierProp.CHANNEL.property());

        return new ProjectSlackChannelConfig(projectKey, slackChannel);
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getSlackChannel() {
        return slackChannel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectSlackChannelConfig that = (ProjectSlackChannelConfig) o;

        if (projectKey != null ? !projectKey.equals(that.projectKey) : that.projectKey != null) return false;
        return slackChannel != null ? slackChannel.equals(that.slackChannel) : that.slackChannel == null;

    }

    @Override
    public int hashCode() {
        int result = projectKey != null ? projectKey.hashCode() : 0;
        result = 31 * result + (slackChannel != null ? slackChannel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProjectSlackChannelConfig{");
        sb.append("projectKey='").append(projectKey).append('\'');
        sb.append(", slackChannel='").append(slackChannel).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
