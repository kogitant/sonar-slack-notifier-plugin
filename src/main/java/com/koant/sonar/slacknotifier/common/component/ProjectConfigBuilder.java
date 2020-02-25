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
        projectHook = c.getProjectHook();
        projectKeyOrRegExp = c.getProjectKey();
        slackChannel = c.getSlackChannel();
        notify = c.getNotify();
        qgFailOnly = c.isQgFailOnly();
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
            this.projectHook,
            this.projectKeyOrRegExp,
            this.slackChannel,
            this.notify,
            this.qgFailOnly
        );
    }


     public ProjectConfigBuilder withConfiguration(final Configuration settings, final String configurationPrefix) {
         this.projectHook =
            settings.get(configurationPrefix + SlackNotifierProp.PROJECT_HOOK.property()).orElse(null);
         this.projectKeyOrRegExp =
            settings.get(configurationPrefix + SlackNotifierProp.PROJECT_REGEXP.property()).orElse("");
         this.slackChannel =
            settings.get(configurationPrefix + SlackNotifierProp.CHANNEL.property()).orElseThrow(() -> new IllegalStateException("No slack channel configured, unable to continue") );
         this.notify = settings.get(configurationPrefix + SlackNotifierProp.NOTIFY.property()).orElse("");
         this.qgFailOnly = settings.getBoolean(
            configurationPrefix + SlackNotifierProp.QG_FAIL_ONLY.property()).orElse(true);
        return this;
    }

    public static ProjectConfig cloneProjectConfig(final ProjectConfig projectConfig) {
        return new ProjectConfigBuilder().from(projectConfig).build();
    }
}
