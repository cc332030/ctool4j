package com.c332030.ctool4j.core.classes;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * <p>
 * Description: CElKeyResolveUtilsTests
 * </p>
 * <p>
 * 测试 {@link CElKeyResolveUtils}：简单 el 表达式解析、多级取值、null 处理、非法表达式、
 * 循环引用检测。仅测试纯逻辑，不依赖 Spring 容器。
 * </p>
 *
 * <p>
 * 是 {@link CElKeyResolveUtils} 的测试用例（对应测试文档
 * <code>doc/design/core/CElKeyResolveUtilsTests.adoc</code>）。
 * </p>
 *
 * @since 2026/9/8
 */
class CElKeyResolveUtilsTests {

    @Data
    @AllArgsConstructor
    static class Inner {

        Long id;
    }

    @Data
    @AllArgsConstructor
    static class Outer {

        Inner inner;
    }

    @Data
    @AllArgsConstructor
    static class Self {

        Long id;

        Self manager;
    }

    @Data
    @AllArgsConstructor
    static class Address {

        String code;
    }

    @Data
    @AllArgsConstructor
    static class Contact {

        Address address;
    }

    @Data
    @AllArgsConstructor
    static class Person {

        Contact contact;
    }

    // ===== 供反射取方法签名（形参名依赖 -parameters 保留）=====

    static String keyOuter(Outer outer) {
        return null;
    }

    static String keyInner(Outer outer, String tag) {
        return null;
    }

    static String keySelf(Self self, String suffix) {
        return null;
    }

    static String keyDeep(Person person) {
        return null;
    }

    private Method method(String name) {
        try {
            for (Method m : CElKeyResolveUtilsTests.class.getDeclaredMethods()) {
                if (m.getName().equals(name)) {
                    return m;
                }
            }
            throw new NoSuchMethodException(name);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 对应测试用例 1.1：一级表达式（仅参数名）返回参数对象本身
     */
    @Test
    void testResolve_singleLevel() {
        Method method = method("keyOuter");
        Inner inner = new Inner(1L);
        Outer outer = new Outer(inner);
        Object key = CElKeyResolveUtils.getResolver(method, "outer").resolve(new Object[] { outer });
        Assertions.assertSame(outer, key);
    }

    /**
     * 对应测试用例 1.2：多级表达式取属性对象
     */
    @Test
    void testResolve_twoLevel() {
        Method method = method("keyOuter");
        Inner inner = new Inner(1L);
        Outer outer = new Outer(inner);
        Object key = CElKeyResolveUtils.getResolver(method, "outer.inner").resolve(new Object[] { outer });
        Assertions.assertSame(inner, key);
    }

    /**
     * 对应测试用例 1.3：二级取到基础属性值（第二个参数不参与）
     */
    @Test
    void testResolve_propertyValue() {
        Method method = method("keySelf");
        Self self = new Self(42L, null);
        Object key = CElKeyResolveUtils.getResolver(method, "self.id").resolve(new Object[] { self, "t" });
        Assertions.assertEquals(42L, key);
    }

    /**
     * 对应测试用例 1.4：目标参数为 null 返回 null
     */
    @Test
    void testResolve_paramNull_returnsNull() {
        Method method = method("keyOuter");
        Object key = CElKeyResolveUtils.getResolver(method, "outer.inner").resolve(new Object[] { null });
        Assertions.assertNull(key);
    }

    /**
     * 对应测试用例 1.5：属性链某级为 null 返回 null
     */
    @Test
    void testResolve_middleNull_returnsNull() {
        Method method = method("keyOuter");
        Outer outer = new Outer(null);
        Object key = CElKeyResolveUtils.getResolver(method, "outer.inner").resolve(new Object[] { outer });
        Assertions.assertNull(key);
    }

    /**
     * 对应测试用例 1.6：表达式为空白抛异常
     */
    @Test
    void testParse_blankExpr_throws() {
        Method method = method("keyOuter");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> CElKeyResolveUtils.getResolver(method, "  "));
    }

    /**
     * 对应测试用例 1.7：参数名不存在抛异常
     */
    @Test
    void testParse_paramNotExist_throws() {
        Method method = method("keyOuter");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> CElKeyResolveUtils.getResolver(method, "notExist.inner"));
    }

    /**
     * 对应测试用例 1.8：非法段（连续点）抛异常
     */
    @Test
    void testParse_illegalSegment_throws() {
        Method method = method("keyOuter");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> CElKeyResolveUtils.getResolver(method, "outer..inner"));
    }

    /**
     * 对应测试用例 1.9：运行期属性在某实际类型不可解析抛异常
     */
    @Test
    void testResolve_propNotResolvable_throws() {
        Method method = method("keyOuter");
        Outer outer = new Outer(new Inner(1L));
        // inner 上没有 manager 属性
        Assertions.assertThrows(IllegalStateException.class,
            () -> CElKeyResolveUtils.getResolver(method, "outer.inner.manager")
                .resolve(new Object[] { outer }));
    }

    /**
     * 对应测试用例 1.10：循环引用（同一对象实例在链中重复访问）运行期抛异常
     */
    @Test
    void testResolve_cycle_throws() {
        Method method = method("keySelf");
        Self self = new Self(1L, null);
        self.manager = self; // 自引用形成环
        Assertions.assertThrows(IllegalStateException.class,
            () -> CElKeyResolveUtils.getResolver(method, "self.manager.id")
                .resolve(new Object[] { self, "t" }));
    }

    /**
     * 对应测试用例 1.11：合法链不误报循环引用
     */
    @Test
    void testResolve_noCycle() {
        Method method = method("keySelf");
        Self manager = new Self(2L, null);
        Self self = new Self(1L, null);
        self.manager = manager; // manager 是另一个实例，不成环
        Object key = CElKeyResolveUtils.getResolver(method, "self.manager.id")
            .resolve(new Object[] { self, "t" });
        Assertions.assertEquals(2L, key);
    }

    /**
     * 对应测试用例 1.12：表达式引用第二个参数（参数下标 > 0）
     */
    @Test
    void testResolve_secondParam() {
        Method method = method("keyInner");
        Outer outer = new Outer(new Inner(1L));
        Object key = CElKeyResolveUtils.getResolver(method, "tag")
            .resolve(new Object[] { outer, "TAG" });
        Assertions.assertEquals("TAG", key);
    }

    /**
     * 对应测试用例 1.13：三级以上深层属性链取值
     */
    @Test
    void testResolve_deepChain() {
        Method method = method("keyDeep");
        Person person = new Person(new Contact(new Address("CN")));
        Object key = CElKeyResolveUtils.getResolver(method, "person.contact.address.code")
            .resolve(new Object[] { person });
        Assertions.assertEquals("CN", key);
    }

    /**
     * 对应测试用例 1.14：深层链中段某级为 null 返回 null
     */
    @Test
    void testResolve_deepChainMiddleNull_returnsNull() {
        Method method = method("keyDeep");
        Person person = new Person(new Contact(null));
        Object key = CElKeyResolveUtils.getResolver(method, "person.contact.address.code")
            .resolve(new Object[] { person });
        Assertions.assertNull(key);
    }

    /**
     * 对应测试用例 1.15：深层链某实际类型属性不可解析抛异常
     */
    @Test
    void testResolve_deepChainPropNotResolvable_throws() {
        Method method = method("keyDeep");
        Person person = new Person(new Contact(new Address("CN")));
        // address 上没有 codeOf 属性
        Assertions.assertThrows(IllegalStateException.class,
            () -> CElKeyResolveUtils.getResolver(method, "person.contact.address.codeOf")
                .resolve(new Object[] { person }));
    }

}
