# CKS Sonar Slack Notifier Plugin
SonarQube plugin for sending notifications to Slack

This plugin sends a Slack message of project analysis outcome to congired project specific slack channel.
The plugin uses Incoming Web Hook as the integration mechanism with Slack.

# Build & Install
To build the plugin call **mvn clean package** (or download the current release). The artifact must be copied to the *SONAR_HOME/extensions/plugins* folder and sonarqube must be restarted.

# Configuration
After the plugin has been installed, you need to configure it.
Although SonarQube offers project level configurations for some plugins, they cannot be used with this plugin because it runs in the "server side", and only sees the global settings.

As administrator, go to the general settings and configure the Sonar instance URL:
!(documentation/screenshots/administration_server_base_url.png?raw=true)

A new category Slack appears in the left menu:
!(documentation/screenshots/administration_slack_category.png?raw=true)

Under it you can find the CKS Slack Notifier plugin configurations:
!(documentation/screenshots/administration_cks_slack_notifier_settings.png?raw=true)

In the above example there is a Project Key to Slack Channel configuration for an example project.
The project key of any SonarQube project can be found in the project page (bottom right corner):
!(documentation/screenshots/project_key_from_project_page.png?raw=true)

Once everything is configured, run an analysis of your project:
```
    $ mvn clean install
    $ mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.1.1:sonar -Dsonar.host.url=http://localhost:9000
```

The result should be something that looks like this in Slack:
!(documentation/screenshots/example_slack_message.png)

# Works with
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

