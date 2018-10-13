package com.xuzp.service;

import com.xuzp.common.ResultBase;

public interface IOssService {

    /**
     * 资源转换
     * @param url 资源地址
     * @return 新oss url
     */
    ResultBase<String> resourceTranslation(String url);
}
