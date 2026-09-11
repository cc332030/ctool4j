package com.c332030.ctool4j.transaction.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Description: CTransactionalTests
 * </p>
 *
 * <p>
 * 是 {@link CTransactional} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>通过 {@code AnnotatedElementUtils.findMergedAnnotation} 验证 {@code @CTransactional} 标注的类/方法</li>
 *   <li>能被 Spring 合并解析为等价的 {@code @Transactional} 语义（元注解映射是否生效）。</li>
 *   <li>覆盖默认值（类级不指定属性）、自定义值（类级显式指定属性）、方法级自定义三个维度，</li>
 *   <li>分别验证 propagation / isolation / readOnly / rollbackFor 是否正确透传。</li>
 *   <li>补充验证注解的 {@code @Retention(RUNTIME)} 与元注解 {@code @Transactional} 标注，确保运行时可用。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对四个属性经 {@code @AliasFor} 映射到 Spring {@code @Transactional} 的约定。</li>
 *   <li>依据功能设计对默认值（REQUIRED / DEFAULT / false / Exception）的约定。</li>
 *   <li>依据白盒原则：注解为纯声明，核心风险点是元注解映射是否生效、默认值与自定义值透传，</li>
 *   <li>以及运行时保留。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：类级默认值、类级自定义值、方法级自定义值、运行时保留、元注解标注。</li>
 *   <li>未覆盖：Spring 容器实际事务拦截行为（需集成环境，本用例仅验证注解元数据层语义）；</li>
 *   <li>{@code noRollbackFor}/{@code timeout} 等未透传属性（本类不提供）。</li>
 * </ul>
 * <h2>元注解映射解析</h2>
 * <ul>
 *   <li>1.1 类级默认值：{@code @CTransactional} 解析为 {@code @Transactional}，默认 REQUIRED/DEFAULT/false/Exception（metaAnnotationResolved_defaultValues）</li>
 *   <li>1.2 类级自定义值：REQUIRES_NEW/READ_COMMITTED/true/IllegalStateException（metaAnnotationResolved_customValues）</li>
 *   <li>1.3 方法级自定义值：MANDATORY/REPEATABLE_READ/true/IllegalArgumentException（metaAnnotationResolved_methodLevel）</li>
 * </ul>
 * <h2>注解元信息</h2>
 * <ul>
 *   <li>2.1 运行时保留：类被 {@code @Transactional} 元注解标注（annotationRetainedAtRuntime）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
class CTransactionalTests {

    @CTransactional
    static class DefaultConfig {
        void method() {
        }
    }

    @CTransactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED,
            readOnly = true,
            rollbackFor = IllegalStateException.class)
    static class CustomConfig {
        void method() {
        }
    }

    static class CustomMethodConfig {

        @CTransactional(
                propagation = Propagation.MANDATORY,
                isolation = Isolation.REPEATABLE_READ,
                readOnly = true,
                rollbackFor = IllegalArgumentException.class)
        void method() {
        }
    }

    /**
     * 对应测试用例 1.1：类级默认值元注解解析
     */
    @Test
    void metaAnnotationResolved_defaultValues() {
        Transactional t = AnnotatedElementUtils.findMergedAnnotation(DefaultConfig.class, Transactional.class);
        Assertions.assertNotNull(t, "@CTransactional 应通过 @AliasFor 映射为 @Transactional");
        Assertions.assertEquals(Propagation.REQUIRED, t.propagation());
        Assertions.assertEquals(Isolation.DEFAULT, t.isolation());
        Assertions.assertFalse(t.readOnly());
        Assertions.assertEquals(1, t.rollbackFor().length);
        Assertions.assertEquals(Exception.class, t.rollbackFor()[0]);
    }

    /**
     * 对应测试用例 1.2：类级自定义值元注解解析
     */
    @Test
    void metaAnnotationResolved_customValues() {
        Transactional t = AnnotatedElementUtils.findMergedAnnotation(CustomConfig.class, Transactional.class);
        Assertions.assertNotNull(t);
        Assertions.assertEquals(Propagation.REQUIRES_NEW, t.propagation());
        Assertions.assertEquals(Isolation.READ_COMMITTED, t.isolation());
        Assertions.assertTrue(t.readOnly());
        Assertions.assertEquals(1, t.rollbackFor().length);
        Assertions.assertEquals(IllegalStateException.class, t.rollbackFor()[0]);
    }

    /**
     * 对应测试用例 1.3：方法级自定义值元注解解析
     */
    @Test
    void metaAnnotationResolved_methodLevel() throws NoSuchMethodException {
        Transactional t = AnnotatedElementUtils.findMergedAnnotation(
                CustomMethodConfig.class.getDeclaredMethod("method"), Transactional.class);
        Assertions.assertNotNull(t);
        Assertions.assertEquals(Propagation.MANDATORY, t.propagation());
        Assertions.assertEquals(Isolation.REPEATABLE_READ, t.isolation());
        Assertions.assertTrue(t.readOnly());
        Assertions.assertEquals(1, t.rollbackFor().length);
        Assertions.assertEquals(IllegalArgumentException.class, t.rollbackFor()[0]);
    }

    /**
     * 对应测试用例 2.1：注解运行时保留
     */
    @Test
    void annotationRetainedAtRuntime() {
        Assertions.assertTrue(CTransactional.class.isAnnotationPresent(Transactional.class),
                "@CTransactional 应被 @Transactional 元注解标注");
    }

}
