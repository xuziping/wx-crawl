package com.xuzp.crawler.weixin.convert;

import com.xuzp.common.WxCrawlerConstant;
import com.xuzp.crawler.weixin.vo.ArticleTransferVO;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author za-xuzhiping
 * @Date 2018/8/21
 * @Time 17:46
 */
public class ArticleConvert {

    public static ArticleTransferVO convert2ArticleTransferVO(ArticleTransferVO articleTransferVO, Document doc){
        Element header = doc.head();
        articleTransferVO.setPublishDate(header.attr(WxCrawlerConstant.BackupArticle.PUBLISH_DATE));
        articleTransferVO.setAuthor(header.attr(WxCrawlerConstant.BackupArticle.AUTHOR));
        articleTransferVO.setAccountId(header.attr(WxCrawlerConstant.BackupArticle.ACCOUNT_ID));
        articleTransferVO.setAccountName(header.attr(WxCrawlerConstant.BackupArticle.ACCOUNT_NAME));
        articleTransferVO.setDigest(header.attr(WxCrawlerConstant.BackupArticle.DIGEST));
        articleTransferVO.setOssCover(header.attr(WxCrawlerConstant.BackupArticle.COVER));
        articleTransferVO.setArticleType(header.attr(WxCrawlerConstant.BackupArticle.ARTICLE_TYPE));
        articleTransferVO.setTitle(header.attr(WxCrawlerConstant.BackupArticle.ARTICLE_TITLE));
        return articleTransferVO;
    }

    public static Document covert2Document(Document targetDoc, ArticleTransferVO article){
        targetDoc.head().attr(WxCrawlerConstant.BackupArticle.AUTHOR, getValue(article.getAuthor()))
                .attr(WxCrawlerConstant.BackupArticle.COVER, getValue(article.getCover()))
                .attr(WxCrawlerConstant.BackupArticle.DIGEST, getValue(article.getDigest()))
                .attr(WxCrawlerConstant.BackupArticle.ACCOUNT_ID, article.getAccountId())
                .attr(WxCrawlerConstant.BackupArticle.ACCOUNT_NAME, article.getAccountName())
                .attr(WxCrawlerConstant.BackupArticle.PUBLISH_DATE, article.getPublishDate())
                .attr(WxCrawlerConstant.BackupArticle.ARTICLE_TYPE, article.getArticleType())
                .attr(WxCrawlerConstant.BackupArticle.ARTICLE_TITLE, article.getTitle());
        return targetDoc;
    }

    private static String getValue(String value){
        if(StringUtils.isEmpty(value)) {
            return "";
        } else {
            return value.trim();
        }
    }
}
