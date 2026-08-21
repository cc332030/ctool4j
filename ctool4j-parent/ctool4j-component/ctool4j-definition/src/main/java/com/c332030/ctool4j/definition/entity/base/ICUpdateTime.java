package com.c332030.ctool4j.definition.entity.base;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * <p>
 * Description: ICUpdateTime
 * </p>
 *
 * @see "doc/design/definition/ICUpdateTime.adoc"
 * @since 2025/5/26
 */
public interface ICUpdateTime {

    /**
     * 获取更新时间
     * @return 更新时间
     */
    @ApiModelProperty("更新时间")
    Date getUpdateTime();

    /**
     * 设置更新时间
     * @param updateTime 更新时间
     */
    void setUpdateTime(Date updateTime);

}
