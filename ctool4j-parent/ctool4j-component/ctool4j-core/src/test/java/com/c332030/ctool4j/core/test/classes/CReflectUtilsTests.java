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
 * @since 2026/6/16
 */
public class CReflectUtilsTests {

    /**
     * 测试缓存的注解获取
     */
    @Test
    public void getAnnotationCached() {

        val anno = CReflectUtils.getAnnotationCached(CPageConfig.class, ConfigurationProperties.class);
        Assertions.assertInstanceOf(ConfigurationProperties.class, anno);

    }

    /**
     * 测试实例字段读写走缓存的 MethodHandle 快速路径
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
     */
    @Test
    public void getSetValueStaticField() {

        val field = CReflectUtils.getAllFieldMap(ValueBean.class).get("STATIC_VALUE");

        CReflectUtils.setValue(null, field, "static-value");
        Assertions.assertEquals("static-value", CReflectUtils.getValue(null, field));

    }

    /**
     * 测试 final 字段 setValue 回退 Field.set（handle 缓存排除 final 字段，保持原行为）
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
     */
    @Test
    public void getSetValueInheritField() {

        val bean = new SubValueBean();
        val field = CReflectUtils.getAllFieldMap(SubValueBean.class).get("name");

        CReflectUtils.setValue(bean, field, "inherit");
        Assertions.assertEquals("inherit", CReflectUtils.getValue(bean, field));

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
