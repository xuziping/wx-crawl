package com.xuzp.crawler.weixin;

import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import okhttp3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * @author za-xuzhiping
 * @Date 2018/8/16
 * @Time 19:24
 */
public class AbuyunProxyRequester extends OkHttpRequester {

    String credential;

    public AbuyunProxyRequester(String proxyUser, String proxyPass) {
        credential = Credentials.basic(proxyUser, proxyPass);
        removeSuccessCode(301);
        removeSuccessCode(302);
    }

    @Override
    public OkHttpClient.Builder createOkHttpClientBuilder() {
        String proxyHost = "http-dyn.abuyun.com";
        int proxyPort = 9020;
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        return super.createOkHttpClientBuilder()
                .proxy(proxy)
                .proxyAuthenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        return response.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    }
                });
    }
}
