package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Description: CMapTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「固定键值构造 / 复制 Map」两个维度组织。</li>
 *   <li>固定键值覆盖空/单/双/三键值，双/三额外断言不可变（put 抛异常）。</li>
 *   <li>复制 Map 覆盖：默认有序副本不可变、原 Map 不受影响、null/空 Map 返回空、supplier 指定容器。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对不可变返回与空 Map 判空的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：各键值数、空 Map、不可变断言、原 Map 隔离。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：of() 空；单/双/三键值正例与双/三不可变；of(Map) 不可变副本与原 Map 隔离；null/空 Map 返回空；</li>
 *   <li>of(Map, supplier) 内容与不可变。</li>
 *   <li>未覆盖：双键值重复 key 场景（未单列，非本类核心语义）。</li>
 * </ul>
 * <h2>固定键值构造</h2>
 * <ul>
 *   <li>1.1 of()：空 Map（ofEmpty）</li>
 *   <li>1.2 单键值：{@code of("a",1)} 大小 1 值正确（ofSingle）</li>
 *   <li>1.3 双键值：{@code of("a",1,"b",2)} 大小 2 值正确（ofTwo）</li>
 *   <li>1.4 双键值不可变：put 抛 UnsupportedOperationException（ofTwoUnmodifiable）</li>
 *   <li>1.5 三键值：{@code of("a",1,"b",2,"c",3)} 大小 3 值正确（ofThree）</li>
 *   <li>1.6 三键值不可变：put 抛 UnsupportedOperationException（ofThreeUnmodifiable）</li>
 * </ul>
 * <h2>复制 Map（of(Map) / of(Map, supplier)）</h2>
 * <ul>
 *   <li>2.1 默认副本：不可变且内容正确、原 Map 不受影响（ofMapCopyUnmodifiable）</li>
 *   <li>2.2 null/空 Map：返回空 Map（ofNullMapReturnsEmpty）</li>
 *   <li>2.3 supplier：指定容器复制，内容正确且不可变（ofMapWithSupplier）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CMapTests {

    /**
     * 对应测试用例 1.1：of()：空 Map
     */
    @Test
    public void ofEmpty() {

        Assertions.assertTrue(CMap.<String, Integer>of().isEmpty());

    }

    /**
     * 对应测试用例 1.2：单键值：{@code of("a",1)} 大小 1 值正确
     */
    @Test
    public void ofSingle() {

        Map<String, Integer> map = CMap.of("a", 1);
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(1, map.get("a"));

    }

    /**
     * 对应测试用例 1.3：双键值：{@code of("a",1,"b",2)} 大小 2 值正确
     */
    @Test
    public void ofTwo() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2);
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(1, map.get("a"));
        Assertions.assertEquals(2, map.get("b"));

    }

    /**
     * 对应测试用例 1.4：双键值不可变：put 抛 UnsupportedOperationException
     */
    @Test
    public void ofTwoUnmodifiable() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2);
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("c", 3));

    }

    /**
     * 对应测试用例 1.5：三键值：{@code of("a",1,"b",2,"c",3)} 大小 3 值正确
     */
    @Test
    public void ofThree() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2, "c", 3);
        Assertions.assertEquals(3, map.size());
        Assertions.assertEquals(3, map.get("c"));

    }

    /**
     * 对应测试用例 1.6：三键值不可变：put 抛 UnsupportedOperationException
     */
    @Test
    public void ofThreeUnmodifiable() {

        Map<String, Integer> map = CMap.of("a", 1, "b", 2, "c", 3);
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("d", 4));

    }

    /**
     * 对应测试用例 2.1：默认副本：不可变且内容正确、原 Map 不受影响
     */
    @Test
    public void ofMapCopyUnmodifiable() {

        Map<String, Integer> source = new HashMap<>();
        source.put("a", 1);

        Map<String, Integer> map = CMap.of(source);
        // of(Map) 与 of 系列一致返回不可变副本
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("b", 2));
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(1, map.get("a"));
        // 原 map 不受影响
        Assertions.assertEquals(1, source.size());

    }

    /**
     * 对应测试用例 2.2：null/空 Map：返回空 Map
     */
    @Test
    public void ofNullMapReturnsEmpty() {

        Assertions.assertTrue(CMap.of((Map<String, Integer>) null).isEmpty());
        Assertions.assertTrue(CMap.of(new HashMap<>(), LinkedHashMap::new).isEmpty());

    }

    /**
     * 对应测试用例 2.3：指定容器复制，内容正确且不可变
     */
    @Test
    public void ofMapWithSupplier() {

        Map<String, Integer> source = new HashMap<>();
        source.put("a", 1);

        Map<String, Integer> map = CMap.of(source, LinkedHashMap::new);
        // supplier 指定内部容器；返回不可变副本（无法直接验证内部类型），验证内容与不可变
        Assertions.assertEquals(1, map.get("a"));
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("b", 2));

    }

}
