package com.xuzp.service;


import com.xuzp.common.ResultBase;
import com.xuzp.crawler.weixin.vo.ArticleTransferVO;

/**
 * @author za-xuzhiping
 * @Date 2018/8/3
 * @Time 16:22
 */
public interface IArticleService {

    enum Operation {
        ADD, UPDATE, SAVE
    }

    /**
     * 保存文章
     * @param articleVO
     * @return
     */
    ResultBase<String> save(ArticleTransferVO articleVO, Operation operation);

    /**
     * 查找文章
     * @param articleVO
     * @return
     */
    ResultBase<ArticleTransferVO> find(ArticleTransferVO articleVO);
}
