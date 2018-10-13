package com.xuzp.crawler.weixin.convert;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import com.xuzp.common.WxCrawlerConstant;
import com.xuzp.crawler.weixin.vo.ArticleTransferVO;

/**
 * @author za-xuzhiping
 * @Date 2018/8/21
 * @Time 17:35
 */
public class CrawlDatumConvert {

    public static CrawlDatum convert2ArticleSummaryCrawlDatum(ArticleTransferVO articleTransferVO){
        return new CrawlDatum(articleTransferVO.getArticleUrl(), WxCrawlerConstant.CrawlDatumType.ARTICLE_DETAIL)
                .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_NAME, articleTransferVO.getAccountName())
                .meta(WxCrawlerConstant.CrawlMetaKey.ACCOUNT_ID, articleTransferVO.getAccountId())
                .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_TITLE, articleTransferVO.getTitle())
                .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_COVER, articleTransferVO.getOssCover())
                .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_DIGEST, articleTransferVO.getDigest())
                .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_PUBLISH_DATE, articleTransferVO.getPublishDate())
                .meta(WxCrawlerConstant.CrawlMetaKey.ARTICLE_AUTHOR, articleTransferVO.getAuthor())
                .meta(WxCrawlerConstant.CrawlMetaKey.TRIED_COUNT, 0);
    }
}
