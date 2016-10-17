# CKS Sonar Slack Notifier Plugin
SonarQube plugin for sending notifications to Slack

This plugin sends a Slack message of project analysis outcome to congired project specific slack channel.
The plugin uses Incoming Web Hook as the integration mechanism with Slack.

# Build & Install
To build the plugin call **mvn clean package** (or download the current release). The artifact must be copied to the *SONAR_HOME/extensions/plugins* folder and sonarqube must be restarted.

# Configuration
After the plugin has been installed, you need to configure it.
Although SonarQube offers project level configurations for some plugins, they cannot be used with this plugin because it runs in the "server side", and only sees the global settings.
 

# Works with
* -Tested with SonarQube 5.5 against Slack on 2016.06.03-
* Tested with SonarQube 6.1 against Slack on 2016.10.17

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

