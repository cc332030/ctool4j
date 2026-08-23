package com.c332030.ctool4j.definition.entity.base;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * <p>
 * Description: ICCreateTime
 * </p>
 *
 * @see "doc/design/definition/ICCreateTime.adoc"
 * @since 2025/5/26
 */
public interface ICCreateTime {

    /**
     * 获取创建时间
     * @return 创建时间
     */
    @ApiModelProperty("创建时间")
    Date getCreateTime();

    /**
     * 设置创建时间
     * @param createTime 创建时间
     */
    void setCreateTime(Date createTime);

}
