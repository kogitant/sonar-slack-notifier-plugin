package com.koant.sonar.slacknotifier.common.component;

import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.sonar.api.config.Settings;

import java.util.Objects;

/**
 * Created by ak on 17/10/16.
 * Modified by poznachowski
 */
public class ProjectConfig {
    private final String projectHook;
    private final String projectKey;
    private final String slackChannel;
    private final boolean qgFailOnly;

    public ProjectConfig(String projectHook, String projectKey, String slackChannel, boolean qgFailOnly) {
        this.projectHook = projectHook;
        this.projectKey = projectKey;
        this.slackChannel = slackChannel;
        this.qgFailOnly = qgFailOnly;
    }

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
    }

    static ProjectConfig create(Settings settings, String configurationId) {
        String configurationPrefix = SlackNotifierProp.CONFIG.property() + "." + configurationId + ".";
        String hook = settings.getString(configurationPrefix + SlackNotifierProp.PROJECT_HOOK.property());
        String projectKey = settings.getString(configurationPrefix + SlackNotifierProp.PROJECT.property());
        String slackChannel = settings.getString(configurationPrefix + SlackNotifierProp.CHANNEL.property());
        boolean qgFailOnly = settings.getBoolean(configurationPrefix + SlackNotifierProp.QG_FAIL_ONLY.property());
        return new ProjectConfig(hook, projectKey, slackChannel, qgFailOnly);
    }

    public String getProjectHook() {
        return projectHook;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectConfig that = (ProjectConfig) o;
        return qgFailOnly == that.qgFailOnly &&
            Objects.equals(projectHook, that.projectHook) &&
            Objects.equals(projectKey, that.projectKey) &&
            Objects.equals(slackChannel, that.slackChannel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectHook, projectKey, slackChannel, qgFailOnly);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProjectConfig{");
        sb.append("projectHook=").append(projectHook).append('\'');
        sb.append("projectKey='").append(projectKey).append('\'');
        sb.append(", slackChannel='").append(slackChannel).append('\'');
        sb.append(", qgFailOnly=").append(qgFailOnly);
        sb.append('}');
        return sb.toString();
    }
}
