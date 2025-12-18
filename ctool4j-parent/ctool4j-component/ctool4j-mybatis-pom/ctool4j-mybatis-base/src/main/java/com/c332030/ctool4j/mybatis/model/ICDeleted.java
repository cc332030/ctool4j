package com.c332030.ctool4j.mybatis.model;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * Description: ICDeleted
 * </p>
 *
 * @since 2025/12/16
 */
public interface ICDeleted {

    @ApiModelProperty("删除标识")
    Boolean getDeleted();

    void setDeleted(Boolean deleted);

}
