package com.xuzp.wxobj;

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
     * 版权码
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
     * 简单摘要
     */
    private String digest;

    private String multi_app_msg_item_list;

    private String content_url;

    private String malicious_title_reason_id;

    private String fileid;

    /**
     * 是否多图文， 1：是； 2：否
     */
    private String is_multi;

}
