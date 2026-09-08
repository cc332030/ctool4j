package com.c332030.ctool4j.definition.entity.base;

import com.c332030.ctool4j.doc.annotation.CSchema;

/**
 * <p>
 * Description: ICCreateBy
 * </p>
 *
 * @see "doc/design/definition/ICCreateBy.adoc"
 * @since 2025/12/6
 */
public interface ICCreateBy {

    /**
     * 获取创建人ID
     * @return 创建人ID
     */
    @CSchema("创建人ID")
    Long getCreateById();

    /**
     * 设置创建人ID
     * @param createById 创建人ID
     */
    void setCreateById(Long createById);

    /**
     * 获取创建人
     * @return 创建人
     */
    @CSchema("创建人")
    String getCreateBy();

    /**
     * 设置创建人
     * @param createBy 创建人
     */
    void setCreateBy(String createBy);

}
