package com.xuzp.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

/**
 * 微信文章模板加载，目前支持图文类型和图片分享类型
 *
 * @author za-xuzhiping
 * @Date 2018/3/6
 * @Time 14:59
 */
@Slf4j
public class SampleHtmlLoader {

    private static final String NORMAL_ARTICLE_PATH = "/static/templates/normal_article.html";
    private static final String SHARE_IMG_ARTICLE_PATH = "/static/templates/share_img_article.html";
    private static Document normal_article_doc = null;
    private static Document share_img_doc = null;

    static {
        try (InputStream inputStream = new ClassPathResource(NORMAL_ARTICLE_PATH).getInputStream();) {
            String normal_article_html = FileUtils.loadInput(inputStream);
            normal_article_doc = Jsoup.parse(normal_article_html);
        } catch (Exception e) {
            log.error("Failed to init normal article， exception={}", e);
        }

        try (InputStream inputStream = new ClassPathResource(SHARE_IMG_ARTICLE_PATH).getInputStream();) {
            String share_img_html = FileUtils.loadInput(inputStream);
            share_img_doc = Jsoup.parse(share_img_html);
        } catch (Exception e) {
            log.error("Failed to init share_img article， exception={}", e);
        }
    }

    public static Document getNormalArticleSampleDocument() {
        return normal_article_doc.clone();
    }

    public static Document getShareImgArticleSampleDocument() {
        return share_img_doc.clone();
    }
}
