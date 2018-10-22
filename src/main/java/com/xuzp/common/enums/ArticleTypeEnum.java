package com.xuzp.common.enums;

import lombok.Getter;

/**
 * @author za-xuzhiping
 * @Date 2018/10/22
 * @Time 14:51
 */
@Getter
public enum ArticleTypeEnum {

    NORMAL("normal", "普通图文类型"),
    IMAGE_SHARE("image_share", "图片分享类型"),
    ;

    private final String code;
    private final String message;

    ArticleTypeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
