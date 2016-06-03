# sonar-slack-notifier-plugin
SonarQube plugin for sending notifications to Slack

This plugin sends notifications to a given slack channel. You can configure a webhook, slack user and slack channel as a global or project specific setting.

# Build & Install
To build the plugin call **mvn clean package** (or download the current release). The artifact must be copied to the *SONAR_HOME/extensions/plugins* folder and sonarqube must be restarted.

# Works with
Tested with SonarQube 5.5 against Slack on 2016.06.03


# SonarQube Plugin Development guides
* http://docs.sonarqube.org/display/DEV/Build+plugin
* http://docs.sonarqube.org/display/DEV/Coding+a+Plugin

# Slack webhook integration and message formatting guides
 * https://api.slack.com/custom-integrations
 * https://api.slack.com/docs/attachments#message_formatting
 * https://api.slack.com/docs/attachments

