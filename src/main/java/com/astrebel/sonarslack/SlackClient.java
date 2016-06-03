package com.astrebel.sonarslack;

import com.astrebel.sonarslack.message.SlackMessage;
import com.astrebel.sonarslack.message.SlackMessageBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.util.Collections;

@BatchSide
@ServerSide
public class SlackClient {
    private static final Logger LOG = Loggers.get(SlackClient.class);

    public void send(SlackMessage message) {

        String payload = new SlackMessageBuilder().build(message);
        HttpClient client = getHttpClient();
        LOG.debug("Sending message to slack: {}", payload);
        try {
            HttpPost post = new HttpPost(message.getWebHookUrl());
            HttpEntity entity = new UrlEncodedFormEntity(Collections.singletonList(new BasicNameValuePair("payload", payload)), "UTF-8");
            post.setEntity(entity);
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() != 200) {
                LOG.error("Slack message sending failed. WebHookUrl=[{}]. Payload=[{}], status=[{}],entity=[{}]", message.getWebHookUrl(), payload, res.getStatusLine(), EntityUtils.toString(res.getEntity(),"UTF-8"));
            }else{
                LOG.debug("Slack message sending succeeded");
            }
        } catch (IOException e) {
            LOG.error("Failed to push to slack.", e);
        }
    }

    protected HttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }
}
