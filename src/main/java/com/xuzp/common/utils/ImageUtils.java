package com.xuzp.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.FileOutputStream;
import java.io.InputStream;


/**
 * @author za-xuzhiping
 * @Date 2018/7/26
 * @Time 14:36
 */
@Slf4j
public class ImageUtils {

    public static String getString(HttpClient http, String url){
        try{
            HttpGet get=new HttpGet(url);
            HttpResponse hr=http.execute(get);
            HttpEntity he=hr.getEntity();
            if(he!=null){
                String charset=EntityUtils.getContentCharSet(he);
                InputStream is=he.getContent();
                return IOUtils.toString(is,charset);
            }
        } catch (Exception e){
            log.error("Failed to get download, {}", e);
        }
        return null;

    }
    public static byte[] download(HttpClient http,String url) {
        try {
            HttpGet hg = new HttpGet(url);
            HttpResponse hr = http.execute(hg);
            HttpEntity he = hr.getEntity();
            if (he != null) {
                InputStream is = he.getContent();
                return IOUtils.toByteArray(is);
            }

        } catch (Exception e) {
            log.error("Failed to get download, {}", e);
        }
        return null;
    }


    public static void main(String[] args) {

        try  {
            String testURL = "http://mmbiz.qpic.cn/mmbiz_jpg/NpmncptibPgaaNtiaVsusvvIkrYNqU4hp6LiaUkianTc5QgzCsgpf6aIgcvfmXxjbokOllq2PXlu9ibibwSKRV7yJJiag/0?wx_fmt=jpeg";
            HttpClient http = HttpClientBuilder.create().build();
            String html = ImageUtils.getString(http, "http://www.baidu.com");
            log.info(html);
            byte[] im = ImageUtils.download(http, testURL);
            IOUtils.write(im, new FileOutputStream("D:/1.jpg"));
        } catch(Exception e){
            e.printStackTrace();
        } finally {

        }
    }

}
