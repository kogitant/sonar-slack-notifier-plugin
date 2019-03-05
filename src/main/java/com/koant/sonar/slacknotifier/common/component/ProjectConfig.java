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

    private final String  projectHook;
    private final String  projectKey;
    private final String  slackChannel;
    private final String  notify;
    private final boolean qgFailOnly;

    public ProjectConfig(String projectHook,
                         String projectKey,
                         String slackChannel,
                         String notify,
                         boolean qgFailOnly) {
        this.projectHook = projectHook;
        this.projectKey = projectKey;
        this.slackChannel = slackChannel;
        this.notify = notify;
        this.qgFailOnly = qgFailOnly;
    }


    public String getProjectHook() { return this.projectHook;  }
    public String getProjectKey() {
        return projectKey;
    }
    public String getSlackChannel() {
        return slackChannel;
    }
    public boolean isQgFailOnly() {
        return qgFailOnly;
    }
    public String getNotify() { return notify; }

    @Override
    public String toString() {
        return "ProjectConfig{" +
            "projectHook='" + projectHook + '\'' +
            ", projectKey='" + projectKey + '\'' +
            ", slackChannel='" + slackChannel + '\'' +
            ", notify='" + notify + '\'' +
            ", qgFailOnly=" + qgFailOnly +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ProjectConfig that = (ProjectConfig) o;
        return this.qgFailOnly == that.qgFailOnly &&
            Objects.equals(this.projectHook, that.projectHook) &&
            Objects.equals(this.projectKey, that.projectKey) &&
            Objects.equals(this.slackChannel, that.slackChannel) &&
            Objects.equals(this.notify, that.notify);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.projectHook, this.projectKey, this.slackChannel, this.notify, this.qgFailOnly);
    }

}
