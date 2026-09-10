package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.classes.CMethodHandleUtils;
import com.c332030.ctool4j.core.test.util.lazy.CLazyLambdaDepHolder;
import com.c332030.ctool4j.core.test.util.lazy.CLazyMethodRefDepHolder;
import com.c332030.ctool4j.core.util.CLazyRef;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Description: CLazyRefTests
 * </p>
 * <p>
 * 是 {@link CLazyRef} 的测试用例。
 * 重点验证懒加载、只求值一次、线程安全、null 缓存，两种环境（有依赖 / 无依赖），
 * 以及 supplier 写法（lambda 是否擦除返回类型、方法引用）对类加载时机的影响。
 * </p>
 *
 * @since 2026/9/10
 * @see "doc/design/core/CLazyRefTests.adoc"
 * @see "doc/design/core/CLazyRef.adoc"
 */
public class CLazyRefTests {

    /**
     * 探测标志：{@link Probe} 静态初始化时置位（读写不经过 Probe，避免探测本身触发其加载）
     */
    private static final AtomicBoolean PROBE_INITIALIZED = new AtomicBoolean(false);

    /**
     * 探测类：首次被初始化时置位 PROBE_INITIALIZED
     */
    static class Probe {

        static {
            PROBE_INITIALIZED.set(true);
        }
    }

    /**
     * 样本类：提供静态/实例工厂方法，供方法引用与静态方法初始化测试
     * <p>注：{@link #STATIC_CREATE_COUNT} 为静态共享计数器，由静态工厂 {@link Samples#staticCreate()}
     * 的调用方（用例 1.7 / 1.8 显式断言计数、用例 1.10 经 {@link #STATIC_LAZY} 间接调用）共享，
     * 用例 1.7 / 1.8 在用例内 {@code set(0)} 复位，故仅在测试串行执行下成立（本类未启用并行）。</p>
     */
    static class Samples {

        /**
         * 静态工厂调用次数（静态共享，用例内先复位；不适用于并行测试）
         */
        static final AtomicInteger STATIC_CREATE_COUNT = new AtomicInteger();

        /**
         * 静态工厂方法
         *
         * @return 样本对象
         */
        static Samples staticCreate() {
            STATIC_CREATE_COUNT.incrementAndGet();
            return new Samples();
        }

        /**
         * 实例工厂方法
         *
         * @return 样本对象
         */
        Samples instanceCreate() {
            return new Samples();
        }
    }

    /**
     * 静态字段懒加载（静态方法初始化）：类初始化即持有 CLazyRef，不立即求值
     */
    private static final CLazyRef<Samples> STATIC_LAZY = CLazyRef.of(Samples::staticCreate);

    /**
     * 对应测试用例 1.1：构造时不求值，首次 get 才求值（懒加载）
     */
    @Test
    public void get_lazy() {

        AtomicInteger count = new AtomicInteger();
        CLazyRef<Integer> lazy = CLazyRef.of(() -> count.incrementAndGet());

        // 构造后未调用 supplier
        Assertions.assertEquals(0, count.get(), "构造时不应求值");
        Assertions.assertEquals(1, lazy.get().intValue());
        Assertions.assertEquals(1, count.get(), "首次 get 应求值一次");
    }

    /**
     * 对应测试用例 1.2：只求值一次，多次 get 返回同一实例
     */
    @Test
    public void get_onlyOnce() {

        AtomicInteger count = new AtomicInteger();
        CLazyRef<Object> lazy = CLazyRef.of(() -> {
            count.incrementAndGet();
            return new Object();
        });

        Object first = lazy.get();
        Object second = lazy.get();

        Assertions.assertSame(first, second, "多次 get 应返回同一缓存实例");
        Assertions.assertEquals(1, count.get(), "supplier 只应被调用一次");
    }

    /**
     * 对应测试用例 1.3：supplier 返回 null 也缓存，不重复求值
     */
    @Test
    public void get_nullCached() {

        AtomicInteger count = new AtomicInteger();
        CLazyRef<Object> lazy = CLazyRef.of(() -> {
            count.incrementAndGet();
            return null;
        });

        Assertions.assertNull(lazy.get());
        Assertions.assertNull(lazy.get());
        Assertions.assertEquals(1, count.get(), "null 结果同样只求值一次");
    }

    /**
     * 对应测试用例 1.15：of(null) 抛异常（@NonNull 空值防御）
     * <p>注：Lombok {@code @NonNull} 生成的是 {@link IllegalArgumentException}（而非 {@code NullPointerException}）。</p>
     */
    @Test
    public void of_nullSupplier_throws() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> CLazyRef.of(null));
    }

    /**
     * 对应测试用例 1.4：并发 get 只求值一次（线程安全）
     */
    @Test
    public void get_concurrent_onlyOnce() throws InterruptedException {

        int threadCount = 8;
        AtomicInteger count = new AtomicInteger();
        CLazyRef<Object> lazy = CLazyRef.of(() -> {
            count.incrementAndGet();
            // 放大竞态窗口
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new Object();
        });

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        try {
            for (int i = 0; i < threadCount; i++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        lazy.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            Assertions.assertTrue(done.await(10, TimeUnit.SECONDS), "并发 get 超时");
        } finally {
            pool.shutdownNow();
        }

        Assertions.assertEquals(1, count.get(), "并发下 supplier 只应被调用一次");
    }

    /**
     * 对应测试用例 1.5：构造 CLazyRef 不触发 supplier 引用的类初始化（懒加载延迟类加载）
     */
    @Test
    public void of_shouldNotInitializeReferencedClass() {

        PROBE_INITIALIZED.set(false);

        // supplier 体内引用 Probe：lambda 体在 get() 时才执行，
        // 故仅构造 CLazyRef 不会触发 Probe 初始化
        CLazyRef<Probe> lazy = CLazyRef.of(() -> new Probe());

        Assertions.assertFalse(PROBE_INITIALIZED.get(), "构造 CLazyRef 时不应初始化被引用类");

        // 首次 get 才真正初始化并创建实例
        Probe probe = lazy.get();
        Assertions.assertNotNull(probe);
        Assertions.assertTrue(PROBE_INITIALIZED.get(), "首次 get 后应初始化被引用类");
    }

    /**
     * 对应测试用例 1.6：被引用类缺失（class not found）延迟到 get 时才抛出，构造不抛
     */
    @Test
    public void of_missingClass_doesNotThrowAtConstruction() {

        // 含不存在的类名：若构造时即解析类将抛 NoClassDefFoundError，本用例即失败
        CLazyRef<Class<?>> lazy = CLazyRef.of(() -> loadMissingClass());

        // 构造成功：未触发类加载
        Assertions.assertNotNull(lazy);

        // 首次 get 才触发类加载；supplier 内部把 ClassNotFoundException 包装为 IllegalStateException 抛出
        Assertions.assertThrowsExactly(IllegalStateException.class, lazy::get);
    }

    /**
     * 加载一个不存在的类，模拟可选依赖缺失的 class not found
     *
     * @return 类
     */
    private static Class<?> loadMissingClass() {
        try {
            return Class.forName("com.c332030.ctool4j.core.test.util.MissingClass");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("class not found", e);
        }
    }

    /**
     * 对应测试用例 1.7：静态方法引用作为 supplier，构造与取值均不抛异常
     * <p>注：本用例用方法引用，与用例 1.8 的 lambda 写法对比（均为"依赖已存在"的正常路径），
     * 局部抑制 IDE 无关告警以保留对比意图。</p>
     */
    @SuppressWarnings("all")
    @Test
    public void of_staticMethodReference_noException() {

        Samples.STATIC_CREATE_COUNT.set(0);

        CLazyRef<Samples> lazy = Assertions.assertDoesNotThrow(() -> CLazyRef.of(Samples::staticCreate));
        Samples samples = Assertions.assertDoesNotThrow(lazy::get);

        Assertions.assertNotNull(samples);
        // 只求值一次
        Assertions.assertSame(samples, lazy.get());
        Assertions.assertEquals(1, Samples.STATIC_CREATE_COUNT.get());
    }

    /**
     * 对应测试用例 1.8：lambda 表达式作为 supplier，构造与取值均不抛异常
     * <p>注：刻意用 lambda 与用例 1.7 的方法引用对比（均为"依赖已存在"的正常路径），
     * 抑制"可替换为方法引用"告警以保留对比意图。</p>
     */
    @SuppressWarnings("all")
    @Test
    public void of_lambda_noException() {

        Samples.STATIC_CREATE_COUNT.set(0);

        CLazyRef<Samples> lazy = Assertions.assertDoesNotThrow(() -> CLazyRef.of(() -> Samples.staticCreate()));
        Samples samples = Assertions.assertDoesNotThrow(lazy::get);

        Assertions.assertNotNull(samples);
        Assertions.assertSame(samples, lazy.get());
        Assertions.assertEquals(1, Samples.STATIC_CREATE_COUNT.get());
    }

    /**
     * 对应测试用例 1.9：实例方法引用作为 supplier，构造与取值均不抛异常
     */
    @Test
    public void of_instanceMethodReference_noException() {

        Samples holder = new Samples();
        CLazyRef<Samples> lazy = Assertions.assertDoesNotThrow(() -> CLazyRef.of(holder::instanceCreate));
        Samples samples = Assertions.assertDoesNotThrow(lazy::get);

        Assertions.assertNotNull(samples);
        Assertions.assertSame(samples, lazy.get());
    }

    /**
     * 对应测试用例 1.10：静态字段初始化（静态方法初始化），构造类不抛异常且懒加载
     */
    @Test
    public void staticField_lazy_noException() {

        // STATIC_LAZY 为类静态字段，此处访问不抛异常；首次 get 才求值，之后缓存
        Samples first = Assertions.assertDoesNotThrow(STATIC_LAZY::get);
        Samples second = Assertions.assertDoesNotThrow(STATIC_LAZY::get);

        Assertions.assertNotNull(first);
        Assertions.assertSame(first, second, "静态字段 CLazyRef 同样只求值一次");
    }

    // ================= 两种环境（有依赖 / 无依赖）=================

    private static final String LAMBDA_HOLDER = "com.c332030.ctool4j.core.test.util.lazy.CLazyLambdaDepHolder";
    private static final String METHOD_REF_HOLDER = "com.c332030.ctool4j.core.test.util.lazy.CLazyMethodRefDepHolder";
    private static final String OPTIONAL_DEP = "com.c332030.ctool4j.core.test.util.lazy.COptionalDep";

    /**
     * 环境 2（有依赖）：lambda 写法（返回类型擦除为 Object），初始化不报错、取值不报错
     * <p>注：本用例用 App 类加载器加载 Holder，与 1.13/1.14 的 {@link BlockingClassLoader} 相互隔离，
     * 故此处首次加载仍会真正触发静态初始化，{@code assertDoesNotThrow} 具备校验能力。</p>
     * 对应测试用例 1.11
     */
    @Test
    public void withDep_lambda_initAndGet_noException() {

        // 初始化：反射加载 Holder 类（首次加载会触发静态初始化）不报错
        Assertions.assertDoesNotThrow(
            () -> Class.forName(LAMBDA_HOLDER, true, CLazyRefTests.class.getClassLoader()));

        // 有依赖环境：直接类型引用（同时使该 Holder 被静态引用，避免"未使用"告警）
        Object value = Assertions.assertDoesNotThrow(CLazyLambdaDepHolder::dep);
        Assertions.assertNotNull(value);
        Assertions.assertEquals(OPTIONAL_DEP, value.getClass().getName());
    }

    /**
     * 环境 2（有依赖）：方法引用写法，初始化不报错、取值不报错
     * <p>注：同 1.11，App 类加载器加载与 1.13/1.14 隔离，首次加载即触发静态初始化。</p>
     * 对应测试用例 1.12
     */
    @Test
    public void withDep_methodRef_initAndGet_noException() {

        // 初始化：反射加载 Holder 类（首次加载会触发静态初始化）不报错
        Assertions.assertDoesNotThrow(
            () -> Class.forName(METHOD_REF_HOLDER, true, CLazyRefTests.class.getClassLoader()));

        // 有依赖环境：直接类型引用（同时使该 Holder 被静态引用，避免"未使用"告警）
        Object value = Assertions.assertDoesNotThrow(CLazyMethodRefDepHolder::dep);
        Assertions.assertNotNull(value);
        Assertions.assertEquals(OPTIONAL_DEP, value.getClass().getName());
    }

    /**
     * 环境 1（无依赖，屏蔽可选依赖类加载）：lambda 写法，初始化不报错、取值才报错
     * 对应测试用例 1.13
     */
    @Test
    public void withoutDep_lambda_initOk_getThrows() throws Exception {

        ClassLoader loader = new BlockingClassLoader(
            CLazyRefTests.class.getClassLoader(), OPTIONAL_DEP);

        // 初始化（加载 Holder 类）：不报错
        Class<?> holder = Assertions.assertDoesNotThrow(() -> Class.forName(LAMBDA_HOLDER, true, loader));

        // 取值：必须报错（无依赖）
        Throwable cause = Assertions.assertThrowsExactly(NoClassDefFoundError.class, () -> invokeDep(holder));
        Assertions.assertTrue(isMissingDependency(cause), "取值应因依赖缺失而报错: " + cause);
    }

    /**
     * 环境 1（无依赖，屏蔽可选依赖类加载）：方法引用写法，因方法引用必然引用目标类型，
     * 类初始化即报错（无法延迟到取值）——如实记录该限制。
     * 对应测试用例 1.14
     */
    @Test
    public void withoutDep_methodRef_initThrows() {

        ClassLoader loader = new BlockingClassLoader(
            CLazyRefTests.class.getClassLoader(), OPTIONAL_DEP);

        // 方法引用构造 supplier 时即需解析目标类型，故"初始化"（加载 Holder 类）即报错：
        // invokedynamic 引导失败抛 BootstrapMethodError，其 cause 为 NoClassDefFoundError
        Throwable cause = Assertions.assertThrowsExactly(
            BootstrapMethodError.class, () -> Class.forName(METHOD_REF_HOLDER, true, loader));
        Assertions.assertTrue(isMissingDependency(cause),
            "方法引用在无依赖环境应于初始化时报错: " + cause);
    }

    /**
     * 通过方法句柄调用 Holder.dep()（{@link java.lang.invoke.MethodHandle#invoke} 直接抛出原始异常，
     * 无需解包 {@code InvocationTargetException}）
     *
     * @param holder Holder 类
     * @return 取值结果
     */
    private static Object invokeDep(Class<?> holder) throws Throwable {
        Method method = holder.getMethod("dep");
        return CMethodHandleUtils.getHandle(method).invoke();
    }

    /**
     * 判定异常是否为"依赖缺失"（NoClassDefFoundError / ClassNotFoundException 及其包装）
     *
     * @param throwable 异常
     * @return 是否依赖缺失
     */
    private static boolean isMissingDependency(Throwable throwable) {
        Throwable current = throwable;
        while (null != current) {
            if (current instanceof NoClassDefFoundError
                || current instanceof ClassNotFoundException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * 屏蔽指定类加载的类加载器：对目标类抛 ClassNotFoundException，
     * 其余（含测试包）类交由自定义加载（不让 parent 看到被屏蔽类），模拟"无依赖"环境。
     * <p>限制：仅对 {@code com.c332030.ctool4j.core.test.util.lazy.} 包下的类强制由本加载器定义，
     * 其余类委派 parent；若被测类引用了测试包外、且该外部类又引用被屏蔽类，则屏蔽不生效。
     * 当前用例不涉及该情形。</p>
     */
    static class BlockingClassLoader extends ClassLoader {

        private final String blockedClass;

        BlockingClassLoader(ClassLoader parent, String blockedClass) {
            super(parent);
            this.blockedClass = blockedClass;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

            // 模拟依赖缺失：本类加载器看不到被屏蔽的类
            if (blockedClass.equals(name)) {
                throw new ClassNotFoundException(name + " (blocked: 模拟无依赖环境)");
            }

            // 测试包下的类强制由本加载器定义（不委派 parent），否则 parent 能看到被屏蔽依赖
            if (name.startsWith("com.c332030.ctool4j.core.test.util.lazy.")) {
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (null == loaded) {
                        loaded = defineFromParentResource(name);
                    }
                    if (resolve) {
                        resolveClass(loaded);
                    }
                    return loaded;
                }
            }

            return super.loadClass(name, resolve);
        }

        /**
         * 从 parent 的资源中读取字节，由本加载器定义（绕过 parent 的类加载）
         */
        private Class<?> defineFromParentResource(String name) throws ClassNotFoundException {
            String resource = name.replace('.', '/') + ".class";
            try (java.io.InputStream in = getParent().getResourceAsStream(resource)) {
                if (null == in) {
                    throw new ClassNotFoundException(name);
                }
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                byte[] bytes = out.toByteArray();
                return defineClass(name, bytes, 0, bytes.length);
            } catch (java.io.IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
    }

}
