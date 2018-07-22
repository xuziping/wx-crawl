package com.xuzp;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.plugin.nextfilter.HashSetNextFilter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;

@Slf4j
public class WxAccountCrawler extends BreadthCrawler {

    protected String historyKeysPath;//历史值存放路径，一个txt文件
    protected BufferedWriter historyKeysWriter;

    public WxAccountCrawler(String crawlPath, String historyKeysPath) throws Exception {
        //自动解析为false，也就是手动解析探索新的URL
        super(crawlPath, false);
        this.historyKeysPath = historyKeysPath;
        LOG.info("initializing history-keys-filter ......");
        //设置URL过滤器
        this.setNextFilter(new HistoryKeysFilter(historyKeysPath));
        LOG.info("creating history-keys-writer");
        //历史值文件写入
        historyKeysWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(historyKeysPath, true), "utf-8"));

    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        String account = page.meta("account");

        if (page.matchType("account_search")) {
            //对于账号搜索页面，手动解析，抽取公众号文章列表页URL
            Element accountLinkEle = page.select("p.tit>a").first();
            //防止搜索结果为空
            if (accountLinkEle == null) {
                LOG.info("公众号\"" + account + "\"不存在，请给出准确的公众号名");
                return;
            }
            //防止公众号名错误
            String detectedAccount = accountLinkEle.text().trim();
            if (!account.equals(detectedAccount)) {
                LOG.info("公众号\"" + account + "\"与搜索结果\"" + detectedAccount + "\"名称不符，请给出准确的公众号名");
                return;
            }
            //解析出公众号搜索结果页面中的URL
            String accountUrl = accountLinkEle.attr("abs:href");
            //添加到待抓取URL队列中
            next.add(new CrawlDatum(accountUrl, "article_list").meta("account", account));

        } else if (page.matchType("article_list")) {
            //对于公众号文章列表页，只显示最近的10篇文章
            String prefix = "msgList = ";
            String suffix = "seajs.use";
            int startIndex = page.html().indexOf(prefix) + prefix.length();
            int endIndex = page.html().indexOf(suffix);
            //trim()函数去除首尾空格
            String jsonStr = page.html().substring(startIndex, endIndex).trim();
            int len = jsonStr.length();
            //去掉最后一个分号，否则无法解析为jsonobject
            jsonStr = jsonStr.substring(0,len-1);
            //System.out.println(jsonStr);
            //将字符串转换为jsonobject
            JSONObject json = JSONObject.parseObject(jsonStr);
            JSONArray articleJSONArray = JSONArray.parseArray(json.getString("list"));
            for (int i = 0; i < articleJSONArray.size(); i++) {
                JSONObject articleJSON = articleJSONArray.getJSONObject(i).getJSONObject("app_msg_ext_info");
                String title = articleJSON.getString("title").trim();
                String key = account + "_" + title;
                //原来问题在这里！！！replace("&amp;", "&")
                //这里是文章的临时链接
                String articleUrl = "http://mp.weixin.qq.com" + articleJSON.getString("content_url").replace("&amp;", "&");
                //添加到待抓取URL队列中
                next.add(new CrawlDatum(articleUrl, "article").key(key).meta("account", account));
            }

        } else if (page.matchType("article")) {
            try {
                //对于文章详情页，抽取标题、内容等信息
                String title = page.select("h2.rich_media_title").first().text().trim();
                //String date = page.select("em#post-date").first().text().trim();
                String content = page.select("div.rich_media_content").first().text().trim();
                //适应数据库中content大小
                content = content.substring(0,255);
                //将页面key写入文件中用来去重
                writeHistoryKey(page.key());

                log.info("OK title={}", title);

                //持久化到数据库
//                writeNewstoDB(title,content);
                //JSONObject articleJSON = new JSONObject();
                //articleJSON.fluentPut("account", account)
                //            .fluentPut("title", title)
                //           .fluentPut("content", content);
                //System.out.println(articleJSON);
            } catch (Exception ex) {
                LOG.info("writer exception", ex);
            }
        }
    }

//    public synchronized void writeNewstoDB(String title, String content) throws Exception {
//
//        JdbcTemplate jdbcTemplate = null;
//        try {
//            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
//                    "jdbc:mysql://localhost:3306/toutiao?useUnicode=true&characterEncoding=utf8&useSSL=false",
//                    "xxxx", "xxxx", 5);
////如果数据库中没有相关的表这里需要添加建表操作
//        } catch (Exception ex) {
//            jdbcTemplate = null;
//            System.out.println("mysql未开启或JDBCHelper.createMysqlTemplate中参数配置不正确!");
//        }
//        if (jdbcTemplate != null) {
//            int updates=jdbcTemplate.update("insert into news"
//                            +" (title, link, image, like_count, comment_count, created_date, user_id) value(?,?,?,?,?,?,?)",
//                    title, content, "http://images.nowcoder.com/head/23m.png", 0, 0, new Date(), 3);
//            if(updates==1){
//                System.out.println("mysql插入成功");
//            }
//        }
//    }

    public synchronized void writeHistoryKey(String key) throws Exception {
        historyKeysWriter.write(key + "\n");
    }

    @Override
    public void start(int depth) throws Exception {
        super.start(depth);
        //关闭文件，保存history keys
        historyKeysWriter.close();
        LOG.info("save history keys");
    }

    public void addAccount(String account) throws UnsupportedEncodingException {
        //根据公众号名称设置种子URL
        String seedUrl = "http://weixin.sogou.com/weixin?type=1&"
                + "s_from=input&ie=utf8&query=" + URLEncoder.encode(account, "utf-8");
        CrawlDatum seed = new CrawlDatum(seedUrl, "account_search").meta("account", account);
        addSeed(seed);
    }

    public class HistoryKeysFilter extends HashSetNextFilter {

        //读取历史文章标题，用于去重
        public HistoryKeysFilter(String historyKeysPath) throws Exception {
            File historyFile = new File(historyKeysPath);
            if (historyFile.exists()) {
                FileInputStream fis = new FileInputStream(historyKeysPath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    this.add(line);
                }
                reader.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        WxAccountCrawler crawler = new WxAccountCrawler("crawl_weixin", "wx_history.txt");
        crawler.addAccount("西电研究生");
        crawler.addAccount("西电导航");
        crawler.addAccount("西电学生会");
        crawler.addAccount("西电小喇叭");
        crawler.setThreads(5);
        crawler.start(10);
    }
}