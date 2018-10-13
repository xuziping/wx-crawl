package com.xuzp.common.utils;

/**
 * @author za-xuzhiping
 * @Date 2018/8/16
 * @Time 20:36
 */
public abstract class LazyLoader<T>{
    /**
     * 延迟加载方法
     * @return
     */
    public abstract T newInstance();
}
