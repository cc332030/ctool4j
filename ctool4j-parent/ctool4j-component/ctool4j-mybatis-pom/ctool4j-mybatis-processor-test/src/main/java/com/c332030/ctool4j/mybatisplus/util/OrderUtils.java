package com.c332030.ctool4j.mybatisplus.util;

import com.c332030.ctool4j.mybatisplus.service.IOrderService;

/**
 * <p>
 * Description: OrderUtils
 * </p>
 *
 * @author c332030
 * @since 2026/5/17
 */
public class OrderUtils {

    IOrderService orderService;

    public void test(){

        orderService.getByOrderNo("123");

    }

}
