package com.xuzp.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.xuzp.common.ResultBase;
import com.xuzp.common.WxCrawlerConstant;
import com.xuzp.common.utils.DateTimeUtils;
import com.xuzp.common.utils.FileUtils;
import com.xuzp.common.utils.SampleHtmlLoader;
import com.xuzp.config.WxCrawlerConfig;
import com.xuzp.crawler.weixin.convert.ArticleConvert;
import com.xuzp.crawler.weixin.convert.ResourceTransfer;
import com.xuzp.crawler.weixin.obj.AppMsgExtInfoObj;
import com.xuzp.crawler.weixin.obj.ArticleSummaryObj;
import com.xuzp.crawler.weixin.obj.CommMsgInfoObj;
import com.xuzp.crawler.weixin.vo.ArticleTransferVO;
import com.xuzp.service.IArticleService;
import com.xuzp.service.IWxCrawlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * @author za-xuzhiping
 * @Date 2018/8/21
 * @Time 16:12
 */
@Service
@Slf4j
public class WxCrawlServiceImpl implements IWxCrawlService {

    @Autowired
    private ResourceTransfer resourceTransfer;

    @Autowired
    private IArticleService articleService;

    @Autowired
    private WxCrawlerConfig wxCrawlerConfig;

    @Override
    public ResultBase<List<ArticleTransferVO>> parseArticleList(String accountId, String accountName,
                                                                List<ArticleSummaryObj> articles) {
        List<ArticleTransferVO> articleTransferVOS = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(articles)) {
            try {
                for (ArticleSummaryObj obj : articles) {
                    log.info("parseArticleSummary:{}", obj);
                    ArticleTransferVO articleTransferVO = parseArticleSummary(accountId, accountName,
                            obj.getApp_msg_ext_info(), obj.getComm_msg_info());
                    if (articleTransferVO != null) {
                        articleTransferVOS.add(articleTransferVO);
                    }

                    // 处理多条图文信息
                    if (WxCrawlerConstant.YES.equals(obj.getApp_msg_ext_info().getIs_multi())
                            && StringUtils.isNotEmpty(obj.getApp_msg_ext_info().getMulti_app_msg_item_list())) {
                        List<AppMsgExtInfoObj> subArticles = JSONArray.parseArray(obj.getApp_msg_ext_info()
                                .getMulti_app_msg_item_list(), AppMsgExtInfoObj.class);
                        for (AppMsgExtInfoObj subArticle : subArticles) {
                            ArticleTransferVO subArticleTransferVO = parseArticleSummary(accountId, accountName,
                                    subArticle, obj.getComm_msg_info());
                            if (subArticleTransferVO != null) {
                                articleTransferVOS.add(subArticleTransferVO);
                            }
                        }
                    }
                }
            } catch(Exception e) {
                log.info("Failed to parseWxArticleList", e);
                return ResultBase.fail("Failed to parseWxArticleList");
            }
        }
        return ResultBase.success(articleTransferVOS);
    }

    @Override
    public ResultBase<ArticleTransferVO> parseArticleDetail(Document sourceDoc) {
        try {
            Document targetDoc = null;
            Element contentNode = sourceDoc.select(WxCrawlerConstant.HTMLElementSelector.RICH_MEDIA_CONTENT).first();
            if(contentNode != null) {
                // 文章为普通图文类型
                targetDoc = SampleHtmlLoader.getNormalArticleSampleDocument();
                targetDoc.select(WxCrawlerConstant.HTMLElementSelector.RICH_MEDIA_CONTENT).first()
                        .replaceWith(contentNode.clone());
            } else {
                // 文章为分享图片类型
                contentNode = sourceDoc.select(WxCrawlerConstant.HTMLElementSelector.SHARE_MEDIA_CONTENT).first();
                if (contentNode == null) {
                    return ResultBase.fail("Failed to parse article detail because of blocked ip");
                }
                targetDoc = SampleHtmlLoader.getShareImgArticleSampleDocument();
                targetDoc.select(WxCrawlerConstant.HTMLElementSelector.SHARE_MEDIA_CONTENT).first()
                        .replaceWith(contentNode.clone());
            }

            targetDoc.outputSettings().prettyPrint(false);
            targetDoc.title(FileUtils.replaceEmoji(sourceDoc.title()));

            // 处理图片节点
            Elements imgElements = targetDoc.select("img");
            if (CollectionUtils.isNotEmpty(imgElements)) {
                for(Element imgElement: imgElements) {
                    resourceTransfer.parseImageElement(imgElement);
                }
            }

            // 处理音频节点
            Elements mpvoiceElements = targetDoc.select("mpvoice");
            if (CollectionUtils.isNotEmpty(mpvoiceElements)) {
                for(Element voiceElement: mpvoiceElements) {
                    resourceTransfer.parseVoiceElement(voiceElement);
                }
            }

            // 处理视频节点
            Elements videoElements = targetDoc.select("iframe.video_iframe");
            if (CollectionUtils.isNotEmpty(videoElements)) {
                for(Element videoElement: videoElements) {
                    resourceTransfer.parseVideoElement(videoElement);
                }
            }

            // 处理背景图属性
            Elements backgroundElements = targetDoc.getElementsByAttributeValueMatching("style",
                    "background-image: url");
            if (CollectionUtils.isNotEmpty(backgroundElements)) {
                for(Element styleElement: backgroundElements) {
                    String value =  resourceTransfer.parseBackgroundImageURL(styleElement.attr("style"));
                    if (StringUtils.isNotEmpty(value)) {
                        styleElement.attr("style", value);
                    }
                }
            }

            String title = sourceDoc.select("h2#activity-name.rich_media_title").first() != null ?
                    sourceDoc.select("h2#activity-name.rich_media_title").first().text() : sourceDoc.title();
            ArticleTransferVO articleVo = new ArticleTransferVO();
            articleVo.setTitle(FileUtils.replaceEmoji(title));
            articleVo.setTargetDoc(targetDoc);
            articleVo.setContent(FileUtils.replaceEmoji(targetDoc.select("body").first().html().trim()));
            return ResultBase.success(articleVo);
        } catch(Exception e) {
            log.info("Failed to parse detail", e);
            return ResultBase.fail("Failed to parse detail");
        }
    }

    @Override
    public ResultBase<List<String>> importWxArticles(String path) {
        if(StringUtils.isEmpty(path)) {
            return ResultBase.fail("Invalid Param");
        }
        File fpath = new File(path);
        if (!fpath.exists()) {
            return ResultBase.fail("Path not found");
        }
        List<String> results = Lists.newArrayList();
        if (fpath.isFile()) {
            if (fpath.getName().endsWith(".html")) {
                ResultBase<String> result = saveByHtmlFile(fpath);
                log.info(result.getMessage());
                results.add(result.getMessage());
            } else {
                return ResultBase.fail("Unsupported file format, file=" + fpath.getName());
            }
        } else {
            File[] files = fpath.listFiles();
            if (files != null && files.length > 0) {
                for(File f: files) {
                    if (f.isFile() && f.getName().endsWith(".html")) {
                        ResultBase<String> result = saveByHtmlFile(f);
                        log.info(result.getMessage());
                        results.add(result.getMessage());
                    }
                }
            } else {
                return ResultBase.fail("Not found any HTML file");
            }
        }
        return ResultBase.success(results);
    }

    @Override
    public ResultBase<String> parseArticleDetail(String accountId, String url) {
        try {
            Document sourceDoc = Jsoup.parse(new URL(url), 60000);
            ResultBase<ArticleTransferVO> articleTransferResult = parseArticleDetail(sourceDoc);
            if (articleTransferResult.isSuccess()) {
                ArticleTransferVO articleVO = articleTransferResult.getValue();
                ResultBase<String> resultBase = articleService.save(articleVO, IArticleService.Operation.UPDATE);
                if (resultBase.isSuccess()) {
                    ResultBase<ArticleTransferVO> articleVOResultBase = articleService.find(articleVO);
                    if (articleVOResultBase.isSuccess()) {
                        ArticleTransferVO article = articleVOResultBase.getValue();
                        Document targetDoc = articleTransferResult.getValue().getTargetDoc();
                        org.apache.commons.io.FileUtils.writeStringToFile(
                                new File(FileUtils.getOutputAccountPath(wxCrawlerConfig.getOutputPath(), article.getAccountName()),
                                        FileUtils.normalize(article.getAccountName() + "_" + article.getTitle() + ".html")),
                                ArticleConvert.covert2Document(targetDoc, article).outerHtml(), "UTF-8");
                    } else {
                        return ResultBase.fail("Not found article, not make backup, accountId="
                                + accountId + ", article=" + articleVO.getTitle());
                    }
                }
                return ResultBase.success(resultBase.getMessage());
            }
        } catch(Exception e) {
            log.info("Failed to parseArticleDetail", e);
        }
        return ResultBase.fail("Failed to parseArticleDetail, url=" + url);
    }

    private ResultBase<String> saveByHtmlFile(File f){
        ResultBase<ArticleTransferVO> articleTransferVOResultBase = parseBackupHtmlFile(f);
        if(articleTransferVOResultBase.isSuccess()) {
            ArticleTransferVO articleTransferVO = articleTransferVOResultBase.getValue();
            return articleService.save(articleTransferVO, IArticleService.Operation.ADD);
        }
        return ResultBase.fail("HTML Error","Failed to parseBackupHtmlFile, file="+f.getAbsolutePath());
    }

    private ResultBase<ArticleTransferVO> parseBackupHtmlFile(File f){
        try {
            Document doc = Jsoup.parse(f, "UTF-8");
            ResultBase<ArticleTransferVO> articleTransferVOResultBase = parseArticleDetail(doc);
            if (articleTransferVOResultBase.isSuccess()) {
                return ResultBase.success(ArticleConvert.convert2ArticleTransferVO(articleTransferVOResultBase.getValue(), doc));
            }
            return articleTransferVOResultBase;
        } catch(Exception e) {
            log.info("Failed to parseBackupHTML, file={}, exception={}", f.getAbsolutePath(), e);
            return ResultBase.fail(e.getMessage());
        }
    }

    /**
     * 解析文章列表中的文章信息
     * @param accountId
     * @param accountName
     * @param appMsgExtInfoObj
     * @param commMsgInfoObj
     * @return
     */
    private ArticleTransferVO parseArticleSummary(String accountId, String accountName,
                                                  AppMsgExtInfoObj appMsgExtInfoObj, CommMsgInfoObj commMsgInfoObj){
        if (appMsgExtInfoObj == null) {
            return null;
        }
        if (StringUtils.isEmpty(appMsgExtInfoObj.getTitle()) || StringUtils.isEmpty(appMsgExtInfoObj.getContent_url())) {
            log.info("Article not exist, skip");
            return null;
        }
        String publishDate = DateTimeUtils.parseDate(commMsgInfoObj.getDatetime() + "000");
        String cover = appMsgExtInfoObj.getCover();
        String author = appMsgExtInfoObj.getAuthor();
        String articleUrl =  appMsgExtInfoObj.getContent_url().replaceAll("&amp;", "&");
        if (!appMsgExtInfoObj.getContent_url().startsWith(WxCrawlerConstant.HTTP)) {
            articleUrl = WxCrawlerConstant.ARTICLE_URL_PREFIX + (articleUrl.startsWith("/")?"": "/") + articleUrl;
        }
        if(articleUrl.startsWith(WxCrawlerConstant.HTTP_PROTOCOL)) {
            articleUrl = articleUrl.replaceFirst(WxCrawlerConstant.HTTP_PROTOCOL, WxCrawlerConstant.HTTPS_PROTOCOL);
        }

        ArticleTransferVO articleVO = new ArticleTransferVO();
        articleVO.setAccountId(accountId);
        articleVO.setAccountName(accountName);
        articleVO.setArticleUrl(articleUrl);
        articleVO.setAuthor(author);
        articleVO.setCover(cover);
        articleVO.setDigest(appMsgExtInfoObj.getDigest());
        articleVO.setTitle(FileUtils.replaceEmoji(appMsgExtInfoObj.getTitle()));
        articleVO.setPublishDate(publishDate);
        return articleVO;
    }
}
