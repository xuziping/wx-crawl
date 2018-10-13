package com.xuzp.service.impl;

import com.xuzp.common.ResultBase;
import com.xuzp.common.WxCrawlerConstant;
import com.xuzp.crawler.weixin.vo.ArticleTransferVO;
import com.xuzp.service.IArticleService;
import org.springframework.stereotype.Service;

/**
 * @author za-xuzhiping
 * @Date 2018/10/12
 * @Time 19:23
 */
@Service
public class ArticleServiceImpl implements IArticleService {

    /**
     * TODO 保存文章 - 入库操作
     *
     * @param articleVO
     * @param operation
     * @return
     */
    @Override
    public ResultBase<String> save(ArticleTransferVO articleVO, Operation operation) {
        return new ResultBase<>(true, "Update article successfully, article: " + articleVO.getTitle() + ", account: "
                + articleVO.getAccountName(), WxCrawlerConstant.SUCCESS);
    }

    /**
     * TODO 查询文章
     *
     * @param articleVO
     * @return
     */
    @Override
    public ResultBase<ArticleTransferVO> find(ArticleTransferVO articleVO) {
        return ResultBase.success();
    }
}
