package com.c332030.ctool4j.definition.entity.base;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * Description: ICId
 * </p>
 *
 * @since 2025/5/26
 */
public interface ICId<T> {

    @ApiModelProperty("主键")
    T getId();

}
