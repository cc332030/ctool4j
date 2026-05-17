package com.c332030.ctool4j.mybatisplus.entity;

import com.c332030.ctool4j.definition.annotation.CBizId;
import com.c332030.ctool4j.definition.entity.base.CBaseEntity;
import com.c332030.ctool4j.mybatisplus.processor.CAutoBizService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: OrderDO 测试实体
 * </p>
 *
 * @since 2025/05/16
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDO extends CBaseEntity<Long> implements IOrderNo {

    @CBizId
    String orderNo;

    String productName;

    Integer quantity;

}
