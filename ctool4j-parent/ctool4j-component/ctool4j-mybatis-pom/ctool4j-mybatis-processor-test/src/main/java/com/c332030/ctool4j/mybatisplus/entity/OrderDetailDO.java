package com.c332030.ctool4j.mybatisplus.entity;

import com.c332030.ctool4j.definition.annotation.CBizId;
import com.c332030.ctool4j.definition.entity.base.CBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: OrderDetailDO
 * </p>
 *
 * @since 2026/5/17
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDO extends CBaseEntity<Long> implements IOrderNo {

    @CBizId
    String orderNo;

}
