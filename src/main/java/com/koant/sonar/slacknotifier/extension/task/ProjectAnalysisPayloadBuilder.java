package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.i18n.I18n;
import org.sonar.api.measures.CoreMetrics;
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
            if ("" .equals(condition.getValue())) {
                sb.append("-");
            } else{
                sb.append(condition.getValue());
            }
            getValuePostfix(condition, sb);
            if(condition.getWarningThreshold()!=null){
                sb.append(", warning if ");
                getValueOperatorPrefix(condition, sb);
                sb.append(condition.getWarningThreshold());
                getValuePostfix(condition, sb);
            }
            if(condition.getErrorThreshold()!=null){
                sb.append(", error if ");
                getValueOperatorPrefix(condition, sb);
                sb.append(condition.getErrorThreshold());
                getValuePostfix(condition, sb);
            }
            return Field.builder().title(conditionName + ": " + condition.getStatus().name())
                .value(sb.toString())
                .valueShortEnough(false)
                .build();

        }
    }

    private void getValueOperatorPrefix(QualityGate.Condition condition, StringBuilder sb) {
        switch(condition.getOperator()){
            case EQUALS:
                sb.append("==");
                break;
            case NOT_EQUALS:
                sb.append("!=");
                break;
            case GREATER_THAN:
                sb.append(">");
                break;
            case LESS_THAN:
                sb.append("<");
                break;
        }
    }

    private void getValuePostfix(QualityGate.Condition condition, StringBuilder sb) {
        switch(condition.getMetricKey()){
            case CoreMetrics.NEW_COVERAGE_KEY:
            case CoreMetrics.NEW_SQALE_DEBT_RATIO_KEY:
                sb.append("%");
                break;
        }
    }


}
