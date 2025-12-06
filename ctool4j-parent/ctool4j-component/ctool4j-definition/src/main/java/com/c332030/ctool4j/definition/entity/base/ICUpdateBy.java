package com.c332030.ctool4j.definition.entity.base;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * Description: ICUpdateBy
 * </p>
 *
 * @since 2025/12/6
 */
public interface ICUpdateBy extends ICCreateBy {

    @ApiModelProperty("更新人ID")
    Long getUpdateById();

    @ApiModelProperty("更新人")
    String getUpdateBy();

}
