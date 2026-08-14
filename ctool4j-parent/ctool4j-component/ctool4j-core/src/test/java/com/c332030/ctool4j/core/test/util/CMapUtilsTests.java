package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.enums.CProfileEnum;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.CMapUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * <p>
 * Description: CMapUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CMapUtilsTests {

    @Test
    public void put() {

        val map = new HashMap<String, Integer>();

        // 正常写入
        Assertions.assertNull(CMapUtils.put(map, "a", 1));
        Assertions.assertEquals(1, map.get("a"));

        // map / key / value 任一为 null 时不写入
        Assertions.assertNull(CMapUtils.put(null, "a", 1));
        Assertions.assertNull(CMapUtils.put(map, null, 1));
        Assertions.assertNull(CMapUtils.put(map, "b", null));
        Assertions.assertEquals(1, map.size());

    }

    @Test
    public void defaultEmpty() {

        // null / 空 map 返回空 map（非 null）
        Assertions.assertNotNull(CMapUtils.defaultEmpty(null));
        Assertions.assertTrue(CMapUtils.defaultEmpty(null).isEmpty());
        Assertions.assertTrue(CMapUtils.defaultEmpty(new HashMap<>()).isEmpty());

        // 非空 map 返回原引用
        val map = CMap.of("a", 1);
        Assertions.assertSame(map, CMapUtils.defaultEmpty(map));

    }

    @Test
    public void toStringValueMap() {

        // null map 返回 null
        Assertions.assertNull(CMapUtils.toStringValueMap(null));

        val map = new LinkedHashMap<String, Object>();
        map.put("a", 1);
        map.put("b", null);

        val result = CMapUtils.toStringValueMap(map);
        Assertions.assertEquals("1", result.get("a"));
        // null 值转换为 null，而不是字符串 "null"
        Assertions.assertNull(result.get("b"));

    }

    @Test
    public void newMap() {

        // null object 抛 IllegalArgumentException
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> CMapUtils.newMap((Object) null, 10));

        // 普通类型返回 LinkedHashMap
        val map = CMapUtils.newMap("key", 10);
        Assertions.assertInstanceOf(LinkedHashMap.class, map);

        // 枚举类型返回 EnumMap
        val enumMap = CMapUtils.newMap(CProfileEnum.class, 10);
        Assertions.assertInstanceOf(EnumMap.class, enumMap);
        enumMap.put(CProfileEnum.DEV, 1);
        Assertions.assertEquals(1, enumMap.get(CProfileEnum.DEV));

    }

    @Test
    public void newEnumMap() {

        val map = CMapUtils.newEnumMap(CProfileEnum.class);
        Assertions.assertInstanceOf(EnumMap.class, map);

        // 非枚举类型抛 IllegalArgumentException（强转原始类型以绕过编译期泛型校验）
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> CMapUtils.newEnumMap((Class) String.class));

    }

    @Test
    public void newIgnoreCaseMap() {

        val map = CMapUtils.newIgnoreCaseMap();
        Assertions.assertInstanceOf(TreeMap.class, map);

        map.put("AbC", 1);
        // 忽略大小写取到值
        Assertions.assertEquals(1, map.get("aBC"));
        Assertions.assertTrue(map.containsKey("abc"));

    }

    @Test
    public void map() {

        // mapKey
        val mapKey = CMapUtils.mapKey(CMap.of("a", 1), String::toUpperCase);
        Assertions.assertEquals(1, mapKey.get("A"));

        // mapValue
        val mapValue = CMapUtils.mapValue(CMap.of("a", 1), e -> e * 10);
        Assertions.assertEquals(10, mapValue.get("a"));

        // map 键值同时转换
        val map = CMapUtils.map(CMap.of("a", 1), String::toUpperCase, e -> e * 10);
        Assertions.assertEquals(10, map.get("A"));

        // 空 map 返回空 map
        Assertions.assertTrue(CMapUtils.map(Collections.emptyMap(), e -> e, e -> e).isEmpty());

        // null key 被过滤
        val nullKeyMap = new HashMap<String, Integer>();
        nullKeyMap.put(null, 1);
        Assertions.assertTrue(CMapUtils.map(nullKeyMap, e -> e, e -> e).isEmpty());

        // null value 被过滤
        val nullValueMap = new HashMap<String, Integer>();
        nullValueMap.put("a", null);
        Assertions.assertTrue(CMapUtils.map(nullValueMap, e -> e, e -> e).isEmpty());

        // 转换后 key / value 为 null 被过滤
        val map2 = CMapUtils.map(CMap.of("a", 1), e -> e, e -> null);
        Assertions.assertTrue(map2.isEmpty());

    }

    @Test
    public void filter() {

        // 空 map 返回空 map
        Assertions.assertTrue(CMapUtils.filter(Collections.emptyMap(), (k, v) -> true).isEmpty());
        Assertions.assertTrue(CMapUtils.filter(null, (k, v) -> true).isEmpty());

        val map = CMapUtils.filter(CMap.of("a", 1, "b", 2), (k, v) -> v > 1);
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(2, map.get("b"));

        val mapKey = CMapUtils.filterKey(CMap.of("a", 1, "b", 2), "a"::equals);
        Assertions.assertEquals(1, mapKey.size());
        Assertions.assertTrue(mapKey.containsKey("a"));

        val mapValue = CMapUtils.filterValue(CMap.of("a", 1, "b", 2), v -> v == 2);
        Assertions.assertEquals(1, mapValue.size());
        Assertions.assertTrue(mapValue.containsKey("b"));

    }

    @Test
    public void merge() {

        // 空数组 / null 数组返回空 map
        Assertions.assertTrue(CMapUtils.merge().isEmpty());
        Assertions.assertTrue(CMapUtils.merge((Map[]) null).isEmpty());

        // 合并多个 map
        val map = CMapUtils.merge(CMap.of("a", 1), CMap.of("b", 2));
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(1, map.get("a"));
        Assertions.assertEquals(2, map.get("b"));

        // 冲突 key 默认取第一个
        val map2 = CMapUtils.merge(CMap.of("a", 1), CMap.of("a", 2));
        Assertions.assertEquals(1, map2.get("a"));

        // 自定义 mergeFunction
        val map3 = CMapUtils.merge((v1, v2) -> v1 + v2, CMap.of("a", 1), CMap.of("a", 2));
        Assertions.assertEquals(3, map3.get("a"));

        // null value 被过滤
        val nullValueMap = new HashMap<String, Integer>();
        nullValueMap.put("a", null);
        val map4 = CMapUtils.merge(nullValueMap, CMap.of("b", 2));
        Assertions.assertEquals(1, map4.size());
        Assertions.assertTrue(map4.containsKey("b"));

        // 返回不可变 map
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("c", 3));

    }

    @Test
    public void toAvailableStrMap() {

        val map = new LinkedHashMap<String, String>();
        map.put(" a ", " 1 ");
        map.put("null", "null");
        map.put("undefined", "2");
        map.put("b", null);
        map.put("c", "3");

        val result = CMapUtils.toAvailableStrMap(map);

        // trim 后的可用 key-value 保留
        Assertions.assertEquals("1", result.get("a"));

        // "null" / "undefined" 关键字被过滤
        Assertions.assertFalse(result.containsKey("null"));
        Assertions.assertFalse(result.containsKey("undefined"));

        // null value 被过滤
        Assertions.assertFalse(result.containsKey("b"));

        // 正常值保留
        Assertions.assertEquals("3", result.get("c"));

    }

    @Test
    public void computeIfAbsent() {

        val map = new HashMap<String, Integer>();
        map.put("a", 1);

        // 已有值直接返回，不调用 supplier
        Assertions.assertEquals(1, CMapUtils.computeIfAbsent(map, "a", () -> 2));
        Assertions.assertEquals(1, map.get("a"));

        // 无值则计算并写入
        Assertions.assertEquals(2, CMapUtils.computeIfAbsent(map, "b", () -> 2));
        Assertions.assertEquals(2, map.get("b"));

        // mappingFunction 版本
        Assertions.assertEquals(3, CMapUtils.computeIfAbsent(map, "c", k -> k.length() + 2));
        Assertions.assertEquals(3, map.get("c"));

    }

}
