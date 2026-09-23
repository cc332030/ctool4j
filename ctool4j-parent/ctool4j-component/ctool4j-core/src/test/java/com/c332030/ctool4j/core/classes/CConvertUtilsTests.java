package com.c332030.ctool4j.core.classes;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: CConvertUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「基础转换 / 特殊源类型 / 枚举 / Opt / 注册」多个维度组织。</li>
 *   <li>基础转换覆盖 String→Integer/Long/Double、Integer→String、同类型、null。</li>
 *   <li>特殊源类型（Collection/Map/数组）验证返回 null；枚举转 String 验证 toString 回退。</li>
 *   <li>Opt 包装验证 present/empty；addConverter 无入参方法跳过注册（已注册数量不变，Q16）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对默认转换器（CClassConvert）与 null/无转换器语义的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：String→Integer/Long/Double；Integer→String；同类型；null；Collection/Map/数组源返回 null；</li>
 *   <li>枚举→String；convertOpt present/empty；addConverter 无入参方法跳过注册。</li>
 *   <li>未覆盖：自定义 addConverter 注册后的实际转换（依赖默认转换器行为，未单列自定义注册场景）。</li>
 * </ul>
 * <h2>基础转换</h2>
 * <ul>
 *   <li>1.1 String→Integer：{@code "123"} → 123（convert_toInt）</li>
 *   <li>1.2 String→Long：{@code "123"} → 123L（convert_toLong）</li>
 *   <li>1.3 String→Double：{@code "1.5"} → 1.5d（convert_toDouble）</li>
 *   <li>1.4 Integer→String：{@code 123} → {@code "123"}（convert_toString）</li>
 *   <li>1.5 同类型：{@code "abc"} → {@code "abc"}（convert_sameType）</li>
 *   <li>1.6 null：返回 null（convert_null）</li>
 * </ul>
 * <h2>特殊源类型</h2>
 * <ul>
 *   <li>2.1 Collection 源：返回 null（convert_collectionFrom）</li>
 *   <li>2.2 Map 源：返回 null（convert_mapFrom）</li>
 *   <li>2.3 数组源：返回 null（convert_arrayFrom）</li>
 * </ul>
 * <h2>枚举</h2>
 * <ul>
 *   <li>3.1 枚举→String：{@code INSERT} → {@code "INSERT"}（convert_enum）</li>
 * </ul>
 * <h2>Opt 包装</h2>
 * <ul>
 *   <li>4.1 convertOpt：可转换 present、不可转换/空 empty（convertOpt）</li>
 * </ul>
 * <h2>注册</h2>
 * <ul>
 *   <li>5.1 addConverter 无入参方法：跳过注册（已注册数量不变）（addConverterNoArgMethod，Q16）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CConvertUtilsTests {

    /**
     * 对应测试用例 1.1：String→Integer：{@code "123"} → 123
     */
    @Test
    public void convert_toInt() {

        Assertions.assertEquals(123, CConvertUtils.convert("123", Integer.class));

    }

    /**
     * 对应测试用例 1.2：String→Long：{@code "123"} → 123L
     */
    @Test
    public void convert_toLong() {

        Assertions.assertEquals(123L, CConvertUtils.convert("123", Long.class));

    }

    /**
     * 对应测试用例 1.3：String→Double：{@code "1.5"} → 1.5d
     */
    @Test
    public void convert_toDouble() {

        Assertions.assertEquals(1.5d, CConvertUtils.convert("1.5", Double.class));

    }

    /**
     * 对应测试用例 1.4：Integer→String：{@code 123} → {@code "123"}
     */
    @Test
    public void convert_toString() {

        Assertions.assertEquals("123", CConvertUtils.convert(123, String.class));

    }

    /**
     * 对应测试用例 1.5：同类型：{@code "abc"} → {@code "abc"}
     */
    @Test
    public void convert_sameType() {

        Assertions.assertEquals("abc", CConvertUtils.convert("abc", String.class));

    }

    /**
     * 对应测试用例 1.6：返回 null
     */
    @Test
    public void convert_null() {

        Assertions.assertNull(CConvertUtils.convert(null, String.class));

    }

    /**
     * 对应测试用例 2.1：Collection 源：返回 null
     */
    @Test
    public void convert_collectionFrom() {

        List<String> list = new ArrayList<>();
        list.add("a");
        Assertions.assertNull(CConvertUtils.convert(list, String.class));

    }

    /**
     * 对应测试用例 2.2：Map 源：返回 null
     */
    @Test
    public void convert_mapFrom() {

        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        Assertions.assertNull(CConvertUtils.convert(map, String.class));

    }

    /**
     * 对应测试用例 2.3：数组源：返回 null
     */
    @Test
    public void convert_arrayFrom() {

        String[] array = new String[] {"a"};
        Assertions.assertNull(CConvertUtils.convert(array, String.class));

    }

    /**
     * 对应测试用例 3.1：枚举→String：{@code INSERT} → {@code "INSERT"}
     */
    @Test
    public void convert_enum() {

        Assertions.assertEquals("INSERT", CConvertUtils.convert(EnumBean.INSERT, String.class));

    }

    /**
     * 对应测试用例 4.1：可转换 present、不可转换/空 empty
     */
    @Test
    public void convertOpt() {

        Assertions.assertTrue(CConvertUtils.convertOpt("123", Integer.class).isPresent());
        Assertions.assertFalse(CConvertUtils.convertOpt(new ArrayList<>(), String.class).isPresent());
        Assertions.assertFalse(CConvertUtils.convertOpt(null, String.class).isPresent());

    }

    /**
     * 对应测试用例 5.1：{@code addConverter} 注册无入参方法：跳过注册（不新增转换器）
     *
     * <p><b>断言有效性（回归点）</b>：原用例只断言 {@code assertDoesNotThrow}——而"跳过注册"的失败形态
     * 是<strong>越界异常</strong>（{@code method.getParameterTypes()[0]} 对无参方法抛
     * {@code ArrayIndexOutOfBoundsException}），{@code assertDoesNotThrow} 恰好能接住它；
     * 但若实现改成"静默注册一个无效转换器"（不抛异常、却污染转换器表），本用例仍全绿。
     * 故改为断言<strong>可观测的行为结果</strong>：注册前后已注册转换器数量不变。</p>
     *
     * <p>转换器表（{@code CLASS_CONVERTERS}）为类静态、全局共享，用例以
     * {@code CReflectUtils#getValue} 读取（项目统一的字段入口，静态字段经 {@code Field#get} 兜底，
     * 不使用原生反射）并做前后比对。</p>
     */
    @Test
    public void addConverterNoArgMethod() throws NoSuchMethodException {

        val method = NoArgConverterBean.class.getDeclaredMethod("noArg");

        val before = converterCount();
        Assertions.assertDoesNotThrow(() -> CConvertUtils.addConverter(method));
        Assertions.assertEquals(before, converterCount(),
            "注册无入参方法应跳过注册，不得新增转换器（也不得抛越界异常）");

    }

    /**
     * 读当前已注册转换器数量（{@code CLASS_CONVERTERS} 为私有静态字段）
     *
     * <p>静态字段不能用 {@code CMethodHandleUtils} 的 getter 句柄（{@code unreflectGetter} 对静态字段
     * 返回 {@code ()Object}，{@code invoke(Class)} 会抛 {@code WrongMethodTypeException}），
     * 故经项目统一的字段入口 {@code CReflectUtils#getValue} 读取。</p>
     *
     * @return 已注册转换器数量
     */
    private static int converterCount() {

        try {
            val field = CConvertUtils.class.getDeclaredField("CLASS_CONVERTERS");
            val converters = (Collection<?>)CReflectUtils.getValue(CConvertUtils.class, field);
            return converters.size();
        } catch (Throwable t) {
            throw new AssertionError(t);
        }

    }

    /**
     * 测试用枚举，实现 toString 语义（此处仅用于验证 Collection 之外的转换回退行为）
     */
    enum EnumBean {
        INSERT
    }

    /**
     * 无参方法 Bean（验证 addConverter 跳过注册）
     */
    static class NoArgConverterBean {

        public String noArg() {
            return "no-arg";
        }

    }

}
