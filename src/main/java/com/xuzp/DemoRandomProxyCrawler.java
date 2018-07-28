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
            proxies.add("111.11.227.84", 8080);
        }

        @Override
        public OkHttpClient.Builder createOkHttpClientBuilder() {
            return super.createOkHttpClientBuilder()
                    // 设置一个代理选择器
                    .proxySelector(new ProxySelector() {
                        @Override
                        public List<Proxy> select(URI uri) {
                            // 随机选择1个代理
                            Proxy randomProxy = proxies.nextRandom();
                            // 返回值类型需要是List
                            List<Proxy> randomProxies = new ArrayList<Proxy>();
                            //如果随机到null，即不需要代理，返回空的List即可
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

        // 设置请求插件
        setRequester(new MyRequester());

        // 爬取github about下面的网页
        addSeed("https://github.com/about");
        addRegex("https://github.com/about/.*");

    }

    @Override
    public void visit(Page page, CrawlDatums crawlDatums) {
        System.out.println(page.doc().title());
    }

    public static void main(String[] args) throws Exception {
        DemoRandomProxyCrawler crawler = new DemoRandomProxyCrawler("crawl");
        crawler.start(2);
    }
}