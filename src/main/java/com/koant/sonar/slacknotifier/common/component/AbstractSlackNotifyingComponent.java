package com.koant.sonar.slacknotifier.common.component;

import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.MessageException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract base component for Slack notifying Sonar extensions.
 * Concrete implementations must call com.koant.sonar.slacknotifier.common.component
 * .AbstractSlackNotifyingComponent#refreshSettings() in the beginning of actual execution.
 */
public abstract class AbstractSlackNotifyingComponent {

    private static final Logger LOG = Loggers.get(AbstractSlackNotifyingComponent.class);

    private final Configuration              settings;
    private       Map<String, ProjectConfig> projectConfigMap = Collections.emptyMap();

    public AbstractSlackNotifyingComponent(Configuration settings) {
        this.settings = settings;
        LOG.info("Constructor called, project slack channel config map constructed from general settings");
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
        LOG.info("Refreshing settings");
        refreshProjectConfigs();
    }

    private void refreshProjectConfigs() {
        LOG.info("Refreshing project configs");
        Set<ProjectConfig> oldValues = this.projectConfigMap.values().stream().
            map(ProjectConfigBuilder::cloneProjectConfig).collect(Collectors.toSet());
        this.projectConfigMap = buildProjectConfigByProjectKeyMap(this.settings);
        Set<ProjectConfig> newValues = new HashSet<>(this.projectConfigMap.values());
        if (!oldValues.equals(newValues)) {
            LOG.info("Old configs [{}] --> new configs [{}]", oldValues, newValues);
        }
    }

    protected String getSlackUser() {

        Optional<String> user = settings.get(SlackNotifierProp.USER.property());
        return user.orElseThrow(() -> new IllegalStateException("User property not found"));
    }

    protected String getIconUrl() {
        final Optional<String> icon = settings.get(SlackNotifierProp.ICON_URL.property());
        return icon.orElse(null);
    }

    protected String getDefaultChannel() {
        final Optional<String> defaultChannel = settings.get(SlackNotifierProp.DEFAULT_CHANNEL.property());
        return defaultChannel.orElseThrow(() -> new IllegalStateException("Default property not found"));
    }

    protected String getDefaultHook() {
        final Optional<String> defaultHook = settings.get(SlackNotifierProp.HOOK.property());
        return defaultHook.orElse(null);
    }


    protected boolean isPluginEnabled() {
        return settings.getBoolean(SlackNotifierProp.ENABLED.property())
                       .orElseThrow(()-> new IllegalStateException("Enabled property not found"));
    }

    /**
     * @return value for INCLUDE_BRANCH property, defaults to false if for some reason not set.
     */
    protected boolean isBranchEnabled() {

        return settings.getBoolean(SlackNotifierProp.INCLUDE_BRANCH.property()).orElse(false);
    }

    /**
     * Returns the sonar server url, with a trailing /
     *
     * @return the sonar server URL
     */
    protected String getSonarServerUrl() {
        Optional<String> urlOptional = settings.get("sonar.core.serverBaseURL");
        if (!urlOptional.isPresent()) {
            return "http://pleaseDefineSonarQubeUrl/";
        }
        String url = urlOptional.get();
        if (url.endsWith("/")) {
            return url;
        }
        return url + "/";
    }

    protected Optional<ProjectConfig> getProjectConfig(String projectKey) {
        List<ProjectConfig> projectConfigs = projectConfigMap.keySet()
                                                             .stream()
                                                             .filter(projectKey::matches)
                                                             .map(projectConfigMap::get)
                                                             .collect(Collectors.toList());
        // Not configured at all
        if (projectConfigs.isEmpty()) {
            LOG.info("Could not find config for project [{}] in [{}]", projectKey, projectConfigMap);

            LOG.info("Building the default project config.");
            final ProjectConfig projectConfig = new ProjectConfigBuilder().setProjectHook(getDefaultHook())
                                                                          .setProjectKeyOrRegExp(projectKey)
                                                                          .setSlackChannel(getDefaultChannel())
                                                                          .setNotify(getSlackUser())
                                                                          .setQgFailOnly(false)
                                                                          .build();
            return Optional.of(projectConfig);
        }

        if (projectConfigs.size() > 1) {
            LOG.warn("More than 1 project key was matched. Using first one: {}", projectConfigs.get(0).getProjectKey());
        }
        return Optional.of(projectConfigs.get(0));
    }

    private static Map<String, ProjectConfig> buildProjectConfigByProjectKeyMap(Configuration settings) {
        Map<String, ProjectConfig> map = new HashMap<>();
        String[] projectConfigIndexes = settings.getStringArray(SlackNotifierProp.CONFIG.property());
        LOG.info("SlackNotifierProp.CONFIG=[{}]", (Object) projectConfigIndexes);
        for (String projectConfigIndex : projectConfigIndexes) {
            String projectKeyProperty = SlackNotifierProp.CONFIG
                .property() + "." + projectConfigIndex + "." + SlackNotifierProp.PROJECT_REGEXP
                .property();
            Optional<String> projectKey = settings.get(projectKeyProperty);
            if (!projectKey.isPresent()) {
                throw MessageException.of(
                    "Slack notifier configuration is corrupted. At least one project specific parameter has no " +
                        "project key. " +
                        "Contact your administrator to update this configuration in the global administration section" +
                        " of SonarQube.");
            }
            ProjectConfig value = new ProjectConfigBuilder().withConfiguration(settings, SlackNotifierProp.CONFIG
                .property() + "." + projectConfigIndex + ".").build();
            LOG.info("Found project configuration [{}]", value);
            map.put(projectKey.get(), value);
        }
        return map;
    }

    protected String logRelevantSettings() {
        Map<String, String> pluginSettings = new HashMap<>();
        mapSetting(pluginSettings, SlackNotifierProp.HOOK);
        mapSetting(pluginSettings, SlackNotifierProp.USER);
        mapSetting(pluginSettings, SlackNotifierProp.PROXY_IP);
        mapSetting(pluginSettings, SlackNotifierProp.PROXY_PORT);
        mapSetting(pluginSettings, SlackNotifierProp.PROXY_PROTOCOL);
        mapSetting(pluginSettings, SlackNotifierProp.ENABLED);
        mapSetting(pluginSettings, SlackNotifierProp.CONFIG);
        mapSetting(pluginSettings, SlackNotifierProp.INCLUDE_BRANCH);
        return pluginSettings.toString() + "; project specific channel config: " + projectConfigMap;
    }

    private void mapSetting(Map<String, String> pluginSettings, SlackNotifierProp key) {
        pluginSettings.put(key.name(), settings.get(key.property()).orElse(""));
    }

    protected boolean shouldSkipSendingNotification(ProjectConfig projectConfig, QualityGate qualityGate) {
        // Disabled due to missing channel value
        if (projectConfig.getSlackChannel() == null ||
            "".equals(projectConfig.getSlackChannel().trim())) {
            LOG.info("Slack channel for project [{}] is blank, notifications disabled", projectConfig.getProjectKey());
            return true;
        }
        if (projectConfig.isQgFailOnly() && qualityGate != null && QualityGate.Status.OK.equals(
            qualityGate.getStatus())) {
            LOG.info("Project [{}] set up to send notification on failed Quality Gate, but was: {}",
                     projectConfig.getProjectKey(), qualityGate.getStatus().name());
            return true;
        }
        return false;
    }
}
