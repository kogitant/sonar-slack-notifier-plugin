package com.koant.sonar.slacknotifier.common.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.sonar.api.config.Settings;

import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.CHANNEL;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.CHANNEL_USERS;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.CONFIG;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.PROJECT;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.QG_FAIL_ONLY;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

/**
 * Created by ak on 17/10/16.
 * Modified by poznachowski
 */
public class ProjectConfig {

    private static final String CHANNEL_USERS_INPUT_VALID_REGEX = "([a-zA-Z0-9]+,?)*[a-zA-Z0-9]";

    private final String projectKey;
    private final String slackChannel;
    private final boolean qgFailOnly;
    private final Collection<String> channelUsersToNotify;

    public ProjectConfig(String projectKey, String slackChannel, boolean qgFailOnly) {
        this(projectKey, slackChannel, qgFailOnly, emptyList());
    }

    public ProjectConfig(String projectKey, String slackChannel, boolean qgFailOnly,
                         Collection<String> channelUsersToNotify) {
        this.projectKey = projectKey;
        this.slackChannel = slackChannel;
        this.qgFailOnly = qgFailOnly;
        this.channelUsersToNotify = channelUsersToNotify;
    }

    /**
     * Cloning constructor
     *
     * @param c
     */
    public ProjectConfig(ProjectConfig c) {
        this.projectKey = c.getProjectKey();
        this.slackChannel = c.getSlackChannel();
        this.qgFailOnly = c.isQgFailOnly();
        this.channelUsersToNotify = c.getChannelUsersToNotify();
    }

    static ProjectConfig create(Settings settings, String configurationId) {
        String configurationPrefix = CONFIG.property() + "." + configurationId + ".";
        String projectKey = settings.getString(configurationPrefix + PROJECT.property());
        String slackChannel = settings.getString(configurationPrefix + CHANNEL.property());
        boolean qgFailOnly = settings.getBoolean(configurationPrefix + QG_FAIL_ONLY.property());
        List<String> channelUsersToNotify = getChannelUsersToNotify(settings, configurationPrefix);
        return new ProjectConfig(projectKey, slackChannel, qgFailOnly, channelUsersToNotify);
    }

    private static List<String> getChannelUsersToNotify(Settings settings, String configurationPrefix) {
        return ofNullable(settings.getString(configurationPrefix + CHANNEL_USERS.property()))
            .filter(s -> s.matches(CHANNEL_USERS_INPUT_VALID_REGEX))
            .map(config -> config.split(","))
            .map(Arrays::asList)
            .orElse(emptyList());
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

    public Collection<String> getChannelUsersToNotify() {
        return channelUsersToNotify;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectConfig that = (ProjectConfig) o;
        return qgFailOnly == that.qgFailOnly &&
            Objects.equals(projectKey, that.projectKey) &&
            Objects.equals(channelUsersToNotify, that.channelUsersToNotify) &&
            Objects.equals(slackChannel, that.slackChannel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectKey, slackChannel, qgFailOnly, channelUsersToNotify);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProjectConfig{");
        sb.append("projectKey='").append(projectKey).append('\'');
        sb.append(", slackChannel='").append(slackChannel).append('\'');
        sb.append(", slackUsersChannel='").append(channelUsersToNotify).append('\'');
        sb.append(", qgFailOnly=").append(qgFailOnly);
        sb.append('}');
        return sb.toString();
    }
}
