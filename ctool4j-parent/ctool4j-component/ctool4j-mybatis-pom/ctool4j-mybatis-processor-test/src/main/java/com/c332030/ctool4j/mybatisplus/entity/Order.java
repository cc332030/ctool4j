package com.c332030.ctool4j.mybatisplus.entity;

import com.c332030.ctool4j.definition.entity.base.CBaseEntity;
import com.c332030.ctool4j.mybatisplus.processor.AutoBizService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: Order 测试实体
 * </p>
 *
 * @since 2025/05/16
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@AutoBizService(bizIdField = "orderNo", bizIdColumn = "order_no")
public class Order extends CBaseEntity<Long> {

    private String orderNo;

    private String productName;

    private Integer quantity;

}
