package com.c332030.ctool4j.mybatisplus.processor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * Description: AutoBizServiceProcessor 测试
 * </p>
 *
 * @since 2025/05/16
 */
class AutoBizServiceProcessorTest {

    @Test
    void testOrderEntityHasAnnotation() {
        Order order = new Order();
        
        assertTrue(order.getClass().isAnnotationPresent(AutoBizService.class));
        
        AutoBizService anno = order.getClass().getAnnotation(AutoBizService.class);
        assertEquals("orderNo", anno.bizIdField());
        assertEquals("order_no", anno.bizIdColumn());
        assertEquals("Service", anno.serviceSuffix());
        assertFalse(anno.generateImpl());
    }

    @Test
    void testVoucherEntityHasAnnotation() {
        Voucher voucher = new Voucher();
        
        assertTrue(voucher.getClass().isAnnotationPresent(AutoBizService.class));
        
        AutoBizService anno = voucher.getClass().getAnnotation(AutoBizService.class);
        assertEquals("voucherNo", anno.bizIdField());
        assertEquals("voucher_no", anno.bizIdColumn());
        assertEquals("Service", anno.serviceSuffix());
        assertTrue(anno.generateImpl());
    }

    @Test
    void testOrderFields() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD20250516001");
        order.setProductName("Test Product");
        order.setQuantity(10);
        
        assertEquals(1L, order.getId());
        assertEquals("ORD20250516001", order.getOrderNo());
        assertEquals("Test Product", order.getProductName());
        assertEquals(10, order.getQuantity());
    }

    @Test
    void testVoucherFields() {
        Voucher voucher = new Voucher();
        voucher.setId(1L);
        voucher.setVoucherNo("VCH20250516001");
        voucher.setVoucherName("Test Voucher");
        voucher.setAmount(1000);
        
        assertEquals(1L, voucher.getId());
        assertEquals("VCH20250516001", voucher.getVoucherNo());
        assertEquals("Test Voucher", voucher.getVoucherName());
        assertEquals(1000, voucher.getAmount());
    }

}
