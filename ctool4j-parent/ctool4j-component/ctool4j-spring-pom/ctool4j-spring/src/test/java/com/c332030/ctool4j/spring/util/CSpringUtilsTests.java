package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.enums.CProfileEnum;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

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
 *   <li>1.2 容器参数实例化（newInstance）：无参构造 / 参数最多的构造（实参从容器解析）/ 无构造方法 / 同参数个数多个构造方法（对应测试方法 1.8-1.11）</li>
 * </ul>
 *
 *
 * @since 2026/6/2
 * @version 1.1
 * @see CSpringUtils
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

    /**
     * 测试无参构造实例化
     * <p>对应测试用例 1.8：参数最多的构造方法为无参构造，无需从容器解析实参</p>
     */
    @Test
    public void newInstance_noArgConstructor() {

        val bean = CSpringUtils.newInstance(NoArgBean.class);
        Assertions.assertEquals("no-arg", bean.value);
    }

    /**
     * 测试参数最多的构造方法实例化（实参从容器解析）
     * <p>对应测试用例 1.9：0 参与 1 参构造并存时取 1 参构造；断言解析出的 Environment 非空，
     * 若误用 0 参构造则 environment 为 null 而失败</p>
     */
    @Test
    public void newInstance_maxArityConstructor() {

        val bean = CSpringUtils.newInstance(EnvironmentBean.class);
        Assertions.assertNotNull(bean.environment);
    }

    /**
     * 测试无构造方法类型实例化的快速失败
     * <p>对应测试用例 1.10：接口等无构造方法的类型抛 RuntimeException；消息含类型描述，故断言语义片段</p>
     */
    @Test
    public void newInstance_noConstructor_throws() {

        val exception = Assertions.assertThrowsExactly(
            RuntimeException.class,
            () -> CSpringUtils.newInstance(Runnable.class)
        );
        Assertions.assertTrue(exception.getMessage().contains("没有构造方法"));
    }

    /**
     * 测试同参数个数多个构造方法实例化的快速失败
     * <p>对应测试用例 1.11：参数最多的构造方法不唯一时无法判定用哪一个，抛 RuntimeException</p>
     */
    @Test
    public void newInstance_multiSameArityConstructor_throws() {

        val exception = Assertions.assertThrowsExactly(
            RuntimeException.class,
            () -> CSpringUtils.newInstance(MultiConstructorBean.class)
        );
        Assertions.assertTrue(exception.getMessage().contains("不支持多个相同参数构造方法"));
    }

    /**
     * 无参构造测试 Bean：验证无参构造路径
     */
    public static class NoArgBean {

        final String value = "no-arg";
    }

    /**
     * 有参构造测试 Bean：0 参与 1 参构造并存，验证取参数最多的构造方法并从容器解析实参
     */
    public static class EnvironmentBean {

        final Environment environment;

        public EnvironmentBean() {
            this(null);
        }

        public EnvironmentBean(Environment environment) {
            this.environment = environment;
        }
    }

    /**
     * 同参数个数多构造方法测试 Bean：两个 2 参构造，验证构造方法不唯一时快速失败
     */
    public static class MultiConstructorBean {

        public MultiConstructorBean(String first, String second) {
        }

        public MultiConstructorBean(String first, Integer second) {
        }
    }

}
