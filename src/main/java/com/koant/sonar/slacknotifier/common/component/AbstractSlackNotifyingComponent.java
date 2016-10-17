package com.koant.sonar.slacknotifier.common.component;

import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.MessageException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.*;

/**
 * Abstract base component for slack notifying Sonar extensions.
 * Concrete implementations must call com.koant.sonar.slacknotifier.common.component.AbstractSlackNotifyingComponent#refreshSettings() in the beginning of actual execution.
 */
public abstract class AbstractSlackNotifyingComponent {

    private static final Logger LOG = Loggers.get(AbstractSlackNotifyingComponent.class);

    private final Settings settings;
    private Map<String, ProjectSlackChannelConfig> projectSlackChannelConfigMap = Collections.emptyMap();

    public AbstractSlackNotifyingComponent(Settings settings) {
        this.settings = settings;
        LOG.info("Constructor called, project slack channel config map constructed from general settings");
    }

    /**
     * This method has to be called in the beginning of every actual plugin execution.
     * SonarQube seems to work in such a way that
     * <pre>
     * 1) the Settings object is constructor injected to this class.
     * 2) the values reflected by the Settings object reflect latest settings configured
     * 3) but the constructor of this class is called only once, and after that the class is never instantiated again (the same instance is reused)
     * 4) thus when the instance is used to perform something, we must refresh the projectSlackChannelConfigMap when the execution starts
     * </pre>
     */
    protected void refreshSettings(){
        LOG.info("Refreshing settings");
        refreshSlackChannelConfig();
    }

    private void refreshSlackChannelConfig() {
        LOG.info("Refreshing slack channel config");
        Set<ProjectSlackChannelConfig> oldValues = new HashSet<>();
        this.projectSlackChannelConfigMap.values().forEach(c -> {oldValues.add(new ProjectSlackChannelConfig(c));});
        this.projectSlackChannelConfigMap = buildProjectSlackChannelConfigByProjectKeyMap(settings);
        Set<ProjectSlackChannelConfig> newValues = new HashSet<>(this.projectSlackChannelConfigMap.values());
        if(!oldValues.equals(newValues)){
            LOG.info("Old configs [{}] --> new configs [{}]", oldValues, newValues);
        }
    }

    protected boolean taskEnabled(String projectKey){
        if(!isPluginEnabled()){
            LOG.info("Slack notifier plugin disabled, skipping. Settings are [{}]", logRelevantSettings());
            return false;
        }
        if(!isTaskEnabled(projectKey)){
            LOG.info("Post analysis task notifications disabled, skipping. Settings are [{}]", logRelevantSettings());
            return false;
        }
        return true;
    }

    protected String getSlackIncomingWebhookUrl() {
        return settings.getString(SlackNotifierProp.HOOK.property());
    }

    protected String getSlackChannel(String projectKey) {
        return projectSlackChannelConfigMap.get(projectKey).getSlackChannel();
    }

    protected String getSlackUser() {
        return settings.getString(SlackNotifierProp.USER.property());
    }

    private boolean isPluginEnabled() {
        return settings.getBoolean(SlackNotifierProp.ENABLED.property());
    }

    private boolean isTaskEnabled(String projectKey) {
        ProjectSlackChannelConfig projectSlackChannelConfig = getProjectSlackChannelConfig(projectKey);
        // Not configured at all
        if(projectSlackChannelConfig==null){
            LOG.info("Could not find slack channel for project [{}] in [{}]", projectKey, projectSlackChannelConfigMap);
            return false;
        }
        // Disabled due to missing channel value
        if(projectSlackChannelConfig.getSlackChannel()==null || "".equals(projectSlackChannelConfig.getSlackChannel().trim())){
            LOG.info("Slack channel for project [{}] is blank, notifications disabled", projectKey);
            return false;
        }
        return true;
    }

    private ProjectSlackChannelConfig getProjectSlackChannelConfig(String projectKey) {
        return projectSlackChannelConfigMap.get(projectKey);
    }

    /**
     * Returns the sonar server url, with a trailing /
     * @return
     */
    protected String getSonarServerUrl() {
        String u = settings.getString("sonar.core.serverBaseURL");
        if(u==null){
            return null;
        }
        if(u.endsWith("/")){
            return u;
        }
        return u + "/";
    }



    private static Map<String, ProjectSlackChannelConfig> buildProjectSlackChannelConfigByProjectKeyMap(Settings settings) {
        Map<String, ProjectSlackChannelConfig> map = new HashMap<>();
        String[] projectSlackChannelConfigIndexes = settings.getStringArray(SlackNotifierProp.CHANNELS.property());
        LOG.info("SlackNotifierProp.CHANNELS=[{}]", projectSlackChannelConfigIndexes);
        for (String projectSlackChannelConfigIndex : projectSlackChannelConfigIndexes) {
            String projectKeyProperty = SlackNotifierProp.CHANNELS.property() + "." + projectSlackChannelConfigIndex + "." + SlackNotifierProp.PROJECT.property();
            String projectKey = settings.getString(projectKeyProperty);
            if (projectKey == null) {
                throw MessageException.of("Slack notifier configuration is corrupted. At least one project specific parameter has no project key. " +
                        "Contact your administrator to update this configuration in the global administration section of SonarQube.");
            }
            ProjectSlackChannelConfig value = ProjectSlackChannelConfig.create(settings, projectSlackChannelConfigIndex);
            LOG.info("Found project slack channel configuration [{}]", value);
            map.put(projectKey, value);
        }
        return map;
    }



    private String logRelevantSettings() {
        Map<String,String> pluginSettings = new HashMap<>();
        mapSetting(pluginSettings, SlackNotifierProp.HOOK);
        mapSetting(pluginSettings, SlackNotifierProp.USER);
        mapSetting(pluginSettings, SlackNotifierProp.ENABLED);
        mapSetting(pluginSettings, SlackNotifierProp.CHANNELS);

        return pluginSettings.toString() + "; project specific channel config: " + projectSlackChannelConfigMap;
    }

    private void mapSetting(Map<String, String> pluginSettings, SlackNotifierProp key) {
        pluginSettings.put(key.name(), settings.getString(key.property()));
    }

}
