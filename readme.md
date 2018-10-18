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

其中 /parseWxArticleDetail 和 /parseWxArticleList 接口是内部

## 配置 ##

