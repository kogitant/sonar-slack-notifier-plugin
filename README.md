# CKS Sonar Slack Notifier Plugin
SonarQube plugin for sending notifications to Slack

This plugin sends a Slack message of project analysis outcome to congired project specific slack channel.
The plugin uses Incoming Web Hook as the integration mechanism with Slack.

# Install
The plugin must be placed in *SONAR_HOME/extensions/plugins* directory and SonarQube must be restarted.

## Using latest release
You can find the latest release from https://github.com/kogitant/sonar-slack-notifier-plugin/releases/ page.
Download the jar file.

## From sources
To build the plugin simply run
```
mvn clean package
```


# Configuration
After the plugin has been installed, you need to configure it.
Although SonarQube offers project level configurations for some plugins, they cannot be used with this plugin because it runs in the "server side", and only sees the global settings.

As administrator, go to the general settings and configure the Sonar instance URL:
![](documentation/screenshots/administration_server_base_url.png?raw=true)

A new category Slack appears in the left menu:
![](documentation/screenshots/administration_slack_category.png?raw=true)

Under it you can find the CKS Slack Notifier plugin configurations:
![](documentation/screenshots/administration_cks_slack_notifier_settings.png?raw=true)

In the above example there is a Project Key to Slack Channel configuration for an example project.
The project key of any SonarQube project can be found in the project page (bottom right corner):
![](documentation/screenshots/project_key_from_project_page.png?raw=true)

## Wildcard support
The project key supports wildcards at the end. See https://github.com/kogitant/sonar-slack-notifier-plugin/issues/2

## Only send notification when Quality Gate fails
Notifications can be sent for all Quality Gate statuses, or just for WARNING/ERROR statuses. See https://github.com/kogitant/sonar-slack-notifier-plugin/issues/1 
 
# Example messages posted to Slack
## New bug introduced
![](documentation/screenshots/example_slack_message_red_due_to_new_bug.png)

## All good
![](documentation/screenshots/example_slack_message_all_green.png)

# Works with
* Tested with SonarQube 6.1 against Slack on 2016.10.17
* Tested with SonarQube 6.1 against Mattermost 3.5.0 on 2017.01.02

# Inspired by
* https://github.com/astrebel/sonar-slack-notifier-plugin
* https://github.com/dbac2002/sonar-hipchat-plugin

# Benefits from
* https://github.com/seratch/jslack

# SonarQube Plugin Development guides
* http://docs.sonarqube.org/display/DEV/Adding+Hooks
* http://docs.sonarqube.org/display/DEV/Build+plugin

# Slack webhook integration and message formatting guides
 * https://api.slack.com/custom-integrations
 * https://api.slack.com/docs/attachments#message_formatting
 * https://api.slack.com/docs/attachments


# Analyzing this project with unit test and integration test coverage
```
    mvn clean jacoco:prepare-agent install -DskipITs=true
    mvn jacoco:prepare-agent-integration failsafe:integration-test
    mvn sonar-maven-plugin:sonar -Dsonar.host.url=http://localhost:9000
```
