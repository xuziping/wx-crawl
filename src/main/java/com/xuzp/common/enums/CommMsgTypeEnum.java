package com.xuzp.common.enums;

import lombok.Getter;

/**
 * @author za-xuzhiping
 * @Date 2018/7/25
 * @Time 17:55
 */
@Getter
public enum CommMsgTypeEnum {

    ARTICLE_WITH_IMG("49", "图文"),
    ARTICLE("1", "文字"),
    IMAGE("3", "图片"),
    AUDIO("34", "音频"),
    VIDEO("62", "视频"),
    ;

    private String code;

    private String name;

    CommMsgTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
