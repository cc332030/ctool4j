package com.c332030.ctool4j.mybatisplus.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: OrderBizService
 * </p>
 *
 * @author c332030
 * @since 2026/5/17
 */
@Service
@AllArgsConstructor
public class OrderBizService {

    OrderService orderService;

    /**
     * 调用 OrderService 按业务单号查询（处理器测试样例方法）
     */
    public void getByOrderNo(){

        orderService.getByOrderNo("123");

    }

}
