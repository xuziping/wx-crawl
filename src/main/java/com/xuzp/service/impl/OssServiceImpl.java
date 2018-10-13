package com.xuzp.service.impl;

import com.xuzp.common.ResultBase;
import com.xuzp.service.IOssService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class OssServiceImpl implements IOssService {

    /**
     * 资源转储
     * @param url 资源地址
     * @return
     */
    @Override
    public ResultBase<String> resourceTranslation(String url) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(new HttpGet(url));
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                return ResultBase.success(upload(url, response));
            } else {
                return ResultBase.fail("资源请求错误：" + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            log.info("获取资源失败[{}],异常为{}", url, e);
            return ResultBase.fail(e.getMessage());
        }
    }

    /**
     * TODO 实现图片视频等静态资源的oss转储，否则在服务器上会无法访问微信地址的静态资源
     * @param url
     * @param response
     * @return
     * @throws IOException
     */
    public String upload(String url, HttpResponse response) throws IOException {
        log.info("转储{}", url);
        return url;
    }
}