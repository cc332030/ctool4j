package com.c332030.ctool4j.spring.util;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * <p>
 * Description: CProxyUtilsPerfTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>被测方法是公共流程热路径（日志、幂等键、路由、指标等每次请求都会取名），故按**对比测试**验证：
 *       同一对象形态下横比「多种取值实现」，再纵比「各类对象形态」，判定哪种实现与哪个形态是成本大头。</li>
 *   <li>参与对比的实现固定为 5 类（同类实现方式的多种写法），覆盖「直接取名」基线到「含反射与类层次解析」的各种路径：
 *       ① 基线 {@code object.getClass().getName()}；② 非代理快速返回 + 惰性解包装；③ 非 Spring 代理快速返回
 *       （{@code instanceof Advised} 直接返回自身）；④ 当前实现（先 {@code AopUtils#getTargetClass} 再取目标实例）；
 *       ⑤ 全程 Spring 工具（{@code AopUtils#isAopProxy} + {@code AopUtils#getTargetClass}）。</li>
 *   <li>被对比的 ②③④⑤ 都是独立复刻的本地实现：性能测试要横比「同一个被测语义的多种写法」，
 *       而主代码里只保留最终选定的那一种，其余写法只存在于本用例中（不新增主代码节点供测试调用）。</li>
 *   <li>对象形态覆盖代理元数据的全部形态：普通对象、Spring CGLIB 代理（有目标实例）、
 *       Spring JDK 动态代理（有目标实例）、Spring JDK 动态代理（无目标实例）、裸 JDK 动态代理（无任何 Spring 元数据）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据公共测试规范「性能测试」：对比测试、实现方式至少 3 类、测两遍（第二次在各实现均初始化完成后进行）、
 *       结果须分析差异原因并给出方案。</li>
 *   <li>依据测试方法（等价类/边界值）：对象形态按「是否代理、是否 Spring 代理、是否持有目标实例」划分为 5 个等价类；
 *       代理类型按 JDK 动态代理 / CGLIB 划分；无目标实例的 Spring 代理与裸 JDK 代理是「解析不到目标」的边界。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：五类实现 × 五类对象形态的单次调用成本；各实现的取值等价性（性能对比不得以改变行为为代价）；第二遍
 *       （全部实现初始化完成后）的复测。</li>
 *   <li>未覆盖：真实 Spring 容器装配出的代理（与 {@code ProxyFactory} 产出的代理同类，本用例不再借助容器）；
 *       高并发下的吞吐（本用例度量的是单次调用成本，并发行为不改变单次成本的相对关系）；
 *       多层代理嵌套（代理解包的成本差异已在「有无目标实例」两个形态里体现）。</li>
 * </ul>
 * <h2>对比结果的使用方式</h2>
 * <ul>
 *   <li>量级断言只做**量级**校验：解析实现相对基线 {@code getClass()} 允许 100 倍以内的数量级差异
 *       （实测为几倍到几十倍，阈值放宽以吸收共享 CI 机器的时钟与调优噪声），防止后续把能走
 *       「非代理快速返回」的场景退化成每次全量解析（或反过来引入更重的实现）而无人察觉。</li>
 * </ul>
 * <h2>取值成本对比</h2>
 * <ul>
 *   <li>1.1 getRealClassName：五类对象形态下各实现的单次调用成本与等价性（getRealClassName）</li>
 * </ul>
 *
 * @see CProxyUtils
 * @see CProxyUtilsTests
 * @since 2026/9/12
 * @version 1.0
 */
public class CProxyUtilsPerfTests {

    /**
     * 参与对比的实现的编号（Map 保持插入顺序，输出稳定）
     */
    private static final String[] CONTRIVANCES = {
        "baseline getClass", "instanceof fast path", "Advised lazy unwrap", "current impl", "AopUtils only"
    };

    /**
     * 预热次数：让被测方法与各实现的调用点先完成 JIT 编译
     */
    private static final int WARMUP = 200_000;

    /**
     * 单次计时轮次的调用次数
     */
    private static final int ITERATIONS = 500_000;

    /**
     * 计时轮次（取中位数，抑制共享机器上的时钟与调优噪声）
     */
    private static final int ROUNDS = 5;

    /**
     * 业务接口（JDK 动态代理的目标接口）
     */
    public interface IOrderService {

        /**
         * 获取服务名
         *
         * @return 服务名
         */
        String getServiceName();

    }

    /**
     * 业务实现：既是 JDK 动态代理的目标，也是 Spring CGLIB 代理的父类
     */
    public static class OrderService implements IOrderService {

        /**
         * 获取服务名
         *
         * @return 服务名
         */
        @Override
        public String getServiceName() {
            return "order";
        }

    }

    /**
     * 测试取值成本对比
     * 对应测试用例 1.1：五类对象形态 × 五类实现的单次调用成本与取值等价性
     */
    @Test
    public void realClassNameCost() {

        val samples = newSamples();
        val controvances = newContrivances();

        // 第一遍：为各实现建立「全部实现均已初始化」的前置状态（首次结果含 JIT 与缓存准备开销，不计入结论）
        val expectedContrivanceCount = controvances.size();
        val warmupRound = measure(samples, controvances);
        Assertions.assertEquals(
            expectedContrivanceCount,
            warmupRound.values().iterator().next().size()
        );

        // 第二遍：全部实现初始化完成后复测，作为结论依据
        val conclusion = measure(samples, controvances);

        report(samples.keySet(), controvances.keySet(), conclusion);
        assertEquivalenceAndMagnitude(samples, controvances, conclusion);

    }

    /**
     * 五类对象形态的样本集合（键为形态名）
     *
     * @return 对象形态样本，按插入顺序输出
     */
    private Map<String, Object> newSamples() {

        val plain = new OrderService();

        val cglibFactory = new ProxyFactory();
        cglibFactory.setTarget(plain);
        cglibFactory.setProxyTargetClass(true);

        val jdkFactory = new ProxyFactory();
        jdkFactory.setInterfaces(IOrderService.class);
        jdkFactory.setTarget(plain);

        val jdkWithoutTarget = new ProxyFactory(IOrderService.class).getProxy();

        val bareJdk = Proxy.newProxyInstance(
            CProxyUtilsPerfTests.class.getClassLoader(),
            new Class<?>[]{IOrderService.class},
            (proxy, method, args) -> null
        );

        val samples = new LinkedHashMap<String, Object>();
        samples.put("plain", plain);
        samples.put("springCglibProxy", cglibFactory.getProxy());
        samples.put("springJdkProxy", jdkFactory.getProxy());
        samples.put("springJdkNoTarget", jdkWithoutTarget);
        samples.put("bareJdkProxy", bareJdk);

        return samples;
    }

    /**
     * 五类取值实现的集合（键为实现名）
     * <p>各实现均返回「真实业务类全限定名」，与 {@link CProxyUtils#getRealClassName(Object)} 语义一致。</p>
     *
     * @return 取值实现
     */
    private Map<String, Function<Object, String>> newContrivances() {

        val controvances = new LinkedHashMap<String, Function<Object, String>>();

        // ① 基线：直接取名，代表「不做任何解析」的不可再优化的下限
        controvances.put(CONTRIVANCES[0], object -> object.getClass().getName());

        // ② 非 Spring 代理快速返回 + 惰性解包装
        controvances.put(CONTRIVANCES[1], CProxyUtilsPerfTests::instanceofFastPath);

        // ③ 非 Spring 代理快速返回（后续交给 Spring 工具解析）
        controvances.put(CONTRIVANCES[2], CProxyUtilsPerfTests::advisedFastPath);

        // ④ 当前实现
        controvances.put(CONTRIVANCES[3], CProxyUtils::getRealClassName);

        // ⑤ 全程 Spring 工具
        controvances.put(CONTRIVANCES[4], object -> AopUtils.getTargetClass(object).getName());

        return controvances;
    }

    /**
     * 实现②：非 Spring 代理直接取名，否则惰性剥离多层代理取目标实例的实际类型
     *
     * @param object 待解析对象
     * @return 真实业务类全限定名
     */
    private static String instanceofFastPath(Object object) {

        if (null == object) {
            return null;
        }

        if (!(object instanceof Advised)) {
            return object.getClass().getName();
        }

        Object current = object;
        while (current instanceof Advised) {

            val next = getTarget(current);
            if (null == next || next == current) {
                break;
            }

            current = next;
        }

        if (current != object) {
            return current.getClass().getName();
        }

        val targetClass = AopUtils.getTargetClass(object);
        return null == targetClass ? object.getClass().getName() : targetClass.getName();
    }

    /**
     * 实现③：非 Spring 代理直接取名，否则交给 Spring 工具解析静态目标类型
     *
     * @param object 待解析对象
     * @return 真实业务类全限定名
     */
    private static String advisedFastPath(Object object) {

        if (null == object || !(object instanceof Advised)) {
            return null == object ? null : object.getClass().getName();
        }

        val target = getTarget(object);
        if (null != target && target != object) {
            return target.getClass().getName();
        }

        return AopUtils.getTargetClass(object).getName();
    }

    /**
     * 取一层代理的目标实例
     *
     * @param object 待解析对象
     * @return 目标实例；非 {@code Advised}、取不到或目标为自身时返回 null
     */
    private static Object getTarget(Object object) {

        if (!(object instanceof Advised)) {
            return null;
        }

        try {
            return ((Advised)object).getTargetSource().getTarget();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 测量每种对象形态下各实现的单次调用耗时（纳秒）
     * <p>每次调用都消费返回值（累加类名长度），避免循环体被 JIT 判定为无副作用而消除；
     * 每个实现取多轮中位数，降低共享机器上的噪声。</p>
     *
     * @param samples 对象形态样本
     * @param controvances 取值实现
     * @return 形态 → 实现 → 单次调用纳秒数
     */
    private Map<String, Map<String, Double>> measure(
        Map<String, Object> samples,
        Map<String, Function<Object, String>> controvances
    ) {

        val result = new LinkedHashMap<String, Map<String, Double>>();

        for (val sample : samples.entrySet()) {

            val perContrivance = new LinkedHashMap<String, Double>();
            for (val contrivance : controvances.entrySet()) {
                perContrivance.put(contrivance.getKey(), nanosPerCall(contrivance.getValue(), sample.getValue()));
            }

            result.put(sample.getKey(), perContrivance);
        }

        return result;
    }

    /**
     * 取单个实现对单个对象的单次调用纳秒数（多轮取中位数）
     *
     * @param contrivance 取值实现
     * @param object 待解析对象
     * @return 单次调用纳秒数
     */
    private double nanosPerCall(Function<Object, String> contrivance, Object object) {

        val rounds = new long[ROUNDS];
        val sink = new AtomicLong();

        for (int round = 0; round < ROUNDS; round++) {

            for (int i = 0; i < WARMUP; i++) {
                sink.addAndGet(contrivance.apply(object).length());
            }

            val started = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                sink.addAndGet(contrivance.apply(object).length());
            }
            rounds[round] = System.nanoTime() - started;
        }

        Assertions.assertNotEquals(Long.MIN_VALUE, sink.get());
        Arrays.sort(rounds);

        return rounds[ROUNDS / 2] / (double)ITERATIONS;
    }

    /**
     * 输出对比结果
     *
     * @param sampleNames 对象形态名
     * @param contrivanceNames 实现名
     * @param result 测量结果
     */
    private void report(
        Iterable<String> sampleNames,
        Iterable<String> contrivanceNames,
        Map<String, Map<String, Double>> result
    ) {

        val header = new StringBuilder("ns/call");
        for (val contrivanceName : contrivanceNames) {
            header.append(" | ").append(contrivanceName);
        }
        System.out.println(header);

        for (val sampleName : sampleNames) {

            val line = new StringBuilder(sampleName);
            val perContrivance = result.get(sampleName);
            for (val contrivanceName : contrivanceNames) {
                line.append(" | ").append(String.format("%.2f", perContrivance.get(contrivanceName)));
            }

            System.out.println(line);
        }
    }

    /**
     * 断言各实现的取值等价、且解析实现的成本在数量级内
     * <p>性能对比不得以改变行为为代价：除基线 {@code getClass()}（代理形态下与真实类语义不同）外，
     * 其余实现必须给出完全一致的类名。成本断言只做量级校验（≤ 100 倍基线），防止后续把可走快路径的场景退化成全量解析。</p>
     *
     * @param samples 对象形态样本
     * @param controvances 取值实现
     * @param result 测量结果
     */
    private void assertEquivalenceAndMagnitude(
        Map<String, Object> samples,
        Map<String, Function<Object, String>> controvances,
        Map<String, Map<String, Double>> result
    ) {

        val expectedBySample = new LinkedHashMap<String, String>();
        for (val sample : samples.entrySet()) {
            expectedBySample.put(sample.getKey(), CProxyUtils.getRealClassName(sample.getValue()));
        }

        for (val sample : samples.entrySet()) {

            val expected = expectedBySample.get(sample.getKey());
            for (val contrivance : controvances.entrySet()) {

                val actual = contrivance.getValue().apply(sample.getValue());
                if (CONTRIVANCES[0].equals(contrivance.getKey())) {
                    // 基线只取对象自身类型：代理形态下与真实业务类不同，普通对象下应一致
                    Assertions.assertEquals(sample.getValue().getClass().getName(), actual);
                    continue;
                }

                Assertions.assertEquals(expected, actual, sample.getKey() + " / " + contrivance.getKey());
            }
        }

        for (val perContrivance : result.values()) {

            val baseline = perContrivance.get(CONTRIVANCES[0]);
            for (val contrivanceName : CONTRIVANCES) {
                Assertions.assertTrue(
                    perContrivance.get(contrivanceName) <= baseline * 100,
                    "解析实现的单次成本超出基线两个数量级: " + contrivanceName + " = " + perContrivance.get(contrivanceName)
                );
            }
        }
    }

}
