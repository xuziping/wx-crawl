package com.xuzp.service;

/**
 * @author za-xuzhiping
 * @Date 2018/10/13
 * @Time 19:01
 */
public interface IRedisService {

    /**
     * @param key
     * @param value
     */
    void set(String key, String value);

    /**
     * @param key
     * @return
     */
    Boolean exists(String key);

    /**
     * @param key
     * @return
     */
    Object get(final String key);
}
