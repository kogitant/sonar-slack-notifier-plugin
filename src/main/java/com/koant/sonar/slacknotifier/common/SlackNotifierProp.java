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
     * Is this plugin enabled in general?
     * Per project slack notification sending depends on this and a project specific slack channel configuration existing.
     */
    ENABLED("ckss.enabled"),

    /**
     * <p>
     * The project specific slack channels have to be configured in General, server side settings, instead of per project
     * This property is the prefix for a comma separated valye list of Sonar Project Keys. For every project key there is a slack channel configuration.
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
    CHANNELS("ckss.projectchannels"),
    /**
     * @see SlackNotifierProp#CHANNELS
     */
    PROJECT("project"),
    /**
     * @see SlackNotifierProp#CHANNELS
     */
    CHANNEL("channel");

    private String property;


    SlackNotifierProp(java.lang.String property) {
        this.property = property;
    }

    public String property() {
        return property;
    }
}
