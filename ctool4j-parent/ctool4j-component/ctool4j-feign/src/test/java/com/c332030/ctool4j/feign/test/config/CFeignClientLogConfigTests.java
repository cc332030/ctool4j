package com.c332030.ctool4j.feign.test.config;

import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * <p>
 * Description: CFeignClientLogConfigTests
 * </p>
 *
 * <p>
 * 是 {@link CFeignClientLogConfig} 的测试用例。
 * </p>
 * <h2>用例设计思路与依据</h2>
 * <ul>
 *   <li>验证 lombok @Data 生成的默认值、setter/getter、toString，以及白名单集合的读写与默认不可变性。</li>
 *   <li>enable/enableHeader 继承自 {@code CRequestLogBaseConfig}，getter/setter/toString 经 {@code @ToString(callSuper=true)} 覆盖父类字段。</li>
 * </ul>
 * <h2>默认值与访问器</h2>
 * <ul>
 *   <li>1.1 默认值（defaultValues）</li>
 *   <li>1.2 setter/getter（settersAndGetters）</li>
 * </ul>
 * <h2>白名单集合</h2>
 * <ul>
 *   <li>2.1 apiWhiteList 可替换可读（apiWhiteList_replaceReadable）</li>
 *   <li>2.2 apiWhiteList 默认不可变（apiWhiteList_defaultImmutable）</li>
 * </ul>
 * <h2>toString</h2>
 * <ul>
 *   <li>3.1 toString 含字段（toString_containsFields）</li>
 * </ul>
 *
 * @since 1.0
 * @version 1.0
 */
public class CFeignClientLogConfigTests {

    /**
     * 对应测试用例 1.1：默认值
     */
    @Test
    void defaultValues() {
        CFeignClientLogConfig config = new CFeignClientLogConfig();

        Assertions.assertFalse(config.getEnable());
        Assertions.assertFalse(config.getEnableHeader());
        Assertions.assertFalse(config.getLogAll());
        Assertions.assertTrue(config.getApiWhiteList().isEmpty());
        Assertions.assertTrue(config.getApiBlackList().isEmpty());
    }

    /**
     * 对应测试用例 1.2：setter/getter
     */
    @Test
    void settersAndGetters() {
        CFeignClientLogConfig config = new CFeignClientLogConfig();

        config.setEnable(true);
        config.setEnableHeader(true);
        config.setLogAll(true);

        Assertions.assertTrue(config.getEnable());
        Assertions.assertTrue(config.getEnableHeader());
        Assertions.assertTrue(config.getLogAll());
    }

    /**
     * 对应测试用例 2.1：apiWhiteList 可替换可读
     */
    @Test
    void apiWhiteList_replaceReadable() {
        CFeignClientLogConfig config = new CFeignClientLogConfig();

        config.setApiWhiteList(new HashSet<>(Arrays.asList("api1", "api2")));

        Assertions.assertEquals(2, config.getApiWhiteList().size());
        Assertions.assertTrue(config.getApiWhiteList().contains("api1"));
        Assertions.assertTrue(config.getApiWhiteList().contains("api2"));
    }

    /**
     * 对应测试用例 2.2：apiWhiteList 默认不可变
     */
    @Test
    void apiWhiteList_defaultImmutable() {
        CFeignClientLogConfig config = new CFeignClientLogConfig();

        Assertions.assertThrowsExactly(
            UnsupportedOperationException.class,
            () -> config.getApiWhiteList().add("api3"));
    }

    /**
     * 对应测试用例 3.1：toString 含字段
     */
    @Test
    void toString_containsFields() {
        CFeignClientLogConfig config = new CFeignClientLogConfig();
        config.setEnable(true);

        String str = config.toString();

        Assertions.assertTrue(str.contains("enable=true"));
    }

}
