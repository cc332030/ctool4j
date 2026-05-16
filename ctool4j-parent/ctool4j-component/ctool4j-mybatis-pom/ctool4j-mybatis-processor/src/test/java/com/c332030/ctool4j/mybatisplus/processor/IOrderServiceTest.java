package com.c332030.ctool4j.mybatisplus.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * Description: IOrderService 测试
 * </p>
 *
 * @since 2025/05/16
 */
class IOrderServiceTest {

    private MockOrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new MockOrderService();
    }

    @Test
    void testGetByOrderNo() {
        Order order = orderService.getByOrderNo("ORD001");
        assertNotNull(order);
        assertEquals("ORD001", order.getOrderNo());
    }

    @Test
    void testGetByOrderNoNotFound() {
        Order order = orderService.getByOrderNo("NOT_EXIST");
        assertNull(order);
    }

    @Test
    void testListByOrderNo() {
        List<Order> orders = orderService.listByOrderNo("ORD001");
        assertNotNull(orders);
        assertEquals(1, orders.size());
    }

    @Test
    void testCountByOrderNo() {
        Long count = orderService.countByOrderNo("ORD001");
        assertEquals(1L, count);
    }

    @Test
    void testListByOrderNos() {
        List<Order> orders = orderService.listByOrderNos(Arrays.asList("ORD001", "ORD002"));
        assertNotNull(orders);
        assertEquals(2, orders.size());
    }

    @Test
    void testListMapByOrderNos() {
        Map<String, Order> map = orderService.listMapByOrderNos(Arrays.asList("ORD001", "ORD002"));
        assertNotNull(map);
        assertEquals(2, map.size());
        assertTrue(map.containsKey("ORD001"));
        assertTrue(map.containsKey("ORD002"));
    }

    private static class MockOrderService implements IOrderService<Order> {

        @Override
        public String getOrderNo(Order entity) {
            return entity.getOrderNo();
        }

        @Override
        public com.baomidou.mybatisplus.core.toolkit.support.SFunction<Order, String> getOrderNoColumn() {
            return Order::getOrderNo;
        }

        @Override
        public String getBizId(Order biz) {
            return getOrderNo(biz);
        }

        @Override
        public com.baomidou.mybatisplus.core.toolkit.support.SFunction<Order, String> getBizIdColumn() {
            return getOrderNoColumn();
        }

        @Override
        public Order getByBizId(String bizId) {
            if ("ORD001".equals(bizId)) {
                Order order = new Order();
                order.setOrderNo("ORD001");
                order.setProductName("Product 1");
                return order;
            }
            if ("ORD002".equals(bizId)) {
                Order order = new Order();
                order.setOrderNo("ORD002");
                order.setProductName("Product 2");
                return order;
            }
            return null;
        }

        @Override
        public List<Order> listByBizIds(java.util.Collection<String> bizIds) {
            return Arrays.asList(getByBizId("ORD001"), getByBizId("ORD002"));
        }

        @Override
        public Long countByBizId(String bizId) {
            return getByBizId(bizId) != null ? 1L : 0L;
        }

        @Override
        public java.util.Map<String, Order> listMapByBizIds(java.util.Collection<String> bizIds) {
            java.util.Map<String, Order> map = new java.util.HashMap<>();
            for (String bizId : bizIds) {
                Order order = getByBizId(bizId);
                if (order != null) {
                    map.put(bizId, order);
                }
            }
            return map;
        }

        @Override
        public boolean removeByBizId(String bizId) {
            return true;
        }

        @Override
        public boolean removeByBizIds(java.util.Collection<String> bizIds) {
            return true;
        }

        @Override
        public Long countByBizIds(java.util.Collection<String> bizIds) {
            return (long) bizIds.size();
        }

        @Override
        public List<Order> listByBizId(String bizId) {
            Order order = getByBizId(bizId);
            return order != null ? Arrays.asList(order) : Arrays.asList();
        }
    }

}
