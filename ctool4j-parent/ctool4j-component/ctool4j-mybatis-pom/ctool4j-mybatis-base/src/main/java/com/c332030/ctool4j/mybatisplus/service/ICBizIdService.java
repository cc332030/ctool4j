package com.c332030.ctool4j.mybatisplus.service;

import com.c332030.ctool4j.mybatis.util.CBizIdUtils;

/**
 * <p>
 * Description: ICBizIdService
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICBizIdService}：业务ID服务接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>扩展 ICCheckService，提供业务ID获取</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>默认 ID 生成</p>
 * <h2>适用范围</h2>
 * <p>业务ID服务</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口</p>
 *
 * @since 2025/12/17
 * @version 1.0
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
