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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SlackHttpClient {
    public static final String CONTENT_TYPE = "content-type";
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final Logger LOG = LoggerFactory.getLogger(SlackHttpClient.class);
    private final OkHttpClient httpClient;
    private final Configuration settings;

    /**
     * Initializes the Slack HTTP Client.
     *
     * @param settings
     */
    public SlackHttpClient(final Configuration settings) {
        this.settings = settings;
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS);
        this.declareProxyIfNecessary(builder);
        httpClient = builder.build();


    }

    private void declareProxyIfNecessary(final OkHttpClient.Builder builder) {
        final Proxy httpProxy = this.getHttpProxy();
        if (httpProxy == null) {
            builder.proxy(httpProxy);
        }
    }

    private Proxy getHttpProxy() {
        final Proxy.Type proxyProtocol = this.getProxyProtocol();
        if (proxyProtocol == null) return null;

        final String proxyIP = this.getProxyIP();
        if (Strings.isNullOrEmpty(proxyIP)) {
            return null;
        }

        return new Proxy(
            proxyProtocol,
            new InetSocketAddress(
                proxyIP,
                this.getProxyPort()
            )
        );
    }

    private Proxy.Type getProxyProtocol() {

        final Optional<String> proxyProtocol = this.settings.get(SlackNotifierProp.PROXY_PROTOCOL.property());
        return proxyProtocol.map(Proxy.Type::valueOf).orElseThrow(() -> new IllegalStateException("Proxy type property not found"));

    }

    private String getProxyIP() {

        final Optional<String> proxyIp = this.settings.get(SlackNotifierProp.PROXY_IP.property());
        return proxyIp.orElse(null);
    }

    private int getProxyPort() {
        final Optional<Integer> proxyPort = this.settings.getInt(SlackNotifierProp.PROXY_PORT.property());
        return proxyPort.orElseThrow(() -> new IllegalStateException("Proxy port property not found"));
    }

    boolean invokeSlackIncomingWebhook(final String projectCustomHook, final Payload payload) throws IOException {

        final Gson gson = GsonFactory.createSnakeCase();
        final String payloadJson = gson.toJson(payload);

        final String slackIncomingWebhookUrl = StringUtils.isEmpty(projectCustomHook) ? this.getSlackIncomingWebhookUrl() :
            projectCustomHook;
        final Request request = this.buildRequest(payloadJson, slackIncomingWebhookUrl);
        LOG.info("Request configuration: [uri={}, payload={}", request.url(), payloadJson);

        try (final Response response = this.httpClient.newCall(request).execute()) {
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

    @NotNull
    private Request buildRequest(final String payloadJson, final String slackIncomingWebhookUrl) {
        final Request.Builder requestBuilder = new Request.Builder().url(slackIncomingWebhookUrl);
        requestBuilder.addHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
        requestBuilder.post(RequestBody.create(payloadJson, MediaType.parse(APPLICATION_X_WWW_FORM_URLENCODED)));
        return requestBuilder.build();
    }

    protected String getSlackIncomingWebhookUrl() {

        final Optional<String> hook = this.settings.get(SlackNotifierProp.HOOK.property());
        return hook.orElseThrow(() -> new IllegalStateException("Hook property not found"));
    }
}
