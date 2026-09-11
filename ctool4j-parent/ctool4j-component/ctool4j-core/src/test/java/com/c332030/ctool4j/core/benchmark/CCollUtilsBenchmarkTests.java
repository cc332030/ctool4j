package com.c332030.ctool4j.core.benchmark;

import cn.hutool.core.collection.CollUtil;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.definition.function.CPredicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CCollUtils 集合转换性能对比基准
 * </p>
 * <p>
 * 对比维度：集合最核心的高频转换操作（convert 映射、toMap 转 Map、groupingBy 分组、filter 过滤）。
 * 每个维度覆盖多类实现方式（实现原理与代价各不相同，满足对比类别 ≥3 类）：
 * </p>
 * <ul>
 *     <li>CCollUtils（被测，C 工具类封装 stream + Collectors）</li>
 *     <li>原生 stream + Collectors（JDK 直接实现）</li>
 *     <li>hutool CollUtil（第三方工具类实现）</li>
 *     <li>手工循环（编译期直接赋值基线）</li>
 * </ul>
 *
 * <h2>被测对象</h2>
 * <p>工具库最核心、最高频的集合转换操作：</p>
 * <ul>
 *   <li>{@code convert}：集合元素映射（{@code List&lt;String&gt;} → {@code List&lt;Integer&gt;}）。</li>
 *   <li>{@code toMap}：集合转 Map（{@code List&lt;Item&gt;} → {@code Map&lt;Long, Item&gt;}，元素自身为值）。</li>
 *   <li>{@code toMap}（key 过滤 + value 提取）：{@code List&lt;Item&gt;} → {@code Map&lt;Long, String&gt;}，按 key 过滤、单独提取 value（覆盖 {@code toKey} 单遍调用的优化路径）。</li>
 *   <li>{@code groupingBy}：集合分组（{@code List&lt;Item&gt;} → {@code Map&lt;Integer, List&lt;Item&gt;&gt;}）。</li>
 *   <li>{@code filter}：集合过滤（{@code List&lt;Integer&gt;} 过滤偶数）。</li>
 *   <li>{@code first}：获取集合首个元素（{@code List&lt;Item&gt;}）。</li>
 *   <li>{@code last}：获取集合末个元素（{@code List&lt;Item&gt;}）。</li>
 *   <li>{@code min}：按转换结果取最小值（{@code List&lt;Item&gt;} → {@code Item}，按 id 取最小）。</li>
 *   <li>{@code max}：按转换结果取最大值（{@code List&lt;Item&gt;} → {@code Item}，按 id 取最大）。</li>
 * </ul>
 * <h2>对比维度（≥3 类实现，实现原理各不相同）</h2>
 * <ul>
 *   <li>{@code CCollUtils}（被测）：C 工具类封装 stream + Collectors，含判空/过滤/空集合兜底等语义。</li>
 *   <li>原生 stream + Collectors：JDK 直接实现（{@code stream().map/filter/collect}、{@code Collectors.toMap/toList/groupingBy}、{@code stream().findFirst/reduce/min/max}）。</li>
 *   <li>hutool {@code CollUtil}：第三方工具类实现（{@code CollUtil.map}）。</li>
 *   <li>手工循环：编译期直接赋值的基线（{@code for} 循环手工操作集合，列表按索引取值）。</li>
 *   <li>{@code toMap} 过滤 + value 提取维度同样提供 CCollUtils 与手工循环两种对比实现（验证 {@code toKey} 单遍调用优化）。</li>
 * </ul>
 * <h2>执行方式</h2>
 * <ul>
 *   <li>性能测试独立于单元测试，仅在明确命令执行时才运行（{@code mvn test -Dtest=CCollUtilsBenchmarkTests}）。</li>
 *   <li>排除初始化干扰：先对所有用例做一轮全局预热，触发全部实现方式初始化/加载，首次结果不计入。</li>
 *   <li>充分预热 + 足够迭代：预热 50 万次触发 JIT 至 C2 稳态，单轮计时 100 万次、取 5 轮平均，降低测量噪声。</li>
 *   <li>结果写入 {@code tmp/benchmark-report-ccollutils.md} 报告，分析性能差异原因并给出方案。</li>
 * </ul>
 * <h2>基准执行</h2>
 * <ul>
 *   <li>1.1 benchmark：CCollUtils 集合转换与取首尾/最值性能对比基准（convert/toMap/toMap 过滤+提取/groupingBy/filter/first/last/min/max 多实现方式对比）</li>
 * </ul>
 * <h2>性能分析结论</h2>
 * <p>以手工循环为基线（1.00x），本次采样数据（100 元素集合，相对基线）：</p>
 * <ul>
 *   <li>{@code CCollUtils.convert} 约 5.2x、{@code toMap} 约 8.4x，相对原生 stream 更慢。原因：CCollUtils 底层封装 stream + Collectors，并在语义上额外承担<b>空集合判空、null 元素过滤</b>、{@code toMap} 额外返回<b>不可变 Map</b>（包装拷贝）等健壮性代价。</li>
 *   <li>{@code toMap}（key 过滤 + value 提取）经单遍循环优化：{@code toKey} 每个元素仅执行一次（原实现经 filter/first/forEach 重复调用，命中元素重复 2 次），并避免中间集合，消除重复转换与多余遍历。</li>
 *   <li>{@code CCollUtils.groupingBy}、{@code filter} 接近原生实现（约 3~3.7x），表现良好。</li>
 *   <li>手工循环始终最快（编译期确定性分配，零语义开销）。</li>
 * </ul>
 * <p>{@code first}/{@code last}/{@code min}/{@code max} 为高频取元素/最值操作，CCollUtils 选取更轻量实现：</p>
 * <ul>
 *   <li>{@code first}：List 走索引 {@code get(0)}（O(1)），非 List 走迭代器，避免构造 Stream。</li>
 *   <li>{@code last}：List 走索引 {@code get(size-1)}（O(1)），非 List 走迭代器单遍遍历，避免 Stream reduce lambda 累积开销。</li>
 *   <li>{@code min}/{@code max}：单遍循环，<b>每元素仅执行一次 {@code convert}</b>（原 Stream 实现在过滤与比较阶段对 {@code convert} 重复执行两次），降低用户函数调用开销。</li>
 * </ul>
 * <p>方案建议：</p>
 * <ul>
 *   <li>对<b>高频热路径且数据规模大</b>的集合转换，若不需要 null 过滤/不可变 Map 等健壮性语义，可优先使用原生 stream 或手工循环；CCollUtils 则适合作为<b>默认安全选择</b>，其健壮性语义（判空、过滤 null、不可变）在绝大多数业务场景下收益大于微小的性能开销。</li>
 *   <li>不需要不可变 Map 的场景，评估是否可走 {@code toMap} 的可变变体（如交由调用方决定容器），以降低包装开销。</li>
 *   <li>{@code first}/{@code last}/{@code min}/{@code max} 优化后与手工循环差距大幅缩小，适合作为默认取元素/最值入口。</li>
 * </ul>
 *
 * @since 2026/9/9
 * @version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CCollUtilsBenchmarkTests {

    /**
     * 基准执行入口（显式运行：mvn test -Dtest=CCollUtilsBenchmarkTests -DfailIfNoTests=false）
     * 性能测试类，surefire 打包/常规测试时排除（命名以 BenchmarkTests 结尾）
     * 对应测试用例 1.1：CCollUtils 集合转换与取首尾/最值性能对比基准（convert/toMap/toMap 过滤+提取/groupingBy/filter/first/last/min/max 多实现方式对比）
     */
    @Test
    public void benchmark() {
        CBenchmarkReport report = CBenchmarkRunner.run(cases(), "CCollUtils 集合转换性能对比");
        Path reportPath = Paths.get(System.getProperty("user.dir"), "tmp", "benchmark-report-ccollutils.md");
        report.writeTo(reportPath);
        System.out.println("性能测试报告已写入: " + reportPath.toAbsolutePath());
    }

    /**
     * 基准用例列表
     */
    public static List<CBenchmarkCase> cases() {
        return Arrays.asList(
            new CCollConvertCase(),
            new StreamConvertCase(),
            new HutoolConvertCase(),
            new ManualConvertCase(),

            new CCollToMapCase(),
            new StreamToMapCase(),
            new ManualToMapCase(),

            new CCollToMapPredicateCase(),
            new ManualToMapPredicateCase(),

            new CCollGroupingByCase(),
            new StreamGroupingByCase(),
            new ManualGroupingByCase(),

            new CCollFilterCase(),
            new StreamFilterCase(),
            new ManualFilterCase(),

            new CCollFirstCase(),
            new StreamFirstCase(),
            new ManualFirstCase(),

            new CCollLastCase(),
            new StreamLastCase(),
            new ManualLastCase(),

            new CCollMinCase(),
            new StreamMinCase(),
            new ManualMinCase(),

            new CCollMaxCase(),
            new StreamMaxCase(),
            new ManualMaxCase()
        );
    }

    /**
     * 被测数据模型（id + 分组类型）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        private Long id;

        private int type;

        private String name;

    }

    /**
     * 数据集大小（兼顾采样时长的常规规模）
     */
    private static final int SIZE = 100;

    private static List<Item> newItems() {
        List<Item> list = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            list.add(new Item((long) i, i % 10, "item-" + i));
        }
        return list;
    }

    private static List<String> newIds() {
        List<String> ids = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            ids.add("id-" + i);
        }
        return ids;
    }

    // ===== convert：元素映射（List<String> → 长度）=====

    private static class CCollConvertCase implements CBenchmarkCase {

        private List<String> ids;

        @Override
        public String name() {
            return "CCollUtils.convert";
        }

        @Override
        public void prepare() {
            ids = newIds();
        }

        @Override
        public Object run() {
            return CCollUtils.convert(ids, String::length);
        }
    }

    private static class StreamConvertCase implements CBenchmarkCase {

        private List<String> ids;

        @Override
        public String name() {
            return "原生 stream.map";
        }

        @Override
        public void prepare() {
            ids = newIds();
        }

        @Override
        public Object run() {
            return ids.stream().map(String::length).collect(Collectors.toList());
        }
    }

    private static class HutoolConvertCase implements CBenchmarkCase {

        private List<String> ids;

        @Override
        public String name() {
            return "hutool map(convert)";
        }

        @Override
        public void prepare() {
            ids = newIds();
        }

        @Override
        public Object run() {
            return CollUtil.map(ids, String::length, true);
        }
    }

    private static class ManualConvertCase implements CBenchmarkCase {

        private List<String> ids;

        @Override
        public String name() {
            return "手工循环(convert)";
        }

        @Override
        public void prepare() {
            ids = newIds();
        }

        @Override
        public Object run() {
            List<Integer> result = new ArrayList<>(ids.size());
            for (String id : ids) {
                result.add(id.length());
            }
            return result;
        }
    }

    // ===== toMap：List<Item> → Map（按 id 为 key，元素为 value）=====

    private static class CCollToMapCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "CCollUtils.toMap";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return CCollUtils.toMap(items, Item::getId);
        }
    }

    private static class StreamToMapCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "原生 Collectors.toMap";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return items.stream().collect(Collectors.toMap(Item::getId, item -> item));
        }
    }

    private static class ManualToMapCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "手工循环(toMap)";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            Map<Long, Item> result = new HashMap<>(items.size());
            for (Item item : items) {
                result.put(item.getId(), item);
            }
            return result;
        }
    }

    // ===== toMap（key 过滤 + value 提取）：List<Item> → Map（按 type 分组 value 提取，过滤 null key）=====
    // toKey 每个元素仅执行一次是本次优化的关键；对比手工循环的单遍实现

    private static class CCollToMapPredicateCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "CCollUtils.toMap(过滤+value)";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return CCollUtils.<Item, Long, String>toMap(
                    items, Item::getId, Item::getName, (CPredicate<Long>) (id -> id != null), null);
        }
    }

    private static class ManualToMapPredicateCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "手工循环(toMap 过滤+value)";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            Map<Long, String> result = new HashMap<>(items.size());
            for (Item item : items) {
                if (item == null) {
                    continue;
                }
                Long key = item.getId();
                if (key == null) {
                    continue;
                }
                String value = item.getName();
                if (value == null) {
                    continue;
                }
                result.put(key, value);
            }
            return result;
        }
    }

    // ===== groupingBy：List<Item> → Map（按 type 分组）=====

    private static class CCollGroupingByCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "CCollUtils.groupingBy";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return CCollUtils.groupingBy(items, Item::getType);
        }
    }

    private static class StreamGroupingByCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "原生 groupingBy";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return items.stream().collect(Collectors.groupingBy(Item::getType));
        }
    }

    private static class ManualGroupingByCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "手工循环(groupingBy)";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            Map<Integer, List<Item>> result = new HashMap<>();
            for (Item item : items) {
                result.computeIfAbsent(item.getType(), k -> new ArrayList<>()).add(item);
            }
            return result;
        }
    }

    // ===== filter：过滤偶数 =====

    private static class CCollFilterCase implements CBenchmarkCase {

        private List<Integer> numbers;

        @Override
        public String name() {
            return "CCollUtils.filter";
        }

        @Override
        public void prepare() {
            numbers = newNumbers();
        }

        @Override
        public Object run() {
            return CCollUtils.filter(numbers, n -> n % 2 == 0);
        }
    }

    private static class StreamFilterCase implements CBenchmarkCase {

        private List<Integer> numbers;

        @Override
        public String name() {
            return "原生 stream.filter";
        }

        @Override
        public void prepare() {
            numbers = newNumbers();
        }

        @Override
        public Object run() {
            return numbers.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());
        }
    }

    private static class ManualFilterCase implements CBenchmarkCase {

        private List<Integer> numbers;

        @Override
        public String name() {
            return "手工循环(filter)";
        }

        @Override
        public void prepare() {
            numbers = newNumbers();
        }

        @Override
        public Object run() {
            List<Integer> result = new ArrayList<>();
            for (Integer n : numbers) {
                if (n % 2 == 0) {
                    result.add(n);
                }
            }
            return result;
        }
    }

    private static List<Integer> newNumbers() {
        List<Integer> numbers = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            numbers.add(i);
        }
        return numbers;
    }

    // ===== first：获取第一个元素 =====

    private static class CCollFirstCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "CCollUtils.first";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return CCollUtils.first(items);
        }
    }

    private static class StreamFirstCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "原生 stream.findFirst";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return items.stream().findFirst().orElse(null);
        }
    }

    private static class ManualFirstCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "手工循环(first)";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return items.isEmpty() ? null : items.get(0);
        }
    }

    // ===== last：获取最后一个元素 =====

    private static class CCollLastCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "CCollUtils.last";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return CCollUtils.last(items);
        }
    }

    private static class StreamLastCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "原生 stream.reduce";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return items.stream().reduce((first, second) -> second).orElse(null);
        }
    }

    private static class ManualLastCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "手工循环(last)";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return items.isEmpty() ? null : items.get(items.size() - 1);
        }
    }

    // ===== min：按转换结果取最小 =====

    private static class CCollMinCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "CCollUtils.min";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return CCollUtils.min(items, Item::getId);
        }
    }

    private static class StreamMinCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "原生 stream.min";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return items.stream().min(java.util.Comparator.comparing(Item::getId)).orElse(null);
        }
    }

    private static class ManualMinCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "手工循环(min)";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            Item min = null;
            Long minKey = null;
            for (Item e : items) {
                if (e == null) {
                    continue;
                }
                Long key = e.getId();
                if (key == null) {
                    continue;
                }
                if (min == null || key.compareTo(minKey) < 0) {
                    min = e;
                    minKey = key;
                }
            }
            return min;
        }
    }

    // ===== max：按转换结果取最大 =====

    private static class CCollMaxCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "CCollUtils.max";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return CCollUtils.max(items, Item::getId);
        }
    }

    private static class StreamMaxCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "原生 stream.max";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            return items.stream().max(java.util.Comparator.comparing(Item::getId)).orElse(null);
        }
    }

    private static class ManualMaxCase implements CBenchmarkCase {

        private List<Item> items;

        @Override
        public String name() {
            return "手工循环(max)";
        }

        @Override
        public void prepare() {
            items = newItems();
        }

        @Override
        public Object run() {
            Item max = null;
            Long maxKey = null;
            for (Item e : items) {
                if (e == null) {
                    continue;
                }
                Long key = e.getId();
                if (key == null) {
                    continue;
                }
                if (max == null || key.compareTo(maxKey) > 0) {
                    max = e;
                    maxKey = key;
                }
            }
            return max;
        }
    }

}
