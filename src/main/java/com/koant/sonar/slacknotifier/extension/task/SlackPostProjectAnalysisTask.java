package com.koant.sonar.slacknotifier.extension.task;

import com.koant.sonar.slacknotifier.common.component.AbstractSlackNotifyingComponent;
import com.koant.sonar.slacknotifier.common.component.ProjectConfig;
import org.assertj.core.util.VisibleForTesting;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Configuration;
import org.sonar.api.i18n.I18n;
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

    @VisibleForTesting
    SlackPostProjectAnalysisTask(final SlackHttpClient httpClient, final Configuration settings, final I18n i18n) {
        super(settings);
        this.i18n = i18n;
        this.httpClient = httpClient;
    }

    /**
     * Default constructor invoked by SonarQube.
     *
     * @param settings
     * @param i18n
     */
    public SlackPostProjectAnalysisTask(final Configuration settings, final I18n i18n) {
        super(settings);
        this.i18n = i18n;
        httpClient = new SlackHttpClient(settings);

    }

    public String getDescription() {
        return "Sonar Plugin to offer Slack Notifications globally or per-project";
    }

    @Override
    public void finished(final PostProjectAnalysisTask.Context context) {

        final ProjectAnalysis analysis = context.getProjectAnalysis();
        this.refreshSettings();
        if (!this.isPluginEnabled()) {
            LOG.info("Slack notifier plugin disabled, skipping. Settings are [{}]", this.logRelevantSettings());
            return;
        }
        LOG.info("Analysis ScannerContext: [{}]", analysis.getScannerContext().getProperties());
        final String projectKey = analysis.getProject().getKey();

        LOG.info("Looking for the configuration of the project {}", projectKey);
        final Optional<ProjectConfig> projectConfigOptional = this.getProjectConfig(projectKey);
        if (!projectConfigOptional.isPresent()) {
            return;
        }


        final var projectConfig = projectConfigOptional.get();
        if (this.shouldSkipSendingNotification(projectConfig, analysis.getQualityGate())) {
            return;
        }

        final String hook = this.getSlackHook(projectConfig);

        LOG.info("Slack notification will be sent: {}", analysis.toString());

        final var payload = ProjectAnalysisPayloadBuilder.of(analysis)
            .i18n(this.i18n)
            .projectConfig(projectConfig)
            .projectUrl(this.projectUrl(projectKey))
            .includeBranch(this.isBranchEnabled())
            .username(this.getSlackUser())
            .iconUrl(this.getIconUrl())
            .build();

        try {

            if (httpClient.invokeSlackIncomingWebhook(hook, payload)) {
                LOG.info("Slack webhook invoked with success.");
            } else {
                throw new IllegalArgumentException("The Slack response has failed");
            }
        } catch (final IOException e) {
            LOG.error("Failed to send slack message {}", e.getMessage(), e);
        }
    }

    private String getSlackHook(final ProjectConfig projectConfig) {
        String hook = projectConfig.getProjectHook();
        if (hook != null) {
            hook = hook.trim();
        }
        LOG.info("Hook is: {}", hook);
        if (hook == null || hook.isEmpty()) {
            hook = this.getDefaultHook();
        }
        return hook;
    }

    private String projectUrl(final String projectKey) {
        return this.getSonarServerUrl() + "dashboard?id=" + projectKey;
    }
}
