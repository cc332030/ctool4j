package com.c332030.ctool4j.definition.entity.base;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * <p>
 * Description: ICCreateTime
 * </p>
 *
 * @since 2025/5/26
 */
public interface ICUpdateTime {

    @ApiModelProperty("更新时间")
    Date getUpdateTime();

    void setUpdateTime(Date updateTime);

}
