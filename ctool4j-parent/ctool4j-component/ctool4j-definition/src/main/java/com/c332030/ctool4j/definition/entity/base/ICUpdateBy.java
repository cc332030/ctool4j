package com.c332030.ctool4j.definition.entity.base;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * Description: ICUpdateBy
 * </p>
 *
 * @since 2025/12/6
 */
public interface ICUpdateBy {

    @ApiModelProperty("更新人ID")
    Long getUpdateById();

    void setUpdateById(Long updateById);

    @ApiModelProperty("更新人")
    String getUpdateBy();

    void setUpdateBy(String updateBy);

}
