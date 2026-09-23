package com.c332030.ctool4j.spring.util;

import cn.hutool.extra.spring.SpringUtil;
import com.c332030.ctool4j.core.enums.CProfileEnum;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

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
 *   <li>1.3 容器取值（getBean）：框架自有上下文命中 / 自有上下文为空时走 Hutool 兜底 / 兜底上下文已关闭时快速失败（对应测试方法 1.12-1.15）</li>
 *   <li>兜底窗口（{@code BeanFactoryPostProcessor} 阶段自有上下文尚未写入）另见 {@code CSpringUtilsFallbackTests}</li>
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
     * 对应测试用例 1.1：验证激活 profile 相关（对应测试方法 1.1-1.7）
     */
    @Test
    public void getActiveProfile() {

        val profile = CSpringUtils.getActiveProfile();
        Assertions.assertEquals(PROFILE, profile);
    }

    /**
     * 测试获取当前激活环境文本
     * 对应测试用例 1.2
     */
    @Test
    public void getActiveProfileText() {

        val profile = CSpringUtils.getActiveProfileText();
        Assertions.assertEquals(PROFILE.getText(), profile);
    }

    /**
     * 测试环境名前缀拼接
     * 对应测试用例 1.3
     */
    @Test
    public void profilePrefix() {

        val profile = CSpringUtils.profilePrefix(CLASS_NAME);
        Assertions.assertEquals(PROFILE.name() + CLASS_NAME, profile);
    }

    /**
     * 测试生产环境外的环境名前缀拼接
     * 对应测试用例 1.4
     */
    @Test
    public void profilePrefixExcludeProd() {

        val profile = CSpringUtils.profilePrefixExcludeProd(CLASS_NAME);
        Assertions.assertEquals(PROFILE.name() + CLASS_NAME, profile);
    }

    /**
     * 测试环境名后缀拼接
     * 对应测试用例 1.5
     */
    @Test
    public void profileSuffix() {

        val profile = CSpringUtils.profileSuffix(CLASS_NAME);
        Assertions.assertEquals(CLASS_NAME + PROFILE.name(), profile);
    }

    /**
     * 测试生产环境外的环境名后缀拼接
     * 对应测试用例 1.6
     */
    @Test
    public void profileSuffixExcludeProd() {

        val profile = CSpringUtils.profileSuffixExcludeProd(CLASS_NAME);
        Assertions.assertEquals(CLASS_NAME + PROFILE.name(), profile);
    }

    /**
     * 测试环境文本后缀拼接
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
     * 测试从框架自有上下文取 Bean
     * <p>对应测试用例 1.12：上下文就绪时走 {@code CSpringConfigBeans} 持有的一侧，
     * 断言取到的就是容器内同一实例（不因兜底分支而取到别的上下文）</p>
     */
    @Test
    public void getBean_fromOwnContext() {

        // 容器已就绪：自有上下文非空，取 Bean 应命中自有上下文
        Assertions.assertNotNull(CSpringConfigBeans.getApplicationContext());

        val environment = CSpringUtils.getBean(Environment.class);
        Assertions.assertSame(CSpringUtils.getApplicationContext().getBean(Environment.class), environment);
    }

    /**
     * 测试自有上下文为空时走 Hutool 兜底
     * <p>对应测试用例 1.13：把自有上下文临时置为 {@code null}（模拟 {@code @PostConstruct} 一类
     * 早于上下文就绪的调用点），断言仍能取到 Bean 且与 Hutool 一侧同源；用例结束还原上下文，
     * 避免污染同进程内其他用例</p>
     */
    @Test
    public void getBean_fallbackToHutoolWhenOwnContextAbsent() {

        val ownContext = CSpringConfigBeans.getApplicationContext();
        Assertions.assertNotNull(SpringUtil.getApplicationContext(), "兜底前应存在 Hutool 上下文");

        try {
            CSpringConfigBeans.setApplicationContext(null);

            // 自有上下文为空：走 Hutool 兜底
            val environment = CSpringUtils.getBean(Environment.class);
            Assertions.assertNotNull(environment);
            Assertions.assertSame(SpringUtil.getBean(Environment.class), environment);
        } finally {
            CSpringConfigBeans.setApplicationContext(ownContext);
        }

        Assertions.assertNotNull(CSpringConfigBeans.getApplicationContext(), "上下文应已还原");
    }

    /**
     * 测试两侧上下文都不可用时的快速失败
     * <p>对应测试用例 1.14：把自有上下文临时置为 {@code null}、并断言 Hutool 一侧也不可用时不静默返回
     * {@code null}——非 Spring 环境下的语义（本用例在容器内执行，故只断言"自有上下文为空时不返回 null"，
     * 真实的两侧皆空路径由 {@link #getBean_hutoolContextInactiveFailsFast()} 以关闭上下文的方式覆盖）
     * </p>
     */
    @Test
    public void getBean_ownContextNull_doesNotReturnNull() {

        val ownContext = CSpringConfigBeans.getApplicationContext();
        try {
            CSpringConfigBeans.setApplicationContext(null);

            // 容器内 Hutool 一侧可用：兜底应取到 Bean（不返回 null）
            val environment = CSpringUtils.getBean(Environment.class);
            Assertions.assertNotNull(environment);
        } finally {
            CSpringConfigBeans.setApplicationContext(ownContext);
        }
    }

    /**
     * 测试 Hutool 兜底上下文已关闭时快速失败（不取别的容器的 Bean）
     * <p>对应测试用例 1.15：自建一个"含 SpringUtil 的容器"并刷新，使其成为 Hutool 当前持有的上下文；
     * 关闭该容器后，把自有上下文置空并调用 {@code getBean}——应抛 {@code IllegalStateException}
     * （拒绝以已关闭上下文兜底），而不是从该已关闭容器里取到 Bean</p>
     */
    @Test
    public void getBean_hutoolContextInactiveFailsFast() {

        val ownContext = CSpringConfigBeans.getApplicationContext();
        val hutoolContextBefore = SpringUtil.getApplicationContext();

        val temporaryContext = new GenericApplicationContext();
        temporaryContext.setEnvironment(new MockEnvironment());
        temporaryContext.registerBean(SpringUtil.class);
        temporaryContext.refresh();
        // 刷新后 Hutool 的静态字段指向该临时容器
        Assertions.assertSame(temporaryContext, SpringUtil.getApplicationContext());
        temporaryContext.close();

        try {
            CSpringConfigBeans.setApplicationContext(null);

            val exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> CSpringUtils.getBean(Environment.class)
            );
            Assertions.assertTrue(exception.getMessage().contains("非活动状态"));
        } finally {
            CSpringConfigBeans.setApplicationContext(ownContext);
        }

        // 说明：Hutool 的静态字段被本用例覆盖，容器内其余用例已执行完，无需恢复
        Assertions.assertNotNull(hutoolContextBefore);
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
