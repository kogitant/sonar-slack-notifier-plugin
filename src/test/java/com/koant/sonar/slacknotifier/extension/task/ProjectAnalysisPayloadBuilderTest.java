package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.common.json.GsonFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.ce.posttask.CeTask;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.PostProjectAnalysisTaskTester;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.System2;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.core.i18n.DefaultI18n;
import org.sonar.core.platform.PluginRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sonar.api.ce.posttask.PostProjectAnalysisTaskTester.*;

/**
 * Created by ak on 18/10/16.
 */
public class ProjectAnalysisPayloadBuilderTest {
    private static final Logger LOG = Loggers.get(ProjectAnalysisPayloadBuilderTest.class);

    CaptorPostProjectAnalysisTask postProjectAnalysisTask = new CaptorPostProjectAnalysisTask();
    DefaultI18n i18n;

    @Before
    public void before(){
        // org/sonar/l10n/core.properties
        PluginRepository pluginRepository = Mockito.mock(PluginRepository.class);
        System2 system2 = Mockito.mock(System2.class);
        i18n = new DefaultI18n(pluginRepository, system2);
        i18n.start();
    }

    @Test
    public void testI18nBundle(){
        assertEquals("Debt Ratio on new code", i18n.message(Locale.ENGLISH, "metric.new_sqale_debt_ratio.short_name", null));
    }

    @Test
    public void execute_is_passed_a_non_null_ProjectAnalysis_object() {
        PostProjectAnalysisTaskTester.of(postProjectAnalysisTask)
            .withCeTask(
                newCeTaskBuilder()
                    .setId("id")
                    .setStatus(CeTask.Status.SUCCESS)
                    .build())
            .withProject(
                PostProjectAnalysisTaskTester.newProjectBuilder()
                    .setUuid("uuid")
                    .setKey("key")
                    .setName("name")
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
        assertNotNull(postProjectAnalysisTask.projectAnalysis);
    }

    @Test
    public void testPayloadBuilder(){
        PostProjectAnalysisTaskTester.of(postProjectAnalysisTask)
            .withCeTask(
                newCeTaskBuilder()
                    .setId("id")
                    .setStatus(CeTask.Status.SUCCESS)
                    .build())
            .withProject(
                PostProjectAnalysisTaskTester.newProjectBuilder()
                    .setUuid("uuid")
                    .setKey("project:key")
                    .setName("Project Name")
                    .build())
            .at(new Date())
            .withQualityGate(
                newQualityGateBuilder()
                    .setId("id")
                    .setName("name")
                    .setStatus(QualityGate.Status.OK)
                    .add(
                        newConditionBuilder()
                            .setMetricKey(CoreMetrics.NEW_VULNERABILITIES_KEY)
                            .setOperator(QualityGate.Operator.GREATER_THAN)
                            .setErrorThreshold("0")
                            .setOnLeakPeriod(true)
                            .build(QualityGate.EvaluationStatus.OK, "0"))
                    .add(
                        newConditionBuilder()
                            .setMetricKey(CoreMetrics.NEW_BUGS_KEY)
                            .setOperator(QualityGate.Operator.GREATER_THAN)
                            .setErrorThreshold("0")
                            .setOnLeakPeriod(true)
                            .build(QualityGate.EvaluationStatus.ERROR, "1"))
                    .add(
                        newConditionBuilder()
                            .setMetricKey(CoreMetrics.NEW_SQALE_DEBT_RATIO_KEY)
                            .setOperator(QualityGate.Operator.GREATER_THAN)
                            .setWarningThreshold("2.0")
                            .setErrorThreshold("10.0")
                            .setOnLeakPeriod(true)
                            .build(QualityGate.EvaluationStatus.OK, "0.0"))
                    .add(
                        newConditionBuilder()
                            .setMetricKey(CoreMetrics.NEW_COVERAGE_KEY)
                            .setOperator(QualityGate.Operator.LESS_THAN)
                            .setErrorThreshold("80.0")
                            .setOnLeakPeriod(true)
                            .build(QualityGate.EvaluationStatus.ERROR, "75.5"))
                    .build())
            .execute();

        Payload actual = new ProjectAnalysisPayloadBuilder(i18n, postProjectAnalysisTask.projectAnalysis)
            .projectUrl("http://localhist:9000/overview?id=project:key")
            .channel("#channel")
            .username("CKSSlackNotifier")
            .build();

        com.google.gson.Gson gson = GsonFactory.createSnakeCase();

        LOG.info("Actual: " + gson.toJson(actual));

        Payload expected = expected();
        assertEquals(expected, actual);

    }

    private Payload expected() {
        List<Attachment> attachments = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        // fields=[
        // Field(title=new_vulnerabilities: OK, value=Value [0], operator [GREATER_THAN], warning threshold [null], error threshold [0], on leak period [true], valueShortEnough=false),
        // Field(title=new_bugs: ERROR, value=Value [1], operator [GREATER_THAN], warning threshold [null], error threshold [0], on leak period [true], valueShortEnough=false),
        // Field(title=new_sqale_debt_ratio: OK, value=Value [0.0], operator [GREATER_THAN], warning threshold [null], error threshold [10.0], on leak period [true], valueShortEnough=false),
        // Field(title=new_coverage: ERROR, value=Value [75.5], operator [LESS_THAN], warning threshold [null], error threshold [80.0], on leak period [true], valueShortEnough=false)], imageUrl=null, thumbUrl=null, footer=null, footerIcon=null, ts=null, mrkdwnIn=null)])
        fields.add(Field.builder()
            .title("New Vulnerabilities: OK")
            .value("0, error if >0")
            .valueShortEnough(false)
            .build());
        fields.add(Field.builder()
            .title("New Bugs: ERROR")
            .value("1, error if >0")
            .valueShortEnough(false)
            .build());
        fields.add(Field.builder()
            .title("Technical Debt Ratio on New Code: OK")
            .value("0.0%, warning if >2.0%, error if >10.0%")
            .valueShortEnough(false)
            .build());
        fields.add(Field.builder()
            .title("Coverage on New Code: ERROR")
            .value("75.5%, error if <80.0%")
            .valueShortEnough(false)
            .build());

        attachments.add(Attachment.builder()
            .fields(fields)
            .build());
        Payload expected = Payload.builder()
            .text("Project [Project Name] analyzed. See http://localhist:9000/overview?id=project:key. Quality gate status is OK")
            .channel("#channel")
            .username("CKSSlackNotifier")
            .attachments(attachments)
            .build();
        return expected;
    }


    private class CaptorPostProjectAnalysisTask implements PostProjectAnalysisTask {
        private ProjectAnalysis projectAnalysis;

        @Override
        public void finished(ProjectAnalysis analysis) {
            this.projectAnalysis = analysis;
        }
    }

}
