package com.c332030.ctool4j.mybatis.model.impl;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CPageReq
 * </p>
 *
 * @since 2026/1/20
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CPageReq<T> extends CPage{

    @ApiModelProperty(value = "查询参数")
    T req;

}
