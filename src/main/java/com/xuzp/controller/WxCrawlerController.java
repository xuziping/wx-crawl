package com.xuzp.controller;

import cn.edu.hfut.dmic.webcollector.crawler.Crawler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.xuzp.common.ResultBase;
import com.xuzp.common.WxCrawlerConstant;
import com.xuzp.common.utils.LazyLoader;
import com.xuzp.config.WxCrawlerConfig;
import com.xuzp.crawler.weixin.WxCrawler;
import com.xuzp.crawler.weixin.obj.ArticleSummaryObj;
import com.xuzp.crawler.weixin.vo.ArticleTransferVO;
import com.xuzp.schedule.CrawlScheduledTasks;
import com.xuzp.service.IArticleService;
import com.xuzp.service.IWxCrawlService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@Api(value = "微信公众号文章爬虫API",tags={"微信公众号文章爬虫API"})
public class WxCrawlerController {

    @Autowired
    private WxCrawlerConfig wxCrawlerConfig;

    @Autowired
    private IWxCrawlService wxCrawlService;

    @Autowired
    private IArticleService articleService;

    @Autowired
    private CrawlScheduledTasks crawlScheduledTasks;

    @ApiOperation("触发爬虫任务")
    @RequestMapping(value = "/wxCrawler",method = RequestMethod.GET)
    public ResultBase<Void> wxCrawler(@ApiParam(value="代理策略",defaultValue = "none") @RequestParam("proxyPolicy")String proxyPolicy){
        crawlScheduledTasks.crawlerStater(new LazyLoader<Crawler>(){
            @Override
            public WxCrawler newInstance() {
                return wxCrawlerConfig.wxCrawler(proxyPolicy);
            }
        }, WxCrawlerConstant.CRAWL_DEPTH, "CRAWLER:WECHAT", "公众号爬虫");
        return ResultBase.success();
    }

    @ApiOperation(value = "解析公众号文章列表", hidden = true)
    @RequestMapping(value = "/parseWxArticleList",method = RequestMethod.POST)
    public ResultBase<Map<String, String>> parseWxArticleList(@RequestBody JSONObject json){
        String accountId = json.getString("accountId");
        String accountName = json.getString("accountName");
        JSONArray list = json.getJSONArray("list");

        if (StringUtils.isEmpty(accountId) || StringUtils.isEmpty(accountName) || CollectionUtils.isEmpty(list)) {
            return ResultBase.fail("Invalid Param");
        }
        try {
            List<ArticleSummaryObj> articleList = JSON.parseArray(JSON.toJSONString(list), ArticleSummaryObj.class);
            ResultBase<List<ArticleTransferVO>> articleTransferResult = wxCrawlService.parseArticleList(accountId,
                    accountName, articleList);
            List<String> result = Lists.newArrayList();
            if(articleTransferResult.isSuccess() && CollectionUtils.isNotEmpty(articleTransferResult.getValue())) {
                articleTransferResult.getValue().forEach(article -> {
                    ResultBase<String> resultBase = articleService.save(article,IArticleService.Operation.ADD);
                    result.add(resultBase.getMessage());
                });
            }
            return ResultBase.success(result);
        } catch (Exception e) {
            log.info("Failed to testParseWxArticleList", e);
        }
        return ResultBase.fail("Failed to testParseWxArticleList");
    }

    @ApiOperation(value = "解析指定公众号文章详情", hidden = true)
    @RequestMapping(value = "/parseWxArticleDetail",method = RequestMethod.POST)
    public ResultBase<List<String>> parseWxArticleDetail(@RequestBody JSONObject json){
        String accountId = json.getString("accountId");
        JSONArray articleURL = json.getJSONArray("articleURL");
        if (StringUtils.isEmpty(accountId) || CollectionUtils.isEmpty(articleURL)) {
            return ResultBase.fail("Invalid Param");
        }
        List<String> result = Lists.newArrayList();
        for(int i = 0; i < articleURL.size(); i++) {
            ResultBase<String> resultBase = wxCrawlService.parseArticleDetail(accountId, articleURL.getString(i));
            result.add(resultBase.getMessage());
        }
        return ResultBase.success(result);
    }

    @ApiOperation("导入备份文章")
    @RequestMapping(value = "/importWxArticles",method = RequestMethod.GET)
    public ResultBase<List<String>> importWxArticles(@ApiParam("备份文章路径") @RequestParam("path")String path){
        return wxCrawlService.importWxArticles(path);
    }

    @ApiOperation("帮助文档")
    @RequestMapping(value = {"/help"},method = RequestMethod.GET)
    public ResultBase<Void> help(){
        return ResultBase.success(new HashMap(){
            {
                put("/healthCheck", "[GET] 健康检查");
                put("/wxCrawler?proxyPolicy=[none|auto|222.182.56.50:8118,124.235.208.252:443|abuyun]", "[GET] 微信文章爬虫");
                put("/parseWxArticleList", "[POST] 手动解析微信文章列表");
                put("/parseWxArticleDetail", "[POST] 手动解析微信文章详情页");
                put("/importWxArticles?path=", "[GET] 手动导入公众号文章");
            }
        });
    }
}
