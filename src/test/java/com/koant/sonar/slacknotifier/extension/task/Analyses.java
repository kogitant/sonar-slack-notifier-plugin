package com.koant.sonar.slacknotifier.extension.task;

import static org.sonar.api.ce.posttask.PostProjectAnalysisTaskTester.newCeTaskBuilder;
import static org.sonar.api.ce.posttask.PostProjectAnalysisTaskTester.newConditionBuilder;
import static org.sonar.api.ce.posttask.PostProjectAnalysisTaskTester.newQualityGateBuilder;
import static org.sonar.api.ce.posttask.PostProjectAnalysisTaskTester.newScannerContextBuilder;

import org.sonar.api.ce.posttask.Branch;
import org.sonar.api.ce.posttask.CeTask;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.PostProjectAnalysisTaskTester;
import org.sonar.api.ce.posttask.Project;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.measures.CoreMetrics;

import java.util.Date;

public class Analyses {

    public static final String PROJECT_KEY = "my-sonar-project-key";
    private static final String PROJECT_NAME = "Sonar Project Name";
    private static final Project PROJECT = PostProjectAnalysisTaskTester.newProjectBuilder()
            .setUuid("uuid")
            .setKey(PROJECT_KEY)
            .setName(PROJECT_NAME)
            .build();
    private static final CeTask CE_TASK = newCeTaskBuilder()
            .setId("id")
            .setStatus(CeTask.Status.SUCCESS)
            .build();

    public static void simple(final PostProjectAnalysisTask analysisTask) {
        PostProjectAnalysisTaskTester.of(analysisTask)
                .withCeTask(CE_TASK)
                .withProject(PROJECT)
                .withScannerContext(newScannerContextBuilder().build())
                .at(new Date())
                .withQualityGate(
                        newQualityGateBuilder()
                                .setId("id")
                                .setName("name")
                                .setStatus(QualityGate.Status.OK)
                                .add(
                                        newConditionBuilder()
                                                .setMetricKey("metric key")
                                                .setOperator(QualityGate.Operator.GREATER_THAN)
                                                .setErrorThreshold("12")
                                                .setOnLeakPeriod(true)
                                                .build(QualityGate.EvaluationStatus.OK, "value"))
                                .build())
                .execute();
    }


    public static void simpleDifferentKey(final PostProjectAnalysisTask analysisTask) {
        PostProjectAnalysisTaskTester.of(analysisTask)
                .withCeTask(CE_TASK)
                .withProject(PostProjectAnalysisTaskTester.newProjectBuilder()
                        .setUuid("uuid")
                        .setKey("different:key")
                        .setName(PROJECT_NAME)
                        .build())
                .withScannerContext(newScannerContextBuilder()
                        .build())
                .at(new Date())
                .withQualityGate(
                        newQualityGateBuilder()
                                .setId("id")
                                .setName("name")
                                .setStatus(QualityGate.Status.OK)
                                .add(
                                        newConditionBuilder()
                                                .setMetricKey("metric key")
                                                .setOperator(QualityGate.Operator.GREATER_THAN)
                                                .setErrorThreshold("12")
                                                .setOnLeakPeriod(true)
                                                .build(QualityGate.EvaluationStatus.OK, "value"))
                                .build())
                .execute();
    }

    public static void qualityGateOk4Conditions(final PostProjectAnalysisTask analysisTask) {
        PostProjectAnalysisTaskTester.of(analysisTask)
                .withCeTask(CE_TASK)
                .withProject(PROJECT)
                .at(new Date())
                .withQualityGate(
                        newQualityGateBuilder()
                                .setId("id")
                                .setName("name")
                                .setStatus(QualityGate.Status.OK)
                                .add(newConditionBuilder()
                                        .setMetricKey(CoreMetrics.NEW_VULNERABILITIES_KEY)
                                        .setOperator(QualityGate.Operator.GREATER_THAN)
                                        .setErrorThreshold("0")
                                        .setOnLeakPeriod(true)
                                        .build(QualityGate.EvaluationStatus.OK, "0"))
                                .add(newConditionBuilder()
                                        .setMetricKey(CoreMetrics.NEW_BUGS_KEY)
                                        .setOperator(QualityGate.Operator.GREATER_THAN)
                                        .setErrorThreshold("0")
                                        .setOnLeakPeriod(true)
                                        .build(QualityGate.EvaluationStatus.ERROR, "1"))
                                .add(newConditionBuilder()
                                        .setMetricKey(CoreMetrics.NEW_SQALE_DEBT_RATIO_KEY)
                                        .setOperator(QualityGate.Operator.GREATER_THAN)
                                        .setWarningThreshold("2.0")
                                        .setErrorThreshold("10.0")
                                        .setOnLeakPeriod(true)
                                        .build(QualityGate.EvaluationStatus.OK, "0.00666667"))
                                .add(newConditionBuilder()
                                        .setMetricKey(CoreMetrics.NEW_COVERAGE_KEY)
                                        .setOperator(QualityGate.Operator.LESS_THAN)
                                        .setErrorThreshold("80.0")
                                        .setOnLeakPeriod(true)
                                        .build(QualityGate.EvaluationStatus.ERROR, "75.509999999999"))
                                .build())
                .execute();
    }

    public static void qualityGateError2Of3ConditionsFailed(final PostProjectAnalysisTask analysisTask) {
        PostProjectAnalysisTaskTester.of(analysisTask)
                .withCeTask(CE_TASK)
                .withProject(PROJECT)
                .at(new Date())
                .withQualityGate(
                        newQualityGateBuilder()
                                .setId("id")
                                .setName("name")
                                .setStatus(QualityGate.Status.ERROR)
                                .add(newConditionBuilder()
                                        .setMetricKey(CoreMetrics.BUGS_KEY)
                                        .setOperator(QualityGate.Operator.GREATER_THAN)
                                        .setErrorThreshold("2")
                                        .build(QualityGate.EvaluationStatus.OK, "0"))
                                .add(newConditionBuilder()
                                        .setMetricKey(CoreMetrics.FUNCTIONS_KEY)
                                        .setErrorThreshold("0")
                                        .setOperator(QualityGate.Operator.GREATER_THAN)
                                        .build(QualityGate.EvaluationStatus.WARN, "1"))
                                .add(newConditionBuilder()
                                        .setMetricKey(CoreMetrics.VIOLATIONS_KEY)
                                        .setErrorThreshold("5")
                                        .setOperator(QualityGate.Operator.GREATER_THAN)
                                        .build(QualityGate.EvaluationStatus.ERROR, "10"))
                                .build())
                .execute();
    }


    public static void noQualityGate(final PostProjectAnalysisTask analysisTask) {
        PostProjectAnalysisTaskTester.of(analysisTask)
                .withCeTask(CE_TASK)
                .withProject(PROJECT)
                .at(new Date())
                .execute();
    }

    public static void withBranch(final PostProjectAnalysisTask analysisTask, final Branch branch) {
        PostProjectAnalysisTaskTester.of(analysisTask)
            .withCeTask(CE_TASK)
            .withProject(PROJECT)
            .withBranch(branch)
            .at(new Date())
            .withScannerContext(newScannerContextBuilder().build())
            .execute();
    }
}
