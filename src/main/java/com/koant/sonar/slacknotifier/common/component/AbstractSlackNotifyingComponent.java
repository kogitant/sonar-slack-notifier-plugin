package com.koant.sonar.slacknotifier.common.component;

import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.MessageException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract base component for Slack notifying Sonar extensions.
 * Concrete implementations must call com.koant.sonar.slacknotifier.common.component
 * .AbstractSlackNotifyingComponent#refreshSettings() in the beginning of actual execution.
 */
@Slf4j
public abstract class AbstractSlackNotifyingComponent {

    private final Configuration configuration;
    private Map<String, ProjectConfig> projectConfigMap = Collections.emptyMap();

    public AbstractSlackNotifyingComponent(final Configuration configuration) {
        this.configuration = configuration;
        log.info("Constructor called, project slack channel config map constructed from general configuration");
    }

    /**
     * This method has to be called in the beginning of every actual plugin execution.
     * SonarQube seems to work in such a way that
     * <pre>
     * 1) the Settings object is constructor injected to this class.
     * 2) the values reflected by the Settings object reflect latest settings configured
     * 3) but the constructor of this class is called only once, and after that the class is never instantiated again
     * (the same instance is reused)
     * 4) thus when the instance is used to perform something, we must refresh the projectConfigMap when the
     * execution starts
     * </pre>
     */
    protected void refreshSettings() {
        log.info("Refreshing settings");
        this.refreshProjectConfigs();
    }

    private void refreshProjectConfigs() {
        log.info("Refreshing project configs");
        // final var oldValues =
        Set<ProjectConfig> oldValues = projectConfigMap.values().stream().
            map(ProjectConfigBuilder::cloneProjectConfig).collect(Collectors.toSet());
        projectConfigMap = buildProjectConfigByProjectKeyMap(configuration);
        // final Set newValues =
        Set<ProjectConfig> newValues = new HashSet<>(projectConfigMap.values());
        if (!oldValues.equals(newValues)) {
            log.info("Old configs [{}] --> new configs [{}]", oldValues, newValues);
        }
    }

    private static Map<String, ProjectConfig> buildProjectConfigByProjectKeyMap(final Configuration settings) {
        final Map<String, ProjectConfig> map = new HashMap<>();
        final String[] projectConfigIndexes = settings.getStringArray(SlackNotifierProp.CONFIG.property());

        log.info("SlackNotifierProp.CONFIG=[{}]", (Object) projectConfigIndexes);
        for (final String projectConfigIndex : projectConfigIndexes) {
            final String projectKeyProperty = SlackNotifierProp.CONFIG
                .property() + "." + projectConfigIndex + "." + SlackNotifierProp.PROJECT_REGEXP
                .property();
            final Optional<String> projectKey = settings.get(projectKeyProperty);
            if (!projectKey.isPresent()) {
                throw MessageException.of(
                    "Slack notifier configuration is corrupted. At least one project specific parameter has no " +
                        "project key. " +
                        "Contact your administrator to update this configuration in the global administration section" +
                        " of SonarQube.");
            }
            final ProjectConfig value = new ProjectConfigBuilder()
                .withConfiguration(settings, SlackNotifierProp.CONFIG.property() + "." + projectConfigIndex + ".")
                .build();
            log.info("Found project configuration [{}]", value);
            map.put(projectKey.get(), value);
        }
        return map;
    }

    protected String getIconUrl() {
        final Optional<String> icon = this.configuration.get(SlackNotifierProp.ICON_URL.property());
        return icon.orElse(null);
    }

    protected boolean isPluginEnabled() {
        return this.configuration.getBoolean(SlackNotifierProp.ENABLED.property())
            .orElseThrow(() -> new IllegalStateException("Enabled property not found"));
    }

    /**
     * @return value for INCLUDE_BRANCH property, defaults to false if for some reason not set.
     */
    protected boolean isBranchEnabled() {

        return this.configuration.getBoolean(SlackNotifierProp.INCLUDE_BRANCH.property()).orElse(false);
    }

    /**
     * Returns the sonar server url, with a trailing /
     *
     * @return the sonar server URL
     */
    @SuppressWarnings("HardcodedFileSeparator")
    protected String getSonarServerUrl() {
        final Optional<String> urlOptional = this.configuration.get("sonar.core.serverBaseURL");
        if (!urlOptional.isPresent()) {
            return "http://pleaseDefineSonarQubeUrl/";
        }
        final String url = urlOptional.get();
        if (url.endsWith("/")) {
            return url;
        }
        return url + "/";
    }

    protected Optional<ProjectConfig> getProjectConfig(final String projectKey) {
        final List<ProjectConfig> projectConfigs = this.searchForProjectConfig(projectKey);
        // Not configured at all
        if (projectConfigs.isEmpty()) {
            log.info("Could not find config for project [{}] in [{}]", projectKey, this.projectConfigMap);

            log.info("Building the default project config.");
            return Optional.of(buildDefaultProjectConfig(projectKey));
        }

        if (projectConfigs.size() > 1) {
            log.warn("More than 1 project key was matched. Using first one: {}", projectConfigs.get(0).getProjectKey());
        }
        return Optional.of(projectConfigs.get(0));
    }

    @NotNull
    private List<ProjectConfig> searchForProjectConfig(final String projectKey) {
        List<ProjectConfig> list = new ArrayList<>();
        Map<String, ProjectConfig> stringProjectConfigMap = this.projectConfigMap;
        for (String s : this.projectConfigMap.keySet()) {
            if (projectKey.matches(s)) {
                ProjectConfig projectConfig = stringProjectConfigMap.get(s);
                list.add(projectConfig);
            }
        }
        return list;
    }

    @NotNull
    private ProjectConfig buildDefaultProjectConfig(final String projectKey) {
        return new ProjectConfigBuilder().setProjectHook(this.getDefaultHook())
            .setProjectKeyOrRegExp(projectKey)
            .setSlackChannel(this.getDefaultChannel())
            .setNotify(this.getSlackUser())
            .setQgFailOnly(false)
            .build();
    }

    protected String getDefaultHook() {
        final Optional<String> defaultHook = this.configuration.get(SlackNotifierProp.HOOK.property());
        return defaultHook.orElse(null);
    }

    protected String getDefaultChannel() {
        final Optional<String> defaultChannel = this.configuration.get(SlackNotifierProp.DEFAULT_CHANNEL.property());
        return defaultChannel.orElseThrow(() -> new IllegalStateException("Default property not found"));
    }

    protected String getSlackUser() {

        final Optional<String> user = this.configuration.get(SlackNotifierProp.USER.property());
        return user.orElseThrow(() -> new IllegalStateException("User property not found"));
    }

    protected String logRelevantSettings() {
        final Map<String, String> pluginSettings = new HashMap<>();
        this.mapSetting(pluginSettings, SlackNotifierProp.HOOK);
        this.mapSetting(pluginSettings, SlackNotifierProp.USER);
        this.mapSetting(pluginSettings, SlackNotifierProp.PROXY_IP);
        this.mapSetting(pluginSettings, SlackNotifierProp.PROXY_PORT);
        this.mapSetting(pluginSettings, SlackNotifierProp.PROXY_PROTOCOL);
        this.mapSetting(pluginSettings, SlackNotifierProp.ENABLED);
        this.mapSetting(pluginSettings, SlackNotifierProp.CONFIG);
        this.mapSetting(pluginSettings, SlackNotifierProp.INCLUDE_BRANCH);
        return pluginSettings + "; project specific channel config: " + this.projectConfigMap;
    }

    private void mapSetting(final Map<String, String> pluginSettings, final SlackNotifierProp key) {
        pluginSettings.put(key.name(), this.configuration.get(key.property()).orElse(""));
    }

    protected boolean shouldSkipSendingNotification(final ProjectConfig projectConfig, final QualityGate qualityGate) {
        // Disabled due to missing channel value
        if (projectConfig.getSlackChannel() == null ||
            "".equals(projectConfig.getSlackChannel().trim())) {
            log.info("Slack channel for project [{}] is blank, notifications disabled", projectConfig.getProjectKey());
            return true;
        }
        if (projectConfig.isQgFailOnly() && qualityGate != null && QualityGate.Status.OK.equals(
            qualityGate.getStatus())) {
            log.info("Project [{}] set up to send notification on failed Quality Gate, but was: {}",
                projectConfig.getProjectKey(), qualityGate.getStatus().name());
            return true;
        }
        return false;
    }
}
