package com.c332030.ctool4j.core.benchmark;

import cn.hutool.core.bean.BeanUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.cglib.beans.BeanCopier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * <p>
 * Description: CBeanUtils 性能对比基准（单一性能测试类，三通道）
 * </p>
 *
 * <h2>口径说明（对比前提）</h2>
 * <ul>
 *   <li><b>简单 copy 通道</b>（{@link #simpleCopyBenchmark}）：被测 Bean 只含标量字段，通道内所有实现
 *   （手工 setter / cglib / Spring / hutool / CBeanUtils）做的是同一件事（标量赋值/类型转换），可直接横向对比。</li>
 *   <li><b>深拷贝通道</b>（{@link #deepCopyBenchmark}）：只测深拷贝；基线为<b>手工等价深拷贝</b>（同路径）。
 *   Spring/hutool/cglib 为浅拷贝不参与；Jackson 属序列化中转（口径不同）亦不参与。每个类型单独一对用例
 *   （被测 + 手工），另设全空（框架固定开销）与组合（全部字段）场景。</li>
 *   <li><b>toMap 通道</b>（{@link #toMapBenchmark}）：对象转 Map，多实现方式对比（对象转 Map 为浅引用视图，无深拷贝语义）。</li>
 * </ul>
 *
 * <h2>执行方式</h2>
 * <ul>
 *   <li>mvn -Pperf test -Dtest=CBeanUtilsPerfTests -Dsurefire.failIfNoSpecifiedTests=false</li>
 *   <li>性能测试类独立于常规测试（命名 {@code *PerfTests} 被 surefire/failsafe 排除，仅显式触发）。</li>
 *   <li>三通道分写报告：doc/design/core/perf/benchmark-report-cbeanutils-{copy,deepcopy,tomap}.md。</li>
 *   <li>测量口径：预热不计时（第一轮全用例预热 + 每轮计时前再预热）；单轮迭代数与轮数见各通道常量
 *   （简 copy / toMap：默认预热 50 万、单轮 100 万 × 5 轮；深拷贝：预热 10 万、单轮 20 万 × 5 轮）；
 *   终值为各轮「单次均摊耗时」均值，离散度为极差（最慢 − 最快），两者同表给出。</li>
 *   <li>深拷贝通道耗时量级高，用运行器重载取较小迭代参数（预热 10 万、单轮 20 万 × 3 轮）；
 *   简单 copy 与 toMap 通道用默认迭代（预热 50 万、单轮 100 万 × 5 轮）。</li>
 * </ul>
 *
 * <h2>基准执行（方案目录）</h2>
 * <ul>
 *   <li>6.1 简单 copy 通道（标量属性，多实现方式同路径对比 + 实例化/缓存/分配诊断项）</li>
 *   <li>6.2 深拷贝通道（按类型分场景 + 手工等价深拷贝基线 + 组合/全空场景）</li>
 *   <li>6.3 对象转 Map 通道（多实现方式对比）</li>
 * </ul>
 *
 * <h2>测量口径</h2>
 * <ul>
 *   <li>运行环境：JDK 8（Temurin 8u504）；单容器实测值，非固定 CPU，仅用于量级与相对比较。</li>
 *   <li>预热/测量：第一轮全用例预热（触发初始化与加载），不计时；随后每用例逐轮
 *   {@code prepare()} → 预热 → 归集垃圾 → 计时；简 copy / toMap 通道 5 轮 × 100 万次，
 *   深 copy 通道 5 轮 × 20 万次（耗时量级高，降迭代数）。</li>
 *   <li>指标与取数：终值 = 各轮「单次均摊耗时」均值；离散度 = 极差（最慢 − 最快）；两者同表给出。
 *   组间差异小于两者离散度之和即标注「无显著差异」，不据此宣称胜出。</li>
 *   <li>计时区间：只含被测调用循环——数据构造（{@code prepare}）与报告写出都在区间外；
 *   结果经 {@code identityHashCode} 累加消费，防 JIT 消除死代码。</li>
 * </ul>
 *
 * <h2>结果与结论（详见 doc/design/core/perf/beanutils-perf.adoc）</h2>
 * <ul>
 *   <li>同类对比中 CBeanUtils 明显优于 Spring {@code BeanUtils} 与 hutool {@code BeanUtil}
 *   （简单 copy 通道相差一个数量级以上）；cglib {@code BeanCopier} 与手工 setter 同量级、优于本类。</li>
 *   <li>本类与手工 setter 的差距主要来自<b>通用性代价</b>：字段访问统一走 {@code MethodHandle}
 *   （反射的 1.5 倍速度、且免字节码），而手工 setter 由 JIT 直接内联为字段读写，属不可消除的机制差异
 *   （禁用字节码处理的前提下的最优选择）。</li>
 *   <li>深拷贝通道的瓶颈已在 3 轮优化中收敛（详见性能文档的优化日志），
 *   剩余差距为 MethodHandle 固有成本与每次复制的必要分配。</li>
 *   <li>第 2 轮（按类折叠候选）同口径 A/B：深拷贝按目标类折叠 `DeepPlan` 有有效收益
 *   （按一次复制命中的目标类次数线性生效，命中 41 次 -3.5%），已采纳；
 *   容器创建的 kind 查表与 toMap 的 `put` 返回值判冲突均无显著收益或负收益，未采纳
 *   （理由与实测数值见 `doc/design/core/perf/beanutils-perf.adoc`）。</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CBeanUtilsPerfTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 深拷贝通道预热次数（耗时量级高，取较小值）
     */
    private static final int DEEP_WARMUP_ITERATIONS = 100_000;

    /**
     * 深拷贝通道单轮计时迭代次数
     */
    private static final int DEEP_MEASURE_ITERATIONS = 200_000;

    /**
     * 深拷贝通道采样轮数
     */
    private static final int DEEP_MEASURE_ROUNDS = 5;

    /**
     * 简单 copy 通道基准（对应测试用例 6.1）
     */
    @Test
    public void simpleCopyBenchmark() {
        CBenchmarkReport report = CBenchmarkRunner.run(copyCases(), "CBeanUtils 简单 copy（标量属性通道）性能对比");
        writeReport(report, "benchmark-report-cbeanutils-copy.md");
    }

    /**
     * 深拷贝通道基准（对应测试用例 6.2）
     */
    @Test
    public void deepCopyBenchmark() {
        CBenchmarkReport report = CBenchmarkRunner.run(
                deepCopyCases(), "CBeanUtils 深拷贝（按类型分场景）性能对比",
                DEEP_WARMUP_ITERATIONS, DEEP_MEASURE_ITERATIONS, DEEP_MEASURE_ROUNDS);
        writeReport(report, "benchmark-report-cbeanutils-deepcopy.md");
    }

    /**
     * 对象转 Map 通道基准（对应测试用例 6.3）
     */
    @Test
    public void toMapBenchmark() {
        CBenchmarkReport report = CBenchmarkRunner.run(toMapCases(), "CBeanUtils 对象转 Map 性能对比");
        writeReport(report, "benchmark-report-cbeanutils-tomap.md");
    }

    /**
     * 报告落点：持久设计文档目录（性能测试内容禁止落 {@code tmp/}，见公共测试规范「性能测试」）
     *
     * <p>原始报告为一次性产物，其"取值 + 来源 + 结论"由 {@code doc/design/core/beanutils-perf.adoc}
     * 与测试类 javadoc 承载；此处按模块相对路径定位（从模块基目录向上找到含 {@code doc/design} 的仓库根）。</p>
     *
     * @param report   基准报告
     * @param fileName 报告文件名
     */
    private static void writeReport(CBenchmarkReport report, String fileName) {

        Path base = Paths.get(System.getProperty("user.dir"));
        // 模块测试的工作目录为模块目录，逐级向上找仓库根（含 doc/design 者）
        while(null != base && !Files.isDirectory(base.resolve("doc").resolve("design"))) {
            base = base.getParent();
        }
        Path reportPath = null == base
                ? Paths.get(System.getProperty("user.dir"), "target", fileName)
                : base.resolve("doc").resolve("design").resolve("core").resolve("perf").resolve(fileName);

        report.writeTo(reportPath);
        System.out.println("性能测试报告已写入: " + reportPath.toAbsolutePath());
    }

    /**
     * 简单 copy 通道用例（同路径：均为标量属性复制；末尾 3 项为诊断项）
     */
    private static List<CBenchmarkCase> copyCases() {
        return Arrays.asList(
                new ManualSetterCase(),
                new CglibBeanCopierCase(),
                new SpringBeanUtilsCase(),
                new HutoolBeanUtilCase(),
                new CBeanUtilsReuseTargetCase(),
                new CBeanUtilsNewTargetCase(),

                new NewInstanceCase(),
                new ClassValueGetCase(),
                new AllocateCase()
        );
    }

    /**
     * 深拷贝通道用例：每个场景"被测 + 手工等价深拷贝"成对出现
     */
    private static List<CBenchmarkCase> deepCopyCases() {

        List<CBenchmarkCase> cases = new ArrayList<>();
        for (Scenario scenario : Scenario.values()) {
            cases.add(new CBeanUtilsDeepCopyCase(scenario));
            cases.add(new ManualDeepCopyCase(scenario));
        }
        return cases;
    }

    /**
     * 对象转 Map 通道用例
     */
    private static List<CBenchmarkCase> toMapCases() {
        return Arrays.asList(
                new ManualToMapCase(),
                new CBeanUtilsToMapCase(),
                new HutoolBeanUtilToMapCase(),
                new JacksonToMapCase()
        );
    }

    /* ============================ 被测 Bean ============================ */

    /**
     * 简单 copy / toMap 被测 Bean：只含标量字段（不含深拷贝字段，保证通道内语义一致）
     */
    @Data
    @NoArgsConstructor
    public static class ScalarBean {

        private String name;

        private Integer age;

        private int level;

        private long score;

        private double ratio;

        private boolean active;

        private String remark;

    }

    private static ScalarBean newSource() {
        ScalarBean source = new ScalarBean();
        source.setName("benchmark");
        source.setAge(30);
        source.setLevel(5);
        source.setScore(10000L);
        source.setRatio(0.85d);
        source.setActive(true);
        source.setRemark("简单 copy 基准数据");
        return source;
    }

    private static ScalarBean manualCopy(ScalarBean source) {
        ScalarBean target = new ScalarBean();
        target.setName(source.getName());
        target.setAge(source.getAge());
        target.setLevel(source.getLevel());
        target.setScore(source.getScore());
        target.setRatio(source.getRatio());
        target.setActive(source.isActive());
        target.setRemark(source.getRemark());
        return target;
    }

    private static Map<String, Object> manualToMap(ScalarBean source) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", source.getName());
        map.put("age", source.getAge());
        map.put("level", source.getLevel());
        map.put("score", source.getScore());
        map.put("ratio", source.getRatio());
        map.put("active", source.isActive());
        map.put("remark", source.getRemark());
        return map;
    }

    /* ============================ 深拷贝场景与 Bean ============================ */

    /**
     * 深拷贝场景：每个场景只放开对应字段，其余保持 null（便于按类型归因）
     */
    private enum Scenario {

        /** 全空：无深拷贝字段值，只计量深拷贝框架固定开销 */
        EMPTY("全空（无深拷贝值）", bean -> {
        }),

        /** 集合（元素为不可变值） */
        LIST_STRING("集合List<String>", bean -> bean.setTags(Arrays.asList("a", "b", "c"))),

        /** 集合（元素为 Bean，需逐元素深拷贝） */
        LIST_BEAN("集合List<Bean>", bean -> bean.setItems(Arrays.asList(newInner("i1", 1), newInner("i2", 2), newInner("i3", 3)))),

        /** Map（键值均为不可变值） */
        MAP_STRING("Map<String,String>", bean -> bean.setAttrs(newStrMap())),

        /** Map（值为 Bean） */
        MAP_BEAN("Map<String,Bean>", bean -> bean.setEntries(newBeanMap())),

        /** 原始类型数组 */
        ARRAY_PRIMITIVE("数组int[]", bean -> bean.setNums(new int[] {1, 2, 3})),

        /** 引用类型数组（元素为 Bean） */
        ARRAY_BEAN("数组Bean[]", bean -> bean.setInners(new Inner[] {newInner("i1", 1), newInner("i2", 2)})),

        /** 单 Bean 字段 */
        BEAN("Bean（单对象）", bean -> bean.setInner(newInner("i1", 1))),

        /** Date（可变值类型，防御性拷贝） */
        DATE("Date", bean -> bean.setDate(new Date(1700000000000L))),

        /** 共享引用：items 与 shared 指向同一批 Inner 实例（应复用同一份副本） */
        SHARED("共享引用（跨字段同一实例）", bean -> {
            List<Inner> shared = Arrays.asList(newInner("s1", 1), newInner("s2", 2));
            bean.setItems(shared);
            bean.setShared(shared);
        }),

        /** 组合：全部字段同时出现（单类型都快、合起来是否退化） */
        COMBINED("组合（全部字段）", bean -> {
            List<Inner> shared = Arrays.asList(newInner("s1", 1), newInner("s2", 2));
            bean.setTags(Arrays.asList("a", "b", "c"));
            bean.setItems(Arrays.asList(newInner("i1", 1), newInner("i2", 2), newInner("i3", 3)));
            bean.setAttrs(newStrMap());
            bean.setEntries(newBeanMap());
            bean.setNums(new int[] {1, 2, 3});
            bean.setInners(new Inner[] {newInner("i1", 1), newInner("i2", 2)});
            bean.setInner(newInner("i1", 1));
            bean.setDate(new Date(1700000000000L));
            bean.setShared(shared);
        });

        private final String description;

        private final Populator populator;

        Scenario(String description, Populator populator) {
            this.description = description;
            this.populator = populator;
        }

    }

    /**
     * 场景数据填充器
     */
    private interface Populator {

        void populate(DeepBean bean);

    }

    /**
     * 深拷贝被测 Bean：标量前缀 + 各类深拷贝字段
     */
    @Data
    @NoArgsConstructor
    public static class DeepBean {

        private String name;

        private Integer age;

        private int level;

        private List<String> tags;

        private List<Inner> items;

        private Map<String, String> attrs;

        private Map<String, Inner> entries;

        private int[] nums;

        private Inner[] inners;

        private Inner inner;

        private Date date;

        private List<Inner> shared;

    }

    /**
     * 深层 Bean（只含标量）
     */
    @Data
    @NoArgsConstructor
    public static class Inner {

        private String name;

        private Integer level;

    }

    private static Inner newInner(String name, Integer level) {
        Inner inner = new Inner();
        inner.setName(name);
        inner.setLevel(level);
        return inner;
    }

    private static Map<String, String> newStrMap() {
        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        return map;
    }

    private static Map<String, Inner> newBeanMap() {
        Map<String, Inner> map = new HashMap<>();
        map.put("k1", newInner("i1", 1));
        map.put("k2", newInner("i2", 2));
        map.put("k3", newInner("i3", 3));
        return map;
    }

    private static DeepBean newDeepSource(Scenario scenario) {
        DeepBean source = new DeepBean();
        source.setName("benchmark");
        source.setAge(30);
        source.setLevel(5);
        scenario.populator.populate(source);
        return source;
    }

    /**
     * 手工等价深拷贝（深拷贝通道基线）
     */
    private static DeepBean manualDeepCopy(DeepBean source) {

        DeepBean target = new DeepBean();
        target.setName(source.getName());
        target.setAge(source.getAge());
        target.setLevel(source.getLevel());

        Map<Inner, Inner> copied = null;

        if (null != source.getTags()) {
            target.setTags(new ArrayList<>(source.getTags()));
        }
        if (null != source.getNums()) {
            target.setNums(Arrays.copyOf(source.getNums(), source.getNums().length));
        }
        if (null != source.getDate()) {
            target.setDate(new Date(source.getDate().getTime()));
        }
        if (null != source.getInner()) {
            target.setInner(manualInnerCopy(source.getInner()));
        }
        if (null != source.getItems()) {
            List<Inner> items = new ArrayList<>();
            for (Inner inner : source.getItems()) {
                if (null == copied) {
                    copied = new IdentityHashMap<>();
                }
                items.add(copied.computeIfAbsent(inner, CBeanUtilsPerfTests::manualInnerCopy));
            }
            target.setItems(items);
        }
        if (null != source.getShared()) {
            List<Inner> shared = new ArrayList<>();
            for (Inner inner : source.getShared()) {
                if (null == copied) {
                    copied = new IdentityHashMap<>();
                }
                shared.add(copied.computeIfAbsent(inner, CBeanUtilsPerfTests::manualInnerCopy));
            }
            target.setShared(shared);
        }
        if (null != source.getAttrs()) {
            target.setAttrs(new HashMap<>(source.getAttrs()));
        }
        if (null != source.getEntries()) {
            Map<String, Inner> entries = new HashMap<>();
            for (Map.Entry<String, Inner> entry : source.getEntries().entrySet()) {
                if (null == copied) {
                    copied = new IdentityHashMap<>();
                }
                entries.put(entry.getKey(), copied.computeIfAbsent(entry.getValue(), CBeanUtilsPerfTests::manualInnerCopy));
            }
            target.setEntries(entries);
        }
        if (null != source.getInners()) {
            Inner[] inners = new Inner[source.getInners().length];
            for (int i = 0; i < inners.length; i++) {
                if (null == copied) {
                    copied = new IdentityHashMap<>();
                }
                inners[i] = copied.computeIfAbsent(source.getInners()[i], CBeanUtilsPerfTests::manualInnerCopy);
            }
            target.setInners(inners);
        }

        return target;
    }

    private static Inner manualInnerCopy(Inner source) {
        Inner target = new Inner();
        target.setName(source.getName());
        target.setLevel(source.getLevel());
        return target;
    }

    /* ============================ 简单 copy 用例 ============================ */

    private static class ManualSetterCase implements CBenchmarkCase {

        private ScalarBean source;

        @Override
        public String name() {
            return "手工 setter";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return manualCopy(source);
        }
    }

    private static class CglibBeanCopierCase implements CBenchmarkCase {

        private ScalarBean source;

        private BeanCopier copier;

        @Override
        public String name() {
            return "cglib BeanCopier";
        }

        @Override
        public void prepare() {
            source = newSource();
            copier = BeanCopier.create(ScalarBean.class, ScalarBean.class, false);
        }

        @Override
        public Object run() {
            ScalarBean target = new ScalarBean();
            copier.copy(source, target, null);
            return target;
        }
    }

    private static class SpringBeanUtilsCase implements CBenchmarkCase {

        private ScalarBean source;

        @Override
        public String name() {
            return "Spring BeanUtils";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            ScalarBean target = new ScalarBean();
            BeanUtils.copyProperties(source, target);
            return target;
        }
    }

    private static class HutoolBeanUtilCase implements CBenchmarkCase {

        private ScalarBean source;

        @Override
        public String name() {
            return "hutool BeanUtil";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return BeanUtil.copyProperties(source, ScalarBean.class);
        }
    }

    private static class CBeanUtilsReuseTargetCase implements CBenchmarkCase {

        private ScalarBean source;

        private ScalarBean target;

        @Override
        public String name() {
            return "CBeanUtils.copy(复用目标)";
        }

        @Override
        public void prepare() {
            source = newSource();
            target = new ScalarBean();
        }

        @Override
        public Object run() {
            return CBeanUtils.copy(source, target);
        }
    }

    private static class CBeanUtilsNewTargetCase implements CBenchmarkCase {

        private ScalarBean source;

        @Override
        public String name() {
            return "CBeanUtils.copy(新建目标)";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return CBeanUtils.copy(source, ScalarBean.class);
        }
    }

    private static class NewInstanceCase implements CBenchmarkCase {

        @Override
        public String name() {
            return "诊断·CReflectUtils.newInstance";
        }

        @Override
        public void prepare() {
        }

        @Override
        public Object run() {
            return CReflectUtils.newInstance(ScalarBean.class);
        }
    }

    private static class ClassValueGetCase implements CBenchmarkCase {

        @Override
        public String name() {
            return "诊断·CClassValue.get";
        }

        @Override
        public void prepare() {
        }

        @Override
        public Object run() {
            return CReflectUtils.FIELD_MAP_CLASS_VALUE.get(ScalarBean.class);
        }
    }

    private static class AllocateCase implements CBenchmarkCase {

        @Override
        public String name() {
            return "诊断·new ScalarBean()";
        }

        @Override
        public void prepare() {
        }

        @Override
        public Object run() {
            return new ScalarBean();
        }
    }

    /* ============================ 深拷贝用例 ============================ */

    private static class CBeanUtilsDeepCopyCase implements CBenchmarkCase {

        private final Scenario scenario;

        private DeepBean source;

        private DeepBean target;

        CBeanUtilsDeepCopyCase(Scenario scenario) {
            this.scenario = scenario;
        }

        @Override
        public String name() {
            return "深拷贝·CBeanUtils·" + scenario.description;
        }

        @Override
        public void prepare() {
            source = newDeepSource(scenario);
            target = new DeepBean();
        }

        @Override
        public Object run() {
            return CBeanUtils.copy(source, target);
        }
    }

    private static class ManualDeepCopyCase implements CBenchmarkCase {

        private final Scenario scenario;

        private DeepBean source;

        ManualDeepCopyCase(Scenario scenario) {
            this.scenario = scenario;
        }

        @Override
        public String name() {
            return "深拷贝·手工·" + scenario.description;
        }

        @Override
        public void prepare() {
            source = newDeepSource(scenario);
        }

        @Override
        public Object run() {
            return manualDeepCopy(source);
        }
    }

    /* ============================ toMap 用例 ============================ */

    private static class ManualToMapCase implements CBenchmarkCase {

        private ScalarBean source;

        @Override
        public String name() {
            return "手工 toMap";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return manualToMap(source);
        }
    }

    private static class CBeanUtilsToMapCase implements CBenchmarkCase {

        private ScalarBean source;

        @Override
        public String name() {
            return "CBeanUtils.toMap";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return CBeanUtils.toMap(source);
        }
    }

    private static class HutoolBeanUtilToMapCase implements CBenchmarkCase {

        private ScalarBean source;

        @Override
        public String name() {
            return "hutool beanToMap";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return BeanUtil.beanToMap(source);
        }
    }

    private static class JacksonToMapCase implements CBenchmarkCase {

        private ScalarBean source;

        @Override
        public String name() {
            return "Jackson toMap";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return OBJECT_MAPPER.convertValue(source, Map.class);
        }
    }

}
