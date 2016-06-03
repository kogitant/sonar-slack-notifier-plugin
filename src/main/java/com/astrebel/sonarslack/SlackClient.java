package com.astrebel.sonarslack;

import com.astrebel.sonarslack.message.SlackMessage;
import com.astrebel.sonarslack.message.SlackMessageBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.internal.apachecommons.io.IOUtils;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.util.Collections;

@BatchSide
@ServerSide
public class SlackClient {
    private static final Logger LOG = Loggers.get(SlackClient.class);

    public void send(String hook, SlackMessage message) {
        String payload = new SlackMessageBuilder().build(message);
        HttpClient client = HttpClientBuilder.create().build();
        LOG.info("Sending message to slack: {}", payload);
        try {
            HttpPost post = new HttpPost(hook);
            HttpEntity entity = new UrlEncodedFormEntity(Collections.singletonList(new BasicNameValuePair("payload", payload)), "UTF-8");
            post.setEntity(entity);
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() != 200) {
                LOG.warn("Slack message sending failed. Hook=[{}]. Payload=[{}], status=[{}],entity=[{}]", hook, payload, res.getStatusLine(), EntityUtils.toString(res.getEntity(),"UTF-8"));
            }else{
                LOG.info("Slack message sending succeeded");
            }
        } catch (IOException e) {
            LOG.warn("Failed to push to slack.", e);
        }
    }
}
