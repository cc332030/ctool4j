package com.c332030.ctool4j.mybatis.model.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.c332030.ctool4j.mybatis.model.ICPage;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

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
public class CPageReq<T> implements ICPage {

    Integer pageNum;

    Integer pageSize;

    List<OrderItem> orders;

    @ApiModelProperty(value = "查询参数")
    T req;

}
