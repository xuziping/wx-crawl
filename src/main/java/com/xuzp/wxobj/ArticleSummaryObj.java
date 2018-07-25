package com.xuzp.wxobj;

import lombok.Data;

import java.io.Serializable;

/**
 * 一条多图文或单图文消息，通俗说就是一天的群发消息都在这个对象中
 *
 * @author za-xuzhiping
 * @Date 2018/7/25
 * @Time 15:57
 */
@Data
public class ArticleSummaryObj implements Serializable{

    private static final long serialVersionUID = 2555742115275768017L;

    /**
     * 图文消息的扩展信息
     */
    private AppMsgExtInfoObj app_msg_ext_info;

    /**
     * 图文消息的基本信息
     */
    private CommMsgInfoObj comm_msg_info;

}
