package com.xuzp.crawler.weixin.vo;

import lombok.Data;
import org.jsoup.nodes.Document;

/**
 * @author za-xuzhiping
 * @Date 2018/8/21
 * @Time 16:35
 */
@Data
public class ArticleTransferVO {

    /**
     * 公众号id
     */
    private String accountId;

    /**
     * 公众号名称
     */
    private String accountName;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章摘要
     */
    private String digest;

    /**
     * 作者名
     */
    private String author;

    /**
     * 发布时间
     */
    private String publishDate;

    /**
     * 封面url
     */
    private String cover;

    /**
     * 封面 oss url
     */
    private String ossCover;

    /**
     * 文章url
     */
    private String articleUrl;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 备份的Document
     */
    private Document targetDoc;

}
