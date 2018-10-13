package com.xuzp.crawler.weixin;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamCrawler;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xuzp.common.ResultBase;
import com.xuzp.common.WxCrawlerConstant;
import com.xuzp.common.utils.FileUtils;
import com.xuzp.config.ProxyConfig;
import com.xuzp.crawler.weixin.convert.CrawlDatumConvert;
import com.xuzp.crawler.weixin.convert.ResourceTransfer;
import com.xuzp.crawler.weixin.obj.ArticleSummaryObj;
import com.xuzp.crawler.weixin.vo.ArticleTransferVO;
import com.xuzp.service.IArticleService;
import com.xuzp.service.IRedisService;
import com.xuzp.service.IWxCrawlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author za-xuzhiping
 * @Date 2018/7/30
 * @Time 16:13
 */
@Slf4j
public class WxCrawler extends RamCrawler {

    private ResourceTransfer resourceTransfer;

    private Long sleepTime;

    private String outputPath;

    private Boolean isResumable;

    private String proxyPolicy;

    private Boolean updateArticle;

    private ProxyConfig proxyConfig;

    private IWxCrawlService wxCrawlService;

    private IArticleService articleService;

    private IRedisService redisService;

    public WxCrawler(String outputPath, Long sleepTime, Boolean resumable, String proxyPolicy, Boolean updateArticle,
                     ResourceTransfer resourceTransfer, IArticleService articleService, IRedisService redisService,
                     ProxyConfig proxyConfig, IWxCrawlService wxCrawlService) throws Exception {
        super(false);
        this.outputPath = outputPath;
        this.sleepTime = sleepTime;
        this.isResumable = resumable;
        this.proxyPolicy = proxyPolicy;
        this.articleService = articleService;
        this.updateArticle = updateArticle;
        this.resourceTransfer = resourceTransfer;
        this.proxyConfig = proxyConfig;
        this.redisService = redisService;
        this.wxCrawlService = wxCrawlService;
    }

    @Override
    public void afterStop(){
        log.info("Finished Weixin Crawl job");
        super.afterStop();
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        try {
            Thread.sleep(sleepTime != null ? sleepTime.longValue() : 5000L);
        } catch (InterruptedException e) {
            log.info("Failed to sleep, e={}", e);
        }
        log.info("Visit {}", page.url());
        if (page.matchType(WxCrawlerConstant.CrawlDatumType.ACCOUNT_SEARCH)) {
            parseSogouSearchResult(page, next);
        } else if (page.matchType(WxCrawlerConstant.CrawlDatumType.ARTICLE_LIST)) {
            parseWxArticleList(page, next);
        } else if (page.matchType(WxCrawlerConstant.CrawlDatumType.ARTICLE_DETAIL)) {
            parseWxArticleDetail(page, next);
        }
    }

    /**
     * 解析搜狗的微信公众号搜索结果页
     * @param page
     * @param next
     */
    protected void parseSogouSearchResult(Page page, CrawlDatums next){
        String accountName = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME);
        int triedCount = page.metaAsInt(WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT);

        // 检查使用不同代理重试次数
        if (triedCount > WxCrawlerConstant.MAX_TRY_COUNT) {
            log.info("Tried so many times using different proxy but all failed" +
                    ", skip, accountName：{}", accountName);
            return;
        }
        log.info("Parsing sogou search result page，accountName: {}", accountName);
        Element accountLinkEle = page.select("p.tit>a").first();
        if (accountLinkEle == null) {
            processBlocked(page, next);
            return;
        }
        //防止公众号名错误
        String detectedAccount = accountLinkEle.text().trim();
        if (!accountName.equals(detectedAccount)) {
            log.info("accountName \"{}\" not matched \"{}\"", accountName, detectedAccount);
            return;
        }
        //解析出公众号搜索结果页面中的URL
        String accountUrl = accountLinkEle.attr("abs:href");
        Element wxAccountEl = page.select("p.info>label[name='em_weixinhao']").first();
        if (wxAccountEl == null || StringUtils.isEmpty(wxAccountEl.text())) {
            log.info("accountId \"{}\" not exist", accountName);
            return;
        }
        if(accountUrl.startsWith(WxCrawlerConstant.HTTP_PROTOCOL)) {
            accountUrl = accountUrl.replaceFirst(WxCrawlerConstant.HTTP_PROTOCOL, WxCrawlerConstant.HTTPS_PROTOCOL);
        }
        next.add(new CrawlDatum(accountUrl, WxCrawlerConstant.CrawlDatumType.ARTICLE_LIST)
                .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME, accountName)
                .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID, wxAccountEl.text())
                .meta(WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT, 0));
    }

    private void processBlocked(Page page, CrawlDatums next){
        log.info("Current proxy IP \"{}\" is blocked, use other proxy IP and try again...", getCurrentProxyInfo());
        next.add(reNewCrawlDatum(page.crawlDatum()));
    }

    private CrawlDatum reNewCrawlDatum(CrawlDatum old) {
        int triedCount = old.metaAsInt(WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT) + 1;
        int index = old.url().indexOf(WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT);
        String url = null;
        if (index != -1) {
            url = old.url().substring(0, index-1) + "&" + WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT + "=" + triedCount;
        } else {
            url = old.url() + "&" + WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT + "=" + triedCount;
        }
        CrawlDatum newObj = new CrawlDatum(url, old.type());
        newObj.meta(old.meta()).meta(WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT, triedCount);
        return newObj;
    }

    /**
     * 是否爬取过。只有在打开断点续爬时才做检查
     * @param key
     * @return
     */
    private boolean hasCrawled(String key){
        if (BooleanUtils.isFalse(isResumable)) {
            return false;
        }
        return redisService.exists(key);
    }

    private List<ArticleSummaryObj> parseArticleListByPage(Page page) throws Exception {
        int startIndex = page.html().indexOf(WxCrawlerConstant.ArticleList.ARTICLE_LIST_KEY) +
                WxCrawlerConstant.ArticleList.ARTICLE_LIST_KEY.length();
        int endIndex = page.html().indexOf(WxCrawlerConstant.ArticleList.ARTICLE_LIST_SUFFIX);
        String jsonStr = page.html().substring(startIndex, endIndex).trim();
        jsonStr = jsonStr.substring(0,jsonStr.length()-1);
        JSONObject json = JSONObject.parseObject(jsonStr);
        return JSONArray.parseArray(json.getString("list"), ArticleSummaryObj.class);
    }

    /**
     * 解析微信公众号主页文章列表
     * @param page
     * @param next
     */
    protected void parseWxArticleList(Page page, CrawlDatums next){
        String accountName = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME);
        log.info("Parsing weixin article list page，accountName:{}", accountName);
        String accountId = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID);
        int triedCount = page.metaAsInt(WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT);

        // Step 1: 检查使用不同代理重试次数
        if (triedCount > WxCrawlerConstant.MAX_TRY_COUNT) {
            log.info("Tried so many times using different proxy but all failed" +
                    ", skip, accountName：{}", accountName);
            return;
        }

        // Step 2: 获取文章列表
        List<ArticleSummaryObj> articles = null;
        try {
            articles = parseArticleListByPage(page);
        } catch(Exception e1) {
            log.info("Need to enter identifying code, {}", page.url());
            processBlocked(page, next);
            return ;
        }

        // Step 3: 解析文章详情，加入爬虫种子
        ResultBase<List<ArticleTransferVO>> articleTransferResult = wxCrawlService.parseArticleList(accountId, accountName, articles);
        if(articleTransferResult.isSuccess() && CollectionUtils.isNotEmpty(articleTransferResult.getValue())) {
            articleTransferResult.getValue().forEach(article -> {
                CrawlDatum crawlDatum = parseArticleSummary(article);
                if (crawlDatum != null) {
                    next.add(crawlDatum);
                }
            });
        }
    }

    private String getCurrentProxyInfo(){
        if (this.getRequester() != null) {
            WxProxyRequest requester = (WxProxyRequest) this.getRequester();
            Proxy currentProxy = requester.getCurrentProxy();
            if (currentProxy != null) {
                return currentProxy.toString();
            }
        }
        return "no proxy";
    }

    /**
     * 解析文章列表中的文章信息
     * @param articleTransferVO
     * @return
     */
    private CrawlDatum parseArticleSummary(ArticleTransferVO articleTransferVO){
        String key = articleTransferVO.getAccountId().trim() + "###" + articleTransferVO.getTitle().trim();
        if (hasCrawled(key)) {
            log.info("Article has crawled, skip, accountName：{}，article：{}", articleTransferVO.getAccountName(),
                    articleTransferVO.getTitle());
            return null;
        }
        String cover = articleTransferVO.getCover();
        ResultBase<String> newURL = resourceTransfer.getOssValue(cover);
        if (newURL.isSuccess()) {
            articleTransferVO.setOssCover(newURL.getValue());
        } else {
            log.info("Failed to CoverImage resourceTranslation, article: {}, cover: {}", articleTransferVO.getTitle(), cover);
        }
        return CrawlDatumConvert.convert2ArticleSummaryCrawlDatum(articleTransferVO);
    }

    /**
     * 解析微信公众号文章详情页
     * @param page
     */
    protected void parseWxArticleDetail (Page page, CrawlDatums next) {
        String accountName = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME);
        String accountId = page.meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID);
        String cover = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_COVER);
        String title = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_TITLE);
        String digest = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_DIGEST);
        String publishDate = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_PUBLISH_DATE);
        String author = page.meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_AUTHOR);
        int triedCount = page.metaAsInt(WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT);

        // 检查使用不同代理重试次数
        if (triedCount > WxCrawlerConstant.MAX_TRY_COUNT) {
            log.info("Tried so many times using different proxy but all failed" +
                    ", skip, accountName：{}，article：{}", accountName, title);
            return;
        }

        String key = accountId.trim() + "###" + title.trim();
        if (hasCrawled(key)) {
            log.info("This article has crawled, skip, accountName：{}，article：{}", accountName, title);
            return;
        }

        try {
            Document sourceDoc = Jsoup.parse(page.html());
            ResultBase<ArticleTransferVO> articleTransferVOResultBase = wxCrawlService.parseArticleDetail(sourceDoc);
            if(articleTransferVOResultBase.isSuccess()) {
                log.info("accountName: {}, accountId: {}, cover: {}, title: {}, author: {}, publishDate: {}, digest: {}",
                        accountName, accountId, cover, title, author, publishDate, digest);

                // 备份html文件
                if(StringUtils.isNotEmpty(outputPath)) {
                    Document targetDoc = articleTransferVOResultBase.getValue().getTargetDoc();
                    targetDoc.head().attr(WxCrawlerConstant.BackupArticle.AUTHOR, author)
                            .attr(WxCrawlerConstant.BackupArticle.COVER, cover)
                            .attr(WxCrawlerConstant.BackupArticle.DIGEST, digest)
                            .attr(WxCrawlerConstant.BackupArticle.ACCOUNT_ID, accountId)
                            .attr(WxCrawlerConstant.BackupArticle.ACCOUNT_NAME, accountName)
                            .attr(WxCrawlerConstant.BackupArticle.PUBLISH_DATE, publishDate);
                    org.apache.commons.io.FileUtils.writeStringToFile(new File(FileUtils.getOutputAccountPath(outputPath, accountName),
                                    FileUtils.normalize(accountName + "_" + title + ".html")),
                            FileUtils.replaceEmoji(targetDoc.outerHtml()), "UTF-8");
                }

                IArticleService.Operation operation = updateArticle ? IArticleService.Operation.SAVE :
                        IArticleService.Operation.ADD;
                ResultBase<String> result = articleService.save(articleTransferVOResultBase.getValue(), operation);
                log.info(result.getMessage());
                if (result.isSuccess()) {
                    setCrawlInfo(key, "");
                }
            } else if(StringUtils.isNotEmpty(articleTransferVOResultBase.getMessage())
                    && articleTransferVOResultBase.getMessage().contains("blocked ip")){
                processBlocked(page, next);
            } else {
                log.info("Failed to parse detail html, accountName：{}，article：{}", accountName, title);
            }
        } catch (Exception ex) {
            log.info("Failed to parseWxArticleDetail, exception={}", ex);
        }
    }

    private void setCrawlInfo(String key, String articleNo) {
        redisService.set(key, articleNo);
    }


    @Override
    public void start(int depth) throws Exception {
        if(WxCrawlerConstant.ProxyPolicy.ABUYUN.equalsIgnoreCase(proxyPolicy)) {
            setRequester(new AbuyunProxyRequester(proxyConfig.getAbuyunAccount(), proxyConfig.getAbuyunPassword()));
        } else {
            setRequester(new WxProxyRequest(proxyPolicy));
        }
        super.start(depth);
    }

    /**
     * 根据公众号名称设置种子URL
     * @param account
     * @throws UnsupportedEncodingException
     */
    public void addAccount(String account) throws UnsupportedEncodingException {
        String seedUrl = WxCrawlerConstant.SEARCH_URL + URLEncoder.encode(account, "utf-8");
        CrawlDatum seed = new CrawlDatum(seedUrl, WxCrawlerConstant.CrawlDatumType.ACCOUNT_SEARCH)
                .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME, account)
                .meta(WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT, 0);
        addSeed(seed);
    }
}