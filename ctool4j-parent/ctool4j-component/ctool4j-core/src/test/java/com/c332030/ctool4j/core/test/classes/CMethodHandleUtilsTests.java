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
 * 测试用例分类与编号见 doc/design/CMethodHandleUtilsTests.adoc，各测试方法以行注释标注对应编号</p>
 *
 * @since 2025/12/12
 */
public class CMethodHandleUtilsTests {

    @Test
    public void getterHandle() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.getGetterHandle(field);
        Person person = Person.builder().name("tom").build();
        Assertions.assertEquals("tom", handle.invoke(person));

    }

    @Test
    public void setterHandle() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.getSetterHandle(field);
        Person person = new Person();
        handle.invoke(person, "jerry");
        Assertions.assertEquals("jerry", person.getName());

    }

    @Test
    public void getterSetterCache() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle h1 = CMethodHandleUtils.getGetterHandle(field);
        MethodHandle h2 = CMethodHandleUtils.getGetterHandle(field);
        Assertions.assertSame(h1, h2);

    }

    @Test
    public void methodHandle() throws Throwable {

        Method method = Person.class.getDeclaredMethod("secret");
        MethodHandle handle = CMethodHandleUtils.getHandle(method);
        Person person = Person.builder().name("tom").build();
        Assertions.assertEquals("secret-tom", handle.invoke(person));

    }

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

    @Test
    public void constructorHandle() throws Throwable {

        Constructor<?> constructor = Person.class.getDeclaredConstructor(String.class, Integer.class);
        MethodHandle handle = CMethodHandleUtils.getHandle(constructor);
        Person person = (Person) handle.invoke("alice", 18);
        Assertions.assertEquals("alice", person.getName());
        Assertions.assertEquals(18, person.getAge());

    }

    @Test
    public void constructorHandleCache() throws Throwable {

        Constructor<?> constructor = Person.class.getDeclaredConstructor(String.class, Integer.class);
        MethodHandle h1 = CMethodHandleUtils.getHandle(constructor);
        MethodHandle h2 = CMethodHandleUtils.getHandle(constructor);
        Assertions.assertSame(h1, h2);

    }

    @Test
    public void toGetterHandle() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.toGetterHandle(field);
        Person person = Person.builder().name("tom").build();
        Assertions.assertEquals("tom", handle.invoke(person));

    }

    @Test
    public void toSetterHandle() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.toSetterHandle(field);
        Person person = new Person();
        handle.invoke(person, "jerry");
        Assertions.assertEquals("jerry", person.getName());

    }

    @Test
    public void getGetterHandleAsType() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.getGetterHandleAsType(field);
        Person person = Person.builder().name("tom").build();
        // 统一 Object 签名，接收者显式转型后 invokeExact 直接调用
        Assertions.assertEquals("tom", handle.invokeExact((Object) person));

    }

    @Test
    public void getGetterHandleAsTypePrimitive() throws Throwable {

        Field field = PrimitiveHolder.class.getDeclaredField("value");
        MethodHandle handle = CMethodHandleUtils.getGetterHandleAsType(field);
        PrimitiveHolder holder = new PrimitiveHolder();
        holder.setValue(18);
        // 原始类型字段装箱后以 Object 返回
        Assertions.assertEquals(18, handle.invokeExact((Object) holder));

    }

    @Test
    public void setGetterHandleAsType() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.getSetterHandleAsType(field);
        Person person = new Person();
        handle.invokeExact((Object) person, (Object) "jerry");
        Assertions.assertEquals("jerry", person.getName());

    }

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

        /** 姓名 */
        String name;

        /** 年龄 */
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

        /** 值（原始类型） */
        int value;

    }

}
