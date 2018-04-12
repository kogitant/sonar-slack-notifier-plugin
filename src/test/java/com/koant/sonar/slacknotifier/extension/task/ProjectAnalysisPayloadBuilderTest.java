package com.koant.sonar.slacknotifier.extension.task;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;
import com.koant.sonar.slacknotifier.common.component.ProjectConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.utils.System2;
import org.sonar.core.i18n.DefaultI18n;
import org.sonar.core.platform.PluginRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ak on 18/10/16.
 * Modified by poznachowski
 */
public class ProjectAnalysisPayloadBuilderTest {
    private static final boolean QG_FAIL_ONLY = true;
    CaptorPostProjectAnalysisTask postProjectAnalysisTask;
    DefaultI18n i18n;
    
    Locale defaultLocale;

    @Before
    public void before() {
        postProjectAnalysisTask = new CaptorPostProjectAnalysisTask();

        // org/sonar/l10n/core.properties
        PluginRepository pluginRepository = Mockito.mock(PluginRepository.class);
        System2 system2 = Mockito.mock(System2.class);
        i18n = new DefaultI18n(pluginRepository, system2);
        i18n.start();

        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }
    
    @After
    public void after(){
        Locale.setDefault(defaultLocale);
        
    }

    @Test
    public void testI18nBundle() {
        assertThat(i18n.message(Locale.ENGLISH, "metric.new_sqale_debt_ratio.short_name", null)).isEqualTo("Debt Ratio on new code");
    }

    @Test
    public void execute_is_passed_a_non_null_ProjectAnalysis_object() {
        Analyses.simple(postProjectAnalysisTask);
        assertThat(postProjectAnalysisTask.getProjectAnalysis()).isNotNull();
    }

    @Test
    public void testPayloadBuilder() {
        Analyses.qualityGateOk4Conditions(postProjectAnalysisTask);
        ProjectConfig projectConfig = new ProjectConfig("key", "#channel", "here", false);
        Payload payload = ProjectAnalysisPayloadBuilder.of(postProjectAnalysisTask.getProjectAnalysis())
                .projectConfig(projectConfig)
                .i18n(i18n)
                .projectUrl("http://localhist:9000/dashboard?id=project:key")
                .username("CKSSlackNotifier")
                .build();
        assertThat(payload).isEqualTo(expected());
    }

    private Payload expected() {
        List<Attachment> attachments = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
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
                .value("0.01%, warning if >2.0%, error if >10.0%")
                .valueShortEnough(false)
                .build());
        fields.add(Field.builder()
                .title("Coverage on New Code: ERROR")
                .value("75.51%, error if <80.0%")
                .valueShortEnough(false)
                .build());

        attachments.add(Attachment.builder()
                .fields(fields)
                .color("good")
                .build());
        return Payload.builder()
                .text("<!here> Project [Project Name] analyzed. See "
                    + "http://localhist:9000/dashboard?id=project:key. Quality gate status: OK")
                .channel("#channel")
                .username("CKSSlackNotifier")
                .attachments(attachments)
                .build();
    }

    @Test
    public void shouldShowOnlyExceededConditionsIfProjectConfigReportOnlyOnFailedQualityGateWay() throws Exception {
        Analyses.qualityGateError2Of3ConditionsFailed(postProjectAnalysisTask);
        ProjectConfig projectConfig = new ProjectConfig("key", "#channel", "here", QG_FAIL_ONLY);
        Payload payload = ProjectAnalysisPayloadBuilder.of(postProjectAnalysisTask.getProjectAnalysis())
                .projectConfig(projectConfig)
                .i18n(i18n)
                .projectUrl("http://localhist:9000/dashboard?id=project:key")
                .username("CKSSlackNotifier")
                .build();

        assertThat(payload.getAttachments())
                .hasSize(1)
                .flatExtracting(Attachment::getFields)
                .hasSize(2)
                .extracting(Field::getTitle)
                .contains("Functions: WARN", "Issues: ERROR");
    }

    @Test
    public void buildPayloadWithoutQualityGateWay() throws Exception {
        Analyses.noQualityGate(postProjectAnalysisTask);
        ProjectConfig projectConfig = new ProjectConfig("key", "#channel", "here", false);
        Payload payload = ProjectAnalysisPayloadBuilder.of(postProjectAnalysisTask.getProjectAnalysis())
                .projectConfig(projectConfig)
                .i18n(i18n)
                .projectUrl("http://localhist:9000/dashboard?id=project:key")
                .username("CKSSlackNotifier")
                .build();

        assertThat(payload.getAttachments()).isNull();
        assertThat(payload.getText()).doesNotContain("Quality Gate status");
    }

    @Test
    public void buildPayloadWithoutNotify() throws Exception {
        Analyses.noQualityGate(postProjectAnalysisTask);
        ProjectConfig projectConfig = new ProjectConfig("key", "#channel", "", false);
        Payload payload = ProjectAnalysisPayloadBuilder.of(postProjectAnalysisTask.getProjectAnalysis())
            .projectConfig(projectConfig)
            .i18n(i18n)
            .projectUrl("http://localhist:9000/dashboard?id=project:key")
            .username("CKSSlackNotifier")
            .build();

        assertThat(payload.getAttachments()).isNull();
        assertThat(payload.getText()).doesNotContain("here");
    }
}
