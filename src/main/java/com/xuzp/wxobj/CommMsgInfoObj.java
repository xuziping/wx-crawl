package com.xuzp.wxobj;

import lombok.Data;

import java.io.Serializable;

/**
 * @author za-xuzhiping
 * @Date 2018/7/25
 * @Time 16:19
 */
@Data
public class CommMsgInfoObj implements Serializable{

    private static final long serialVersionUID = 8327261273161784601L;

    /**
     * 发布时间，为unix时间戳
     */
    private String datetime;

    /**
     * 公众号独一无二的id
     */
    private String fakeid;

    private String id;

    /**
     * 49:图文，1:文字，3:图片，34:音频，62:视频
     */
    private String type;

    private String content;

    private String status;
}
