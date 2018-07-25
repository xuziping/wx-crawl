package com.xuzp.common.enums;

import lombok.Getter;

/**
 * @author za-xuzhiping
 * @Date 2018/7/25
 * @Time 18:17
 */
@Getter
public enum CrawlerNodeTypeEnum {

    ACCOUNT_SEARCH("account_search", "公众号搜索页"),
    ARTICLE_LIST("article_list", "公众号文章列表页"),
    ARTICLE_DETAIL("article_detail", "公众号文章详情页");

    private String code;

    private String name;

    CrawlerNodeTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
