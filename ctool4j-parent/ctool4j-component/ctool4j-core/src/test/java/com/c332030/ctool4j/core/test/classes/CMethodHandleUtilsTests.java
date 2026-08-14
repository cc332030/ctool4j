package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CMethodHandleUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * <p>
 * Description: CMethodHandleUtilsTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CMethodHandleUtilsTests {

    @Test
    public void getterHandle() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.getGetterHandle(field);
        Person person = new Person("tom");
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
        Person person = new Person("tom");
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

        Constructor<?> constructor = Person.class.getConstructor(String.class);
        MethodHandle handle = CMethodHandleUtils.getHandle(constructor);
        Person person = (Person) handle.invoke("alice");
        Assertions.assertEquals("alice", person.getName());

    }

    @Test
    public void constructorHandleCache() throws Throwable {

        Constructor<?> constructor = Person.class.getConstructor(String.class);
        MethodHandle h1 = CMethodHandleUtils.getHandle(constructor);
        MethodHandle h2 = CMethodHandleUtils.getHandle(constructor);
        Assertions.assertSame(h1, h2);

    }

    @Test
    public void toGetterHandle() throws Throwable {

        Field field = Person.class.getDeclaredField("name");
        MethodHandle handle = CMethodHandleUtils.toGetterHandle(field);
        Person person = new Person("tom");
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

    /**
     * 测试用 POJO
     */
    static class Person {

        private String name;

        public Person() {
        }

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private String secret() {
            return "secret-" + name;
        }

    }

}
