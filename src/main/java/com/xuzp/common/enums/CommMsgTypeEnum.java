package com.xuzp.common.enums;

import lombok.Getter;

/**
 * @author za-xuzhiping
 * @Date 2018/7/25
 * @Time 17:55
 */
@Getter
public enum CommMsgTypeEnum {

    TYPE_49("49", "图文信息");

    private String code;

    private String name;

    CommMsgTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
