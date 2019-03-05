/*
 * FAVEEO SA
 * __________________
 *
 *  [2016] - [2019] Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Faveeo SA and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Faveeo SA
 * and its suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Faveeo SA.
 */
package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;
import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SlackHttpClient {
    private static final Logger LOG = LoggerFactory.getLogger(SlackHttpClient.class);
    private final OkHttpClient httpClient;
    private Configuration settings;

    public SlackHttpClient(final Configuration settings) {
        this.settings = settings;
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS);
        final Proxy httpProxy = getHttpProxy();
        if (httpProxy == null) {
            builder.proxy(httpProxy);
        }
        this.httpClient = builder.proxy(getHttpProxy())
            .build();


    }

    private Proxy getHttpProxy() {
        final Proxy.Type proxyProtocol = getProxyProtocol();
        if (proxyProtocol == null) return null;

        if (Strings.isNullOrEmpty(getProxyIP())) {
            return null;
        }

        return new Proxy(
            proxyProtocol,
            new InetSocketAddress(
                getProxyIP(),
                getProxyPort()
            )
        );
    }

    private Proxy.Type getProxyProtocol() {

        final Optional<String> proxyProtocol = settings.get(SlackNotifierProp.PROXY_PROTOCOL.property());
        return proxyProtocol.map(Proxy.Type::valueOf).orElseThrow(() -> new IllegalStateException("Proxy type property not found"));

    }

    private String getProxyIP() {

        final Optional<String> proxyIp = settings.get(SlackNotifierProp.PROXY_IP.property());
        return proxyIp.orElse(null);
    }

    private int getProxyPort() {
        final Optional<Integer> proxyPort = settings.getInt(SlackNotifierProp.PROXY_PORT.property());
        return proxyPort.orElseThrow(() -> new IllegalStateException("Proxy port property not found"));
    }

    boolean invokeSlackIncomingWebhook(final String projectCustomHook, final Payload payload) throws IOException {

        Gson gson = GsonFactory.createSnakeCase();
        String payloadJson = gson.toJson(payload);

        String slackIncomingWebhookUrl = StringUtils.isEmpty(projectCustomHook) ? getSlackIncomingWebhookUrl() :
            projectCustomHook;
        Request.Builder requestBuilder = new Request.Builder().url(slackIncomingWebhookUrl);
        requestBuilder.addHeader("content-type", "application/x-www-form-urlencoded");
        requestBuilder.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), payloadJson));
        final Request request = requestBuilder.build();
        LOG.info("Request configuration: [uri={}, payload={}", request.url(), payloadJson);

        try (Response response = httpClient.newCall(request).execute()) {
            LOG.info("Slack HTTP response status: {}", response.code());
            if (!response.isSuccessful()) {
                throw new IllegalArgumentException("The Slack response has failed");
            }
            final ResponseBody body = response.body();
            if (body != null) {
                LOG.info("Slack HTTP response body: {}", body.string());
                return true;
            } else {
                LOG.error("Slack HTTP response body no body ");
            }
        }
        return false;
    }

    protected String getSlackIncomingWebhookUrl() {

        Optional<String> hook = settings.get(SlackNotifierProp.HOOK.property());
        return hook.orElseThrow(() -> new IllegalStateException("Hook property not found"));
    }
}
