package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.i18n.I18n;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by ak on 18/10/16.
 */
public class ProjectAnalysisPayloadBuilder {
    private static final Logger LOG = Loggers.get(ProjectAnalysisPayloadBuilder.class);

    I18n i18n;
    PostProjectAnalysisTask.ProjectAnalysis analysis;
    private String projectUrl;
    private String slackChannel;
    private String slackUser;

    public ProjectAnalysisPayloadBuilder(I18n i18n, PostProjectAnalysisTask.ProjectAnalysis analysis) {
        this.i18n = i18n;
        this.analysis = analysis;
    }

    public ProjectAnalysisPayloadBuilder projectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
        return this;
    }

    public ProjectAnalysisPayloadBuilder channel(String slackChannel) {
        this.slackChannel = slackChannel;
        return this;
    }

    public ProjectAnalysisPayloadBuilder username(String slackUser) {
        this.slackUser = slackUser;
        return this;
    }

    public Payload build() {
        String shortText = "Project [" + analysis.getProject().getName() + "] analyzed. See " + projectUrl;
        List<Field> fields = new ArrayList<>();
        QualityGate qualityGate = analysis.getQualityGate();
        if (qualityGate != null) {
            shortText += ". Quality gate status is " + qualityGate.getStatus();
            qualityGate.getConditions().stream().forEach(condition -> fields.add(translate(condition)));
        }
        Payload payload = Payload.builder()
            .channel(slackChannel)
            .username(slackUser)
            .text(shortText)
            .attachments(Arrays.asList(new Attachment[]{
                Attachment.builder()
                    .fields(fields)
                    .build()
            }))
            .build();

        return payload;
    }

    /**
     * See https://api.slack.com/docs/message-attachments#message_formatting
     *
     * @param condition
     * @return
     */
    private Field translate(QualityGate.Condition condition) {
        String i18nKey = "metric." + condition.getMetricKey() + ".name";
        String conditionName = i18n.message(Locale.ENGLISH, i18nKey, condition.getMetricKey());

        if (QualityGate.EvaluationStatus.NO_VALUE.equals(condition.getStatus())) {
            // No value for given metric
            return Field.builder().title(conditionName)
                .value(condition.getStatus().name())
                .valueShortEnough(true)
                .build();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Value [").append(condition.getValue()).append("], ");
            sb.append("operator [").append(condition.getOperator()).append("], ");
            sb.append("warning threshold [").append(condition.getWarningThreshold()).append("], ");
            sb.append("error threshold [").append(condition.getErrorThreshold()).append("], ");
            sb.append("on leak period [").append(condition.isOnLeakPeriod()).append(']');

            return Field.builder().title(conditionName + ": " + condition.getStatus().name())
                .value(sb.toString())
                .valueShortEnough(false)
                .build();

        }
    }


}
