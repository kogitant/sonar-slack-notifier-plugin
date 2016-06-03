package com.astrebel.sonarslack;

import com.astrebel.sonarslack.message.SlackMessage;
import com.astrebel.sonarslack.message.SlackMessageBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;

@BatchSide
@ServerSide
public class SlackClient {
    private static final Logger LOG = Loggers.get(SlackClient.class);

    public void send(String hook, SlackMessage message) {
        HttpPost post = new HttpPost(hook);
        String payload = new SlackMessageBuilder().build(message);
        HttpEntity entity = new StringEntity(payload, "UTF-8");
        post.addHeader("Content-Type", "application/json");
        post.setEntity(entity);
        HttpClient client = HttpClientBuilder.create().build();
        LOG.info("Sending message to slack: {}", payload);
        try {
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() != 200) {
                LOG.warn("Slack message sending failed. Hook=[{}]. Payload=[{}]", hook, payload);
            }else{
                LOG.info("Slack message sending succeeded");
            }
        } catch (IOException e) {
            LOG.warn("Failed to push to slack.", e);
        }
    }
}
