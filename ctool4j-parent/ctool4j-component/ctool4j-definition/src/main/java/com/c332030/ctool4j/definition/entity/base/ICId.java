package com.c332030.ctool4j.definition.entity.base;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>
 * Description: ICId
 * </p>
 *
 * @since 2025/5/26
 */
public interface ICId<T extends Serializable> {

    @ApiModelProperty("主键")
    T getId();

    void setId(T id);

}
