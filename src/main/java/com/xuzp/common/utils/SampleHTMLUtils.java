package com.xuzp.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

/**
 * @author za-xuzhiping
 * @Date 2018/3/6
 * @Time 14:59
 */
@Slf4j
public class SampleHTMLUtils {

    private static String html = null;
    private static Document doc = null;
    private static final String PATH = "/static/templates/sample.html";

    static {
        try (InputStream inputStream = new ClassPathResource(PATH).getInputStream();){
            html = FileUtils.loadInput(inputStream);
            doc = Jsoup.parse(html);
        } catch (Exception e) {
            log.error("Failed to init， exception={}", e);
        }
    }

    public static String getSampleHTML() {
        return html;
    }

    public static Document getSampleDocument() {
        return doc.clone();
    }

}
