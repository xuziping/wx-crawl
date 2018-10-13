package com.xuzp.service.impl;

import com.xuzp.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author za-xuzhiping
 * @Date 2018/10/13
 * @Time 19:01
 */
@Service
@Slf4j
public class RedisServiceImpl implements IRedisService {

    private Map<String, Object> concurrentMap = new ConcurrentHashMap<String, Object>();

    /**
     * TODO Redis实现落地
     * @param key
     * @param value
     */
    @Override
    public void set(String key, String value) {
        concurrentMap.put(key, value);
    }

    /**
     * TODO Redis实现检索
     * @param key
     * @return
     */
    @Override
    public Boolean exists(String key) {
        return concurrentMap.containsKey(key);
    }

    /**
     * TODO Redis检索
     * @param key
     * @return
     */
    @Override
    public Object get(String key) {
        return concurrentMap.get(key);
    }
}
