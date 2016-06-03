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
        HttpEntity entity = new StringEntity(new SlackMessageBuilder().build(message), "UTF-8");
        post.addHeader("Content-Type", "application/json");
        post.setEntity(entity);
        HttpClient client = HttpClientBuilder.create().build();
        LOG.info("Pushing notifications to the Slack");

        try {
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() != 200) {
                LOG.warn("Failed to push to slack. Post body: '" + post.toString() + "'");
            }
        } catch (IOException e) {
            LOG.warn("Failed to push to slack.", e);
        }
    }
}
