package com.koant.sonar.slacknotifier.common;

import com.koant.sonar.slacknotifier.SlackNotifierPlugin;

public enum SlackNotifierProp {

    /**
     * The Slack Incoming Web Hook URL
     */
    HOOK("ckss.hook"),

    /**
     * Appear in Slack channels as this user
     */
    USER("ckss.user"),

    /**
     * Appear in Slack channels with this icon
     */
    ICON_URL("ckss.icon"),
    /**
     * Is this plugin enabled in general?
     * Per project slack notification sending depends on this and a project specific slack channel configuration existing.
     */
    ENABLED("ckss.enabled"),
    
    /**
     * Proxy settings (IP, port, protocol)
     */
    PROXY_IP("ckss.proxy_ip"),
    PROXY_PORT("ckss.proxy_port"),
    PROXY_PROTOCOL("ckss.proxy_protocol"),
    /**
     * Appear in Slack channels as this user
     */
    DEFAULT_CHANNEL("ckss.channel"),
    /**
     * Include branch name in slack message (only supported in licenced versions of SonarQube)
     */
    INCLUDE_BRANCH("ckss.include_branch"),

    /**
     * <p>
     * The project specific slack channels have to be configured in General, server side settings, instead of per project
     * This property is the prefix for a comma separated value list of Sonar Project Keys. For every project key there is a slack channel configuration.
     * This is a standard SonarQube way of configuring multivalued fields with org.sonar.api.config.PropertyDefinition.Builder#fields
     * </p>
     * <pre>
     *     ckss.projectchannels=com.koant.sonar.slack:sonar-slack-notifier-plugin,some:otherproject
     *
     *     ckss.projectchannels.com.koant.sonar.slack:sonar-slack-notifier-plugin.project=com.koant.sonar.slack:sonar-slack-notifier-plugin
     *     ckss.projectchannels.com.koant.sonar.slack:sonar-slack-notifier-plugin.channel=#random
     *
     *     ckss.projectchannels.some:otherproject.project=some:otherproject
     *     ckss.projectchannels.some:otherproject.channel=#general
     * </pre>
     *
     * @see SlackNotifierPlugin#define(org.sonar.api.Plugin.Context)
     */
    CONFIG("ckss.projectconfig"),

    /**
     * @see SlackNotifierProp#CONFIG
     */
    PROJECT("project"),

    /**
     * @see SlackNotifierProp#CONFIG
     */
    CHANNEL("channel"),

    /**
     * add @ to someone on notify message
     */
    NOTIFY("notify"),

    /**
     * @see SlackNotifierProp#CONFIG
     */
    QG_FAIL_ONLY("qg");

    private String property;

    SlackNotifierProp(java.lang.String property) {

        this.property = property;
    }

    public String property() {

        return property;
    }
}
