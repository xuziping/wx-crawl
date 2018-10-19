## 项目介绍 ##

本项目是一个基于Java的微信公众号文章爬虫，使用Web Collector开源爬虫框架和SpringBoot实现，通过搜狗平台获取可以使用多种代理策略对指定公众号文章进行定时爬取，也可以通过访问浏览器URL手动触发爬取。

你可以基于本项目进行二次开发，配置Redis避免反复爬取，也可以通过实现OSS进行静态资源转储，以及实现文章保存接口来对爬取的内容入库。

本项目特色：

- 基于Web Collector爬虫框架和Spring Boot实现

- 有包括指定代理IP，不使用代理IP以及使用阿布云代理等多种代理策略来避免爬虫IP被屏蔽

- 有完善的爬虫结果导入功能支持服务器端使用

- 预留了Redis服务，OSS服务以及数据库保存等在内的接口

- 支持Swagger接口文档

## 开始使用 ##


## API列表 ##

启动项目，访问 `http://localhost:11111/swagger-ui.html`，呈现所有的对外接口如下：


![](https://i.imgur.com/FUTC3bh.png)


也可以通过直接访问 `http://localhost:11111/help` 获取接口介绍：

![](https://i.imgur.com/yGGwO85.png)

其中 `/parseWxArticleList` 和 `/parseWxArticleDetail` 接口是内部测试使用，解析fiddler从微信客户端抓取的文章列表json以及文章详情json。由于目前搜狗微信仅收录微信“订阅号”不收录“服务号”，因此对于服务号类型的公众号需要额外处理。另外，目前搜狗微信不收录文章“阅读次数”和“点赞次数”，因此也需要通过抓包的方式获取。

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
    	# 是否使用断点续爬，通过redis避免重复爬取。注意，由于会跳过已爬过的文章，因此就无法更新旧文章了
    	resumable: false
    	# 代理使用策略，包括不使用代理，使用指定代理IP以及使用阿布云代理： none | 222.182.56.50:8118,124.235.208.252:443 | abuyun
    	proxyPolicy: none
    	# 是否更新已存在的文章
    	updateArticle: true
      proxy:
    	# 阿布云账号
    	abuyunAccount: xxxx
    	# 阿布云密码
    	abuyunPassword: xxxxx