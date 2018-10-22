package com.xuzp.service;

import com.xuzp.common.ResultBase;

public interface IOssService {

    /**
     * 把存于腾讯服务器上的包括视频，音频，图片等静态资源转储
     * @param url 资源地址
     * @return 新oss url
     */
    ResultBase<String> resourceTranslation(String url);
}
