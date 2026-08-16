package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CLambdaUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CLambdaUtilsTests
 * </p>
 *
 * @author c332030
 * @since 2025/12/20
 */
public class CLambdaUtilsTests {

    /**
     * 测试 Bean：覆盖基本类型与引用类型字段
     */
    public static class ValueBean {

        private long id;

        private String name;

    }

    /**
     * 测试获取字段的 getter lambda
     */
    @Test
    public void getFieldGetLambda() {

        val id = 332030L;

        val bean = new ValueBean();
        bean.id = id;

        // 基本类型字段装箱后经 lambda 读取
        val idField = CReflectUtils.getField(ValueBean.class, "id");
        val idLambda = CLambdaUtils.getFieldGetLambda(ValueBean.class, idField);
        Assertions.assertEquals(id, idLambda.apply(bean));

        // 引用类型字段经 lambda 读取
        val name = "ctool4j";
        bean.name = name;
        val nameField = CReflectUtils.getField(ValueBean.class, "name");
        val nameLambda = CLambdaUtils.getFieldGetLambda(ValueBean.class, nameField);
        Assertions.assertEquals(name, nameLambda.apply(bean));

    }

    /**
     * 测试获取字段的 setter lambda
     */
    @Test
    public void getFieldSetLambda() {

        val id = 332030L;

        val bean = new ValueBean();

        // 基本类型字段经 lambda 写入（拆箱）
        val idField = CReflectUtils.getField(ValueBean.class, "id");
        val idLambda = CLambdaUtils.getFieldSetLambda(ValueBean.class, idField);
        idLambda.accept(bean, id);
        Assertions.assertEquals(id, bean.id);

        // 引用类型字段经 lambda 写入
        val name = "ctool4j";
        val nameField = CReflectUtils.getField(ValueBean.class, "name");
        val nameLambda = CLambdaUtils.getFieldSetLambda(ValueBean.class, nameField);
        nameLambda.accept(bean, name);
        Assertions.assertEquals(name, bean.name);

    }

}
