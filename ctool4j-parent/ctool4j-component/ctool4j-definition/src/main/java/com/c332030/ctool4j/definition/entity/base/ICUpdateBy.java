package com.c332030.ctool4j.definition.entity.base;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * Description: ICUpdateBy
 * </p>
 *
 * @see "doc/design/definition/ICUpdateBy.adoc"
 * @since 2025/12/6
 */
public interface ICUpdateBy {

    /**
     * 获取更新人ID
     * @return 更新人ID
     */
    @ApiModelProperty("更新人ID")
    Long getUpdateById();

    /**
     * 设置更新人ID
     * @param updateById 更新人ID
     */
    void setUpdateById(Long updateById);

    /**
     * 获取更新人
     * @return 更新人
     */
    @ApiModelProperty("更新人")
    String getUpdateBy();

    /**
     * 设置更新人
     * @param updateBy 更新人
     */
    void setUpdateBy(String updateBy);

}
