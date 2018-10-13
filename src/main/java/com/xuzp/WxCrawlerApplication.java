package com.xuzp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.xuzp.*" })
public class WxCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxCrawlerApplication.class,args);
    }
}
