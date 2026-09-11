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
 * 是 {@link CStringStringRedisService} 的测试用例。
 * </p>
 * <h2>设计思路</h2>
 * <ul>
 *   <li>通过 mock RedisTemplate/ValueOperations 隔离外部 Redis，覆盖 isInvalidKey、setValue（各形态短路/JSON 序列化）、</li>
 *   <li>getValue（默认值/反序列化/泛型）、getValueOpt、getValueWithTtl。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对空白 key/value 无效、对象 JSON 序列化存储、无效返回默认值、getValueWithTtl 管道读的约定。</li>
 *   <li>依据白盒/黑盒原则：key/value 空白/null、timeout 非正数、JSON 序列化/反序列化、泛型/Class 读取均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：isInvalidKey 空白、setValue 空白 key/null 值/正常序列化/超时非正数/带超时/带 Duration、</li>
 *   <li>getValue 无效默认/正常反序列化/泛型、getValueOpt、getValueWithTtl 委托回调。</li>
 *   <li>未覆盖：真实 Redis 读写（依赖环境）。</li>
 * </ul>
 * <h2>key/value 有效性</h2>
 * <ul>
 *   <li>1.1 空白 key 视为无效（isInvalidKey_blank_isInvalid）</li>
 * </ul>
 * <h2>setValue</h2>
 * <ul>
 *   <li>2.1 空白 key 短路（setValue_blankKey_shortCircuit）</li>
 *   <li>2.2 null 值短路（setValue_nullValue_shortCircuit）</li>
 *   <li>2.3 正常值序列化为 JSON（setValue_normal_serializesToJson）</li>
 *   <li>2.4 超时非正数短路（setValue_timeout_nonPositive_shortCircuit）</li>
 *   <li>2.5 带正超时序列化为 JSON（setValue_timeout_positive_serializesToJson）</li>
 *   <li>2.6 带 Duration 序列化为 JSON（setValue_duration_serializesToJson）</li>
 * </ul>
 * <h2>getValue</h2>
 * <ul>
 *   <li>3.1 无效 key 返回默认值（getValue_invalidKey_returnsDefault）</li>
 *   <li>3.2 正常反序列化（getValue_normal_returnsDeserialized）</li>
 *   <li>3.3 泛型反序列化（getValue_typeReference_normal_returnsDeserialized）</li>
 * </ul>
 * <h2>getValueOpt</h2>
 * <ul>
 *   <li>4.1 无效 key 返回空 Opt（getValueOpt_invalidKey_empty）</li>
 *   <li>4.2 有效 key 返回有值 Opt（getValueOpt_validKey_present）</li>
 * </ul>
 * <h2>getValueWithTtl</h2>
 * <ul>
 *   <li>5.1 委托 RedisCallback 读取（getValueWithTtl_delegatesToRedisCallback）</li>
 * </ul>
 *
 * @since 1.0
 * @version 1.0
 */
public class CStringStringRedisServiceTests {

    private RedisTemplate<String, String> redisTemplate;
    private ValueOperations<String, String> valueOps;
    private CStringStringRedisService service;

    /**
     * 每个用例执行前的准备
     */
    @BeforeEach
    public void setUp() {
        redisTemplate = Mockito.mock(RedisTemplate.class);
        valueOps = Mockito.mock(ValueOperations.class);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service = new CStringStringRedisService();
        service.setRedisTemplate(redisTemplate);
    }

    /**
     * 对应测试用例 1.1：空白 key 视为无效
     */
    @Test
    void isInvalidKey_blank_isInvalid() {
        Assertions.assertTrue(service.isInvalidKey(null));
        Assertions.assertTrue(service.isInvalidKey(""));
        Assertions.assertTrue(service.isInvalidKey(" "));
        Assertions.assertFalse(service.isInvalidKey("key"));
    }

    /**
     * 对应测试用例 2.1：空白 key 短路
     */
    @Test
    void setValue_blankKey_shortCircuit() {
        service.setValue("", "value");

        Mockito.verify(valueOps, Mockito.never()).set(Mockito.any(), Mockito.any());
    }

    /**
     * 对应测试用例 2.2：null 值短路
     */
    @Test
    void setValue_nullValue_shortCircuit() {
        service.setValue("key", null);

        Mockito.verify(valueOps, Mockito.never()).set(Mockito.any(), Mockito.any());
    }

    /**
     * 对应测试用例 2.3：正常值序列化为 JSON
     */
    @Test
    void setValue_normal_serializesToJson() {
        service.setValue("key", new TestUser("c332030"));

        Mockito.verify(valueOps).set("key", "{\"name\":\"c332030\"}");
    }

    /**
     * 对应测试用例 2.4：超时非正数短路
     */
    @Test
    void setValue_timeout_nonPositive_shortCircuit() {
        service.setValue("key", new TestUser("c332030"), 0, TimeUnit.SECONDS);

        Mockito.verify(valueOps, Mockito.never()).set(Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.any());
    }

    /**
     * 对应测试用例 2.5：带正超时序列化为 JSON
     */
    @Test
    void setValue_timeout_positive_serializesToJson() {
        service.setValue("key", new TestUser("c332030"), 10L, TimeUnit.SECONDS);

        Mockito.verify(valueOps).set("key", "{\"name\":\"c332030\"}", 10L, TimeUnit.SECONDS);
    }

    /**
     * 对应测试用例 2.6：带 Duration 序列化为 JSON
     */
    @Test
    void setValue_duration_serializesToJson() {
        service.setValue("key", new TestUser("c332030"), Duration.ofSeconds(10));

        Mockito.verify(valueOps).set("key", "{\"name\":\"c332030\"}", Duration.ofSeconds(10));
    }

    /**
     * 对应测试用例 3.1：无效 key 返回默认值
     */
    @Test
    void getValue_invalidKey_returnsDefault() {
        TestUser defaultValue = new TestUser("default");

        TestUser result = service.getValue("", TestUser.class, defaultValue);

        Assertions.assertSame(defaultValue, result);
        Mockito.verify(valueOps, Mockito.never()).get(Mockito.any());
    }

    /**
     * 对应测试用例 3.2：正常反序列化
     */
    @Test
    void getValue_normal_returnsDeserialized() {
        Mockito.when(valueOps.get("key")).thenReturn("{\"name\":\"c332030\"}");

        TestUser result = service.getValue("key", TestUser.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("c332030", result.getName());
    }

    /**
     * 对应测试用例 3.3：泛型反序列化
     */
    @Test
    void getValue_typeReference_normal_returnsDeserialized() {
        Mockito.when(valueOps.get("key")).thenReturn("{\"name\":\"c332030\"}");

        TestUser result = service.getValue("key", new TypeReference<TestUser>() {});

        Assertions.assertNotNull(result);
        Assertions.assertEquals("c332030", result.getName());
    }

    /**
     * 对应测试用例 4.1：无效 key 返回空 Opt
     */
    @Test
    void getValueOpt_invalidKey_empty() {
        Opt<String> opt = service.getValueOpt("");

        Assertions.assertTrue(opt.isEmpty());
    }

    /**
     * 对应测试用例 4.2：有效 key 返回有值 Opt
     */
    @Test
    void getValueOpt_validKey_present() {
        Mockito.when(valueOps.get("key")).thenReturn("value");

        Opt<String> opt = service.getValueOpt("key");

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals("value", opt.get());
    }

    /**
     * 对应测试用例 5.1：委托 RedisCallback 读取
     */
    @Test
    void getValueWithTtl_delegatesToRedisCallback() {
        CValueWithTtl<String> expected = CValueWithTtl.<String>builder()
            .value("value")
            .ttl(100L)
            .build();
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
