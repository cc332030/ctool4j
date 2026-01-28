package com.c332030.ctool4j.definition.interfaces;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * Description: ICPassword
 * </p>
 *
 * @since 2026/1/24
 */
public interface ICPassword {

    @ApiModelProperty("用户名")
    String getPassword();

    void setPassword(String password);

}
