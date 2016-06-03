package com.astrebel.sonarslack;

import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlackNotifierPlugin extends SonarPlugin {

    public static final String SLACK_HOOK = "slack.hook";
    public static final String SLACK_CHANNEL = "slack.channel";
    public static final String SLACK_SLACKUSER = "slack.slackuser";

    private static final String SLACK_CATEGORY = "Slack";


    public SlackNotifierPlugin() {
        super();
    }

    private static List<PropertyDefinition> getSlackProperties() {
        return Arrays.asList(
                PropertyDefinition.builder(SLACK_SLACKUSER)
                        .name("User")
                        .description("User name shown in slack.")
                        .category(SLACK_CATEGORY)
                        .onQualifiers(Qualifiers.PROJECT)
                        .type(PropertyType.STRING)
                        .defaultValue("Sonar")
                        .build(),
                PropertyDefinition.builder(SLACK_CHANNEL)
                        .name("Channel")
                        .description("Channel where the notification should be sent to (#channel).")
                        .category(SLACK_CATEGORY)
                        .onQualifiers(Qualifiers.PROJECT)
                        .type(PropertyType.STRING)
                        .build(),
                PropertyDefinition.builder(SLACK_HOOK)
                        .name("Slack Web Hook")
                        .description("Slack web hook used to send notifications.")
                        .category(SLACK_CATEGORY)
                        .onQualifiers(Qualifiers.PROJECT)
                        .type(PropertyType.STRING)
                        .build());
    }

    @Override
    public List<Object> getExtensions() {
        List<Object> extensions = new ArrayList<>();
        extensions.add(SlackNotificationChannel.class);
        extensions.add(SlackClient.class);
        extensions.addAll(getSlackProperties());
        return extensions;
    }
}
