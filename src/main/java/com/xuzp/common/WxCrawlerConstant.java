package com.xuzp.common;

/**
 * @author za-xuzhiping
 * @Date 2018/7/25
 * @Time 16:03
 */
public interface WxCrawlerConstant {

    String YES = "1";
    String NO = "0";

    String HTTP = "http";

    String HTTP_PROTOCOL = "http:";
    String HTTPS_PROTOCOL = "https:";

    int MAX_TRY_COUNT = 30;

    int CRAWL_DEPTH = 50;

    interface ProxyPolicy{
        String ABUYUN = "abuyun";
        String AUTO = "auto";
        String NONE = "none";
    }

    interface RequestInfo {
        int REQUEST_TIMEOUT = 3000;
        String CHARSET_NAME = "UTF-8";
        String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.79 Safari/537.36";
        String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8";
        String ACCEPT_ENCODING = "gzip"; // 不用设置，否则返回压缩内容无法解析
        String ACCEPT_LANGUAGE = "zh-CN,zh;q=0.9,en;q=0.8";
        String CONNECTION = "keep-alive";
        String SOGOU_HOST = "weixin.sogou.com";
        String WEIXIN_HOST = "mp.weixin.qq.com";
        String CACHE_CONTROL = "max-age=0";
        String REFERER = "http://weixin.sogou.com/weixin?type=2&ie=utf8&s_from=hotnews&query=%E6%9D%A8%E8%B6%85%E8%B6%8A%E4%B8%AD%E4%BA%862%E4%B8%87";
        String UPGRADE_INSECURE_REQUESTS = "1";
        String WX_BROWSER_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36 MicroMessenger/6.5.2.501 NetType/WIFI WindowsWechat QBCore/3.43.884.400 QQBrowser/9.0.2524.400";
    }

    /**
     * 微信公众号文章列表页相关常量
     */
    interface ArticleList {
        String ARTICLE_LIST_KEY = "msgList = ";
        String ARTICLE_LIST_SUFFIX = "seajs.use";
    }

    String ARTICLE_URL_PREFIX = "https://mp.weixin.qq.com";

    String SEARCH_URL = "https://weixin.sogou.com/weixin?type=1&s_from=input&ie=utf8&query=";

    String VOICE_URL = "https://res.wx.qq.com/voice/getvoice?mediaid=";

    String VIDEO_URL = "https://v.qq.com/txp/iframe/player.html?origin=https%3A%2F%2Fmp.weixin.qq.com&vid=${vid}&autoplay=false&full=true&show1080p=false";

    interface CrawlMetaKey {
        String ACCOUNT_NAME = "wx_account_name";
        String ACCOUNT_ID = "wx_account_id";
        String ARTICLE_TITLE = "wx_article_title";
        String ARTICLE_COVER = "wx_article_cover";
        String ARTICLE_DIGEST = "wx_article_digest";
        String ARTICLE_PUBLISH_DATE = "wx_article_publish_date";
        String ARTICLE_AUTHOR = "wx_article_author";
        String TRIED_COUNT = "wx_crawl_tried_count";
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
         * 文章内容
         */
        String RICH_MEDIA_CONTENT = "div.rich_media_content";
        /**
         * 图片分享
         */
        String SHARE_MEDIA_CONTENT = "div.share_media";
        /**
         * 标题
         */
        String TITLE = "h2#activity-name.rich_media_title";
    }

    interface BackupArticle {
        String AUTHOR = "author";
        String COVER = "cover";
        String DIGEST = "digest";
        String ACCOUNT_ID = "accountId";
        String ACCOUNT_NAME = "accountName";
        String PUBLISH_DATE = "publishDate";
        String ARTICLE_TYPE = "articleType";
        String ARTICLE_TITLE = "articleTitle";
    }
}
