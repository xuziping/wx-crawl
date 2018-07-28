package com.xuzp;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xuzp.common.WxCrawlerConstant;
import com.xuzp.common.utils.DateTimeUtils;
import com.xuzp.common.utils.ImageUtils;
import com.xuzp.wxobj.ArticleSummaryObj;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.nodes.Element;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class WxAccountCrawler extends BreadthCrawler {

    protected BufferedWriter historyKeysWriter;

    public WxAccountCrawler(String crawlPath) throws Exception {
        //自动解析为false，也就是手动解析探索新的URL
        super(crawlPath, false);
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        String account = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (page.matchType(WxCrawlerConstant.CrawlDatumType.ACCOUNT_SEARCH)) {
            //对于账号搜索页面，手动解析，抽取公众号文章列表页URL
            Element accountLinkEle = page.select("p.tit>a").first();
            if (accountLinkEle == null) {
                LOG.info("公众号\"{}\"不存在，请给出准确的公众号名", account);
                return;
            }
            //防止公众号名错误
            String detectedAccount = accountLinkEle.text().trim();
            if (!account.equals(detectedAccount)) {
                LOG.info("公众号\"{}\"与搜索结果\"{}\"名称不符，请给出准确的公众号名", account, detectedAccount);
                return;
            }
            //解析出公众号搜索结果页面中的URL
            String accountUrl = accountLinkEle.attr("abs:href");
            LOG.info("添加到待抓取URL队列中：{}，类型：{}", accountUrl, WxCrawlerConstant.CrawlDatumType.ARTICLE_LIST);
            next.add(new CrawlDatum(accountUrl, WxCrawlerConstant.CrawlDatumType.ARTICLE_LIST).meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT, account));

        } else if (page.matchType(WxCrawlerConstant.CrawlDatumType.ARTICLE_LIST)) {
            int startIndex = page.html().indexOf(WxCrawlerConstant.ArticleList.ARTICLE_LIST_KEY) +
                    WxCrawlerConstant.ArticleList.ARTICLE_LIST_KEY.length();
            int endIndex = page.html().indexOf(WxCrawlerConstant.ArticleList.ARTICLE_LIST_SUFFIX);
            String jsonStr = page.html().substring(startIndex, endIndex).trim();
            jsonStr = jsonStr.substring(0,jsonStr.length()-1);

            try {
                JSONObject json = JSONObject.parseObject(jsonStr);
                List<ArticleSummaryObj> articles = JSONArray.parseArray(json.getString("list"), ArticleSummaryObj.class);
                HttpClient http = HttpClientBuilder.create().build();
                for (ArticleSummaryObj obj: articles) {
                    LOG.info("obj:{}", obj);
                    String title = obj.getApp_msg_ext_info().getTitle();
                    String key = account + "_" + title;
                    String cover = obj.getApp_msg_ext_info().getCover();
                    String author = obj.getApp_msg_ext_info().getAuthor();
                    String publishDate = DateTimeUtils.parseDate(obj.getComm_msg_info().getDatetime());
                    byte[] im = ImageUtils.download(http, cover);
                    IOUtils.write(im, new FileOutputStream("D:/covers/" + obj.getApp_msg_ext_info().getTitle() + ".jpg"));
                    String articleUrl = WxCrawlerConstant.ARTICLE_URL_PREFIX +obj.getApp_msg_ext_info().getContent_url().replaceAll("&amp;", "&");
                    next.add(new CrawlDatum(articleUrl, WxCrawlerConstant.CrawlDatumType.ARTICLE_DETAIL).key(key).meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT, account));
                }
            } catch(Exception e) {
                LOG.error("Failed to parse jsonStr");
            }
        } else if (page.matchType(WxCrawlerConstant.CrawlDatumType.ARTICLE_DETAIL)) {
            try {
                String title = page.select(WxCrawlerConstant.HTMLElementSelector.RICH_MEDIA_TITLE).first().text().trim();
                String date = page.select(WxCrawlerConstant.HTMLElementSelector.PUBLISH_TIME).first().text().trim();
                String content = page.select(WxCrawlerConstant.HTMLElementSelector.RICH_MEDIA_CONTENT).first().html().trim();
                LOG.info("OK title: {}, date: {}, content: {}", title, date, content);
            } catch (Exception ex) {
                LOG.info("writer exception", ex);
            }
        }
    }

    @Override
    public void start(int depth) throws Exception {
        super.start(depth);
        LOG.info("save history keys");
    }

    public void addAccount(String account) throws UnsupportedEncodingException {
        //根据公众号名称设置种子URL
        String seedUrl = WxCrawlerConstant.SEARCH_URL + URLEncoder.encode(account, "utf-8");
        CrawlDatum seed = new CrawlDatum(seedUrl, WxCrawlerConstant.CrawlDatumType.ACCOUNT_SEARCH).meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT, account);
        addSeed(seed);
    }

    public static void main(String[] args) throws Exception {
        WxAccountCrawler crawler = new WxAccountCrawler("crawl_weixin");
        crawler.addAccount("横琴人寿");
        crawler.setThreads(1);
        crawler.setResumable(false);
        crawler.start(10);
    }
}