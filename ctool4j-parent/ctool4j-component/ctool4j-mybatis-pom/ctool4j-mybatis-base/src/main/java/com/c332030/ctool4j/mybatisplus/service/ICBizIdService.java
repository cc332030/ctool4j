package com.c332030.ctool4j.mybatisplus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c332030.ctool4j.mybatis.util.CBizIdUtils;

/**
 * <p>
 * Description: ICBizIdService
 * </p>
 *
 * @since 2025/12/17
 */
public interface ICBizIdService<ENTITY> extends IService<ENTITY> {

    Class<ENTITY> getEntityClass();

    default String getBizId() {
        return CBizIdUtils.getBizId(getEntityClass());
    }

    default String getBizId(int length) {
        return CBizIdUtils.getBizId(getEntityClass(), length);
    }

}
