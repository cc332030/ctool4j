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

    /**
     * 获取主键
     * @return 主键
     */
    @ApiModelProperty("主键")
    T getId();

    /**
     * 设置主键
     * @param id 主键
     */
    void setId(T id);

}
