package com.c332030.ctool4j.mybatis.model.impl;

import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.mybatis.model.ICPageResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * <p>
 * Description: CPageResultTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证分页结果模型的默认值、builder 构建、便捷构造、翻页边界判断，以及 {@code ICPageResult} 的接口默认契约。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 current/size/total/pages/records 默认值与 hasNext/hasPrevious 判定的约定。</li>
 *   <li>依据测试方法（等价类/边界）：默认值、builder、便捷构造的部分填充、翻页边界（首页/末页/空结果）、接口默认实现。</li>
 *   <li>用例内显式写出泛型类型、且 builder 带类型见证（如 {@code CPageResult.<String>builder()}）：本类为泛型类，{@code val} 对泛型方法的返回类型报 type cannot be resolved，泛型 builder 不加类型见证则得到 capture 类型、无法赋值给具体泛型类型。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认值；builder 构建；of 便捷构造；hasNext/hasPrevious 首页、末页、空结果；接口默认实现；默认 records 不可修改。</li>
 *   <li>未覆盖：Jackson 序列化/反序列化往返（由引用方的框架集成覆盖，本类不引入序列化依赖）。</li>
 * </ul>
 * <h2>分页结果模型</h2>
 * <ul>
 *   <li>1.1 默认值（{@code defaultValues}）</li>
 *   <li>1.2 builder 构建（{@code builder}）</li>
 *   <li>1.3 便捷构造 of（{@code of}）</li>
 *   <li>1.4 翻页判断（{@code hasNextAndHasPrevious}）</li>
 *   <li>1.5 接口默认实现（{@code interfaceDefaults}）</li>
 *   <li>1.6 默认 records 不可修改（{@code recordsDefaultUnmodifiable}）</li>
 * </ul>
 *
 * @since 2026/9/19
 * @version 1.0
 * @see CPageResult
 */
public class CPageResultTests {

    /**
     * 极简实现：不覆写任何 getter，用于验证 {@code ICPageResult} 的默认实现
     */
    private static class DefaultPageResult implements ICPageResult<Object> {
    }

    /**
     * 对应测试用例 1.1：默认值（{@code defaultValues}）
     */
    @Test
    public void defaultValues() {
        CPageResult<Object> result = new CPageResult<>();
        Assertions.assertEquals(1L, result.getCurrent());
        Assertions.assertEquals(0L, result.getSize());
        Assertions.assertEquals(0L, result.getTotal());
        Assertions.assertEquals(0L, result.getPages());
        Assertions.assertNotNull(result.getRecords());
        Assertions.assertEquals(0, result.getRecords().size());
    }

    /**
     * 对应测试用例 1.2：builder 构建（{@code builder}）
     */
    @Test
    public void builder() {
        CPageResult<String> result = CPageResult.<String>builder()
            .current(2L)
            .size(10L)
            .total(25L)
            .pages(3L)
            .records(CList.of("a", "b"))
            .build();
        Assertions.assertEquals(2L, result.getCurrent());
        Assertions.assertEquals(10L, result.getSize());
        Assertions.assertEquals(25L, result.getTotal());
        Assertions.assertEquals(3L, result.getPages());
        Assertions.assertEquals(CList.of("a", "b"), result.getRecords());
    }

    /**
     * 对应测试用例 1.3：便捷构造 of（{@code of}）
     */
    @Test
    public void of() {
        List<String> records = CList.of("a", "b");
        CPageResult<String> result = CPageResult.of(2L, records);
        Assertions.assertEquals(2L, result.getTotal());
        Assertions.assertEquals(records, result.getRecords());
        // 便捷构造只填充 total/records，分页信息保持默认值、不推断
        Assertions.assertEquals(1L, result.getCurrent());
        Assertions.assertEquals(0L, result.getSize());
        Assertions.assertEquals(0L, result.getPages());
    }

    /**
     * 对应测试用例 1.4：翻页判断（{@code hasNextAndHasPrevious}）
     */
    @Test
    public void hasNextAndHasPrevious() {
        // 首页：无上一页、有下一页
        CPageResult<Object> first = CPageResult.<Object>builder().current(1L).size(10L).total(25L).pages(3L).build();
        Assertions.assertFalse(first.hasPrevious());
        Assertions.assertTrue(first.hasNext());

        // 末页：有上一页、无下一页
        CPageResult<Object> last = CPageResult.<Object>builder().current(3L).size(10L).total(25L).pages(3L).build();
        Assertions.assertTrue(last.hasPrevious());
        Assertions.assertFalse(last.hasNext());

        // 空结果（默认值）：两向都没有
        CPageResult<Object> empty = CPageResult.<Object>builder().build();
        Assertions.assertFalse(empty.hasPrevious());
        Assertions.assertFalse(empty.hasNext());
    }

    /**
     * 对应测试用例 1.5：接口默认实现（{@code interfaceDefaults}）
     */
    @Test
    public void interfaceDefaults() {
        ICPageResult<Object> result = new DefaultPageResult();
        Assertions.assertEquals(1L, result.getCurrent());
        Assertions.assertEquals(0L, result.getSize());
        Assertions.assertEquals(0L, result.getTotal());
        Assertions.assertEquals(0L, result.getPages());
        Assertions.assertNotNull(result.getRecords());
        Assertions.assertEquals(0, result.getRecords().size());
        Assertions.assertFalse(result.hasNext());
        Assertions.assertFalse(result.hasPrevious());
    }

    /**
     * 对应测试用例 1.6：默认 records 不可修改（{@code recordsDefaultUnmodifiable}）
     */
    @Test
    public void recordsDefaultUnmodifiable() {
        CPageResult<Object> result = new CPageResult<>();
        List<Object> records = result.getRecords();
        Assertions.assertThrowsExactly(
            UnsupportedOperationException.class,
            () -> records.add(null)
        );
    }

}
