package com.xuzp.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author za-xuzhiping
 * @Date 2018/8/7
 * @Time 18:04
 */
@Configuration
@Slf4j
@Getter
public class ProxyConfig {

    @Value("${crawler.proxy.abuyunAccount}")
    private String abuyunAccount;

    @Value("${crawler.proxy.abuyunPassword}")
    private String abuyunPassword;
}
