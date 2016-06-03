package com.astrebel.sonarslack.posttask;

import com.astrebel.sonarslack.SlackClient;
import com.astrebel.sonarslack.SlackNotifierPlugin;
import com.astrebel.sonarslack.message.SlackAttachment;
import com.astrebel.sonarslack.message.SlackMessage;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.Locale;

/**
 * Created by 616286 on 3.6.2016.
 */
public class SlackPostProjectAnalysisTask implements PostProjectAnalysisTask {

    private static final Logger LOG = Loggers.get(SlackPostProjectAnalysisTask.class);

    org.sonar.api.i18n.I18n i18n;
    private SlackClient slackClient;
    private Settings settings;

    public SlackPostProjectAnalysisTask(SlackClient slackClient, Settings settings, org.sonar.api.i18n.I18n i18n) {
        LOG.info("Constructor called");
        this.slackClient = slackClient;
        this.settings = settings;
        this.i18n = i18n;
    }

    @Override
    public void finished(ProjectAnalysis analysis) {
        LOG.info("New analysis: " + analysis.toString());
        SlackMessage message = slackMessage(settings);
        if (message == null) {
            LOG.info("Slack integration not properly configured. Properties are [{}]", settings.getProperties());
            return;
        }
        String shortText = "Project [" + analysis.getProject().getName() + "] analyzed. See " + projectUrl(analysis);

        SlackAttachment a = new SlackAttachment();
        QualityGate qualityGate = analysis.getQualityGate();
        if(qualityGate!=null){
            shortText += ". Quality gate status is " + qualityGate.getStatus();
            qualityGate.getConditions().stream().forEach(condition -> a.getReasons().add(translate(condition)));
            message.setAttachment(a);
        }
        message.setShortText(shortText);
        slackClient.send(message);
    }

    private String projectUrl(ProjectAnalysis analysis) {
        return settings.getString("sonar.core.serverBaseURL") + "overview?id=" + analysis.getProject().getKey();
    }

    private SlackMessage slackMessage(Settings settings) {
        String hook = settings.getString(SlackNotifierPlugin.SLACK_HOOK);
        if(hook == null) {
            return null;
        }
        String channel = settings.getString(SlackNotifierPlugin.SLACK_CHANNEL);
        if(channel == null) {
            return null;
        }
        String slackUser = settings.getString(SlackNotifierPlugin.SLACK_SLACKUSER);
        if(slackUser == null) {
            return null;
        }
        return new SlackMessage(channel, slackUser, hook);
    }

    private String translate(QualityGate.Condition condition) {
        StringBuilder sb = new StringBuilder();
        // sonarqube/sonar-core/src/main/resources/org/sonar/l10n/core.properties
        sb.append(i18n.message(Locale.ENGLISH, "metric."+condition.getMetricKey()+".name", condition.getMetricKey()));
        sb.append(" ");
        sb.append(i18n.message(Locale.ENGLISH, "overview.gate."+condition.getStatus(), condition.getStatus().name()));
        sb.append(" ");
        sb.append("Value [").append(condition.getValue()).append("], ");
        sb.append("operator [").append(condition.getOperator()).append("], ");
        sb.append("warning threshold [").append(condition.getWarningThreshold()).append("], ");
        sb.append("error threshold [").append(condition.getErrorThreshold()).append("], ");
        sb.append("on leak period [").append(condition.isOnLeakPeriod()).append(']');
        return sb.toString();
    }
}
