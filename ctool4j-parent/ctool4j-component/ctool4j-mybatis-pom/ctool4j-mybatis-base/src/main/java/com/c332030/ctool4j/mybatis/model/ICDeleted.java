package com.c332030.ctool4j.mybatis.model;

import com.c332030.ctool4j.doc.annotation.CSchema;

/**
 * <p>
 * Description: ICDeleted
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICDeleted}：删除标识接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>定义 deleted 字段的 getter/setter，@ApiModelProperty 描述</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>逻辑删除标识</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口定义</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口定义</p>
 *
 * @since 2025/12/16
 * @version 1.0
 */
public interface ICDeleted {

    /**
     * 获取删除标识
     * @return 删除标识
     */
    @CSchema("删除标识")
    Boolean getDeleted();

    /**
     * 设置删除标识
     * @param deleted 删除标识
     */
    void setDeleted(Boolean deleted);

}
