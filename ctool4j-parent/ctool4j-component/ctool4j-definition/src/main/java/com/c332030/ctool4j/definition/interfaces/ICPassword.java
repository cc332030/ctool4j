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

    /**
     * 获取密码
     * @return 密码
     */
    @ApiModelProperty("密码")
    String getPassword();

    /**
     * 设置密码
     * @param password 密码
     */
    void setPassword(String password);

}
