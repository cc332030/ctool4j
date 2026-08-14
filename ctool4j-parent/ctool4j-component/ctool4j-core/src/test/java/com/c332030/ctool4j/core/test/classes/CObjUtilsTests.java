package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CBiFunction;
import com.c332030.ctool4j.definition.function.CPredicate;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CObjUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CObjUtilsTests {

    private static class TestBean {

    }

    @Test
    public void emptyObject() {

        // emptyObject 返回同一单例
        Assertions.assertSame(CObjUtils.OBJECT, CObjUtils.emptyObject());
        Assertions.assertSame(CObjUtils.emptyObject(), CObjUtils.emptyObject());

    }

    @Test
    public void to() {

        // 类型匹配返回原对象
        val value = "a";
        Assertions.assertSame(value, CObjUtils.to(value, String.class));

        // null 返回 null
        Assertions.assertNull(CObjUtils.to(null, String.class));

        // 类型不匹配抛 IllegalStateException
        Assertions.assertThrowsExactly(IllegalStateException.class, () -> CObjUtils.to("a", Integer.class));

    }

    @Test
    public void convertClass() {

        // null 返回 null
        Assertions.assertNull(CObjUtils.convert(null, String.class));

        // 类型匹配直接返回
        Assertions.assertSame("a", CObjUtils.convert("a", String.class));

        // 有转换器则转换（String -> Integer）
        Assertions.assertEquals(1, CObjUtils.convert("1", Integer.class));

        // 无转换器返回 null（自定义类型 -> Integer，避免命中 objectStr 的 Object -> String 转换器）
        Assertions.assertNull(CObjUtils.convert(new TestBean(), Integer.class));

    }

    @Test
    public void convertFunction() {

        // 正常转换
        Assertions.assertEquals(1, CObjUtils.convert("a", String::length));

        // null 对象返回默认值
        Assertions.assertEquals(5, CObjUtils.convert(null, String::length, 5));
        Assertions.assertNull(CObjUtils.convert(null, String::length));

        // 转换结果为 null 时返回默认值
        Assertions.assertEquals(5, CObjUtils.convert("a", s -> null, 5));

        // 双对象转换：优先 o1
        Assertions.assertEquals(1, CObjUtils.convert("a", String::length, "bb", String::length));

        // o1 为 null 时回退 o2
        Assertions.assertEquals(2, CObjUtils.convert(null, String::length, "bb", String::length));

        // o1 转换结果为 null 时回退 o2
        Assertions.assertEquals(2, CObjUtils.convert("a", s -> null, "bb", String::length));

        // 均为 null 返回 null
        Assertions.assertNull(CObjUtils.convert(null, String::length, null, String::length));

    }

    @Test
    public void equals() {

        Assertions.assertTrue(CObjUtils.equals(1, "1", Integer::parseInt));
        Assertions.assertFalse(CObjUtils.equals(1, "2", Integer::parseInt));

        // o2 为 null 时转换结果为 null
        Assertions.assertFalse(CObjUtils.<Integer, String>equals(1, null, Integer::parseInt));

    }

    @Test
    public void merge() {

        // v1 不可用返回 v2
        Assertions.assertEquals(2, CObjUtils.merge(null, 2, (v1, v2) -> v1));

        // v2 不可用返回 v1
        Assertions.assertEquals(1, CObjUtils.merge(1, null, (v1, v2) -> v1));

        // 都可用且 merge 为 null 抛 IllegalStateException
        Assertions.assertThrowsExactly(IllegalStateException.class,
                () -> CObjUtils.merge(1, 2, null));

        // 都可用时执行 merge
        Assertions.assertEquals(3, CObjUtils.merge(1, 2, (v1, v2) -> v1 + v2));

        // key 版本
        Assertions.assertEquals(3, CObjUtils.merge("k", 1, 2, (v1, v2) -> v1 + v2));

        // 自定义 availablePredicate：v1 不满足时返回 v2（用 5 参版本规避 4 参重载歧义）
        Assertions.assertEquals(2, CObjUtils.merge(null, 1, 2, v -> v > 1, (v1, v2) -> v1 + v2));

        // 全参数版本
        Assertions.assertEquals(3, CObjUtils.merge("k", 1, 2, v -> v > 0, (v1, v2) -> v1 + v2));

    }

    @Test
    public void defaultIfNull() {

        Assertions.assertEquals(1, CObjUtils.defaultIfNull(1, () -> 2));
        Assertions.assertEquals(2, CObjUtils.defaultIfNull(null, () -> 2));

    }

    @Test
    public void ifThenGet() {

        Assertions.assertEquals(1, CObjUtils.ifThenGet(true, () -> 1));
        Assertions.assertNull(CObjUtils.ifThenGet(false, () -> 1));

    }

    @Test
    public void equalsThenGet() {

        Assertions.assertEquals(1, CObjUtils.equalsThenGet("a", "a", () -> 1));
        Assertions.assertNull(CObjUtils.equalsThenGet("a", "b", () -> 1));

    }

    @Test
    public void notNullThenGet() {

        Assertions.assertEquals(1, CObjUtils.notNullThenGet("a", () -> 1));
        Assertions.assertNull(CObjUtils.notNullThenGet(null, () -> 1));

        // 函数版本
        Assertions.assertEquals(1, CObjUtils.notNullThenGet("a", String::length));
        Assertions.assertNull(CObjUtils.notNullThenGet(null, String::length));

    }

}
