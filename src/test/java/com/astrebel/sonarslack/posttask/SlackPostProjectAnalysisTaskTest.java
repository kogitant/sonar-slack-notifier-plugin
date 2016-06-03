package com.astrebel.sonarslack.posttask;

import com.astrebel.sonarslack.SlackClient;
import com.astrebel.sonarslack.SlackNotifierPlugin;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.ce.posttask.CeTask;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.Project;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.config.Settings;
import org.sonar.api.i18n.I18n;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;

/**
 * Created by 616286 on 3.6.2016.
 */
public class SlackPostProjectAnalysisTaskTest {

    SlackPostProjectAnalysisTask t;
    private HttpClient httpClient;
    private SlackClient slackClient;
    private Settings settings;
    private I18n i18n;
    HttpPost actualRequest;

    @Before
    public void before(){
        settings = new Settings();
        settings.setProperty(SlackNotifierPlugin.SLACK_HOOK, "hook");
        settings.setProperty(SlackNotifierPlugin.SLACK_CHANNEL, "channel");
        settings.setProperty(SlackNotifierPlugin.SLACK_SLACKUSER, "user");
        settings.setProperty("sonar.core.serverBaseURL","http://your.sonar.com/");
        i18n = Mockito.mock(I18n.class);
        Mockito.when(i18n.message(Matchers.any(Locale.class), anyString(), anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String)invocation.getArguments()[2];
            }
        });
        httpClient = Mockito.mock(HttpClient.class);
        try {
            Mockito.when(httpClient.execute(Matchers.any(HttpUriRequest.class))).then(new Answer<HttpResponse>() {
                @Override
                public HttpResponse answer(InvocationOnMock invocation) throws Throwable {
                    HttpResponse response = new BasicHttpResponse(new ProtocolVersion("http",1,1), 200, "OK");
                    actualRequest = (HttpPost)invocation.getArguments()[0];
                    return response;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        slackClient = new SlackClient(){
            @Override
            protected HttpClient getHttpClient() {
                return httpClient;
            }
        };
        t = new SlackPostProjectAnalysisTask(slackClient, settings, i18n);
    }

    @Test
    public void finished() throws Exception {
        PostProjectAnalysisTask.ProjectAnalysis analysis = new PostProjectAnalysisTask.ProjectAnalysis() {
            @Override
            public CeTask getCeTask() {
                return new CeTask() {
                    @Override
                    public String getId() {
                        return "id";
                    }

                    @Override
                    public Status getStatus() {
                        return Status.SUCCESS;
                    }
                };
            }

            @Override
            public Project getProject() {
                return new Project() {
                    @Override
                    public String getUuid() {
                        return "uuid";
                    }

                    @Override
                    public String getKey() {
                        return "com.koant.github:foobar";
                    }

                    @Override
                    public String getName() {
                        return "Foobar";
                    }
                };
            }

            @Override
            public QualityGate getQualityGate() {
                return new QualityGate() {
                    @Override
                    public String getId() {
                        return "qgid";
                    }

                    @Override
                    public String getName() {
                        return "qualityqate";
                    }

                    @Override
                    public Status getStatus() {
                        return Status.OK;
                    }

                    @Override
                    public Collection<Condition> getConditions() {
                        List<Condition> list = new ArrayList<>();
                        list.add(new Condition() {
                            @Override
                            public EvaluationStatus getStatus() {
                                return EvaluationStatus.OK;
                            }

                            @Override
                            public String getMetricKey() {
                                return "metric_key_1";
                            }

                            @Override
                            public Operator getOperator() {
                                return Operator.EQUALS;
                            }

                            @Override
                            public String getErrorThreshold() {
                                return "1";
                            }

                            @Override
                            public String getWarningThreshold() {
                                return "-1";
                            }

                            @Override
                            public boolean isOnLeakPeriod() {
                                return false;
                            }

                            @Override
                            public String getValue() {
                                return "0";
                            }
                        });
                        list.add(new Condition() {
                            @Override
                            public EvaluationStatus getStatus() {
                                return EvaluationStatus.ERROR;
                            }

                            @Override
                            public String getMetricKey() {
                                return "metric_key_2";
                            }

                            @Override
                            public Operator getOperator() {
                                return Operator.GREATER_THAN;
                            }

                            @Override
                            public String getErrorThreshold() {
                                return "100";
                            }

                            @Override
                            public String getWarningThreshold() {
                                return "50";
                            }

                            @Override
                            public boolean isOnLeakPeriod() {
                                return false;
                            }

                            @Override
                            public String getValue() {
                                return "101";
                            }
                        });
                        return list;
                    }
                };
            }

            @Override
            public Date getDate() {
                return new Date();
            }
        };
        t.finished(analysis);

        String actual = EntityUtils.toString(actualRequest.getEntity());
        String expected = "payload={\"channel\":\"channel\",\"username\":\"user\",\"text\":\"Project [Foobar] analyzed. See http://your.sonar.com/overview?id=com.koant.github:foobar. Quality gate status is OK\",\"attachments\":[{\"text\":\"- metric_key_1 OK Value [0], operator [EQUALS], warning threshold [-1], error threshold [1], on leak period [false]\\n- metric_key_2 ERROR Value [101], operator [GREATER_THAN], warning threshold [50], error threshold [100], on leak period [false]\\n\",\"fallback\": \"Reasons: metric_key_1 OK Value [0], operator [EQUALS], warning threshold [-1], error threshold [1], on leak period [false], metric_key_2 ERROR Value [101], operator [GREATER_THAN], warning threshold [50], error threshold [100], on leak period [false], \"}]}";
        assertEquals(expected, URLDecoder.decode(actual,"UTF-8"));

    }

}