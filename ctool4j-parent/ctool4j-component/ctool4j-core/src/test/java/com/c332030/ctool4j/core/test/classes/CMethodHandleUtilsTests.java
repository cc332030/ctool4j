package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CMethodHandleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * <p>
 * Description: CMethodHandleUtils 测试（handle 生成与缓存、统一 Object 签名适配）
 * </p>
 *
 * <p>`com.c332030.ctool4j.core.classes.CMethodHandleUtils`（core 工具类）的测试用例；
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 handle 的「类型维度」组织分类：getter/setter、method、constructor，再按「生成 vs 缓存 vs asType 签名适配」细分，覆盖 CMethodHandleUtils 的全部对外入口</li>
 *   <li>每个入口给出「生成 + 调用生效」用例，缓存相关入口另给「二次获取同一 handle（assertSame）」用例，验证按 Field/Method/Constructor 弱 key 缓存</li>
 *   <li>asType 统一 Object 签名（{@code GETTER_HANDLE_TYPE}/{@code SETTER_HANDLE_TYPE}）单独成类：getter 返回 Object（引用类型）、原始类型字段装箱返回 Object、setter 收 Object（引用类型）、原始类型字段拆箱写入——覆盖统一签名下引用/原始两种字段的装箱拆箱行为</li>
 *   <li>测试输入结构 {@code Person}/{@code PrimitiveHolder}：{@code PrimitiveHolder} 字段刻意保留原始类型 {@code int}，以覆盖原始类型字段经 asType 的装箱/拆箱（POJO 属性按规范用包装类，此处为验证核心行为特例）</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对职责的约定：{@code CMethodHandleUtils} 只保留 handle 生成与缓存（toXxxHandle/getXxxHandle）；asType 版本供 invokeExact 快速路径与 Lambda 转换</li>
 *   <li>依据 3.5 语义区分：{@code getGetterHandle}（原始签名缓存）与 {@code getGetterHandleAsType}（统一 Object 签名）并存，故分别覆盖</li>
 *   <li>依据测试方法（等价类/边界值）：引用类型字段与原始类型字段为两类等价输入；缓存二次获取断言同一实例验证缓存命中</li>
 * </ul>
 * <h2>覆盖场景</h2>
 * <ul>
 *   <li>handle 生成 + 调用生效：getter/setter（原始签名）、method、constructor 各一用例（生成后 invoke 取引用类型字段值/写入）</li>
 *   <li>缓存命中：getterSetter/method/constructor 三类 handle 二次获取 assertSame 同一实例（按 Field/Method/Constructor 弱 key 缓存）</li>
 *   <li>asType 统一 Object 签名：getter 取引用类型字段（接收者显式转型后 invokeExact 直接调用）、原始类型字段装箱返回 Object、setter 写引用类型字段、原始类型字段 Object 值自动拆箱写入</li>
 * </ul>
 * <h2>边界与取舍</h2>
 * <ul>
 *   <li>原始类型字段（{@code PrimitiveHolder} 的 int）经 asType 自动装箱（getter）/拆箱（setter），与引用类型字段两类等价输入分别覆盖（POJO 属性按规范用包装类，此为验证核心行为特例）</li>
 *   <li>{@code toHandleSpecial}（special 方法句柄）当前无调用方、未使用，不作问题提示，暂无测试用例</li>
 * </ul>
 * <h2>getter handle（原始签名）</h2>
 * <ul>
 *   <li>1.1.1 getGetterHandle 生成 getter handle，invoke 取引用类型字段值（getterHandle）</li>
 *   <li>1.1.2 toGetterHandle 生成（toGetterHandle）</li>
 *   <li>1.1.3 二次获取 getGetterHandle 缓存命中（assertSame）（getterSetterCache）</li>
 * </ul>
 * <h2>setter handle（原始签名）</h2>
 * <ul>
 *   <li>1.2.1 getSetterHandle 生成 setter handle，invoke 写入引用类型字段（setterHandle）</li>
 *   <li>1.2.2 toSetterHandle 生成（toSetterHandle）</li>
 * </ul>
 * <h2>asType 统一 Object 签名</h2>
 * <ul>
 *   <li>1.3.1 getGetterHandleAsType：接收者显式转型后 invokeExact 直接调用，取引用类型字段（getGetterHandleAsType）</li>
 *   <li>1.3.2 getGetterHandleAsType 原始类型字段：装箱后以 Object 返回（getGetterHandleAsTypePrimitive）</li>
 *   <li>1.3.3 getSetterHandleAsType：收 Object 值写入引用类型字段（setGetterHandleAsType）</li>
 *   <li>1.3.4 getSetterHandleAsType 原始类型字段：Object 值经 asType 自动拆箱写入（setGetterHandleAsTypePrimitive）</li>
 * </ul>
 * <h2>method handle</h2>
 * <ul>
 *   <li>2.1 getHandle(Method) 生成方法 handle 并调用（methodHandle）</li>
 *   <li>2.2 二次获取 getHandle(Method) 缓存命中（assertSame）（methodHandleCache）</li>
 * </ul>
 * <h2>constructor handle</h2>
 * <ul>
 *   <li>3.1 getHandle(Constructor) 生成构造器 handle 并调用（constructorHandle）</li>
 *   <li>3.2 二次获取 getHandle(Constructor) 缓存命中（assertSame）（constructorHandleCache）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CMethodHandleUtilsTests {
    /**
     * 对应测试用例 1.1.1：getGetterHandle 生成 getter handle，invoke 取引用类型字段值
     */

    @Test
    public void getterHandle() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.getGetterHandle(field);
        Person person = Person.builder().name("tom").build();
        Assertions.assertEquals("tom", handle.invoke(person));

    }
    /**
     * 对应测试用例 1.2.1：getSetterHandle 生成 setter handle，invoke 写入引用类型字段
     */

    @Test
    public void setterHandle() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.getSetterHandle(field);
        Person person = new Person();
        handle.invoke(person, "jerry");
        Assertions.assertEquals("jerry", person.getName());

    }
    /**
     * 对应测试用例 1.1.3：二次获取 getGetterHandle 缓存命中（assertSame）
     */

    @Test
    public void getterSetterCache() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle h1 = CMethodHandleUtils.getGetterHandle(field);
        MethodHandle h2 = CMethodHandleUtils.getGetterHandle(field);
        Assertions.assertSame(h1, h2);

    }
    /**
     * 对应测试用例 2.1：getHandle(Method) 生成方法 handle 并调用
     */

    @Test
    public void methodHandle() throws Throwable {

        Method method = Person.class.getDeclaredMethod("secret");
        MethodHandle handle = CMethodHandleUtils.getHandle(method);
        Person person = Person.builder().name("tom").build();
        Assertions.assertEquals("secret-tom", handle.invoke(person));

    }
    /**
     * 对应测试用例 2.2：二次获取 getHandle(Method) 缓存命中（assertSame）
     */

    @Test
    public void methodHandleCache() {

        try {
            Method method = Person.class.getDeclaredMethod("secret");
            MethodHandle h1 = CMethodHandleUtils.getHandle(method);
            MethodHandle h2 = CMethodHandleUtils.getHandle(method);
            Assertions.assertSame(h1, h2);
        } catch (NoSuchMethodException e) {
            Assertions.fail(e.getMessage());
        }

    }
    /**
     * 对应测试用例 3.1：getHandle(Constructor) 生成构造器 handle 并调用
     */

    @Test
    public void constructorHandle() throws Throwable {

        Constructor<?> constructor = Person.class.getDeclaredConstructor(String.class, Integer.class);
        MethodHandle handle = CMethodHandleUtils.getHandle(constructor);
        Person person = (Person) handle.invoke("alice", 18);
        Assertions.assertEquals("alice", person.getName());
        Assertions.assertEquals(18, person.getAge());

    }
    /**
     * 对应测试用例 3.2：二次获取 getHandle(Constructor) 缓存命中（assertSame）
     */

    @Test
    public void constructorHandleCache() throws Throwable {

        Constructor<?> constructor = Person.class.getDeclaredConstructor(String.class, Integer.class);
        MethodHandle h1 = CMethodHandleUtils.getHandle(constructor);
        MethodHandle h2 = CMethodHandleUtils.getHandle(constructor);
        Assertions.assertSame(h1, h2);

    }
    /**
     * 对应测试用例 1.1.2：toGetterHandle 生成
     */

    @Test
    public void toGetterHandle() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.toGetterHandle(field);
        Person person = Person.builder().name("tom").build();
        Assertions.assertEquals("tom", handle.invoke(person));

    }
    /**
     * 对应测试用例 1.2.2：toSetterHandle 生成
     */

    @Test
    public void toSetterHandle() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.toSetterHandle(field);
        Person person = new Person();
        handle.invoke(person, "jerry");
        Assertions.assertEquals("jerry", person.getName());

    }
    /**
     * 对应测试用例 1.3.1：接收者显式转型后 invokeExact 直接调用，取引用类型字段
     */

    @Test
    public void getGetterHandleAsType() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.getGetterHandleAsType(field);
        Person person = Person.builder().name("tom").build();
        // 统一 Object 签名，接收者显式转型后 invokeExact 直接调用
        Assertions.assertEquals("tom", handle.invokeExact((Object) person));

    }
    /**
     * 对应测试用例 1.3.2：getGetterHandleAsType 原始类型字段：装箱后以 Object 返回
     */

    @Test
    public void getGetterHandleAsTypePrimitive() throws Throwable {

        Field field = PrimitiveHolder.class.getDeclaredField("value");
        MethodHandle handle = CMethodHandleUtils.getGetterHandleAsType(field);
        PrimitiveHolder holder = new PrimitiveHolder();
        holder.setValue(18);
        // 原始类型字段装箱后以 Object 返回
        Assertions.assertEquals(18, handle.invokeExact((Object) holder));

    }
    /**
     * 对应测试用例 1.3.3：收 Object 值写入引用类型字段
     */

    @Test
    public void setGetterHandleAsType() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.getSetterHandleAsType(field);
        Person person = new Person();
        handle.invokeExact((Object) person, (Object) "jerry");
        Assertions.assertEquals("jerry", person.getName());

    }
    /**
     * 对应测试用例 1.3.4：getSetterHandleAsType 原始类型字段：Object 值经 asType 自动拆箱写入
     */

    @Test
    public void setGetterHandleAsTypePrimitive() throws Throwable {

        Field field = PrimitiveHolder.class.getDeclaredField("value");
        MethodHandle handle = CMethodHandleUtils.getSetterHandleAsType(field);
        PrimitiveHolder holder = new PrimitiveHolder();
        // Object 值经 asType 适配自动拆箱写入原始类型字段
        handle.invokeExact((Object) holder, (Object) 18);
        Assertions.assertEquals(18, holder.getValue());

    }

    /**
     * 测试用 POJO
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    static class Person {

        /**
         * 姓名
         */
        String name;

        /**
         * 年龄
         */
        Integer age;

        private String secret() {
            return "secret-" + name;
        }

    }

    /**
     * 原始类型字段载体：验证 CMethodHandleUtils 对原始类型字段的装箱/拆箱处理
     * <p>测试输入结构，非业务 POJO——POJO 属性按阿里规范使用包装类（如 {@link Person#age}）；
     * 此处字段保留原始类型是为覆盖 asType 统一 Object 签名下的装箱/拆箱这一核心行为</p>
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    static class PrimitiveHolder {

        /**
         * 值（原始类型）
         */
        int value;

    }

}
