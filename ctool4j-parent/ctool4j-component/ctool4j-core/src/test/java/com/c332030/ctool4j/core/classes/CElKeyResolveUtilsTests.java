package com.c332030.ctool4j.core.classes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
 * 是 {@link CElKeyResolveUtils} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>仅测试纯逻辑，不依赖 Spring 容器与 Redis。</li>
 *   <li>通过反射取测试类内带形参名的方法（父 pom 编译开启 {@code -parameters}，保证参数名可靠），据此解析表达式。</li>
 *   <li>覆盖表达式层级、取值形态、null 边界、非法表达式、运行期属性缺失、循环引用。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 {@code getResolver}/{@code resolve} 的约定（见 CElKeyResolveUtils.adoc）。</li>
 *   <li>依据白盒/黑盒原则：多级取值、null、非法段、参数名缺失、运行期不可解析、循环引用均需覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：一级/多级表达式、属性值、参数为 null、链中某级 null、空白表达式、参数名不存在、非法段（含末尾空段）、运行期属性缺失、循环引用、无环不误报、第二参数引用、三级以上深链、同一方法多表达式互不串用（含<strong>缓存条目分别持有</strong>与<strong>并发交替</strong>）。</li>
 *   <li>未覆盖：真实 Spring AOP 拦截下的端到端取 key（由 cache 模块集成测试覆盖）；接口/泛型动态类型（运行期验证）。</li>
 * </ul>
 * <h2>CElKeyResolveUtils 解析与取值</h2>
 * <ul>
 *   <li>1.1 一级表达式（仅参数名）：返回参数对象本身（testResolve_singleLevel）</li>
 *   <li>1.2 多级表达式：取属性对象（testResolve_twoLevel）</li>
 *   <li>1.3 二级取基础属性值（testResolve_propertyValue）</li>
 *   <li>1.4 目标参数为 null：返回 null（testResolve_paramNull_returnsNull）</li>
 *   <li>1.5 属性链某级为 null：返回 null（testResolve_middleNull_returnsNull）</li>
 *   <li>1.6 表达式为空白：抛异常（testParse_blankExpr_throws）</li>
 *   <li>1.7 参数名不存在：抛异常（testParse_paramNotExist_throws）</li>
 *   <li>1.8 非法段（连续点）：抛异常（testParse_illegalSegment_throws）</li>
 *   <li>1.9 运行期属性在某实际类型不可解析：抛异常（testResolve_propNotResolvable_throws）</li>
 *   <li>1.10 循环引用（同实例链中重复）：抛异常（testResolve_cycle_throws）</li>
 *   <li>1.11 合法链不误报循环引用（testResolve_noCycle）</li>
 *   <li>1.12 引用第二个参数（参数下标 &gt; 0）：取第二实参（testResolve_secondParam）</li>
 *   <li>1.13 三级以上深层属性链取值（testResolve_deepChain）</li>
 *   <li>1.14 深层链中段某级为 null：返回 null（testResolve_deepChainMiddleNull_returnsNull）</li>
 *   <li>1.15 深层链某实际类型属性不可解析：抛异常（testResolve_deepChainPropNotResolvable_throws）</li>
 *   <li>1.16 表达式以点结尾（末尾空段）：抛异常，不再被静默接受（testParse_trailingDot_throws）</li>
 *   <li>1.17 同一方法的不同表达式各自独立解析（testResolve_sameMethodDifferentExprs）</li>
 *   <li>1.18 同一方法的不同表达式反复交替调用仍各取各值（testResolve_sameMethodDifferentExprs_repeat）</li>
 *   <li>1.19 同一方法不同表达式在多线程并发下仍各取各值（testResolve_sameMethodDifferentExprs_concurrent）</li>
 * </ul>
 *
 * @since 2026/9/8
 * @version 1.2
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
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
            () -> CElKeyResolveUtils.getResolver(method, "  "));
    }

    /**
     * 对应测试用例 1.7：参数名不存在抛异常
     */
    @Test
    void testParse_paramNotExist_throws() {
        Method method = method("keyOuter");
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
            () -> CElKeyResolveUtils.getResolver(method, "notExist.inner"));
    }

    /**
     * 对应测试用例 1.8：非法段（连续点）抛异常
     */
    @Test
    void testParse_illegalSegment_throws() {
        Method method = method("keyOuter");
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
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
        Assertions.assertThrowsExactly(IllegalStateException.class,
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
        Assertions.assertThrowsExactly(IllegalStateException.class,
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
        Assertions.assertThrowsExactly(IllegalStateException.class,
            () -> CElKeyResolveUtils.getResolver(method, "person.contact.address.codeOf")
                .resolve(new Object[] { person }));
    }

    /**
     * 对应测试用例 1.16：表达式以点结尾（末尾空段）抛异常
     *
     * <p>回归点：{@code String#split("\\.")} 默认丢弃末尾空串，{@code "outer."} 会被切成
     * {@code ["outer"]}，末尾空段逃过段校验而被静默接受（解析结果与 {@code "outer"} 完全相同）。
     * 修法为 {@code split("\\." , -1)} 保留末尾空串。本用例断言其按"非法段"报错，
     * 以固化"空段一律报错"的契约。</p>
     */
    @Test
    void testParse_trailingDot_throws() {
        Method method = method("keyOuter");
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
            () -> CElKeyResolveUtils.getResolver(method, "outer."));
        // 仅由点构成的表达式同样应报错
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
            () -> CElKeyResolveUtils.getResolver(method, "."));
    }

    /**
     * 对应测试用例 1.17：同一方法的不同表达式各自独立解析
     *
     * <p>回归点：解析器缓存原以 {@code Method} 为 key，同一方法先解析的表达式会成为后续所有
     * 表达式的解析器（后一个表达式静默沿用前一个，取错值）。修法为缓存按「方法 + 表达式」两级。
     * 本用例用同一方法依次取两个不同表达式，断言各自取到对应属性值。</p>
     */
    @Test
    void testResolve_sameMethodDifferentExprs() {
        Method method = method("keySelf");
        Self self = new Self(7L, null);
        self.manager = new Self(9L, null);

        Object byId = CElKeyResolveUtils.getResolver(method, "self.id")
            .resolve(new Object[] { self, "t" });
        Object byManagerId = CElKeyResolveUtils.getResolver(method, "self.manager.id")
            .resolve(new Object[] { self, "t" });

        Assertions.assertEquals(7L, byId);
        Assertions.assertEquals(9L, byManagerId);
    }

    /**
     * 对应测试用例 1.18：同一方法的不同表达式反复交替调用仍各取各值
     *
     * <p>与 1.17 互补：1.17 只覆盖"先 A 后 B"，本用例覆盖"反复交替"，
     * 确认缓存命中路径（{@code getIfPresent} 直接返回）不会因缓存二次命中也串用表达式。</p>
     */
    @Test
    void testResolve_sameMethodDifferentExprs_repeat() {
        Method method = method("keySelf");
        Self self = new Self(1L, null);
        self.manager = new Self(2L, null);
        Object[] args = new Object[] { self, "t" };

        for (int i = 0; i < 3; i++) {
            Assertions.assertEquals(1L,
                CElKeyResolveUtils.getResolver(method, "self.id").resolve(args));
            Assertions.assertEquals(2L,
                CElKeyResolveUtils.getResolver(method, "self.manager.id").resolve(args));
        }
    }

    /**
     * 对应测试用例 1.19：同一方法的不同表达式在多线程并发下仍各取各值（线程安全）
     *
     * <p>并发的意义在于「线程安全」而非「竞态检出」：{@code getResolver} 的缓存读路径
     * （{@code getIfPresent}）与外层 {@code computeIfAbsent} 都是并发入口，须确认多线程下两个表达式
     * 的取值始终各归各、不因并发装载而串用。</p>
     *
     * <p><b>子线程断言须解包</b>：{@code Callable} 内抛出的 {@code AssertionError} 会被
     * {@code Future.get()} 包成 {@code ExecutionException}，JUnit 不会自动解包——只调 {@code get()}
     * 的写法在断言失败时仍是用例通过（断言失效），故经 {@link #awaitAssertions(Future[])} 解包。</p>
     */
    @Test
    void testResolve_sameMethodDifferentExprs_concurrent() throws Exception {

        Method method = method("keyInner");
        Outer outer = new Outer(new Inner(1L));
        Object[] args = new Object[] { outer, "TAG" };

        val byId = (Callable<Object>)() -> {
            for (int i = 0; i < 20; i++) {
                Assertions.assertEquals(1L,
                    CElKeyResolveUtils.getResolver(method, "outer.inner.id").resolve(args));
            }
            return null;
        };
        val byTag = (Callable<Object>)() -> {
            for (int i = 0; i < 20; i++) {
                Assertions.assertEquals("TAG",
                    CElKeyResolveUtils.getResolver(method, "tag").resolve(args));
            }
            return null;
        };

        val pool = Executors.newFixedThreadPool(4);
        try {
            // 子线程的断言失败经 Future.get() 抛 ExecutionException，须解包成 AssertionError，
            // 否则用例会「断言失败但仍通过」（JUnit 5 不会自动解包；assertAll 只接受 Executable、
            // 不能直接传 Future）
            awaitAssertions(pool.submit(byId), pool.submit(byTag), pool.submit(byId), pool.submit(byTag));
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * 等待并发任务结束并把子线程的断言失败解包为 {@link AssertionError}
     *
     * <p>{@code Callable} 内抛出的 {@code AssertionError} 会被 {@code Future.get()} 包进
     * {@code ExecutionException}——不解包就等于没断言，用例恒通过。本方法逐个 get 并解包，
     * 使子线程的断言失败如实反映到用例结果上。</p>
     *
     * @param futures 并发任务
     */
    private static void awaitAssertions(Future<?>... futures) throws Exception {

        for (val future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new AssertionError(e.getCause());
            }
        }
    }
}
