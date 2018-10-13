package com.xuzp.schedule;

import cn.edu.hfut.dmic.webcollector.crawler.Crawler;
import com.xuzp.common.WxCrawlerConstant;
import com.xuzp.common.utils.LazyLoader;
import com.xuzp.config.WxCrawlerConfig;
import com.xuzp.crawler.weixin.WxCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author za-xuzhiping
 * @Date 2018/7/31
 * @Time 11:19
 */
@Service
@Slf4j
public class CrawlScheduledTasks {

    private final static String WX_CRAWLER = "CRAWLER:WECHAT";

    @Autowired
    private WxCrawlerConfig wxCrawlerConfig;

    /**
     * 定时爬虫去抓取微信公众号文章
     */
    @Scheduled(cron = "0 30 10,18 * * ?")
    public void weixinCrawlTask() {
        crawlerStater(new LazyLoader<Crawler>(){
            @Override
            public WxCrawler newInstance() {
                return wxCrawlerConfig.wxCrawler(null);
            }
         }, WxCrawlerConstant.CRAWL_DEPTH, WX_CRAWLER, "公众号爬虫");
    }

    public void crawlerStater(LazyLoader<Crawler> crawler, int depth, String lockKey, String desc) {
        log.info("{}准备启动", desc);
        try {
            log.info("{}开始执行", desc);
            crawler.newInstance().start(depth);
        } catch (Exception e) {
            log.info("启动{}失败,异常{}", desc, e);
        }
    }
}
