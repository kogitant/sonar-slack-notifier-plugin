package com.koant.sonar.slacknotifier;

import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.CHANNEL;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.CONFIG;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.ENABLED;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.HOOK;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.INCLUDE_BRANCH;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.NOTIFY;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.PROJECT;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.QG_FAIL_ONLY;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.USER;

import com.koant.sonar.slacknotifier.extension.task.SlackPostProjectAnalysisTask;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.PropertyFieldDefinition;

import java.util.ArrayList;
import java.util.List;

public class SlackNotifierPlugin implements Plugin {

    private static final String CATEGORY = "Slack";
    private static final String SUBCATEGORY = "CKS Slack Notifier";

    @Override
    public void define(Context context) {
        List<Object> extensions = new ArrayList<>();

        // The configurable properties
        addPluginPropertyDefinitions(extensions);

        // The actual plugin component(s)
        extensions.add(SlackPostProjectAnalysisTask.class);

        context.addExtensions(extensions);
    }

    private void addPluginPropertyDefinitions(List<Object> extensions) {
        extensions.add(PropertyDefinition.builder(HOOK.property())
            .name("Slack web integration hook")
            .description("https://api.slack.com/incoming-webhooks")
            .type(PropertyType.STRING)
            .category(CATEGORY)
            .subCategory(SUBCATEGORY)
            .index(0)
            .build());
        extensions.add(PropertyDefinition.builder(USER.property())
            .name("Slack user alias")
            .description("Messages from this plugin appear with given username")
            .defaultValue("SonarQube Slack Notifier Plugin")
            .type(PropertyType.STRING)
            .category(CATEGORY)
            .subCategory(SUBCATEGORY)
            .index(1)
            .build());
        extensions.add(PropertyDefinition.builder(ENABLED.property())
            .name("Plugin enabled")
            .description("Are Slack notifications enabled in general?")
            .defaultValue("false")
            .type(PropertyType.BOOLEAN)
            .category(CATEGORY)
            .subCategory(SUBCATEGORY)
            .index(2)
            .build());
        extensions.add(PropertyDefinition.builder(INCLUDE_BRANCH.property())
            .name("Branch enabled")
            .description("Include branch name in slack messages?\nNB: Not supported with free version of SonarQube")
            .defaultValue("false")
            .type(PropertyType.BOOLEAN)
            .category(CATEGORY)
            .subCategory(SUBCATEGORY)
            .index(3)
            .build());

        extensions.add(
            PropertyDefinition.builder(CONFIG.property())
                .name("Project specific configuration")
                .description("Project specific configuration: Specify Slack channel and notification only on failing Qualilty Gate. " +
                        "If a slack channel is not configured for a project, no slack message will be sent for project.")
                .category(CATEGORY)
                .subCategory(SUBCATEGORY)
                .index(4)
                .fields(
                    PropertyFieldDefinition.build(PROJECT.property())
                        .name("Project Key")
                        .description("Ex: com.koant.sonar.slack:sonar-slack-notifier-plugin, can use '*' wildcard at the end")
                        .type(PropertyType.STRING)
                        .build(),
                    PropertyFieldDefinition.build(CHANNEL.property())
                        .name("Slack channel")
                        .description("Channel to send project specific messages to")
                        .type(PropertyType.STRING)
                        .build(),
                        PropertyFieldDefinition.build(QG_FAIL_ONLY.property())
                        .name("Send on failed Quality Gate")
                        .description("Should notification be sent only if Quality Gate did not pass OK")
                        .type(PropertyType.BOOLEAN)
                        .build(),
                    PropertyFieldDefinition.build(NOTIFY.property())
                        .name("Notify")
                        .description("add @ to someone before messages, for example @channel")
                        .type(PropertyType.STRING)
                        .build()
                )
                .build());
    }
}
