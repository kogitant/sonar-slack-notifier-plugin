package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.Payload.PayloadBuilder;
import com.koant.sonar.slacknotifier.common.component.ProjectConfig;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.i18n.I18n;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Created by ak on 18/10/16.
 * Modified by poznachowski
 */

public class ProjectAnalysisPayloadBuilder {
    private static final Logger LOG = Loggers.get(ProjectAnalysisPayloadBuilder.class);

    private static final String SLACK_GOOD_COLOUR = "good";
    private static final String SLACK_WARNING_COLOUR = "warning";
    private static final String SLACK_DANGER_COLOUR = "danger";
    private static final Map<QualityGate.Status, String> statusToColor = new EnumMap<>(QualityGate.Status.class);

    static {
        statusToColor.put(QualityGate.Status.OK, SLACK_GOOD_COLOUR);
        statusToColor.put(QualityGate.Status.WARN, SLACK_WARNING_COLOUR);
        statusToColor.put(QualityGate.Status.ERROR, SLACK_DANGER_COLOUR);
    }

    I18n i18n;
    PostProjectAnalysisTask.ProjectAnalysis analysis;
    private ProjectConfig projectConfig;
    private String iconUrl;
    private String slackUser;
    private String projectUrl;

    private DecimalFormat percentageFormat;

    private ProjectAnalysisPayloadBuilder(PostProjectAnalysisTask.ProjectAnalysis analysis) {
        this.analysis = analysis;
        // Format percentages as 25.01 instead of 25.0066666666666667 etc.
        this.percentageFormat = new DecimalFormat();
        this.percentageFormat.setMaximumFractionDigits(2);
    }

    public static ProjectAnalysisPayloadBuilder of(PostProjectAnalysisTask.ProjectAnalysis analysis) {
        return new ProjectAnalysisPayloadBuilder(analysis);
    }

    public ProjectAnalysisPayloadBuilder projectConfig(ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
        return this;
    }

    public ProjectAnalysisPayloadBuilder i18n(I18n i18n) {
        this.i18n = i18n;
        return this;
    }

    public ProjectAnalysisPayloadBuilder username(String slackUser) {
        this.slackUser = slackUser;
        return this;
    }

    public ProjectAnalysisPayloadBuilder projectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
        return this;
    }

    public ProjectAnalysisPayloadBuilder iconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public Payload build() {
        assertNotNull(projectConfig, "projectConfig");
        assertNotNull(projectUrl, "projectUrl");
        assertNotNull(slackUser, "slackUser");
        assertNotNull(i18n, "i18n");
        assertNotNull(analysis, "analysis");

        QualityGate qualityGate = analysis.getQualityGate();
        String shortText = String.join("",
                "Project [", analysis.getProject().getName(), "] analyzed. See ",
                projectUrl,
                qualityGate == null ? "." : ". Quality gate status: " + qualityGate.getStatus());

        PayloadBuilder builder = Payload.builder()
                .channel(projectConfig.getSlackChannel())
                .username(slackUser)
                .text(shortText)
                .attachments(qualityGate == null ? null : buildConditionsAttachment(qualityGate, projectConfig.isQgFailOnly()));

        if (iconUrl != null) {
            builder.iconUrl(iconUrl);
        }

        return builder.build();
    }

    private void assertNotNull(Object object, String argumentName) {
        if (object == null) {
            throw new IllegalArgumentException("[Assertion failed] - " +argumentName + " argument is required; it must not be null");
        }
    }

    private List<Attachment> buildConditionsAttachment(QualityGate qualityGate, boolean qgFailOnly) {

        List<Attachment> attachments = new ArrayList<>();
        attachments.add(Attachment.builder()
                .fields(
                        qualityGate.getConditions()
                                .stream()
                                .filter(condition -> !qgFailOnly || notOkNorNoValue(condition))
                                .map(this::translate)
                                .collect(Collectors.toList()))
                .color(statusToColor.get(qualityGate.getStatus()))
                .build());
        return attachments;
    }

    private boolean notOkNorNoValue(QualityGate.Condition condition) {
        return !(QualityGate.EvaluationStatus.OK.equals(condition.getStatus())
                || QualityGate.EvaluationStatus.NO_VALUE.equals(condition.getStatus()));
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
            appendValue(condition, sb);
            appendValuePostfix(condition, sb);
            if (condition.getWarningThreshold() != null) {
                sb.append(", warning if ");
                appendValueOperatorPrefix(condition, sb);
                sb.append(condition.getWarningThreshold());
                appendValuePostfix(condition, sb);
            }
            if (condition.getErrorThreshold() != null) {
                sb.append(", error if ");
                appendValueOperatorPrefix(condition, sb);
                sb.append(condition.getErrorThreshold());
                appendValuePostfix(condition, sb);
            }
            return Field.builder().title(conditionName + ": " + condition.getStatus().name())
                    .value(sb.toString())
                    .valueShortEnough(false)
                    .build();

        }
    }

    private void appendValue(QualityGate.Condition condition, StringBuilder sb) {
        if ("".equals(condition.getValue())) {
            sb.append("-");
        } else {
            if (valueIsPercentage(condition)){
                appendPercentageValue(condition.getValue(), sb);
            }else {
                sb.append(condition.getValue());
            }
        }
    }

    private void appendPercentageValue(String s, StringBuilder sb) {
        try {
            Double d = Double.parseDouble(s);
            sb.append(percentageFormat.format(d));
        }catch(NumberFormatException e){
            LOG.error("Failed to parse [{}] into a Double due to [{}]", s, e.getMessage());
            sb.append(s);
        }
    }

    private void appendValueOperatorPrefix(QualityGate.Condition condition, StringBuilder sb) {
        switch (condition.getOperator()) {
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

    private void appendValuePostfix(QualityGate.Condition condition, StringBuilder sb) {
        if(valueIsPercentage(condition)){
            sb.append("%");
        }
    }

    private boolean valueIsPercentage(QualityGate.Condition condition){
        switch (condition.getMetricKey()) {
            case CoreMetrics.NEW_COVERAGE_KEY:
            case CoreMetrics.NEW_SQALE_DEBT_RATIO_KEY:
                return true;
        }
        return false;
    }


}
