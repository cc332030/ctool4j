package com.c332030.ctool4j.definition.interfaces;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * Description: ICUsername
 * </p>
 *
 * @since 2026/1/24
 */
public interface ICUsername {

    @ApiModelProperty("用户名")
    String getUsername();

    void setUsername(String username);

}
