package com.c332030.ctool4j.mybatis.model;

import com.c332030.ctool4j.doc.annotation.CSchema;

/**
 * <p>
 * Description: ICDeleted
 * </p>
 *
 * @since 2025/12/16
 * @see "doc/design/mybatis/ICDeleted.adoc"
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
