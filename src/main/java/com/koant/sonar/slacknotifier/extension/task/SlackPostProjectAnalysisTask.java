package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;
import com.koant.sonar.slacknotifier.common.component.AbstractSlackNotifyingComponent;
import com.koant.sonar.slacknotifier.common.component.ProjectConfig;
import okhttp3.*;
import org.assertj.core.util.VisibleForTesting;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Configuration;
import org.sonar.api.i18n.I18n;
import org.sonar.api.internal.apachecommons.lang.StringUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by 616286 on 3.6.2016.
 * Modified by gmilosavljevic
 */
public class SlackPostProjectAnalysisTask extends AbstractSlackNotifyingComponent implements PostProjectAnalysisTask {

    private static final Logger LOG = Loggers.get(SlackPostProjectAnalysisTask.class);

    private final I18n i18n;
    private final SlackHttpClient httpClient;

    /**
     * Default constructor invoked by SonarQube.
     * @param settings
     * @param i18n
     */
    public SlackPostProjectAnalysisTask(Configuration settings, I18n i18n) {
        super(settings);
        this.i18n = i18n;
        this.httpClient = new SlackHttpClient(settings);

    }

    @VisibleForTesting
    SlackPostProjectAnalysisTask(SlackHttpClient httpClient, Configuration settings, I18n i18n) {
        super(settings);
        this.i18n = i18n;
        this.httpClient = httpClient;
    }

    @Override
    public void finished(final ProjectAnalysis analysis) {
        refreshSettings();
        if (!isPluginEnabled()) {
            LOG.info("Slack notifier plugin disabled, skipping. Settings are [{}]", logRelevantSettings());
            return;
        }
        LOG.info("Analysis ScannerContext: [{}]", analysis.getScannerContext().getProperties());
        String projectKey = analysis.getProject().getKey();

        LOG.info("Looking for the configuration of the project {}", projectKey);
        Optional<ProjectConfig> projectConfigOptional = getProjectConfig(projectKey);
        if (!projectConfigOptional.isPresent()) {
            return;
        }

        ProjectConfig projectConfig = projectConfigOptional.get();
        if (shouldSkipSendingNotification(projectConfig, analysis.getQualityGate())) {
            return;
        }

        String hook = projectConfig.getProjectHook();
        if (hook != null) {
            hook = hook.trim();
        }
        LOG.info("Hook is: " + hook);
        if (hook == null || hook.isEmpty()) {
            hook = getSlackIncomingWebhookUrl();
        }

        LOG.info("Slack notification will be sent: " + analysis.toString());

        Payload payload = ProjectAnalysisPayloadBuilder.of(analysis)
            .i18n(i18n)
            .projectConfig(projectConfig)
            .projectUrl(projectUrl(projectKey))
            .includeBranch(isBranchEnabled())
            .username(getSlackUser())
            .iconUrl(getIconUrl())
            .build();

        try {

            if (this.httpClient.invokeSlackIncomingWebhook(payload)) {
                LOG.info("Slack webhook invoked with success.");
            } else {
                throw new IllegalArgumentException("The Slack response has failed");
            }
        } catch (final IOException e) {
            LOG.error("Failed to send slack message", e);
        }
    }

    private String projectUrl(String projectKey) {
        return getSonarServerUrl() + "dashboard?id=" + projectKey;
    }
}
