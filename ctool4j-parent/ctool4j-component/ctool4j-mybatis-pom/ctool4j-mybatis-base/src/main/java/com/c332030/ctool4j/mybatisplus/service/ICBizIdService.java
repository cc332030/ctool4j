package com.c332030.ctool4j.mybatisplus.service;

import com.c332030.ctool4j.mybatis.util.CBizIdUtils;

/**
 * <p>
 * Description: ICBizIdService
 * </p>
 *
 * @since 2025/12/17
 */
public interface ICBizIdService<ENTITY> extends ICCheckService<ENTITY> {

    /**
     * 获取实体类类型
     *
     * @return 实体类类型
     */
    Class<ENTITY> getEntityClass();

    /**
     * 获取业务 ID
     *
     * @return 业务 ID
     */
    default String getBizId() {
        return CBizIdUtils.getBizId(getEntityClass());
    }

    /**
     * 获取指定长度的业务 ID
     *
     * @param length 业务 ID 长度
     * @return 业务 ID
     */
    default String getBizId(int length) {
        return CBizIdUtils.getBizId(getEntityClass(), length);
    }

}
