package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.config.CPageConfig;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Description: CReflectUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「注解缓存 / 实例字段 / 静态字段 / final 字段 / 继承字段 / 按名读写」多个维度组织。</li>
 *   <li>用 ValueBean（实例 name/静态 STATIC_VALUE/final finalValue）与 SubValueBean（继承）验证字段读写各路径。</li>
 *   <li>按字段名读写验证存在正常、不存在快速失败抛 IllegalArgumentException。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 MethodHandle 快速路径、静态/final 回退、按名快速失败的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getAnnotationCached；实例字段走快速路径；静态字段回退；final 字段 setValue 回退；父类字段经子类</li>
 *   <li>实例读写；按字段名读写存在/不存在抛异常。</li>
 *   <li>未覆盖：getAllConstructors/getMethods/getAllMethods 等其余入口（当前测试聚焦字段读写核心路径）。</li>
 * </ul>
 * <h2>注解缓存</h2>
 * <ul>
 *   <li>1.1 getAnnotationCached：CPageConfig 命中 ConfigurationProperties 注解（getAnnotationCached）</li>
 * </ul>
 * <h2>字段读写</h2>
 * <ul>
 *   <li>2.1 实例字段：走 MethodHandle 快速路径（getSetValueInstanceField）</li>
 *   <li>2.2 静态字段：回退 Field 原生路径（getSetValueStaticField）</li>
 *   <li>2.3 final 字段：setValue 回退 Field.set（setValueFinalField）</li>
 *   <li>2.4 继承字段：父类字段经子类实例读写（getSetValueInheritField）</li>
 *   <li>2.5 按字段名：存在正常、不存在抛 IllegalArgumentException（getSetValueByFieldName）</li>
 * </ul>
 *
 * @since 2026/6/16
 * @version 1.0
 */
public class CReflectUtilsTests {

    /**
     * 测试缓存的注解获取
     * 对应测试用例 1.1：CPageConfig 命中 ConfigurationProperties 注解
     */
    @Test
    public void getAnnotationCached() {

        val anno = CReflectUtils.getAnnotationCached(CPageConfig.class, ConfigurationProperties.class);
        Assertions.assertInstanceOf(ConfigurationProperties.class, anno);

    }

    /**
     * 测试实例字段读写走缓存的 MethodHandle 快速路径
     * 对应测试用例 2.1：实例字段：走 MethodHandle 快速路径
     */
    @Test
    public void getSetValueInstanceField() {

        val bean = new ValueBean();
        val field = CReflectUtils.getInstanceFieldMap(ValueBean.class).get("name");

        CReflectUtils.setValue(bean, field, "value");
        Assertions.assertEquals("value", CReflectUtils.getValue(bean, field));

    }

    /**
     * 测试静态字段读写回退 Field 原生路径
     * 对应测试用例 2.2：静态字段：回退 Field 原生路径
     */
    @Test
    public void getSetValueStaticField() {

        val field = CReflectUtils.getAllFieldMap(ValueBean.class).get("STATIC_VALUE");

        CReflectUtils.setValue(null, field, "static-value");
        Assertions.assertEquals("static-value", CReflectUtils.getValue(null, field));

    }

    /**
     * 测试 final 字段 setValue 回退 Field.set（handle 缓存排除 final 字段，保持原行为）
     * 对应测试用例 2.3：final 字段：setValue 回退 Field.set
     */
    @Test
    public void setValueFinalField() {

        val bean = new ValueBean();
        val field = CReflectUtils.getAllFieldMap(ValueBean.class).get("finalValue");

        CReflectUtils.setValue(bean, field, "final-value");
        Assertions.assertEquals("final-value", CReflectUtils.getValue(bean, field));

    }

    /**
     * 测试父类声明字段经子类实例读写
     * 对应测试用例 2.4：继承字段：父类字段经子类实例读写
     */
    @Test
    public void getSetValueInheritField() {

        val bean = new SubValueBean();
        val field = CReflectUtils.getAllFieldMap(SubValueBean.class).get("name");

        CReflectUtils.setValue(bean, field, "inherit");
        Assertions.assertEquals("inherit", CReflectUtils.getValue(bean, field));

    }

    /**
     * 测试按字段名读写：字段存在时正常，字段不存在时快速失败
     * 对应测试用例 2.5：按字段名：存在正常、不存在抛 IllegalArgumentException
     */
    @Test
    public void getSetValueByFieldName() {

        val bean = new ValueBean();

        CReflectUtils.setValue(bean, "name", "by-name");
        Assertions.assertEquals("by-name", CReflectUtils.getValue(bean, "name"));

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CReflectUtils.getValue(bean, "noSuchField")
        );
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CReflectUtils.setValue(bean, "noSuchField", "x")
        );

    }

    /**
     * 测试 Bean：覆盖实例/静态/final 字段
     */
    public static class ValueBean {

        private String name;

        private static String STATIC_VALUE;

        private final String finalValue = "init";

    }

    /**
     * 继承测试 Bean
     */
    public static class SubValueBean extends ValueBean {

        private String subName;

    }

}
