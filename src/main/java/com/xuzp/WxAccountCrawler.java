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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class WxAccountCrawler extends BreadthCrawler {

    public WxAccountCrawler(String crawlPath) throws Exception {
        super(crawlPath, false);
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (page.matchType(WxCrawlerConstant.CrawlDatumType.ACCOUNT_SEARCH)) {
            parseSogouSearchResult(page, next);
        } else if (page.matchType(WxCrawlerConstant.CrawlDatumType.ARTICLE_LIST)) {
            parseWxArticleList(page, next);
        } else if (page.matchType(WxCrawlerConstant.CrawlDatumType.ARTICLE_DETAIL)) {
            parseWxArticleDetail(page);
        }
    }

    /**
     * 解析搜狗的微信公众号搜索结果页
     * @param page
     * @param next
     */
    protected void parseSogouSearchResult(Page page, CrawlDatums next){
        String accountName = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME);
        Element accountLinkEle = page.select("p.tit>a").first();
        if (accountLinkEle == null) {
            LOG.info("公众号\"{}\"不存在，请给出准确的公众号名", accountName);
            return;
        }
        //防止公众号名错误
        String detectedAccount = accountLinkEle.text().trim();
        if (!accountName.equals(detectedAccount)) {
            LOG.info("公众号\"{}\"与搜索结果\"{}\"名称不符，请给出准确的公众号名", accountName, detectedAccount);
            return;
        }
        //解析出公众号搜索结果页面中的URL
        String accountUrl = accountLinkEle.attr("abs:href");
        LOG.info("添加到待抓取URL队列中：{}，类型：{}", accountUrl, WxCrawlerConstant.CrawlDatumType.ARTICLE_LIST);
        Element wxAccountEl = page.select("p.info>label[name='em_weixinhao']").first();
        if (wxAccountEl == null) {
            LOG.info("公众号id\"{}\"不存在", accountName);
            return;
        }
        String accountId = wxAccountEl.text();
        next.add(new CrawlDatum(accountUrl, WxCrawlerConstant.CrawlDatumType.ARTICLE_LIST)
                .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME, accountName)
                .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID, accountId));
    }

    /**
     * 解析微信公众号主页文章列表
     * @param page
     * @param next
     */
    protected void parseWxArticleList(Page page, CrawlDatums next){
        String accountName = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME);
        String accountId = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID);
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
                String key = accountName + "_" + title;
                String cover = obj.getApp_msg_ext_info().getCover();
                String author = obj.getApp_msg_ext_info().getAuthor();
                String publishDate = DateTimeUtils.parseDate(obj.getComm_msg_info().getDatetime() + "000");
                byte[] im = ImageUtils.download(http, cover);
                IOUtils.write(im, new FileOutputStream(getCoverFolder(title)));
                String articleUrl = WxCrawlerConstant.ARTICLE_URL_PREFIX +obj.getApp_msg_ext_info().getContent_url().replaceAll("&amp;", "&");
                next.add(new CrawlDatum(articleUrl, WxCrawlerConstant.CrawlDatumType.ARTICLE_DETAIL).key(key)
                        .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME, accountName)
                        .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID, accountId)
                        .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_TITLE, title)
                        .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_PUBLISH_DATE, publishDate)
                        .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_AUTHOR, author));
            }
        } catch(Exception e) {
            LOG.error("Failed to parse jsonStr");
        }
    }

    protected File getArticleFolder(String articleName) throws IOException {
        File file = new File("D:/wx_crawl", articleName);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    protected String getCoverFolder(String articleName) throws IOException {
        File file = new File(getArticleFolder(articleName), "cover.jpg");
        if (!file.exists()) {
            file.createNewFile();
        }
        return file.getAbsolutePath();
    }

    protected String getContentFile(String articleName, String fileName) throws IOException {
        File folder = new File(getArticleFolder(articleName), "resources");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file.getAbsolutePath();
    }

    /**
     * 解析微信公众号文章详情页
     * @param page
     */
    protected void parseWxArticleDetail (Page page) {

        String accountName = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME);
        String accountId = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID);
        String title = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_TITLE);
        String publishDate = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_PUBLISH_DATE);
        String author = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_AUTHOR);

        try {
            String content = page.select(WxCrawlerConstant.HTMLElementSelector.RICH_MEDIA_CONTENT).first().html().trim();

            Document doc = Jsoup.parse(content);
            Elements imgElements = doc.select("img");
            if (CollectionUtils.isNotEmpty(imgElements)) {
                HttpClient http = HttpClientBuilder.create().build();
                for(Element imgElement: imgElements) {
                    String imgURL = imgElement.attr("data-src");
                    byte[] im = ImageUtils.download(http, imgURL);
                    String newPath = getContentFile(title, RandomStringUtils.randomNumeric(7) + ".jpg");
                    IOUtils.write(im, new FileOutputStream(newPath));
                    imgElement.attr("data-src", newPath);
                }
            }

            Elements mpvoiceElements = doc.select("mpvoice");
            if (CollectionUtils.isNotEmpty(mpvoiceElements)) {
                HttpClient http = HttpClientBuilder.create().build();
                for(Element voiceElement: mpvoiceElements) {
                    String voiceURL = WxCrawlerConstant.VOICE_URL + voiceElement.attr("voice_encode_fileid");
                    byte[] im = ImageUtils.download(http, voiceURL);
                    String newPath = getContentFile(title, RandomStringUtils.randomNumeric(7) + ".mp3");
                    IOUtils.write(im, new FileOutputStream(newPath));
                    voiceElement.append("<audio src=\"" + newPath + "\">您的浏览器不支持audio标签</audio>");
                }
            }

            content = doc.toString();

            LOG.info("accountName: {}, accountId: {}, title: {}, author: {}, publishDate: {}, content: {}",
                    accountName, accountId, title, author, publishDate, content);
        } catch (Exception ex) {
            LOG.info("writer exception", ex);
        }
    }

    @Override
    public void start(int depth) throws Exception {
        super.start(depth);
    }

    /**
     * 根据公众号名称设置种子URL
     * @param account
     * @throws UnsupportedEncodingException
     */
    public void addAccount(String account) throws UnsupportedEncodingException {
        String seedUrl = WxCrawlerConstant.SEARCH_URL + URLEncoder.encode(account, "utf-8");
        CrawlDatum seed = new CrawlDatum(seedUrl, WxCrawlerConstant.CrawlDatumType.ACCOUNT_SEARCH).meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME, account);
        addSeed(seed);
    }

    public static void main(String[] args) throws Exception {
        WxAccountCrawler crawler = new WxAccountCrawler("crawl_weixin");
        crawler.addAccount("雪球");
        crawler.setThreads(1);
        crawler.setResumable(false);
        crawler.start(10);
    }
}