package com.astrebel.sonarslack.batch;


import com.astrebel.sonarslack.SlackClient;
import com.astrebel.sonarslack.SlackNotifierPlugin;
import com.astrebel.sonarslack.message.SlackMessage;
import org.sonar.api.batch.CheckProject;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.ProjectIssues;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class SlackPostJob implements org.sonar.api.batch.PostJob, CheckProject {

    private static final Logger LOG = Loggers.get(SlackPostJob.class);

    private final ProjectIssues projectIssues;
    private SlackClient slackClient;
    private Settings settings;

    public SlackPostJob(ProjectIssues projectIssues, SlackClient slackClient, Settings settings) {
        LOG.info("Constructor called");
        this.slackClient = slackClient;
        this.settings = settings;
        this.projectIssues = projectIssues;
    }


    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return true;
    }

    @Override
    public void executeOn(Project project, SensorContext context) {
        String hook = settings.getString(SlackNotifierPlugin.SLACK_HOOK);
        String channel = settings.getString(SlackNotifierPlugin.SLACK_CHANNEL);
        String slackUser = settings.getString(SlackNotifierPlugin.SLACK_SLACKUSER);

        if (hook == null) {
            LOG.info("No slack webhook configured...");
            return;
        }


        int newIssues = 0;
        int resolvedIssues = 0;

        // all open issues
        for (Issue issue : projectIssues.issues()) {
            boolean isNew = issue.isNew();
            if(isNew){
                newIssues++;
            }
        }

        // all resolved issues
        for (Issue issue : projectIssues.resolvedIssues()) {
            resolvedIssues++;
        }

        SlackMessage message = new SlackMessage(project.getName() + " has [" + newIssues + "] new issues and [" + resolvedIssues + "] resolved issues", slackUser);
        message.setChannel(channel);

        slackClient.send(hook, message);

    }
}



