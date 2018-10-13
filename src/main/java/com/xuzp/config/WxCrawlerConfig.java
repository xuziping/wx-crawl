package com.xuzp.config;

import com.xuzp.crawler.weixin.WxCrawler;
import com.xuzp.crawler.weixin.convert.ResourceTransfer;
import com.xuzp.service.IArticleService;
import com.xuzp.service.IRedisService;
import com.xuzp.service.IWxCrawlService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author za-xuzhiping
 * @Date 2018/7/30
 * @Time 18:21
 */
@Configuration
@Slf4j
@Getter
public class WxCrawlerConfig {

    @Value("${crawler.weixin.accounts}")
    private String accounts;

    @Value("${crawler.weixin.outputPath}")
    private String outputPath;

    @Value("${crawler.weixin.sleepTime}")
    private Long sleepTime;

    @Value("${crawler.weixin.resumable}")
    private Boolean resumable;

    @Value("${crawler.weixin.proxyPolicy}")
    private String proxyPolicy;

    @Value("${crawler.weixin.updateArticle}")
    private Boolean updateArticle;

    @Autowired
    private ResourceTransfer resourceTransfer;

    @Autowired
    private ProxyConfig proxyConfig;

    @Autowired
    private IWxCrawlService wxCrawlService;

    @Autowired
    private IArticleService articleService;

    @Autowired
    private IRedisService redisService;

    public WxCrawler wxCrawler(String proxy){
        try {
            String proxyPolicyValue = null;
            if (StringUtils.isNotEmpty(proxy)) {
                proxyPolicyValue = proxy;
            } else {
                proxyPolicyValue = proxyPolicy;
            }
            WxCrawler wxCrawler = new WxCrawler(outputPath, sleepTime, resumable, proxyPolicyValue, updateArticle,
                    resourceTransfer, articleService, redisService, proxyConfig, wxCrawlService);
            String[] accountArray = accounts.split(";");
            for(String account: accountArray) {
                wxCrawler.addAccount(account.trim());
            }
            wxCrawler.setThreads(1);
            wxCrawler.setResumable(false);
            return wxCrawler;
        } catch(Exception e) {
            log.info("Failed to create bean WxCrawler, exception={}", e);
        }
        return null;
    }
}
