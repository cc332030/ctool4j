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
 * 是 {@link CFeignClientLogConfig} 的测试用例（对应测试文档
 * <code>doc/design/feign/CFeignClientLogConfigTests.adoc</code>）。
 * </p>
 */
public class CFeignClientLogConfigTests {

    /** 对应测试用例 1.1 */
    @Test
    void defaultValues() {
        CFeignClientLogConfig config = new CFeignClientLogConfig();

        Assertions.assertFalse(config.getEnable());
        Assertions.assertFalse(config.getEnableHeader());
        Assertions.assertTrue(config.getEnableCost());
        Assertions.assertFalse(config.getLogAll());
        Assertions.assertTrue(config.getApiWhiteList().isEmpty());
        Assertions.assertTrue(config.getApiBlackList().isEmpty());
    }

    /** 对应测试用例 1.2 */
    @Test
    void settersAndGetters() {
        CFeignClientLogConfig config = new CFeignClientLogConfig();

        config.setEnable(true);
        config.setEnableHeader(true);
        config.setEnableCost(false);
        config.setLogAll(true);

        Assertions.assertTrue(config.getEnable());
        Assertions.assertTrue(config.getEnableHeader());
        Assertions.assertFalse(config.getEnableCost());
        Assertions.assertTrue(config.getLogAll());
    }

    /** 对应测试用例 2.1 */
    @Test
    void apiWhiteList_replaceReadable() {
        CFeignClientLogConfig config = new CFeignClientLogConfig();

        config.setApiWhiteList(new HashSet<>(Arrays.asList("api1", "api2")));

        Assertions.assertEquals(2, config.getApiWhiteList().size());
        Assertions.assertTrue(config.getApiWhiteList().contains("api1"));
        Assertions.assertTrue(config.getApiWhiteList().contains("api2"));
    }

    /** 对应测试用例 2.2 */
    @Test
    void apiWhiteList_defaultImmutable() {
        CFeignClientLogConfig config = new CFeignClientLogConfig();

        Assertions.assertThrowsExactly(
            UnsupportedOperationException.class,
            () -> config.getApiWhiteList().add("api3"));
    }

    /** 对应测试用例 3.1 */
    @Test
    void toString_containsFields() {
        CFeignClientLogConfig config = new CFeignClientLogConfig();
        config.setEnable(true);

        String str = config.toString();

        Assertions.assertTrue(str.contains("enable=true"));
        Assertions.assertTrue(str.contains("enableCost"));
    }

}
