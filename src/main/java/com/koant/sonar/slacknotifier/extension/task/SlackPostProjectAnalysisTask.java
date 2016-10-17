package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.koant.sonar.slacknotifier.common.component.AbstractSlackNotifyingComponent;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by 616286 on 3.6.2016.
 */
public class SlackPostProjectAnalysisTask extends AbstractSlackNotifyingComponent implements PostProjectAnalysisTask {

    private static final Logger LOG = Loggers.get(SlackPostProjectAnalysisTask.class);

    private Settings settings;
    private Slack slackClient;

    public SlackPostProjectAnalysisTask(Settings settings) {
        this(Slack.getInstance(), settings);
    }

    public SlackPostProjectAnalysisTask(Slack slackClient, Settings settings) {
        super(settings);
        this.slackClient = slackClient;
        this.settings = settings;
    }

    @Override
    public void finished(ProjectAnalysis analysis) {
        refreshSettings();

        LOG.info("analysis.getScannerContext().getProperties()=[{}}", analysis.getScannerContext().getProperties());
        String projectKey = analysis.getProject().getKey();
        if(!taskEnabled(projectKey)){
            return;
        }
        LOG.info("Slack notification will be sent: " + analysis.toString());

        String shortText = "Project [" + analysis.getProject().getName() + "] analyzed. See " + projectUrl(analysis);

        List<Field> fields = new ArrayList<>();
        QualityGate qualityGate = analysis.getQualityGate();
        if(qualityGate!=null){
            shortText += ". Quality gate status is " + qualityGate.getStatus();
            qualityGate.getConditions().stream().forEach(condition -> fields.add(translate(condition)));
        }


        Payload payload = Payload.builder()
                .channel(getSlackChannel(projectKey))
                .username(getSlackUser())
                .text(shortText)
                .attachments(Arrays.asList(new Attachment[]{
                        Attachment.builder()
                                .fields(fields)
                                .build()
                }))
                .build();

        try {
            // See https://github.com/seratch/jslack
            WebhookResponse response = slackClient.send(getSlackIncomingWebhookUrl(), payload);
            if(!Integer.valueOf(200).equals(response.getCode())){
                LOG.error("Failed to post to slack, response is [{}]", response);
            }
        } catch (IOException e) {
            LOG.error("Failed to send slack message", e);
        }
    }

    private String projectUrl(ProjectAnalysis analysis) {
        return settings.getString("sonar.core.serverBaseURL") + "overview?id=" + analysis.getProject().getKey();
    }


    /**
     * See https://api.slack.com/docs/message-attachments#message_formatting
     * @param condition
     * @return
     */
    private Field translate(QualityGate.Condition condition) {
        if(QualityGate.EvaluationStatus.NO_VALUE.equals(condition.getStatus())){
            // No value for given metric
            return Field.builder().title(condition.getMetricKey())
                    .value(condition.getStatus().name())
                    .valueShortEnough(true)
                    .build();
        }else {
            StringBuilder sb = new StringBuilder();
            sb.append("Value [").append(condition.getValue()).append("], ");
            sb.append("operator [").append(condition.getOperator()).append("], ");
            sb.append("warning threshold [").append(condition.getWarningThreshold()).append("], ");
            sb.append("error threshold [").append(condition.getErrorThreshold()).append("], ");
            sb.append("on leak period [").append(condition.isOnLeakPeriod()).append(']');

            return Field.builder().title(condition.getMetricKey() + ": " + condition.getStatus().name())
                    .value(sb.toString())
                    .valueShortEnough(false)
                    .build();

        }
    }
}
