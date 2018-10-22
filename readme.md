## 项目介绍 ##

本项目是一个基于Java的微信公众号文章爬虫，使用 Web Collector 开源爬虫框架和 Spring Boot 实现，通过搜狗平台获取可以使用多种代理策略对指定公众号文章进行定时爬取，也可以通过访问浏览器URL手动触发爬取。

你可以基于本项目进行二次开发，配置Redis避免反复爬取（本项目使用 RamCrawler，没有使用 BreadthCrawler），也可以通过实现OSS进行静态资源转储，以及实现文章保存接口来对爬取的内容入库。

本项目特色：

- 基于Web Collector爬虫框架和Spring Boot实现

- 有包括指定代理IP，不使用代理IP以及使用阿布云代理等多种代理策略来避免爬虫IP被屏蔽

- 有完善的爬虫结果导入功能支持服务器端使用

- 预留了Redis服务，OSS服务以及数据库保存等在内的接口

- 支持Swagger接口文档

## 开始使用 ##

1. maven install

2. 调整配置文件 `application.yml` ，具体参数请参见 **配置** 

3. 启动项目 `WxCrawlerApplication`

4. 打开浏览器访问 `http://localhost:11111/wxCrawler?proxyPolicy=none` 触发爬虫任务

5. 爬完后文件在 `crawler.weixin.outputPath` 指定路径下生成，以 `公众号名_文章名.html` 的命名方式存档



## API列表 ##

启动项目，访问 `http://localhost:11111/swagger-ui.html`，呈现所有的对外接口如下：


![](https://i.imgur.com/FUTC3bh.png)


也可以通过直接访问 `http://localhost:11111/help` 获取接口介绍：

![](https://i.imgur.com/yGGwO85.png)

其中 `/parseWxArticleList` 和 `/parseWxArticleDetail` 接口是内部测试使用，解析fiddler从微信客户端抓取的文章列表json以及文章详情json。由于目前搜狗微信仅收录微信“订阅号”不收录“服务号”，因此对于服务号类型的公众号需要额外处理。另外，目前搜狗微信不收录文章“阅读-次数”和“点赞次数”，因此也需要通过抓包的方式获取。详情请参见 **对爬取服务号文章以及阅读数和点赞数的支持**。

## 配置 ##
    
    server:
      port: 11111
    spring:
      application:
    	name: wx-crawl
    
    crawler:
      weixin:
    	# 待爬取的微信公众号，支持爬取多个公众号，以;分隔（目前仅支持订阅号）
    	accounts: 雪球;缘聚小许
    	# outputPath 生成文章內容html文件
    	outputPath: D:/article_output
    	# 爬取访问一次后休眠时间，毫秒单位，为避免搜狗和微信封杀，建议设置至少3000以上
    	sleepTime: 5000
    	# 是否使用断点续爬，通过redis避免重复爬取
		# 注意，由于会跳过已爬过的文章，因此就无法更新旧文章了
    	resumable: false
    	# 代理使用策略，包括不使用代理，使用指定代理IP以及使用阿布云代理
		# 如： none | 222.182.56.50:8118,124.235.208.252:443 | abuyun
    	proxyPolicy: none
    	# 是否更新已存在的文章
    	updateArticle: true
      proxy:
    	# 阿布云账号
    	abuyunAccount: xxxx
    	# 阿布云密码
    	abuyunPassword: xxxxx

## 二次开发 & 扩展 ##

- 入库实现

	文章入库接口是 `com.xuzp.service.IArticleService`：
	
		/**
	     * 保存文章
	     * @param articleVO
	     * @return
	     */
	    ResultBase<String> save(ArticleTransferVO articleVO, Operation operation);
	
	    /**
	     * 查找文章
	     * @param articleVO
	     * @return
	     */
	    ResultBase<ArticleTransferVO> find(ArticleTransferVO articleVO);


	文章入库实现类是 `com.xuzp.service.impl.ArticleServiceImpl`, 请自行扩展。
	

- Redis扩展

	Redis接口是 `com.xuzp.service.IRedisService`:

	 	 /**
	     * @param key
	     * @param value
	     */
	    void set(String key, String value);
	
	    /**
	     * @param key
	     * @return
	     */
	    Boolean exists(String key);
	
	    /**
	     * @param key
	     * @return
	     */
	    Object get(final String key);

	Redis接口的实现类是 `com.xuzp.service.impl.RedisServiceImpl`，请自行扩展。

- OSS扩展

	OSS接口是 `com.xuzp.service.IOssService`:

	    /**
	     * 把存于腾讯服务器上的包括视频，音频，图片等静态资源转储
	     * @param url 资源地址
	     * @return 新oss url
	     */
	    ResultBase<String> resourceTranslation(String url);

	实现类位于 `com.xuzp.service.impl.OssServiceImpl`，请自行扩展。

- 调整自动爬取时间

	定时任务代码是 `com.xuzp.schedule.CrawlScheduledTasks`，请自行调整时间:

		/**
	     * 定时爬虫去抓取微信公众号文章
	     */
	    @Scheduled(cron = "0 30 10,18 * * ?")
	    public void weixinCrawlTask() {
	        crawlerStater(new LazyLoader<Crawler>(){
	            @Override
	            public WxCrawler newInstance() {
	                return wxCrawlerConfig.wxCrawler(null);
	            }
	         }, WxCrawlerConstant.CRAWL_DEPTH, WX_CRAWLER, "公众号爬虫");
	    }

- 对爬取服务号文章以及阅读数和点赞数的支持

	目前的爬取是基于搜狗微信平台 `http://weixin.sogou.com/`，因此受到以下限制：

	- 只收录了微信订阅号，没有收录微信服务号的文章
	
	- 只能爬取最近10条记录，无法爬取过往所有的历史记录 

	- 只收录了文章基本信息，包括标题，作者，摘要，文章内容，发布时间等，但没有收录文章阅读数以及文章点赞数等信息

	目前，本项目特提供了两个额外接口通过解析Fiddler抓包数据来获取文章信息：
		
		/parseWxArticleList： 解析抓包获取的文章列表json

		/parseWxArticleDetail: 解析获取抓包获取的文章详情json，后于 parseWxArticleList 接口执行。

	后续会通过扩展分析fiddler的抓包数据，实现自动化，请等待后续版本的更新。