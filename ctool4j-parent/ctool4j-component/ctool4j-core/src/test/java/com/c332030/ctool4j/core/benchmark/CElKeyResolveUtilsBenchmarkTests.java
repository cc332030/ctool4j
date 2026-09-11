package com.c332030.ctool4j.core.benchmark;

import cn.hutool.core.bean.BeanUtil;
import com.c332030.ctool4j.core.classes.CElKeyResolveUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Description: CElKeyResolveUtils 性能对比基准
 * </p>
 * <p>
 * 对比维度：简单 el 属性链表达式（{@code person.contact.address.code}）的取值性能。
 * 每个维度覆盖多类实现方式（实现原理各不相同，满足对比类别 ≥3 类）：
 * </p>
 * <ul>
 *     <li>MethodHandle + 解析缓存：CElKeyResolveUtils（被测，表达式一次解析按方法缓存、getter 按类缓存）</li>
 *     <li>原生反射：每次调用逐级 {@code getDeclaredField + Field.get}（无任何缓存）</li>
 *     <li>hutool {@code BeanUtil.getProperty}（反射实现，内部有缓存）</li>
 *     <li>编译期直接赋值（基线）：手工 getter 链</li>
 * </ul>
 * <p>
 * 预期：CElKeyResolveUtils 通过"解析缓存 + MethodHandle 缓存"接近编译期基线，
 * 显著优于每次全量解析的原生反射。
 * </p>
 *
 * <h2>被测对象</h2>
 * <p>简单 el 属性链表达式取值，代表性场景：</p>
 * <ul>
 *   <li>深链（多级属性跳）：{@code person.contact.address.code}，验证多级 MethodHandle getter 链。</li>
 *   <li>一级（仅参数名）：{@code user}，验证无属性跳的最快路径（零分配直接返回参数对象）。</li>
 * </ul>
 * <h2>对比维度（≥3 类实现，实现原理各不相同）</h2>
 * <ul>
 *   <li>{@code CElKeyResolveUtils}（被测）：表达式一次解析按方法缓存 + getter 按类缓存 MethodHandle。</li>
 *   <li>原生反射：每次调用逐级 {@code getDeclaredField + Field.get}，无任何缓存（最朴素实现）。</li>
 *   <li>hutool {@code BeanUtil.getProperty}：反射实现，内部有缓存。</li>
 *   <li>编译期直接赋值（基线）：手工 getter 链。</li>
 * </ul>
 * <h2>执行方式</h2>
 * <ul>
 *   <li>性能测试独立于单元测试，仅在明确命令执行时才运行（{@code mvn test -Dtest=CElKeyResolveUtilsBenchmarkTests}）。</li>
 *   <li>排除初始化干扰：先对所有用例做一轮全局预热，触发全部实现方式初始化/加载，首次结果不计入。</li>
 *   <li>充分预热 + 足够迭代：预热 50 万次触发 JIT 至 C2 稳态，单轮计时 100 万次、取 5 轮平均，降低测量噪声。</li>
 *   <li>结果写入 {@code tmp/benchmark-report-celkeyresolver.md} 报告，分析性能差异原因并给出方案。</li>
 * </ul>
 * <h2>基准执行</h2>
 * <ul>
 *   <li>1.1 benchmark：CElKeyResolveUtils el 属性链取值性能对比基准（多实现方式对比）</li>
 * </ul>
 *
 * @since 2026/9/8
 * @version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CElKeyResolveUtilsBenchmarkTests {

    /**
     * 基准执行入口（显式运行：mvn test -Dtest=CElKeyResolveUtilsBenchmarkTests -DfailIfNoTests=false）
     * 性能测试类，surefire 打包/常规测试时排除（命名以 BenchmarkTests 结尾）
     * 对应测试用例 1.1：CElKeyResolveUtils el 属性链取值性能对比基准（多实现方式对比）
     */
    @Test
    public void benchmark() {
        CBenchmarkReport report = CBenchmarkRunner.run(cases(), "CElKeyResolveUtils el 属性链取值性能对比");
        Path reportPath = Paths.get(System.getProperty("user.dir"), "tmp", "benchmark-report-celkeyresolver.md");
        report.writeTo(reportPath);
        System.out.println("性能测试报告已写入: " + reportPath.toAbsolutePath());
    }

    /**
     * 基准用例列表
     */
    public static List<CBenchmarkCase> cases() {
        return Arrays.asList(
            new CElKeyResolveUtilsDeepChainCase(),
            new RawReflectDeepChainCase(),
            new HutoolPropertyDeepChainCase(),
            new ManualGetterDeepChainCase(),

            new CElKeyResolveUtilsSingleCase(),
            new RawReflectSingleCase(),
            new ManualGetterSingleCase()
        );
    }

    // ===== 被测数据模型（深链 + 一级）=====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {

        private String code;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {

        private Address address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Person {

        private Contact contact;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {

        private Long id;
    }

    // ===== 供反射取方法签名（形参名依赖 -parameters 保留）=====

    static String deepKey(Person person) {
        return null;
    }

    static String singleKey(User user) {
        return null;
    }

    private static Person newDeepPerson() {
        return new Person(new Contact(new Address("CN")));
    }

    private static User newSingleUser() {
        return new User(42L);
    }

    private static Method method(String name) {
        for (Method m : CElKeyResolveUtilsBenchmarkTests.class.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new IllegalStateException("method not found: " + name);
    }

    /**
     * CElKeyResolveUtils 深链：getResolver 命中缓存后反复 resolve（表达式一次解析、getter 按类缓存）
     */
    private static class CElKeyResolveUtilsDeepChainCase implements CBenchmarkCase {

        private Object[] args;

        private CElKeyResolveUtils.Resolver resolver;

        @Override
        public String name() {
            return "CElKeyResolveUtils(深链)";
        }

        @Override
        public void prepare() {
            args = new Object[] { newDeepPerson() };
            resolver = CElKeyResolveUtils.getResolver(method("deepKey"), "person.contact.address.code");
        }

        @Override
        public Object run() {
            return resolver.resolve(args);
        }
    }

    /**
     * CElKeyResolveUtils 一级：仅参数名（无属性跳，最高频零分配路径）
     */
    private static class CElKeyResolveUtilsSingleCase implements CBenchmarkCase {

        private Object[] args;

        private CElKeyResolveUtils.Resolver resolver;

        @Override
        public String name() {
            return "CElKeyResolveUtils(一级)";
        }

        @Override
        public void prepare() {
            args = new Object[] { newSingleUser() };
            resolver = CElKeyResolveUtils.getResolver(method("singleKey"), "user");
        }

        @Override
        public Object run() {
            return resolver.resolve(args);
        }
    }

    /**
     * 原生反射（每次调用逐级 getDeclaredField + Field.get，无任何缓存，最朴素实现）
     */
    private static class RawReflectDeepChainCase implements CBenchmarkCase {

        private Person person;

        @Override
        public String name() {
            return "原生反射(深链)";
        }

        @Override
        public void prepare() {
            person = newDeepPerson();
        }

        @Override
        public Object run() {
            return fieldValue(fieldValue(fieldValue(person, "contact"), "address"), "code");
        }
    }

    /**
     * 原生反射（一级，Field.get 直接取参数值）
     */
    private static class RawReflectSingleCase implements CBenchmarkCase {

        private User user;

        @Override
        public String name() {
            return "原生反射(一级)";
        }

        @Override
        public void prepare() {
            user = newSingleUser();
        }

        @Override
        public Object run() {
            return fieldValue(user, "id");
        }
    }

    /**
     * hutool BeanUtil.getProperty（反射实现，内部有缓存，逐级 getProperty）
     */
    private static class HutoolPropertyDeepChainCase implements CBenchmarkCase {

        private Person person;

        @Override
        public String name() {
            return "hutool getProperty(深链)";
        }

        @Override
        public void prepare() {
            person = newDeepPerson();
        }

        @Override
        public Object run() {
            return BeanUtil.getProperty(person, "contact.address.code");
        }
    }

    /**
     * 编译期直接赋值（基线，getter 链）
     */
    private static class ManualGetterDeepChainCase implements CBenchmarkCase {

        private Person person;

        @Override
        public String name() {
            return "手工 getter(深链)";
        }

        @Override
        public void prepare() {
            person = newDeepPerson();
        }

        @Override
        public Object run() {
            return person.getContact().getAddress().getCode();
        }
    }

    /**
     * 编译期直接赋值（基线）
     */
    private static class ManualGetterSingleCase implements CBenchmarkCase {

        private User user;

        @Override
        public String name() {
            return "手工 getter(一级)";
        }

        @Override
        public void prepare() {
            user = newSingleUser();
        }

        @Override
        public Object run() {
            return user.getId();
        }
    }

    /**
     * 原生反射取值（抛异常则向上抛）
     */
    private static Object fieldValue(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("反射取值失败", e);
        }
    }

}
