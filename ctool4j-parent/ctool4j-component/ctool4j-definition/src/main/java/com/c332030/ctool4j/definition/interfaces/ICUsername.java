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

    /**
     * 获取用户名
     * @return 用户名
     */
    @ApiModelProperty("用户名")
    String getUsername();

    /**
     * 设置用户名
     * @param username 用户名
     */
    void setUsername(String username);

}
