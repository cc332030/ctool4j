package com.c332030.ctool4j.mybatis.model.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CPageUtils;
import com.c332030.ctool4j.mybatis.model.ICPage;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

    @Builder.Default
    Integer pageNum = 1;

    @Builder.Default
    Integer pageSize = CPageUtils.DEFAULT_PAGE_SIZE;

    @Builder.Default
    List<OrderItem> orders = CList.of();

    @ApiModelProperty(value = "查询参数")
    T req;

}
