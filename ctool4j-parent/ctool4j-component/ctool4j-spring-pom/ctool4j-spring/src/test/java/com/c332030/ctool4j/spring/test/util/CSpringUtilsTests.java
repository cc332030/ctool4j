package com.c332030.ctool4j.spring.test.util;

import com.c332030.ctool4j.core.enums.CProfileEnum;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import com.c332030.ctool4j.spring.util.CSpringUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CSpringUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证激活 profile 相关的各条路径与边界。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对激活 profile 相关的约定。</li>
 *   <li>依据测试方法（等价类/边界/分支覆盖）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：激活 profile 相关的正常、边界与异常路径。</li>
 *   <li>未覆盖：真实容器/框架集成场景。</li>
 * </ul>
 * <h2>Spring 工具</h2>
 * <ul>
 *   <li>1.1 验证激活 profile 相关（对应测试方法 1.1-1.7）</li>
 * </ul>
 *
 *
 * @since 2026/6/2
 * @version 1.0
 */
@CTool4jSpringBootTest
public class CSpringUtilsTests {

    private static final CProfileEnum PROFILE = CProfileEnum.DEV;

    private static final String CLASS_NAME = CSpringUtilsTests.class.getSimpleName();

    /**
     * 测试获取当前激活环境
     */
        /**
         * 对应测试用例 1.1：验证激活 profile 相关（对应测试方法 1.1-1.7）
         */
    @Test
    public void getActiveProfile() {

        val profile = CSpringUtils.getActiveProfile();
        Assertions.assertEquals(PROFILE, profile);
    }

    /**
     * 测试获取当前激活环境文本
     */
        /**
         * 对应测试用例 1.2
         */
    @Test
    public void getActiveProfileText() {

        val profile = CSpringUtils.getActiveProfileText();
        Assertions.assertEquals(PROFILE.getText(), profile);
    }

    /**
     * 测试环境名前缀拼接
     */
        /**
         * 对应测试用例 1.3
         */
    @Test
    public void profilePrefix() {

        val profile = CSpringUtils.profilePrefix(CLASS_NAME);
        Assertions.assertEquals(PROFILE.name() + CLASS_NAME, profile);
    }

    /**
     * 测试生产环境外的环境名前缀拼接
     */
        /**
         * 对应测试用例 1.4
         */
    @Test
    public void profilePrefixExcludeProd() {

        val profile = CSpringUtils.profilePrefixExcludeProd(CLASS_NAME);
        Assertions.assertEquals(PROFILE.name() + CLASS_NAME, profile);
    }

    /**
     * 测试环境名后缀拼接
     */
        /**
         * 对应测试用例 1.5
         */
    @Test
    public void profileSuffix() {

        val profile = CSpringUtils.profileSuffix(CLASS_NAME);
        Assertions.assertEquals(CLASS_NAME + PROFILE.name(), profile);
    }

    /**
     * 测试生产环境外的环境名后缀拼接
     */
        /**
         * 对应测试用例 1.6
         */
    @Test
    public void profileSuffixExcludeProd() {

        val profile = CSpringUtils.profileSuffixExcludeProd(CLASS_NAME);
        Assertions.assertEquals(CLASS_NAME + PROFILE.name(), profile);
    }

    /**
     * 测试环境文本后缀拼接
     */
        /**
         * 对应测试用例 1.7
         */
    @Test
    public void profileTextSuffix() {

        val profile = CSpringUtils.profileTextSuffix(CLASS_NAME);
        Assertions.assertEquals(CLASS_NAME + "-" + PROFILE.getText(), profile);
    }

}
