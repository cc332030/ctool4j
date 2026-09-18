package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.core.config.CPageConfig;
import lombok.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * <p>
 * Description: CReflectUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「注解缓存 / 实例字段 / 静态字段 / final 字段 / 继承字段 / 按名读写 / 方法调用 / 构造器实例化 / 按 Map 填充」
 *   多个维度组织。</li>
 *   <li>用 ValueBean（实例 name/静态 STATIC_VALUE/final finalValue + 实例/静态/私有/无参方法）与 SubValueBean（继承）、
 *   CtorBean（有参/无参构造器）、PrivateCtorBean（私有构造器）验证字段读写、方法调用与构造器实例化各路径。</li>
 *   <li>方法与构造器经 MethodHandle 调用：正例断言返回值/字段值；私有成员经句柄统一 setAccessible 后可直接调用
 *   （用例 3.5/4.3）；实参类型不匹配为句柄路径的已知取舍，按实现精确断言异常类型（用例 3.6）。</li>
 *   <li>按字段名读写验证存在正常、不存在快速失败抛 IllegalArgumentException。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 MethodHandle 快速路径、静态/final 回退、按名快速失败、方法/构造器句柄调用与
 *   fillValues 统一走 setValue 的约定。</li>
 *   <li>依据等价类/边界值/分支覆盖：实例/静态/私有方法、方法存在/不存在且忽略/不忽略、实参为 null、
 *   有参/无参/私有构造器、按类/按对象填充、空 Map、不存在的字段名。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getAnnotationCached；实例字段走快速路径；静态字段回退；final 字段 setValue 回退；父类字段经子类
 *   实例读写；按字段名读写存在/不存在抛异常；invoke 实例/静态/私有/无参方法、方法不存在两种策略、实参 null、
 *   实参类型不匹配、目标方法抛异常原样透传；newInstance 有参/无参/私有构造器；fillValues 按类创建填充、空 Map 返回 null、按对象填充、
 *   不存在字段跳过且空 Map 不改动。</li>
 *   <li>未覆盖：getAllConstructors/getMethods/getAllMethods/getAnnotationValueCached 等纯元数据查询入口
 *   （当前测试聚焦读写、调用与填充）。</li>
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
 * <h2>方法调用</h2>
 * <ul>
 *   <li>3.1 实例方法：有返回值（invoke_instanceMethod）</li>
 *   <li>3.2 静态方法：无接收者调用（invoke_staticMethod）</li>
 *   <li>3.3 方法不存在且忽略：返回 null（invoke_noMethod_ignoreReturnsNull）</li>
 *   <li>3.4 方法不存在且不忽略：抛 IllegalStateException（invoke_noMethod_notIgnoreThrows）</li>
 *   <li>3.5 私有方法：句柄统一 setAccessible 后可直接调用（invoke_privateMethod）</li>
 *   <li>3.6 实参类型不匹配：抛 ClassCastException（invoke_argTypeMismatch_throws）</li>
 *   <li>3.7 无实参：实参省略与显式 null 均可调用（invoke_noArgs）</li>
 *   <li>3.8 目标方法抛出异常：原样透传、不经 InvocationTargetException 包装（invoke_targetThrows_propagates）</li>
 * </ul>
 * <h2>构造器实例化</h2>
 * <ul>
 *   <li>4.1 有参构造器：经句柄实例化并赋值（newInstance_withArgs）</li>
 *   <li>4.2 无参构造器：实参为 null（newInstance_noArgs）</li>
 *   <li>4.3 私有构造器：句柄统一 setAccessible 后可实例化（newInstance_privateConstructor）</li>
 * </ul>
 * <h2>按字段值填充</h2>
 * <ul>
 *   <li>5.1 按类填充：创建对象并填充实例与 final 字段（fillValues_class_createsAndFills）</li>
 *   <li>5.2 按类填充：字段 Map 为空返回 null（fillValues_class_emptyMap_returnsNull）</li>
 *   <li>5.3 按对象填充：填充实例与 final 字段（fillValues_object_fillsFields）</li>
 *   <li>5.4 按对象填充：不存在的字段名跳过、空 Map 不改动（fillValues_object_skipUnknownAndEmpty）</li>
 * </ul>
 *
 * @since 2026/6/16
 * @version 1.1
 * @see CReflectUtils
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
     * 测试调用实例方法（句柄以接收者为首参，bindTo 绑定）
     * 对应测试用例 3.1：实例方法：有返回值
     */
    @Test
    public void invoke_instanceMethod() {

        val bean = new ValueBean();
        bean.setName("c332030");

        Assertions.assertEquals("c332030-suffix", CReflectUtils.invokeMustHaveMethod(bean, "joinName", "-suffix"));

    }

    /**
     * 测试调用静态方法（句柄不带接收者）
     * 对应测试用例 3.2：静态方法：无接收者调用
     */
    @Test
    public void invoke_staticMethod() {

        val bean = new ValueBean();

        Assertions.assertEquals("ab", CReflectUtils.invokeMustHaveMethod(bean, "staticJoin", "a", "b"));

    }

    /**
     * 测试方法不存在且忽略：返回 null
     * 对应测试用例 3.3：方法不存在且忽略：返回 null
     */
    @Test
    public void invoke_noMethod_ignoreReturnsNull() {

        val bean = new ValueBean();

        Assertions.assertNull(CReflectUtils.invokeIgnoreNoMethod(bean, "noSuchMethod"));

    }

    /**
     * 测试方法不存在且不忽略：抛 IllegalStateException
     * 对应测试用例 3.4：方法不存在且不忽略：抛 IllegalStateException
     */
    @Test
    public void invoke_noMethod_notIgnoreThrows() {

        val bean = new ValueBean();

        Assertions.assertThrowsExactly(
            IllegalStateException.class,
            () -> CReflectUtils.invokeMustHaveMethod(bean, "noSuchMethod")
        );

    }

    /**
     * 测试调用私有方法：句柄生成时统一 setAccessible，调用方无需另行处理
     * 对应测试用例 3.5：私有方法：句柄统一 setAccessible 后可直接调用
     */
    @Test
    public void invoke_privateMethod() {

        val bean = new ValueBean();
        bean.setName("private");

        Assertions.assertEquals("private-x", CReflectUtils.invokeMustHaveMethod(bean, "joinPrivate", "x"));

    }

    /**
     * 测试实参类型不匹配：句柄按签名适配，失败抛 ClassCastException
     * （已知取舍：原生反射此处抛 IllegalArgumentException）
     * 对应测试用例 3.6：实参类型不匹配：抛 ClassCastException
     */
    @Test
    public void invoke_argTypeMismatch_throws() {

        val bean = new ValueBean();

        Assertions.assertThrowsExactly(
            ClassCastException.class,
            () -> CReflectUtils.invokeMustHaveMethod(bean, "joinName", 123)
        );

    }

    /**
     * 测试无实参调用：实参省略与显式 null 均等价于无实参
     * 对应测试用例 3.7：无实参：实参省略与显式 null 均可调用
     */
    @Test
    public void invoke_noArgs() {

        val bean = new ValueBean();
        bean.setName("no-arg");

        Assertions.assertEquals("name:no-arg", CReflectUtils.invokeMustHaveMethod(bean, "summary"));
        Assertions.assertEquals("name:no-arg", CReflectUtils.invoke(bean, "summary", false, (Object[]) null));

    }

    /**
     * 测试目标方法抛出异常时原样透传
     * 对应测试用例 3.8：目标方法抛出异常：原样透传、不经 InvocationTargetException 包装
     * <p>经方法句柄调用不经 {@code Method#invoke} 的 {@link java.lang.reflect.InvocationTargetException} 包装，
     * 目标异常类型与信息原样抛出（与旧实现的行为差异，见 {@code doc/design/core/method-handle.adoc}「已知限制与取舍」）</p>
     */
    @Test
    public void invoke_targetThrows_propagates() {

        val bean = new ValueBean();

        val ex = Assertions.assertThrowsExactly(
            IllegalStateException.class,
            () -> CReflectUtils.invokeMustHaveMethod(bean, "fail")
        );
        Assertions.assertEquals("boom", ex.getMessage());

    }

    /**
     * 测试有参构造器实例化：经构造器句柄创建并赋值
     * 对应测试用例 4.1：有参构造器：经句柄实例化并赋值
     */
    @Test
    public void newInstance_withArgs() {

        @SuppressWarnings("unchecked")
        val constructor = (Constructor<CtorBean>) CReflectUtils.getConstructors(CtorBean.class, String.class, int.class).get(0);
        val bean = CReflectUtils.newInstance(constructor, "with-args", 30);

        Assertions.assertEquals("with-args", bean.getName());
        Assertions.assertEquals(30, bean.getAge());

    }

    /**
     * 测试无参构造器实例化：实参为 null 视为无实参
     * 对应测试用例 4.2：无参构造器：实参为 null
     */
    @Test
    public void newInstance_noArgs() {

        @SuppressWarnings("unchecked")
        val constructor = (Constructor<CtorBean>) CReflectUtils.getNoArgConstructor(CtorBean.class);
        val bean = CReflectUtils.newInstance(constructor, (Object[]) null);

        Assertions.assertNull(bean.getName());
        Assertions.assertEquals(0, bean.getAge());

    }

    /**
     * 测试私有构造器实例化：句柄生成时统一 setAccessible
     * （getConstructors 只收集 public 构造器，私有构造器经 getDeclaredConstructor 取元数据后交由被测方法）
     * 对应测试用例 4.3：私有构造器：句柄统一 setAccessible 后可实例化
     */
    @Test
    public void newInstance_privateConstructor() throws Exception {

        val constructor = PrivateCtorBean.class.getDeclaredConstructor();
        val bean = CReflectUtils.newInstance(constructor);

        Assertions.assertInstanceOf(PrivateCtorBean.class, bean);

    }

    /**
     * 测试按字段值 Map 创建对象并填充（含 final 字段，final 经 setValue 回退）
     * 对应测试用例 5.1：按类填充：创建对象并填充实例与 final 字段
     */
    @Test
    public void fillValues_class_createsAndFills() {

        val fieldValueMap = new HashMap<String, Object>();
        fieldValueMap.put("name", "filled");
        fieldValueMap.put("finalValue", "final-filled");

        val bean = CReflectUtils.fillValues(ValueBean.class, fieldValueMap);

        Assertions.assertNotNull(bean);
        Assertions.assertEquals("filled", bean.getName());
        Assertions.assertEquals("final-filled", CReflectUtils.getValue(bean, "finalValue"));

    }

    /**
     * 测试按字段值 Map 创建对象：字段 Map 为空返回 null
     * 对应测试用例 5.2：按类填充：字段 Map 为空返回 null
     */
    @Test
    public void fillValues_class_emptyMap_returnsNull() {

        Assertions.assertNull(CReflectUtils.fillValues(ValueBean.class, new HashMap<>()));

    }

    /**
     * 测试按字段值 Map 填充既有对象（含 final 字段）
     * 对应测试用例 5.3：按对象填充：填充实例与 final 字段
     */
    @Test
    public void fillValues_object_fillsFields() {

        val bean = new ValueBean();

        val fieldValueMap = new HashMap<String, Object>();
        fieldValueMap.put("name", "object-filled");
        fieldValueMap.put("finalValue", "final-object-filled");

        CReflectUtils.fillValues(bean, fieldValueMap);

        Assertions.assertEquals("object-filled", bean.getName());
        Assertions.assertEquals("final-object-filled", CReflectUtils.getValue(bean, "finalValue"));

    }

    /**
     * 测试按字段值 Map 填充既有对象：不存在的字段名跳过、空 Map 不改动
     * 对应测试用例 5.4：按对象填充：不存在的字段名跳过、空 Map 不改动
     */
    @Test
    public void fillValues_object_skipUnknownAndEmpty() {

        val bean = new ValueBean();
        bean.setName("keep");

        val fieldValueMap = new HashMap<String, Object>();
        fieldValueMap.put("noSuchField", "ignored");

        CReflectUtils.fillValues(bean, fieldValueMap);
        Assertions.assertEquals("keep", bean.getName());

        CReflectUtils.fillValues(bean, new HashMap<>());
        Assertions.assertEquals("keep", bean.getName());

    }

    /**
     * 测试 Bean：覆盖实例/静态/final 字段与实例/静态/私有/无参方法
     */
    @Data
    public static class ValueBean {

        private String name;

        private static String STATIC_VALUE;

        private final String finalValue = "init";

        /**
         * 实例方法：拼接后缀
         *
         * @param suffix 后缀
         * @return 名称 + 后缀
         */
        public String joinName(String suffix) {
            return name + suffix;
        }

        /**
         * 静态方法：拼接两段
         *
         * @param first  前段
         * @param second 后段
         * @return 拼接结果
         */
        public static String staticJoin(String first, String second) {
            return first + second;
        }

        /**
         * 无参方法：返回名称摘要
         *
         * @return 名称摘要
         */
        public String summary() {
            return "name:" + name;
        }

        /**
         * 私有方法：验证句柄统一 setAccessible
         *
         * @param suffix 后缀
         * @return 名称 + "-" + 后缀
         */
        private String joinPrivate(String suffix) {
            return name + "-" + suffix;
        }

        /**
         * 抛出异常的方法：验证目标异常经句柄调用原样透传
         *
         * @throws IllegalStateException 固定抛出，信息为 {@code boom}
         */
        public void fail() {
            throw new IllegalStateException("boom");
        }

    }

    /**
     * 继承测试 Bean
     */
    public static class SubValueBean extends ValueBean {

        private String subName;

    }

    /**
     * 构造器测试 Bean：有参与无参构造器由 lombok 生成
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CtorBean {

        private String name;

        private int age;

    }

    /**
     * 私有构造器测试 Bean：类为私有 → 隐式构造器为私有，用于验证句柄统一 setAccessible
     */
    private static class PrivateCtorBean {

    }

}
