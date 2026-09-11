package com.c332030.ctool4j.core.test.util;

import cn.hutool.core.lang.Pair;
import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.definition.function.CPredicate;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * <p>
 * Description: CCollUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「空兜底 / 遍历分组 / 添加拼接 / 过滤 / 转换 / 新建 / 元素 / 最值 / 转 Map / 其他」多维度组织。</li>
 *   <li>空/null 入参统一作为边界覆盖，验证返回空结果而非抛异常。</li>
 *   <li>过滤、转换、最值、转 Map 覆盖 null 元素过滤与异常路径。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对空值语义、null 过滤、异常路径的约定。</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖/异常路径）：null 入参、空集合、null 元素、冲突、越界。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：defaultEmpty；forEach（null 不抛）；groupingBy（空/分组/null key 过滤/不可变/predicate）；</li>
 *   <li>addIgnore（null/empty/blank）；addAllIgnoreNull；concatOne（前后/null）；concat（null 过滤/全 null）；</li>
 *   <li>filter（Collection/List/Set/null/filterNull/filterString/filterKey/filterStringKey/convert 过滤）；</li>
 *   <li>convert（转换/null 过滤/predicate/convertSet/convertToList/convertToSet/convertToCollection/</li>
 *   <li>convertString/convertCollection）；newCollection（size0 不可变/size&gt;0 可变）；contains；get（越界/负数/null）；</li>
 *   <li>first（List 索引/非 List 迭代器/空/null）；last（List 索引/非 List 迭代器/空/null）；</li>
 *   <li>onlyOne（空/单/多抛异常）；minMax（null/空/convert null 过滤）；</li>
 *   <li>stream（null/正常）；toMap（基本/空/value null/自定义 value/predicate/merge/冲突抛异常/不可变/Pair）；</li>
 *   <li>containsAny；getValues；size；collectionType（返回类型）。</li>
 *   <li>未覆盖：{@code newLinkedSet}/{@code newLinkedMap} 独立创建断言（在 newCollection 中覆盖 isEmpty，类型未逐一断言）。</li>
 * </ul>
 * <h2>空兜底</h2>
 * <ul>
 *   <li>1.1 defaultEmpty：null/空返回空、非空返回原引用（defaultEmpty）</li>
 *   <li>1.2 size：null/空返回 0、正常返回大小（size）</li>
 * </ul>
 * <h2>遍历与分组</h2>
 * <ul>
 *   <li>2.1 forEach：null 不抛、正常遍历（forEach）</li>
 *   <li>2.2 groupingBy：空返回空；分组正确；null key 过滤；不可变；自定义 predicate（groupingBy）</li>
 * </ul>
 * <h2>添加与拼接</h2>
 * <ul>
 *   <li>3.1 addIgnoreNull/Empty/Blank：忽略 null/空/空白（addIgnore）</li>
 *   <li>3.2 addAllIgnoreNull：null 集合不抛、正常添加（addAllIgnoreNull）</li>
 *   <li>3.3 concatOne：元素前/后；null 元素与 null 集合忽略；不影响原集合（concatOne）</li>
 *   <li>3.4 concat：null 集合过滤、全 null 返回空、正常拼接（concat）</li>
 * </ul>
 * <h2>过滤</h2>
 * <ul>
 *   <li>4.1 filter：Collection/List/Set 版本；null 入参返回空；filterNull；filterString；filterKey；</li>
 *   <li>filterStringKey；convert 过滤（filter）</li>
 * </ul>
 * <h2>转换</h2>
 * <ul>
 *   <li>5.1 convert：基本转换；null 元素过滤；predicate；convertSet；空集合返回新集合；null 对象返回空；</li>
 *   <li>convertString（空白过滤）；convertCollection（null 过滤）（convert）</li>
 * </ul>
 * <h2>集合新建</h2>
 * <ul>
 *   <li>6.1 newCollection：size0 不可变；size&gt;0 可变（newCollection）</li>
 * </ul>
 * <h2>元素获取</h2>
 * <ul>
 *   <li>7.1 contains：null/空返回 false、正常正反（contains）</li>
 *   <li>7.2 get：正索引；越界/负数/null 返回 null（get）</li>
 *   <li>7.3 first：List 按索引与按迭代器、null/空返回 null（first）</li>
 *   <li>7.4 last：List 索引与非 List 迭代器遍历、null/空返回 null（last）</li>
 *   <li>7.5 onlyOne：空 null、单元素、多元素抛业务异常（onlyOne）</li>
 * </ul>
 * <h2>最值</h2>
 * <ul>
 *   <li>8.1 minMax：min/max 正例；null/空返回 null；convert 结果 null 过滤（minMax）</li>
 * </ul>
 * <h2>流与转 Map</h2>
 * <ul>
 *   <li>9.1 stream：null 空流、正常计数（stream）</li>
 *   <li>9.2 toMap：基本；空集合；value null 跳过；自定义 value；predicate 过滤 key；merge 冲突；无 merge</li>
 *   <li>冲突抛异常；不可变；Pair 版本；自定义谓词放行 null key 时 null key 被过滤，不入 Map（toMap）</li>
 * </ul>
 * <h2>其他</h2>
 * <ul>
 *   <li>10.1 containsAny：命中/多候选/未命中；空 elements/null 集合返回 false（containsAny）</li>
 *   <li>10.2 getValues：null 枚举返回空；正常枚举取值（getValues）</li>
 *   <li>10.3 返回类型验证：defaultEmpty 返回 List、convertSet 返回 LinkedHashSet、toMap 返回 Map（collectionType）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CCollUtilsTests {

    /**
     * 对应测试用例 1.1：null/空返回空、非空返回原引用
     */
    @Test
    public void defaultEmpty() {

        // null / 空集合返回空集合（非 null）
        Assertions.assertNotNull(CCollUtils.defaultEmpty((Collection<?>) null));
        Assertions.assertTrue(CCollUtils.defaultEmpty((Collection<?>) null).isEmpty());

        Assertions.assertNotNull(CCollUtils.defaultEmpty((List<?>) null));
        Assertions.assertTrue(CCollUtils.defaultEmpty((List<?>) null).isEmpty());

        Assertions.assertNotNull(CCollUtils.defaultEmpty((Set<?>) null));
        Assertions.assertTrue(CCollUtils.defaultEmpty((Set<?>) null).isEmpty());

        // 非空集合返回原引用
        val list = CList.of(1);
        Assertions.assertSame(list, CCollUtils.defaultEmpty(list));

    }

    /**
     * 对应测试用例 2.1：null 不抛、正常遍历
     */
    @Test
    public void forEach() {

        // null 集合不抛异常
        CCollUtils.forEach(null, e -> {
        });

        val list = new ArrayList<Integer>();
        CCollUtils.forEach(CList.of(1, 2, 3), list::add);
        Assertions.assertEquals(CList.of(1, 2, 3), list);

    }

    /**
     * 对应测试用例 2.2：空返回空；分组正确；null key 过滤；不可变；自定义 predicate
     */
    @Test
    public void groupingBy() {

        // 空集合返回空 map
        Assertions.assertTrue(CCollUtils.groupingBy(CList.of(), String::valueOf).isEmpty());

        val map = CCollUtils.groupingBy(CList.of("a1", "a2", "b1"), s -> s.substring(0, 1));
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(CList.of("a1", "a2"), map.get("a"));
        Assertions.assertEquals(CList.of("b1"), map.get("b"));

        // null key 被默认 predicate 过滤
        val map2 = CCollUtils.groupingBy(Arrays.asList("a", null), s -> s);
        Assertions.assertEquals(1, map2.size());
        Assertions.assertFalse(map2.containsKey(null));

        // 返回不可变 map
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("c", CList.of("c1")));

        // 自定义 predicate 过滤
        val map3 = CCollUtils.groupingBy(CList.of("a1", "b1"), s -> s.substring(0, 1), "a"::equals);
        Assertions.assertEquals(1, map3.size());
        Assertions.assertTrue(map3.containsKey("a"));

    }

    /**
     * 对应测试用例 3.1：addIgnoreNull/Empty/Blank：忽略 null/空/空白
     */
    @Test
    public void addIgnore() {

        val list = new ArrayList<String>();

        CCollUtils.addIgnoreNull(list, null);
        Assertions.assertTrue(list.isEmpty());

        CCollUtils.addIgnoreNull(list, "a");
        Assertions.assertEquals(CList.of("a"), list);

        CCollUtils.addIgnoreEmpty(list, "");
        CCollUtils.addIgnoreEmpty(list, "b");
        Assertions.assertEquals(CList.of("a", "b"), list);

        CCollUtils.addIgnoreBlank(list, " ");
        CCollUtils.addIgnoreBlank(list, "c");
        Assertions.assertEquals(CList.of("a", "b", "c"), list);

    }

    /**
     * 对应测试用例 3.2：null 集合不抛、正常添加
     */
    @Test
    public void addAllIgnoreNull() {

        val list = new ArrayList<Integer>();

        // null 集合不抛异常
        CCollUtils.addAllIgnoreNull(list, null);
        Assertions.assertTrue(list.isEmpty());

        CCollUtils.addAllIgnoreNull(list, CList.of(1, 2));
        Assertions.assertEquals(CList.of(1, 2), list);

    }

    /**
     * 对应测试用例 3.3：元素前/后；null 元素与 null 集合忽略；不影响原集合
     */
    @Test
    public void concatOne() {

        // 元素在前
        Assertions.assertEquals(CList.of(1, 2, 3), CCollUtils.concatOne(1, CList.of(2, 3)));

        // 元素在后
        Assertions.assertEquals(CList.of(1, 2, 3), CCollUtils.concatOne(CList.of(1, 2), 3));

        // null 元素 / null 集合均被忽略
        Assertions.assertEquals(CList.of(2), CCollUtils.concatOne((Integer) null, CList.of(2)));
        Assertions.assertEquals(CList.of(1), CCollUtils.concatOne(CList.of(1), (Integer) null));
        Assertions.assertTrue(CCollUtils.concatOne((Integer) null, null).isEmpty());

        // 返回新集合，不影响原集合
        val source = new ArrayList<Integer>(CList.of(2));
        val result = CCollUtils.concatOne(1, source);
        Assertions.assertEquals(CList.of(2), source);
        Assertions.assertEquals(CList.of(1, 2), result);

    }

    /**
     * 对应测试用例 3.4：null 集合过滤、全 null 返回空、正常拼接
     */
    @Test
    public void concat() {

        // null 集合参数被过滤
        Assertions.assertEquals(CList.of(1, 2), CCollUtils.concat(CList.of(1), null, CList.of(2)));

        // 全为 null 返回空
        Assertions.assertTrue(CCollUtils.concat(null, null).isEmpty());

        Assertions.assertEquals(CList.of(1, 2, 3), CCollUtils.concat(CList.of(1), CList.of(2, 3)));

    }

    /**
     * 对应测试用例 4.1：Collection/List/Set 版本；null 入参返回空；filterNull；filterString；filterKey；
     */
    @Test
    public void filter() {

        // Collection 版本
        Collection<Integer> coll = CCollUtils.filter(CList.of(1, 2, 3), e -> e > 1);
        Assertions.assertEquals(CList.of(2, 3), new ArrayList<>(coll));

        // List 版本
        Assertions.assertEquals(CList.of(2, 3), CCollUtils.filter(CList.of(1, 2, 3), e -> e > 1));

        // Set 版本
        Set<Integer> set = CCollUtils.filter(new LinkedHashSet<>(CList.of(1, 2, 3)), e -> e > 1);
        Assertions.assertEquals(2, set.size());

        // 空集合返回空（不抛异常）
        Assertions.assertTrue(CCollUtils.filter((List<Integer>) null, e -> true).isEmpty());
        Assertions.assertTrue(CCollUtils.filter((Collection<Integer>) null, e -> true).isEmpty());
        Assertions.assertTrue(CCollUtils.filter((Set<Integer>) null, e -> true).isEmpty());

        // null 元素参与 predicate 判断（Objects::nonNull 过滤）
        Assertions.assertEquals(CList.of(1, 2), CCollUtils.filterNull(CList.of(1, null, 2)));

        // filterString 过滤空/空白/null
        Assertions.assertEquals(CList.of("a"), CCollUtils.filterString(CList.of("a", "", " ", null)));

        // filterKey / filterStringKey
        Assertions.assertEquals(CList.of("ab", "cd"), CCollUtils.filterKey(CList.of("ab", "cd", null), s -> s));
        Assertions.assertEquals(CList.of("ab"), CCollUtils.filterStringKey(CList.of("ab", "", " "), s -> s));

        // 集合转换 filter
        Assertions.assertEquals(CList.of("ab"), CCollUtils.filter(CList.of("ab", "c"), String::toUpperCase, "AB"::equals));

    }

    /**
     * 对应测试用例 5.1：基本转换；null 元素过滤；predicate；convertSet；空集合返回新集合；null 对象返回空；
     */
    @Test
    public void convert() {

        // 基本转换
        Assertions.assertEquals(CList.of(10, 20), CCollUtils.convert(CList.of(1, 2), e -> e * 10));

        // null 元素被过滤
        Assertions.assertEquals(CList.of(1, 2), CCollUtils.convert(Arrays.asList(1, null, 2), e -> e));

        // predicate 过滤（predicate 作用于转换后元素：1*10=10、2*10=20 均 >5，全部保留）
        Assertions.assertEquals(CList.of(10, 20), CCollUtils.convert(CList.of(1, 2), e -> e * 10, e -> e > 5));

        // convertSet
        Set<Integer> set = CCollUtils.convertSet(CList.of(1, 1, 2), e -> e);
        Assertions.assertEquals(2, set.size());

        // 空集合返回 cSupplier 提供的新集合
        Assertions.assertTrue(CCollUtils.convert(CList.of(), e -> e).isEmpty());

        // null 对象返回空集合
        Assertions.assertTrue(CCollUtils.convertToList(null, e -> CList.of(1)).isEmpty());
        Assertions.assertTrue(CCollUtils.convertToSet(null, e -> CSet.of(1)).isEmpty());
        Assertions.assertTrue(CCollUtils.convertToCollection(null, e -> CList.of(1)).isEmpty());

        // convertString 过滤转换后空白的元素
        Assertions.assertEquals(CList.of("a"), CCollUtils.convertString(Arrays.asList(" a ", "", null), String::trim));

        // convertCollection 过滤 null 元素后执行转换
        Assertions.assertEquals(CList.of(1, 2), CCollUtils.convertCollection(Arrays.asList(1, null, 2), e -> e));

    }

    /**
     * 对应测试用例 6.1：size0 不可变；size&gt;0 可变
     */
    @Test
    public void newCollection() {

        // size 0 返回不可变空集合
        Assertions.assertTrue(CCollUtils.newList(0).isEmpty());
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> CCollUtils.newList(0).add(1));

        Assertions.assertTrue(CCollUtils.newSet(0).isEmpty());
        Assertions.assertTrue(CCollUtils.newLinkedSet(0).isEmpty());
        Assertions.assertTrue(CCollUtils.newMap(0).isEmpty());
        Assertions.assertTrue(CCollUtils.newLinkedMap(0).isEmpty());

        // size > 0 返回可变集合（size 是初始容量，初始为空）
        val list = CCollUtils.newList(1);
        list.add(1);
        Assertions.assertEquals(1, list.size());

        val set = CCollUtils.newSet(1);
        set.add(1);
        Assertions.assertEquals(1, set.size());

        val map = CCollUtils.newMap(1);
        map.put("k", 1);
        Assertions.assertEquals(1, map.size());

    }

    /**
     * 对应测试用例 7.1：null/空返回 false、正常正反
     */
    @Test
    public void contains() {

        // null / 空集合返回 false
        Assertions.assertFalse(CCollUtils.contains(null, 1));
        Assertions.assertFalse(CCollUtils.contains(CList.of(), 1));

        Assertions.assertTrue(CCollUtils.contains(CList.of(1, 2), 1));
        Assertions.assertFalse(CCollUtils.contains(CList.of(1, 2), 3));

    }

    /**
     * 对应测试用例 7.2：正索引；越界/负数/null 返回 null
     */
    @Test
    public void get() {

        Assertions.assertEquals(1, CCollUtils.get(CList.of(1, 2), 0));
        Assertions.assertEquals(2, CCollUtils.get(CList.of(1, 2), 1));

        // 越界 / 负数 / null 集合返回 null
        Assertions.assertNull(CCollUtils.get(CList.of(1, 2), 2));
        Assertions.assertNull(CCollUtils.get(CList.of(1, 2), -1));
        Assertions.assertNull(CCollUtils.get(null, 0));

    }

    /**
     * 对应测试用例 7.3：List 按索引与按迭代器、null/空返回 null
     */
    @Test
    public void first() {

        // List 按索引取第一个
        Assertions.assertEquals(1, CCollUtils.first(CList.of(1, 2)));

        // 非 List 集合按迭代器取第一个
        val set = new LinkedHashSet<>(CList.of(1, 2, 3));
        Assertions.assertEquals(1, CCollUtils.first(set));

        // null / 空集合返回 null
        Assertions.assertNull(CCollUtils.first(null));
        Assertions.assertNull(CCollUtils.first(CList.of()));

    }

    /**
     * 对应测试用例 7.4：List 索引与非 List 迭代器遍历、null/空返回 null
     */
    @Test
    public void last() {

        // List 按索引取最后一个
        Assertions.assertEquals(2, CCollUtils.last(CList.of(1, 2)));

        // 非 List 集合按迭代器单遍遍历取最后一个
        val set = new LinkedHashSet<>(CList.of(1, 2, 3));
        Assertions.assertEquals(3, CCollUtils.last(set));

        // null / 空集合返回 null
        Assertions.assertNull(CCollUtils.last(null));
        Assertions.assertNull(CCollUtils.last(CList.of()));

    }

    /**
     * 对应测试用例 7.5：空 null、单元素、多元素抛业务异常
     */
    @Test
    public void onlyOne() {

        Assertions.assertNull(CCollUtils.onlyOne(CList.of()));
        Assertions.assertEquals(1, CCollUtils.onlyOne(CList.of(1)));

        // 多个元素抛业务异常
        Assertions.assertThrowsExactly(CBusinessException.class, () -> CCollUtils.onlyOne(CList.of(1, 2)));

    }

    /**
     * 对应测试用例 8.1：min/max 正例；null/空返回 null；convert 结果 null 过滤
     */
    @Test
    public void minMax() {

        Assertions.assertEquals(1, CCollUtils.min(CList.of(3, 1, 2), e -> e));
        Assertions.assertEquals(3, CCollUtils.max(CList.of(3, 1, 2), e -> e));

        // null / 空集合返回 null
        Assertions.assertNull(CCollUtils.min((List<Integer>) null, e -> e));
        Assertions.assertNull(CCollUtils.max(CList.<Integer>of(), e -> e));

        // max 过滤 null 元素；min/max 过滤 convert 结果为 null 的元素
        Assertions.assertEquals(3, CCollUtils.max(Arrays.asList(1, null, 3), e -> e));
        Assertions.assertEquals(2, CCollUtils.min(CList.of(1, 2), e -> e > 1 ? e : null));
        Assertions.assertNull(CCollUtils.max(CList.of(1, 2), e -> null));

        // min 与 max 一致：过滤 null 元素
        Assertions.assertEquals(1, CCollUtils.min(Arrays.asList(1, null, 3), e -> e));
        Assertions.assertNull(CCollUtils.min(Arrays.<Integer>asList(null, null), e -> e));

    }

    /**
     * 对应测试用例 9.1：null 空流、正常计数
     */
    @Test
    public void stream() {

        Assertions.assertEquals(0, CCollUtils.stream(null).count());
        Assertions.assertEquals(2, CCollUtils.stream(CList.of(1, 2)).count());

    }

    /**
     * 对应测试用例 9.2：基本；空集合；value null 跳过；自定义 value；predicate 过滤 key；merge 冲突；无 merge
     */
    @Test
    public void toMap() {

        // 基本 key-value
        val map = CCollUtils.toMap(CList.of("a", "b"), e -> e);
        Assertions.assertEquals("a", map.get("a"));
        Assertions.assertEquals("b", map.get("b"));

        // 空集合返回空 map
        Assertions.assertTrue(CCollUtils.toMap(CList.of(), e -> e).isEmpty());

        // toValue 返回 null 的元素被跳过
        Map<String, String> map2 = CCollUtils.<String, String, String>toMap(CList.of("a"), e -> e, e -> null);
        Assertions.assertTrue(map2.isEmpty());

        // 自定义 toValue（显式类型参数，消除无目标类型时 lambda 推断失败）
        Map<String, String> map3 = CCollUtils.<String, String, String>toMap(CList.of("a"), e -> e, e -> e.toUpperCase());
        Assertions.assertEquals("A", map3.get("a"));

        // predicate 过滤 key（强转 CPredicate，避免与 CFunction 重载歧义）
        val map4 = CCollUtils.toMap(CList.of("a", "b"), e -> e, (CPredicate<String>) "a"::equals);
        Assertions.assertEquals(1, map4.size());
        Assertions.assertTrue(map4.containsKey("a"));

        // mergeFunction 合并冲突 key
        val map5 = CCollUtils.toMap(CList.of("a", "a"), e -> e, (v1, v2) -> v1 + v2);
        Assertions.assertEquals("aa", map5.get("a"));

        // 无 mergeFunction 且 key 冲突抛 IllegalStateException
        Assertions.assertThrowsExactly(IllegalStateException.class, () -> CCollUtils.toMap(CList.of("a", "a"), e -> e));

        // 返回不可变 map
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("c", "c"));

        // List[Pair] 版本
        val map6 = CCollUtils.toMap(CList.of(new Pair<>("a", 1), new Pair<>("b", 2)));
        Assertions.assertEquals(1, map6.get("a"));
        Assertions.assertEquals(2, map6.get("b"));

        // toMap 始终过滤 null key：即使自定义谓词放行 null key，null key 也被跳过
        val map7 = CCollUtils.<String, String, String>toMap(
                CList.of("a"), e -> null, e -> e.toUpperCase(), k -> true, (v1, v2) -> v1 + v2);
        Assertions.assertTrue(map7.isEmpty());

    }

    /**
     * 对应测试用例 10.1：命中/多候选/未命中；空 elements/null 集合返回 false
     */
    @Test
    public void containsAny() {

        Assertions.assertTrue(CCollUtils.containsAny(CList.of(1, 2), 2));
        Assertions.assertTrue(CCollUtils.containsAny(CList.of(1, 2), 1, 3));
        Assertions.assertFalse(CCollUtils.containsAny(CList.of(1, 2), 3));

        // 空 elements / null 集合返回 false
        Assertions.assertFalse(CCollUtils.containsAny(CList.of(1, 2)));
        Assertions.assertFalse(CCollUtils.containsAny(null, 1));

    }

    /**
     * 对应测试用例 10.2：null 枚举返回空；正常枚举取值
     */
    @Test
    public void getValues() {

        // null 枚举返回空集合
        Assertions.assertTrue(CCollUtils.getValues(null).isEmpty());

        val vector = new Vector<Integer>(CList.of(1, 2, 3));
        Assertions.assertEquals(CList.of(1, 2, 3), CCollUtils.getValues(vector.elements()));

    }

    /**
     * 对应测试用例 1.2：null/空返回 0、正常返回大小
     */
    @Test
    public void size() {

        // null / 空集合返回 0
        Assertions.assertEquals(0, CCollUtils.size(null));
        Assertions.assertEquals(0, CCollUtils.size(CList.of()));
        Assertions.assertEquals(0, CCollUtils.size(Collections.emptyMap().keySet()));

        Assertions.assertEquals(3, CCollUtils.size(CList.of(1, 2, 3)));

    }

    /**
     * 对应测试用例 10.3：返回类型验证：defaultEmpty 返回 List、convertSet 返回 LinkedHashSet、toMap 返回 Map
     */
    @Test
    public void collectionType() {

        // 返回类型保持正确
        val collection = CCollUtils.defaultEmpty(Arrays.asList(1, 2));
        Assertions.assertInstanceOf(List.class, collection);

        val set = CCollUtils.convertSet(CList.of(1, 2), e -> e);
        Assertions.assertInstanceOf(LinkedHashSet.class, set);

        val map = CCollUtils.toMap(CList.of("a"), e -> e);
        Assertions.assertInstanceOf(Map.class, map);

    }

}
