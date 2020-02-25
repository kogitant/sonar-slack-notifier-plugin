package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;
import com.koant.sonar.slacknotifier.common.component.ProjectConfig;
import org.sonar.api.ce.posttask.Branch;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.i18n.I18n;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by ak on 18/10/16.
 * Modified by poznachowski
 */

class ProjectAnalysisPayloadBuilder {
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

    private final PostProjectAnalysisTask.ProjectAnalysis analysis;
    private final DecimalFormat percentageFormat;
    private I18n i18n;
    private ProjectConfig projectConfig;
    private String iconUrl;
    private String slackUser;
    private String projectUrl;
    private boolean includeBranch;

    private ProjectAnalysisPayloadBuilder(final PostProjectAnalysisTask.ProjectAnalysis analysis) {
        this.analysis = analysis;
        // Format percentages as 25.01 instead of 25.0066666666666667 etc.
        this.percentageFormat = new DecimalFormat();
        this.percentageFormat.setMaximumFractionDigits(2);
    }

    static ProjectAnalysisPayloadBuilder of(final PostProjectAnalysisTask.ProjectAnalysis analysis) {
        return new ProjectAnalysisPayloadBuilder(analysis);
    }

    ProjectAnalysisPayloadBuilder projectConfig(final ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
        return this;
    }

    ProjectAnalysisPayloadBuilder i18n(final I18n i18n) {
        this.i18n = i18n;
        return this;
    }

    ProjectAnalysisPayloadBuilder username(final String slackUser) {
        this.slackUser = slackUser;
        return this;
    }

    ProjectAnalysisPayloadBuilder projectUrl(final String projectUrl) {
        this.projectUrl = projectUrl;
        return this;
    }

    ProjectAnalysisPayloadBuilder iconUrl(final String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    ProjectAnalysisPayloadBuilder includeBranch(final boolean includeBranch) {
        this.includeBranch = includeBranch;
        return this;
    }

    Payload build() {
        assertNotNull(projectConfig, "projectConfig");
        assertNotNull(projectUrl, "projectUrl");
        assertNotNull(slackUser, "slackUser");
        assertNotNull(i18n, "i18n");
        assertNotNull(analysis, "analysis");

        final String notifyPrefix = isNotBlank(projectConfig.getNotify()) ? format("<!%s> ", projectConfig.getNotify()) : "";

        final QualityGate qualityGate = analysis.getQualityGate();
        final StringBuilder shortText = new StringBuilder();
        shortText.append(notifyPrefix);
        shortText.append(format("Project [%s] analyzed", analysis.getProject().getName()));

        final Optional<Branch> branch = analysis.getBranch();
        if (branch.isPresent() && !branch.get().isMain() && this.includeBranch) {
            shortText.append(format(" for branch [%s]", branch.get().getName().orElse("")));
        }
        shortText.append(". ");
        shortText.append(format("See %s", projectUrl));
        shortText.append(qualityGate == null ? "." : format(". Quality gate status: %s", qualityGate.getStatus()));

        final Payload.PayloadBuilder builder = Payload.builder()
            .channel(projectConfig.getSlackChannel())
            .username(slackUser)
            .text(shortText.toString())
            .attachments(qualityGate == null ? null : buildConditionsAttachment(qualityGate, projectConfig.isQgFailOnly()));

        if (iconUrl != null) {
            builder.iconUrl(iconUrl);
        }

        return builder.build();
    }

    private void assertNotNull(final Object object, final String argumentName) {
        if (object == null) {
            throw new IllegalArgumentException("[Assertion failed] - " + argumentName + " argument is required; it must not be null");
        }
    }

    private List<Attachment> buildConditionsAttachment(final QualityGate qualityGate, final boolean qgFailOnly) {

        final List<Attachment> attachments = new ArrayList<>();

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

    private boolean notOkNorNoValue(final QualityGate.Condition condition) {
        return !(QualityGate.EvaluationStatus.OK.equals(condition.getStatus())
            || QualityGate.EvaluationStatus.NO_VALUE.equals(condition.getStatus()));
    }

    /**
     * See https://api.slack.com/docs/message-attachments#message_formatting
     *
     * @param condition the condition
     * @return
     */
    private Field translate(final QualityGate.Condition condition) {
        final String i18nKey = "metric." + condition.getMetricKey() + ".name";
        final String conditionName = i18n.message(Locale.ENGLISH, i18nKey, condition.getMetricKey());

        if (QualityGate.EvaluationStatus.NO_VALUE.equals(condition.getStatus())) {
            // No value for given metric
            return Field.builder().title(conditionName)
                .value(condition.getStatus().name())
                .valueShortEnough(true)
                .build();
        } else {
            final StringBuilder sb = new StringBuilder();
            appendValue(condition, sb);
            appendValuePostfix(condition, sb);
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

    private void appendValue(final QualityGate.Condition condition, final StringBuilder sb) {
        if ("".equals(condition.getValue())) {
            sb.append("-");
        } else {
            if (isPercentageCondition(condition)) {
                appendPercentageValue(condition.getValue(), sb);
            } else {
                sb.append(condition.getValue());
            }
        }
    }

    private void appendValuePostfix(final QualityGate.Condition condition, final StringBuilder sb) {
        if (isPercentageCondition(condition)) {
            sb.append("%");
        }
    }

    private void appendValueOperatorPrefix(final QualityGate.Condition condition, final StringBuilder sb) {
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
            default:
                break;
        }
    }

    private boolean isPercentageCondition(final QualityGate.Condition condition) {
        switch (condition.getMetricKey()) {
            case CoreMetrics.NEW_COVERAGE_KEY:
            case CoreMetrics.NEW_SQALE_DEBT_RATIO_KEY:
                return true;
            default:
                break;
        }
        return false;
    }

    private void appendPercentageValue(final String s, final StringBuilder sb) {
        try {
            final Double d = Double.parseDouble(s);
            sb.append(percentageFormat.format(d));
        } catch (final NumberFormatException e) {
            LOG.error("Failed to parse [{}] into a Double due to [{}]", s, e.getMessage());
            sb.append(s);
        }
    }
}

