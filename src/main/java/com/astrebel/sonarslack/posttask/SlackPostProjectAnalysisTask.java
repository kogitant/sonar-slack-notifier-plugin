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

/**
 * Created by 616286 on 3.6.2016.
 */
public class SlackPostProjectAnalysisTask implements PostProjectAnalysisTask {

    private static final Logger LOG = Loggers.get(SlackPostProjectAnalysisTask.class);

    private SlackClient slackClient;
    private Settings settings;

    public SlackPostProjectAnalysisTask(SlackClient slackClient, Settings settings) {
        LOG.info("Constructor called");
        this.slackClient = slackClient;
        this.settings = settings;
    }

    @Override
    public void finished(ProjectAnalysis analysis) {
        String hook = settings.getString(SlackNotifierPlugin.SLACK_HOOK);
        String channel = settings.getString(SlackNotifierPlugin.SLACK_CHANNEL);
        String slackUser = settings.getString(SlackNotifierPlugin.SLACK_SLACKUSER);

        if (hook == null) {
            LOG.info("No slack webhook configured...");
            return;
        }

        LOG.info("New analysis: " + analysis.toString());
        /*
        New analysis: ProjectAnalysis{
        ceTask=CeTaskImpl{id='AVUV4AmWMV3GgauddeNg', status=SUCCESS},
        project=ProjectImpl{uuid='AVUVezVeHpvlfGhlS1Fr', key='com.astrebel.sonarslack:sonar-slack-notifier-plugin',
        name='SonarQube Slack Notifier Plugin'},
        date=Fri Jun 03 13:47:25 EEST 2016,
        qualityGate=QualityGateImpl{
            id='1', name='SonarQube way', status=ERROR,
            conditions=[
                ConditionImpl{status=OK, metricKey='new_vulnerabilities', operator=GREATER_THAN, errorThreshold='0', warningThreshold='null', onLeakPeriod=true, value='0'},
                ConditionImpl{status=OK, metricKey='new_bugs', operator=GREATER_THAN, errorThreshold='0', warningThreshold='null', onLeakPeriod=true, value='0'},
                ConditionImpl{status=OK, metricKey='new_sqale_debt_ratio', operator=GREATER_THAN, errorThreshold='5', warningThreshold='null', onLeakPeriod=true, value='1.358811040339703'},
                ConditionImpl{status=ERROR, metricKey='new_coverage', operator=LESS_THAN, errorThreshold='80', warningThreshold='null', onLeakPeriod=true, value='8.24742268041237'}
               ]
               }
               }
         */

        String defaultMessage = "Project [" + analysis.getProject().getName() + "] analyzed";

        SlackMessage message = new SlackMessage(defaultMessage, slackUser);
        message.setChannel(channel);
        message.setShortText(defaultMessage);

        try{
            SlackAttachment a = new SlackAttachment();
            int qualityCateConditionsFailed = 0;
            for (QualityGate.Condition condition : analysis.getQualityGate().getConditions()) {
                if (QualityGate.EvaluationStatus.ERROR.equals(condition.getStatus())) {
                    qualityCateConditionsFailed++;
                    a.getReasons().add(condition.getMetricKey() + " " + condition.getStatus() + ": " + condition.getValue());
                }
            }
            message.setAttachment(a);
        }catch(NullPointerException e){
            // Ugh.
            LOG.error("NPE");
        }

        slackClient.send(hook, message);
    }
}
