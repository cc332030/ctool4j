package com.c332030.ctool4j.mybatisplus.service;

import com.c332030.ctool4j.mybatisplus.entity.IOrderNo;
import com.c332030.ctool4j.mybatisplus.entity.OrderDO;
import com.c332030.ctool4j.mybatisplus.mapper.OrderMapper;
import com.c332030.ctool4j.mybatisplus.service.impl.CServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: OrderService
 * </p>
 *
 * @author c332030
 * @since 2026/5/17
 */
@Service
public class OrderService extends CServiceImpl<OrderMapper, OrderDO> implements IOrderService<IOrderNo> {

}
