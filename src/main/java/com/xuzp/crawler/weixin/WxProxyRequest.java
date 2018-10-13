package com.xuzp.crawler.weixin;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.Proxys;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import com.xuzp.common.WxCrawlerConstant;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 处理微信爬虫请求响应，不支持多线程
 *
 * @author za-xuzhiping
 * @Date 2018/8/9
 * @Time 17:37
 */
@Slf4j
public class WxProxyRequest extends OkHttpRequester {

    private Proxys proxies;

    private Proxy currentProxy;

    private Proxys goodProxies;

    private Boolean isLastTry;


    public WxProxyRequest(String proxyPolicy){

        proxies = new Proxys();
        goodProxies = new Proxys();

        if(WxCrawlerConstant.ProxyPolicy.NONE.equalsIgnoreCase(proxyPolicy)) {
            // 不使用代理ip
        } else if (StringUtils.isNotEmpty(proxyPolicy)){
            // 使用配置文件指定代理ip
            try {
                String proxyArray[] = proxyPolicy.split(",");
                if (proxyArray != null && proxyArray.length > 0) {
                    for(String p: proxyArray) {
                        p = p.trim();
                        String[] value = p.split(":");
                        proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(value[0], Integer.parseInt(value[1]))));
                    }
                }
            }catch (Exception e) {
                log.info("Failed to parse useProlicy, value={}, exception={}", proxyPolicy, e);
            }
        }

        log.info("Loaded available proxies");
    }

    @Override
    public Page getResponse(CrawlDatum crawlDatum) throws Exception {
        Page page = super.getResponse(crawlDatum);
        Response response = page.obj();
        if (response != null) {
            if (currentProxy != null && Proxy.Type.DIRECT.equals(currentProxy.type())) {
                log.info("##### NOW it is DIRECT proxy #####");
                log.info("##### response: {}", response);
            }
            if (response.isRedirect()) {
                log.info("Resource is redirect, code: {}, location: {}", response.code(), response.header("location"));
                removeBadProxy(currentProxy);
            } else {
                List<Cookie> cookies = Cookie.parseAll(response.request().url(), response.headers());
                if (CollectionUtils.isNotEmpty(cookies)) {
                    client.cookieJar().saveFromResponse(response.request().url(), cookies);
                    addGoodProxy(currentProxy);
                }
            }
        }
        return page;
    }

    private String loadCookies(String url){
        HttpUrl httpUrl = HttpUrl.parse(url);
        StringBuilder cookieStr = new StringBuilder();
        List<Cookie> cookies = client.cookieJar().loadForRequest(httpUrl);
        for(Cookie cookie:cookies){
            cookieStr.append(cookie.name()).append("=").append(cookie.value()+";");
        }
        return cookieStr.toString();
    }

    @Override
    public Request.Builder createRequestBuilder(CrawlDatum crawlDatum) {
        setIsLastTry(crawlDatum);
        okhttp3.Request.Builder builder = super.createRequestBuilder(crawlDatum)
                .header("Accept", WxCrawlerConstant.RequestInfo.ACCEPT)
                .header("Accept-Language", WxCrawlerConstant.RequestInfo.ACCEPT_LANGUAGE)
                .header("Connection", WxCrawlerConstant.RequestInfo.CONNECTION)
                .header("Upgrade-Insecure-Requests", WxCrawlerConstant.RequestInfo.UPGRADE_INSECURE_REQUESTS)
                .header("Cookie", loadCookies(crawlDatum.url()))
                .header("User-Agent", WxCrawlerConstant.RequestInfo.USER_AGENT);
        if(crawlDatum.url().indexOf(WxCrawlerConstant.RequestInfo.SOGOU_HOST) != -1) {
            builder.header("Host", WxCrawlerConstant.RequestInfo.SOGOU_HOST);
        } else {
            builder.header("Host", WxCrawlerConstant.RequestInfo.WEIXIN_HOST)
                    .header("Cache-Control", WxCrawlerConstant.RequestInfo.CACHE_CONTROL)
//                    .header("User-Agent", WxCrawlerConstant.RequestInfo.WX_BROWSER_USER_AGENT)
                    .header("Referer", WxCrawlerConstant.RequestInfo.REFERER);
        }
        return builder;
    }

    private void setIsLastTry(CrawlDatum crawlDatum) {
        this.isLastTry = crawlDatum.metaAsInt(WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT) == WxCrawlerConstant.MAX_TRY_COUNT;
    }

    @Override
    public OkHttpClient.Builder createOkHttpClientBuilder() {
        return super.createOkHttpClientBuilder()
                .connectTimeout(WxCrawlerConstant.RequestInfo.REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(WxCrawlerConstant.RequestInfo.REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .proxySelector(new ProxySelector (){

                    @Override
                    public List<Proxy> select(URI uri) {
                        List<Proxy> randomProxies = new ArrayList<Proxy>();

                        // 当达到最大尝试次数，尝试不用代理试最后一次
                        if (BooleanUtils.isTrue(isLastTry) || CollectionUtils.isEmpty(proxies)) {
                            randomProxies.add(Proxy.NO_PROXY);
                            currentProxy = Proxy.NO_PROXY;
                            return randomProxies;
                        }

                        Proxy randomProxy = CollectionUtils.isNotEmpty(goodProxies) && RandomUtils.nextInt(0,10)>7 ? goodProxies.nextRandom() : null;
                        if (randomProxy == null) {
                            randomProxy = proxies.nextRandom();
                        }
                        if(randomProxy != null) {
                            randomProxies.add(randomProxy);
                            log.info("{} is using proxy: {}", uri.toString(), randomProxy.toString());
                        } else {
                            log.info("Get proxy nextRandom failed");
                            log.info("Not use proxy!");
                            randomProxies.add(Proxy.NO_PROXY);
                        }
                        currentProxy = randomProxies.get(0);
                        return randomProxies;
                    }

                    @Override
                    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                        log.info("Proxy connect failed, uri: {}, proxy: {}", uri.toString(),
                                currentProxy.toString());
                        removeBadProxy(currentProxy);
                        if (currentProxy != null && proxies.contains(currentProxy)) {
                            proxies.remove(currentProxy);
                        }
                    }
                });
    }

    public Proxy getCurrentProxy(){
        return currentProxy;
    }

    public void addGoodProxy(Proxy proxy){
        if (proxy != null && !goodProxies.contains(proxy)) {
            goodProxies.add(proxy);
        }
    }

    public void removeBadProxy(Proxy proxy) {
        if (proxy != null && goodProxies.contains(proxy)) {
            goodProxies.remove(proxy);
        }
    }
}
