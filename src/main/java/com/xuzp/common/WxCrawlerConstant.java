package com.xuzp.common;

/**
 * @author za-xuzhiping
 * @Date 2018/7/25
 * @Time 16:03
 */
public interface WxCrawlerConstant {

    String YES = "1";
    String NO = "0";

    /**
     * 微信公众号文章列表页相关常量
     */
    interface ArticleList {
        String ARTICLE_LIST_KEY = "msgList = ";
        String ARTICLE_LIST_SUFFIX = "seajs.use";
    }

    String ARTICLE_URL_PREFIX = "http://mp.weixin.qq.com";

    String SEARCH_URL = "http://weixin.sogou.com/weixin?type=1&s_from=input&ie=utf8&query=";

    String VOICE_URL = "https://res.wx.qq.com/voice/getvoice?mediaid=";

    interface CrawlMetaKey {

        String ACCOUNT = "wx_account";
    }

    interface CrawlDatumType{
        /**
         * 公众号搜索页
         */
        String ACCOUNT_SEARCH = "wx_account_search";

        /**
         * 公众号文章列表页
         */
        String ARTICLE_LIST = "wx_article_list";

        /**
         * 公众号文章详情页
         */
        String ARTICLE_DETAIL = "wx_article_detail";
    }

    interface HTMLElementSelector{

        /**
         * 文章标题
         */
        String RICH_MEDIA_TITLE  = "h2.rich_media_title";

        String PUBLISH_TIME = "em#publish_time";

        /**
         * 文章内容
         */
        String RICH_MEDIA_CONTENT = "div.rich_media_content";
    }
}
