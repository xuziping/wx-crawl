package com.xuzp;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.Proxys;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 教程：WebCollector随机代理
 *
 * @author hu
 */
public class DemoRandomProxyCrawler extends BreadthCrawler {

    // 自定义的请求插件
    // 可以设置随机代理选择器
    public static class MyRequester extends OkHttpRequester {

        Proxys proxies;

        public MyRequester() {
            proxies = new Proxys();
            // add a socks proxy
            proxies.add("116.62.194.248", 3128);
            proxies.add("113.200.56.13", 8010);
            proxies.add("218.60.8.99", 3129);
//            proxies.add("58.253.108.214", 80);
//            proxies.add("220.249.185.178", 9797);
        }

        @Override
        public OkHttpClient.Builder createOkHttpClientBuilder() {
            return super.createOkHttpClientBuilder()
                    .proxySelector(new ProxySelector() {
                        @Override
                        public List<Proxy> select(URI uri) {
                            Proxy randomProxy = proxies.nextRandom();
                            List<Proxy> randomProxies = new ArrayList<Proxy>();
                            if(randomProxy != null) {
                                randomProxies.add(randomProxy);
                            }
                            System.out.println("Random Proxies:" + randomProxies);
                            return randomProxies;
                        }

                        @Override
                        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

                        }
                    });
        }
    }

    public DemoRandomProxyCrawler(String crawlPath) {
        super(crawlPath, true);
        setRequester(new MyRequester());

        addSeed("https://www.baidu.com/");
        addRegex("https://www.baidu.com/.*");

    }

    @Override
    public void visit(Page page, CrawlDatums crawlDatums) {
        System.out.println(page.doc().title());
    }

    public static void main(String[] args) throws Exception {
        DemoRandomProxyCrawler crawler = new DemoRandomProxyCrawler("crawl");
        crawler.start(5);
    }
}