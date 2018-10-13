package com.xuzp.crawler.weixin.obj;

import lombok.Data;

import java.io.Serializable;

/**
 * @author za-xuzhiping
 * @Date 2018/7/25
 * @Time 16:13
 */
@Data
public class AppMsgExtInfoObj implements Serializable {

    private static final long serialVersionUID = -5657010332956164846L;

    private String del_flag;

    /**
     * 是否原创?
     */
    private String copyright_stat;

    private String play_url;

    /**
     * 作者名
     */
    private String author;

    private String malicious_content_type;

    private String item_show_type;

    /**
     * 子内容标题
     */
    private String title;

    /**
     * 文本内容，针对于type1文字类型
     */
    private String content;

    /**
     * 阅读原文的地址
     */
    private String source_url;

    /**
     * 封面图片url
     */
    private String cover;

    private String duration;

    private String audio_fileid;

    private String subtype;

    /**
     * 摘要
     */
    private String digest;

    private String multi_app_msg_item_list;

    /**
     * 文章来源，针对于type49图文类型，（可以用来去重？）
     */
    private String content_url;

    private String malicious_title_reason_id;

    /**
     * 音频文件id，针对于type34音频类型 （微信定义的一个id，每条文章唯一）？
     */
    private String fileid;

    /**
     * 是否多图文， 1：是； 2：否
     */
    private String is_multi;

}
