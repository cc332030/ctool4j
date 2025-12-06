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

    @ApiModelProperty("创建人ID")
    Long getCreateById();

    @ApiModelProperty("创建人")
    String getCreateBy();

}
