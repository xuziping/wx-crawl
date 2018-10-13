package com.xuzp.crawler.weixin.convert;

import com.xuzp.common.ResultBase;
import com.xuzp.common.WxCrawlerConstant;
import com.xuzp.service.IOssService;
import com.xuzp.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;

/**
 * @author za-xuzhiping
 * @Date 2018/8/3
 * @Time 16:49
 */
@Service
@Slf4j
public class ResourceTransfer {

    @Autowired
    private IOssService ossService;

    @Autowired
    private IRedisService redisService;

    /**
     * 替换style属性中background-img的外部资源引用
     * @param style
     * @return
     */
    public String parseBackgroundImageURL(String style) {
        if(StringUtils.isEmpty(style) || style.indexOf("background-image: url(") == -1) {
            return style;
        }
        style = style.replaceAll("&quot;", "\"");
        String regex = "background-image: url\\(\"(.*?)\"\\)";
        Matcher m = java.util.regex.Pattern.compile(regex).matcher(style);
        StringBuffer sb = new StringBuffer();
        while(m.find()) {
            String url = m.group(1);
            if (url.startsWith("//res.wx.qq.com")) {
                url = "http:" + url;
            }
            ResultBase<String> newURL = getOssValue(url);
            if (newURL.isSuccess()) {
                String newValue = String.format("background-image: url(\"%s\")", newURL.getValue());
                m.appendReplacement(sb, newValue);
            } else {
                log.info("Failed to background url resourceTranslation, url={}", url);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 处理mpvoice音频节点
     * @param voiceElement
     */
    public void parseVoiceElement(Element voiceElement) {
        String voiceURL = WxCrawlerConstant.VOICE_URL + voiceElement.attr("voice_encode_fileid");
        ResultBase<String> newURL = getOssValue(voiceURL);
        if (newURL.isSuccess()) {
            voiceElement.append("<audio src=\"" + newURL + "\">您的浏览器不支持audio标签</audio>");
        } else {
            log.info("Failed to voice resourceTranslation, voiceURL={}", voiceURL);
        }
    }

    /**
     * 处理腾讯视频节点
     * @param videoElement
     */
    public void parseVideoElement(Element videoElement) {
        //   腾讯视频标准url:
        //    String example = "<iframe frameborder=\"0\" width=\"640\" height=\"498\" src=\"https://v.qq.com/iframe/player.html?vid=t0027wte25o&tiny=0&auto=0\" allowfullscreen></iframe>";
        //   微信文章内url:
        //    https://v.qq.com/iframe/preview.html?vid=i0733g55u22&width=500&height=375&auto=0

        //    https://v.qq.com/txp/iframe/player.html?origin=https%3A%2F%2Fmp.weixin.qq.com&vid=i0733g55u22&autoplay=false&full=true&show1080p=false

        videoElement.attr("width", "341").attr("height", "192");
        videoElement.attr("frameborder", "0");

        String url = videoElement.attr("data-src");
        if (StringUtils.isEmpty(url)) {
            url = videoElement.attr("src");
        }
        int vidStart = url.indexOf("vid=");
        int vidEnd = url.indexOf("&", vidStart);
        String vid = url.substring(vidStart+4, vidEnd!=-1?vidEnd:url.length());
        String newURL = WxCrawlerConstant.VIDEO_URL.replace("${vid}", vid);

//        url.replaceFirst("https://v.qq.com/iframe/preview.html", "https://v.qq.com/iframe/player.html");
        videoElement.attr("data-src", newURL).attr("src", newURL);
    }


    /**
     * 替换图片中的链接
     * @param imgElement
     */
    public void parseImageElement(Element imgElement){

        // Step1: 处理 data-src 属性
        String imgURL = imgElement.attr("data-src");
        if (StringUtils.isNotEmpty(imgURL)) {
            ResultBase<String> newURL = getOssValue(imgURL);
            if (newURL.isSuccess()) {
                imgElement.attr("data-src", newURL.getValue());
            } else {
                log.info("Failed to Image data-src resourceTranslation, imgURL={}", imgURL);
            }
        }

        // Step2: 处理 src 属性
        String imgURL2 = imgElement.attr("src");
        if (StringUtils.isNotEmpty(imgURL2)) {
            if (imgURL2.equals(imgURL)) {
                imgElement.attr("src", imgElement.attr("data-src"));
            } else {
                ResultBase<String> newURL = getOssValue(imgURL2);
                if (newURL.isSuccess()) {
                    imgElement.attr("src", newURL.getValue());
                } else {
                    log.info("Failed to Image src resourceTranslation, imgURL2={}", imgURL2);
                }
            }
        } else {
            imgElement.attr("src", imgElement.attr("data-src"));
        }
    }

    /**
     * 缓存资源链接，避免静态资源重复存储。
     * @param key
     * @return
     */
    public ResultBase<String> getOssValue(String key){

        if(StringUtils.isNotEmpty(key)) {
            if (redisService.exists(key)) {
                return ResultBase.success(redisService.get(key));
            } else {
                ResultBase<String> ret = ossService.resourceTranslation(key);
                if (ret.isSuccess()) {
                    redisService.set(key, ret.getValue());
                    return ResultBase.success(ret.getValue());
                } else {
                    return ResultBase.fail("failed to do resourceTranslation, key=" + key);
                }
            }
        }
        return ResultBase.fail("key is null");
    }
}
