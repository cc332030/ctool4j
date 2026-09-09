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
 * @since 2026/9/9
 * @see "doc/design/core/CCollUtilsBenchmarkTests.adoc"
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CCollUtilsBenchmarkTests {

    /**
     * 基准执行入口（显式运行：mvn test -Dtest=CCollUtilsBenchmarkTests -DfailIfNoTests=false）
     * 性能测试类，surefire 打包/常规测试时排除（命名以 BenchmarkTests 结尾）
     * 对应测试用例 1.1
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
