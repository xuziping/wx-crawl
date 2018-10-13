package com.xuzp.common.utils;

import com.xuzp.common.WxCrawlerConstant;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * 测试代理访问搜狗平台和微信文章地址
 *
 * @author za-xuzhiping
 * @Date 2018/8/13
 * @Time 16:27
 */
@Slf4j
public class HttpRequestUtils {

    public static Document sendGetSogou(String url, Proxy proxy) throws Exception {
        URL realURL = new URL(url);
        URLConnection conn = realURL.openConnection(proxy);
        conn.setConnectTimeout(WxCrawlerConstant.RequestInfo.REQUEST_TIMEOUT);
        conn.setReadTimeout(WxCrawlerConstant.RequestInfo.REQUEST_TIMEOUT);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Connection", WxCrawlerConstant.RequestInfo.CONNECTION);
        conn.setRequestProperty("User-Agent", WxCrawlerConstant.RequestInfo.USER_AGENT);
        conn.setRequestProperty("Accept", WxCrawlerConstant.RequestInfo.ACCEPT);
        conn.setRequestProperty("Accept-Language", WxCrawlerConstant.RequestInfo.ACCEPT_LANGUAGE);
        conn.setRequestProperty("Host", WxCrawlerConstant.RequestInfo.SOGOU_HOST);
        conn.setRequestProperty("Upgrade-Insecure-Requests", WxCrawlerConstant.RequestInfo.UPGRADE_INSECURE_REQUESTS);
        conn.connect();
        return Jsoup.parse(conn.getInputStream(), WxCrawlerConstant.RequestInfo.CHARSET_NAME, url);
    }

    public static Document sendGetWxArticle(String url, Proxy proxy) throws Exception {
        URL realURL = new URL(url);
        URLConnection conn = realURL.openConnection(proxy);
        conn.setConnectTimeout(WxCrawlerConstant.RequestInfo.REQUEST_TIMEOUT);
        conn.setReadTimeout(WxCrawlerConstant.RequestInfo.REQUEST_TIMEOUT);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Connection", WxCrawlerConstant.RequestInfo.CONNECTION);
        conn.setRequestProperty("User-Agent", WxCrawlerConstant.RequestInfo.USER_AGENT);
        conn.setRequestProperty("Accept", WxCrawlerConstant.RequestInfo.ACCEPT);
        conn.setRequestProperty("Referer", WxCrawlerConstant.RequestInfo.REFERER);
        conn.setRequestProperty("Cache-Control", WxCrawlerConstant.RequestInfo.CACHE_CONTROL);
        conn.setRequestProperty("Accept-Language", WxCrawlerConstant.RequestInfo.ACCEPT_LANGUAGE);
        conn.setRequestProperty("Host", WxCrawlerConstant.RequestInfo.WEIXIN_HOST);
        conn.setRequestProperty("Upgrade-Insecure-Requests", WxCrawlerConstant.RequestInfo.UPGRADE_INSECURE_REQUESTS);
        conn.connect();
        return Jsoup.parse(conn.getInputStream(), WxCrawlerConstant.RequestInfo.CHARSET_NAME, url);
    }
}