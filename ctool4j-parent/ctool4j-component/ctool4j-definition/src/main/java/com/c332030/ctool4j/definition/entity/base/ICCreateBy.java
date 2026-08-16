package com.c332030.ctool4j.definition.entity.base;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * Description: ICCreateBy
 * </p>
 *
 * @since 2025/12/6
 */
public interface ICCreateBy {

    /**
     * 获取创建人ID
     * @return 创建人ID
     */
    @ApiModelProperty("创建人ID")
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
    @ApiModelProperty("创建人")
    String getCreateBy();

    /**
     * 设置创建人
     * @param createBy 创建人
     */
    void setCreateBy(String createBy);

}
