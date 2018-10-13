package com.xuzp.service;

import com.xuzp.common.ResultBase;
import com.xuzp.crawler.weixin.obj.ArticleSummaryObj;
import com.xuzp.crawler.weixin.vo.ArticleTransferVO;
import org.jsoup.nodes.Document;

import java.util.List;

/**
 * @author za-xuzhiping
 * @Date 2018/8/21
 * @Time 16:12
 */
public interface IWxCrawlService {

    /**
     * 解析微信公众号文章列表
     * @param accountId
     * @param accountName
     * @param articles
     * @return
     */
    ResultBase<List<ArticleTransferVO>> parseArticleList(String accountId, String accountName,
                                                         List<ArticleSummaryObj> articles);

    /**
     * 解析微信公众号文章详情页
     * @param doc
     * @return
     */
    ResultBase<ArticleTransferVO> parseArticleDetail(Document doc);

    /**
     * 导入微信公众号文章html文件
     * @param path
     * @return
     */
    ResultBase<List<String>> importWxArticles(String path);

    /**
     * 根据url解析公众号文章详情页
     * @param accountId
     * @param url
     * @return
     */
    ResultBase<String> parseArticleDetail(String accountId, String url);
}
