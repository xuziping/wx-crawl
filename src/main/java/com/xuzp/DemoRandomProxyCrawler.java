package com.xuzp;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.Proxys;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
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
 */
public class DemoRandomProxyCrawler extends BreadthCrawler {

    public static class MyRequester extends OkHttpRequester {

//        static Proxys proxies = new Proxys();
//
//        static {
//            proxies.add("116.62.194.248", 3128);
//            proxies.add("113.200.56.13", 8010);
//            proxies.add("218.60.8.99", 3129);
//        }

        @Override
        public OkHttpClient.Builder createOkHttpClientBuilder() {
            return super.createOkHttpClientBuilder()
                    .proxySelector(new ProxySelector() {
                        @Override
                        public List<Proxy> select(URI uri) {
                            Proxys proxies = new Proxys();
                            proxies.add("116.62.194.248", 3128);
                            proxies.add("113.200.56.13", 8010);
                            proxies.add("218.60.8.99", 3129);
                            Proxy randomProxy = proxies.nextRandom();
                            List<Proxy> randomProxies = new ArrayList<Proxy>();
                            if(randomProxy != null) {
                                randomProxies.add(randomProxy);
                            } else {
                                randomProxies.add(Proxy.NO_PROXY);
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
        addSeed("http://pv.sohu.com/cityjson");
        addSeed("http://pv.sohu.com/cityjson?ie=utf-8");
//        addRegex("http://pv.sohu.com/cityjson/.*");
    }

    @Override
    public void visit(Page page, CrawlDatums crawlDatums) {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info(page.html());
    }

    public static void main(String[] args) throws Exception {
        DemoRandomProxyCrawler crawler = new DemoRandomProxyCrawler("proxy_crawl");
        for(int i=0; i<10; i++) {
            LOG.info("#############################################");
            crawler.setRequester(new MyRequester());
            crawler.start(20);
        }
    }
}