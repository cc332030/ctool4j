package com.c332030.ctool4j.redis.service.impl;

import cn.hutool.core.lang.Opt;
import com.c332030.ctool4j.redis.model.CValueWithTtl;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: CStringStringRedisServiceTests
 * </p>
 *
 * <p>
 * 是 {@link CStringStringRedisService} 的测试用例（对应测试文档
 * <code>doc/design/redis/CStringStringRedisServiceTests.adoc</code>）。
 * </p>
 */
public class CStringStringRedisServiceTests {

    private RedisTemplate<String, String> redisTemplate;
    private ValueOperations<String, String> valueOps;
    private CStringStringRedisService service;

    @BeforeEach
    public void setUp() {
        redisTemplate = Mockito.mock(RedisTemplate.class);
        valueOps = Mockito.mock(ValueOperations.class);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service = new CStringStringRedisService();
        service.setRedisTemplate(redisTemplate);
    }

    /** 对应测试用例 1.1 */
    @Test
    void isInvalidKey_blank_isInvalid() {
        Assertions.assertTrue(service.isInvalidKey(null));
        Assertions.assertTrue(service.isInvalidKey(""));
        Assertions.assertTrue(service.isInvalidKey(" "));
        Assertions.assertFalse(service.isInvalidKey("key"));
    }

    /** 对应测试用例 2.1 */
    @Test
    void setValue_blankKey_shortCircuit() {
        service.setValue("", "value");

        Mockito.verify(valueOps, Mockito.never()).set(Mockito.any(), Mockito.any());
    }

    /** 对应测试用例 2.2 */
    @Test
    void setValue_nullValue_shortCircuit() {
        service.setValue("key", null);

        Mockito.verify(valueOps, Mockito.never()).set(Mockito.any(), Mockito.any());
    }

    /** 对应测试用例 2.3 */
    @Test
    void setValue_normal_serializesToJson() {
        service.setValue("key", new TestUser("c332030"));

        Mockito.verify(valueOps).set("key", "{\"name\":\"c332030\"}");
    }

    /** 对应测试用例 2.4 */
    @Test
    void setValue_timeout_nonPositive_shortCircuit() {
        service.setValue("key", new TestUser("c332030"), 0, TimeUnit.SECONDS);

        Mockito.verify(valueOps, Mockito.never()).set(Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.any());
    }

    /** 对应测试用例 2.5 */
    @Test
    void setValue_timeout_positive_serializesToJson() {
        service.setValue("key", new TestUser("c332030"), 10L, TimeUnit.SECONDS);

        Mockito.verify(valueOps).set("key", "{\"name\":\"c332030\"}", 10L, TimeUnit.SECONDS);
    }

    /** 对应测试用例 2.6 */
    @Test
    void setValue_duration_serializesToJson() {
        service.setValue("key", new TestUser("c332030"), Duration.ofSeconds(10));

        Mockito.verify(valueOps).set("key", "{\"name\":\"c332030\"}", Duration.ofSeconds(10));
    }

    /** 对应测试用例 3.1 */
    @Test
    void getValue_invalidKey_returnsDefault() {
        TestUser defaultValue = new TestUser("default");

        TestUser result = service.getValue("", TestUser.class, defaultValue);

        Assertions.assertSame(defaultValue, result);
        Mockito.verify(valueOps, Mockito.never()).get(Mockito.any());
    }

    /** 对应测试用例 3.2 */
    @Test
    void getValue_normal_returnsDeserialized() {
        Mockito.when(valueOps.get("key")).thenReturn("{\"name\":\"c332030\"}");

        TestUser result = service.getValue("key", TestUser.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("c332030", result.getName());
    }

    /** 对应测试用例 3.3 */
    @Test
    void getValue_typeReference_normal_returnsDeserialized() {
        Mockito.when(valueOps.get("key")).thenReturn("{\"name\":\"c332030\"}");

        TestUser result = service.getValue("key", new TypeReference<TestUser>() {});

        Assertions.assertNotNull(result);
        Assertions.assertEquals("c332030", result.getName());
    }

    /** 对应测试用例 4.1 */
    @Test
    void getValueOpt_invalidKey_empty() {
        Opt<String> opt = service.getValueOpt("");

        Assertions.assertTrue(opt.isEmpty());
    }

    /** 对应测试用例 4.2 */
    @Test
    void getValueOpt_validKey_present() {
        Mockito.when(valueOps.get("key")).thenReturn("value");

        Opt<String> opt = service.getValueOpt("key");

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals("value", opt.get());
    }

    /** 对应测试用例 5.1 */
    @Test
    void getValueWithTtl_delegatesToRedisCallback() {
        CValueWithTtl<String> expected = new CValueWithTtl<>("value", 100L);
        Mockito.when(redisTemplate.execute(Mockito.any(RedisCallback.class))).thenReturn(expected);

        CValueWithTtl<String> result = service.getValueWithTtl("key", String.class);

        Assertions.assertSame(expected, result);
        Mockito.verify(redisTemplate).execute(Mockito.any(RedisCallback.class));
    }

    /**
     * 测试辅助对象：Jackson 序列化/反序列化需要默认构造器与 getter/setter
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestUser {

        private String name;

    }

}
