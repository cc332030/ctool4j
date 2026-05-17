package com.c332030.ctool4j.mybatisplus.mapper;

import com.c332030.ctool4j.definition.annotation.CBizId;
import com.c332030.ctool4j.mybatisplus.entity.OrderDO;
import com.c332030.ctool4j.mybatisplus.entity.OrderDetailDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Description: OrderMapper
 * </p>
 *
 * @author c332030
 * @since 2026/5/17
 */
@Mapper
public interface OrderDetailMapper extends CBaseMapper<OrderDetailDO> {

}
