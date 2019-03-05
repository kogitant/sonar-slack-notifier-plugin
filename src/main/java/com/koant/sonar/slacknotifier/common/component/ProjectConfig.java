package com.koant.sonar.slacknotifier.common.component;

import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.sonar.api.config.Configuration;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by ak on 17/10/16.
 * Modified by poznachowski
 */
public class ProjectConfig {
    private final String projectHook;
    private final String projectKey;
    private final String slackChannel;
    private final String notify;
    private final boolean qgFailOnly;

    /**
     * Cloning constructor
     *
     * @param c
     */
    public ProjectConfig(ProjectConfig c) {
        this.projectHook = c.getProjectHook();
        this.projectKey = c.getProjectKey();
        this.slackChannel = c.getSlackChannel();
        this.qgFailOnly = c.isQgFailOnly();
        this.notify = c.getNotify();
    }

    public ProjectConfig(String projectHook, String projectKey, String slackChannel, String notify, boolean qgFailOnly) {
        this.projectKey = projectKey;
        this.slackChannel = slackChannel;
        this.notify = notify;
        this.qgFailOnly = qgFailOnly;
    }

    static ProjectConfig create(Configuration settings, String configurationId) {
        String hook = settings.getString(configurationPrefix + SlackNotifierProp.PROJECT_HOOK.property());
        Optional<String> projectKey = settings.get(configurationPrefix + SlackNotifierProp.PROJECT.property());
        Optional<String> slackChannel = settings.get(configurationPrefix + SlackNotifierProp.CHANNEL.property());
        Optional<String> notify = settings.get(configurationPrefix+SlackNotifierProp.NOTIFY.property());
        Optional<Boolean> qgFailOnly = settings.getBoolean(configurationPrefix + SlackNotifierProp.QG_FAIL_ONLY.property());
        if(!slackChannel.isPresent()){
            throw new IllegalStateException("No slack channel configured, unable to continue");
        }
        return new ProjectConfig(hook, projectKey.orElse(""), slackChannel.get(), notify.orElse(""), qgFailOnly.orElse(true));
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getSlackChannel() {
        return slackChannel;
    }

    public boolean isQgFailOnly() {
        return qgFailOnly;
    }

    public String getNotify() {return notify;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectConfig that = (ProjectConfig) o;
        return qgFailOnly == that.qgFailOnly &&
            Objects.equals(projectHook, that.projectHook) &&
            Objects.equals(projectKey, that.projectKey) &&
            Objects.equals(notify, that.notify) &&
            Objects.equals(slackChannel, that.slackChannel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectHook, projectKey, slackChannel,notify, qgFailOnly);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProjectConfig{");
        sb.append("projectHook=").append(projectHook).append('\'');
        sb.append("projectKey='").append(projectKey).append('\'');
        sb.append(", slackChannel='").append(slackChannel).append('\'');
        sb.append(", notify='").append(notify).append('\'');
        sb.append(", qgFailOnly=").append(qgFailOnly);
        sb.append('}');
        return sb.toString();
    }
}
