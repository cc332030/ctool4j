package com.c332030.ctool4j.mybatisplus.service;

import com.c332030.ctool4j.mybatisplus.entity.OrderDetailDO;
import com.c332030.ctool4j.mybatisplus.mapper.OrderDetailMapper;
import com.c332030.ctool4j.mybatisplus.service.impl.CServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: OrderDetailService
 * </p>
 *
 * @author c332030
 * @since 2026/5/17
 */
@Service
public class OrderDetailService
        extends CServiceImpl<OrderDetailMapper, OrderDetailDO>
        implements IOrderNoService<OrderDetailDO> {

}
