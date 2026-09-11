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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「读取 lambda / 写入 lambda」两个维度组织，用 ValueBean（long id 基本类型 + String name 引用类型）覆盖两种字段类型。</li>
 *   <li>读取验证 lambda.apply 返回字段值（基本类型装箱）；写入验证 lambda.accept 设置字段值（拆箱）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 getter/setter lambda 的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：基本类型字段的 get/set lambda；引用类型字段的 get/set lambda。</li>
 *   <li>未覆盖：无（覆盖了两种字段类型的两入口）。</li>
 * </ul>
 * <h2>读取 Lambda</h2>
 * <ul>
 *   <li>1.1 getFieldGetLambda：基本类型（装箱）与引用类型字段读取正确（getFieldGetLambda）</li>
 * </ul>
 * <h2>写入 Lambda</h2>
 * <ul>
 *   <li>2.1 getFieldSetLambda：基本类型（拆箱）与引用类型字段写入正确（getFieldSetLambda）</li>
 * </ul>
 *
 * @author c332030
 * @since 2025/12/20
 * @version 1.0
 */
public class CLambdaUtilsTests {

    /**
     * 测试 Bean：覆盖基本类型与引用类型字段
     */
    public static class ValueBean {

        public long id;

        public String name;

    }

    /**
     * 测试获取字段的 getter lambda
     * 对应测试用例 1.1：基本类型（装箱）与引用类型字段读取正确
     */
    @Test
    public void getFieldGetLambda() {

        val id = 332030L;

        val bean = new ValueBean();
        bean.id = id;

        // 基本类型字段装箱后经 lambda 读取
        val idField = CReflectUtils.getField(ValueBean.class, "id");
        val idLambda = CLambdaUtils.getFieldGetLambda(idField);
        Assertions.assertEquals(id, idLambda.apply(bean));

        // 引用类型字段经 lambda 读取
        val name = "ctool4j";
        bean.name = name;
        val nameField = CReflectUtils.getField(ValueBean.class, "name");
        val nameLambda = CLambdaUtils.getFieldGetLambda(nameField);
        Assertions.assertEquals(name, nameLambda.apply(bean));

    }

    /**
     * 测试获取字段的 setter lambda
     * 对应测试用例 2.1：基本类型（拆箱）与引用类型字段写入正确
     */
    @Test
    public void getFieldSetLambda() {

        val id = 332030L;

        val bean = new ValueBean();

        // 基本类型字段经 lambda 写入（拆箱）
        val idField = CReflectUtils.getField(ValueBean.class, "id");
        val idLambda = CLambdaUtils.getFieldSetLambda(idField);
        idLambda.accept(bean, id);
        Assertions.assertEquals(id, bean.id);

        // 引用类型字段经 lambda 写入
        val name = "ctool4j";
        val nameField = CReflectUtils.getField(ValueBean.class, "name");
        val nameLambda = CLambdaUtils.getFieldSetLambda(nameField);
        nameLambda.accept(bean, name);
        Assertions.assertEquals(name, bean.name);

    }

}
