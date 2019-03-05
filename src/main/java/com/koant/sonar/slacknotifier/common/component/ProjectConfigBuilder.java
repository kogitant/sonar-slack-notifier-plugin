package com.koant.sonar.slacknotifier.common.component;

import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.sonar.api.config.Configuration;

public class ProjectConfigBuilder {
    private String  projectHook;
    private String  projectKeyOrRegExp;
    private String  slackChannel;
    private String  notify;
    private boolean qgFailOnly;



    public ProjectConfigBuilder from(final ProjectConfig c) {
        this.projectHook = c.getProjectHook();
        this.projectKeyOrRegExp = c.getProjectKey();
        this.slackChannel = c.getSlackChannel();
        this.notify = c.getNotify();
        this.qgFailOnly = c.isQgFailOnly();
        return this;
    }

    public ProjectConfigBuilder setProjectHook(final String projectHook) {
        this.projectHook = projectHook;
        return this;
    }

    public ProjectConfigBuilder setProjectKeyOrRegExp(final String projectKeyOrRegExp) {
        this.projectKeyOrRegExp = projectKeyOrRegExp;
        return this;
    }

    public ProjectConfigBuilder setSlackChannel(final String slackChannel) {
        this.slackChannel = slackChannel;
        return this;
    }

    public ProjectConfigBuilder setNotify(final String notify) {
        this.notify = notify;
        return this;
    }

    public ProjectConfigBuilder setQgFailOnly(final boolean qgFailOnly) {
        this.qgFailOnly = qgFailOnly;
        return this;
    }

    public ProjectConfig build() {
        return new ProjectConfig(
            projectHook,
            projectKeyOrRegExp,
            slackChannel,
            notify,
            qgFailOnly
        );
    }


     public ProjectConfigBuilder withConfiguration(Configuration settings, String configurationPrefix) {
        projectHook =
            settings.get(configurationPrefix + SlackNotifierProp.PROJECT_HOOK.property()).orElse(null);
        projectKeyOrRegExp =
            settings.get(configurationPrefix + SlackNotifierProp.PROJECT_REGEXP.property()).orElse("");
        slackChannel =
            settings.get(configurationPrefix + SlackNotifierProp.CHANNEL.property()).orElseThrow(() -> new IllegalStateException("No slack channel configured, unable to continue") );
        notify = settings.get(configurationPrefix + SlackNotifierProp.NOTIFY.property()).orElse("");
        qgFailOnly = settings.getBoolean(
            configurationPrefix + SlackNotifierProp.QG_FAIL_ONLY.property()).orElse(true);
        return this;
    }

    public static ProjectConfig cloneProjectConfig(final ProjectConfig projectConfig) {
        return new ProjectConfigBuilder().from(projectConfig).build();
    }
}
