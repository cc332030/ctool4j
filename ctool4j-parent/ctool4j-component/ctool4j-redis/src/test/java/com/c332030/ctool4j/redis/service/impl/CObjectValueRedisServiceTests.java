package com.c332030.ctool4j.redis.service.impl;

import cn.hutool.core.lang.Opt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class CObjectValueRedisServiceTests {

    private RedisTemplate<? super String, Object> redisTemplate;
    private ValueOperations<Object, Object> valueOps;
    private CObjectValueRedisService service;

    @BeforeEach
    public void setUp() {
        redisTemplate = Mockito.mock(RedisTemplate.class);
        valueOps = Mockito.mock(ValueOperations.class);
        Mockito.doReturn(valueOps).when(redisTemplate).opsForValue();

        service = new CObjectValueRedisService(redisTemplate);
    }

    @Test
    public void getValue_nullKey_shortCircuit() {
        Assertions.assertNull(service.getValue(null));

        Mockito.verify(redisTemplate, Mockito.never()).opsForValue();
    }

    @Test
    public void getValue_validKey_returnsValue() {
        Object expected = new Object();
        Mockito.when(valueOps.get("key")).thenReturn(expected);

        Object result = service.getValue("key");

        Assertions.assertSame(expected, result);
        Mockito.verify(valueOps).get("key");
    }

    @Test
    public void getValueOpt_empty() {
        Mockito.when(valueOps.get("missing")).thenReturn(null);

        Opt<Object> opt = service.getValueOpt("missing");

        Assertions.assertTrue(opt.isEmpty());
    }

    @Test
    public void getValueOpt_present() {
        Object expected = "value";
        Mockito.when(valueOps.get("key")).thenReturn(expected);

        Opt<Object> opt = service.getValueOpt("key");

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertSame(expected, opt.get());
    }

    @Test
    public void setValue_nullKey_shortCircuit() {
        service.setValue(null, "value");
        service.setValue("key", null);

        Mockito.verify(valueOps, Mockito.never()).set(Mockito.any(), Mockito.any());
    }

    @Test
    public void setValue_valid_callsValueOpsSet() {
        service.setValue("key", "value");

        Mockito.verify(valueOps).set("key", "value");
    }

    @Test
    public void getValueForGenericType_converts() {
        Mockito.when(valueOps.get("key")).thenReturn(123);

        Integer result = service.getValueForGenericType("key");

        Assertions.assertEquals(Integer.valueOf(123), result);
    }

}
