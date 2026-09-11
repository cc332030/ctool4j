package com.c332030.ctool4j.redis.service.impl;

import cn.hutool.core.lang.Opt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * <p>
 * Description: CObjectValueRedisServiceTests
 * </p>
 *
 * <p>
 * 是 {@link CObjectValueRedisService} 的测试用例。
 * </p>
 * <h2>设计思路</h2>
 * <ul>
 *   <li>通过 mock RedisTemplate/ValueOperations 隔离外部 Redis，覆盖 getValue/setValue 的短路与正常路径、</li>
 *   <li>getValueOpt、getValueForGenericType。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 isInvalidKey（null 短路）、getValue/setValue、getValueForGenericType 的约定。</li>
 *   <li>依据白盒/黑盒原则：null key 短路、有效 key 读写、Optional 包装、泛型转换均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getValue null key 短路、有效 key 返回值、getValueOpt 空/有值、setValue null key 短路、</li>
 *   <li>setValue 正常调用、getValueForGenericType 转换。</li>
 *   <li>未覆盖：真实 Redis 读写（依赖环境）。</li>
 * </ul>
 * <h2>getValue</h2>
 * <ul>
 *   <li>1.1 null key 短路返回 null（getValue_nullKey_shortCircuit）</li>
 *   <li>1.2 有效 key 返回值（getValue_validKey_returnsValue）</li>
 *   <li>1.3 getValueOpt 空值（getValueOpt_empty）</li>
 *   <li>1.4 getValueOpt 有值（getValueOpt_present）</li>
 * </ul>
 * <h2>setValue</h2>
 * <ul>
 *   <li>2.1 null key 短路（setValue_nullKey_shortCircuit）</li>
 *   <li>2.2 有效值调用 opsForValue().set（setValue_valid_callsValueOpsSet）</li>
 * </ul>
 * <h2>泛型转换</h2>
 * <ul>
 *   <li>3.1 getValueForGenericType 类型转换（getValueForGenericType_converts）</li>
 * </ul>
 *
 * <p>被测依赖类（异常 / 序列化器 / 日志 / 服务 / 切面 / 拦截器等）无 builder，测试按常规直接 new 构造——属规范允许的取舍，依据与边界在此记录。</p>
 *
 * @since 1.0
 * @version 1.0
 */
public class CObjectValueRedisServiceTests {

    private RedisTemplate<? super String, Object> redisTemplate;
    private ValueOperations<Object, Object> valueOps;
    private CObjectValueRedisService service;

    /**
     * 每个用例执行前的准备
     */
    @BeforeEach
    public void setUp() {
        redisTemplate = Mockito.mock(RedisTemplate.class);
        valueOps = Mockito.mock(ValueOperations.class);
        Mockito.doReturn(valueOps).when(redisTemplate).opsForValue();

        service = new CObjectValueRedisService(redisTemplate);
    }

    /**
     * 对应测试用例 1.1：null key 短路返回 null
     */
    @Test
    void getValue_nullKey_shortCircuit() {
        Assertions.assertNull(service.getValue(null));

        Mockito.verify(redisTemplate, Mockito.never()).opsForValue();
    }

    /**
     * 对应测试用例 1.2：有效 key 返回值
     */
    @Test
    void getValue_validKey_returnsValue() {
        Object expected = new Object();
        Mockito.when(valueOps.get("key")).thenReturn(expected);

        Object result = service.getValue("key");

        Assertions.assertSame(expected, result);
        Mockito.verify(valueOps).get("key");
    }

    /**
     * 对应测试用例 1.3：getValueOpt 空值
     */
    @Test
    void getValueOpt_empty() {
        Mockito.when(valueOps.get("missing")).thenReturn(null);

        Opt<Object> opt = service.getValueOpt("missing");

        Assertions.assertTrue(opt.isEmpty());
    }

    /**
     * 对应测试用例 1.4：getValueOpt 有值
     */
    @Test
    void getValueOpt_present() {
        Object expected = "value";
        Mockito.when(valueOps.get("key")).thenReturn(expected);

        Opt<Object> opt = service.getValueOpt("key");

        Assertions.assertTrue(opt.isPresent());
        Assertions.assertSame(expected, opt.get());
    }

    /**
     * 对应测试用例 2.1：null key 短路
     */
    @Test
    void setValue_nullKey_shortCircuit() {
        service.setValue(null, "value");
        service.setValue("key", null);

        Mockito.verify(valueOps, Mockito.never()).set(Mockito.any(), Mockito.any());
    }

    /**
     * 对应测试用例 2.2：有效值调用 opsForValue().set
     */
    @Test
    void setValue_valid_callsValueOpsSet() {
        service.setValue("key", "value");

        Mockito.verify(valueOps).set("key", "value");
    }

    /**
     * 对应测试用例 3.1：getValueForGenericType 类型转换
     */
    @Test
    void getValueForGenericType_converts() {
        Mockito.when(valueOps.get("key")).thenReturn(123);

        Integer result = service.getValueForGenericType("key");

        Assertions.assertEquals(Integer.valueOf(123), result);
    }

}
