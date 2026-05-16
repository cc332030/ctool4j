package com.c332030.ctool4j.mybatisplus.processor;

import com.c332030.ctool4j.definition.entity.base.CBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Description: Order 测试实体
 * </p>
 *
 * @since 2025/05/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoBizService(bizIdField = "orderNo", bizIdColumn = "order_no")
public class Order extends CBaseEntity<Long> {

    private String orderNo;

    private String productName;

    private Integer quantity;
}
