package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CValueUtils;
import com.c332030.ctool4j.definition.interfaces.ICValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Description: CValueUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「直接取值 / 对象重载取值 / 消费」三个维度组织。</li>
 *   <li>直接取值：从 ICValue 取值正例与 null 枚举返回 null。</li>
 *   <li>对象重载：obj 取值正例、obj 为 null、函数返回 null 三分支。</li>
 *   <li>消费：枚举非空时消费、枚举 null 时不消费（初始值保持不变）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 null 语义的约定（返回 null / 跳过消费）。</li>
 *   <li>依据测试方法（等价类/边界值）：正例、null 对象、null 函数结果、null 枚举。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getValue(ICValue) 正例与 null；getValue(obj, function) 正例、obj null、函数 null；</li>
 *   <li>setValue 非空消费与 null 不消费。</li>
 *   <li>未覆盖：ICValue 实现为 null 值的情况（其取值仍为 null，与 null 枚举语义一致，未单列）。</li>
 * </ul>
 * <h2>直接取值（getValue(ICValue)）</h2>
 * <ul>
 *   <li>1.1 正例：{@code ValueHolder("hello")} → {@code "hello"}；null 枚举 → null（getValueFromICValue）</li>
 * </ul>
 * <h2>对象重载取值（getValue(obj, function)）</h2>
 * <ul>
 *   <li>2.1 正例：{@code holder → "obj-value"}；obj 为 null → null；函数返回 null → null（getValueFromObj）</li>
 * </ul>
 * <h2>消费（setValue）</h2>
 * <ul>
 *   <li>3.1 非空消费：枚举 {@code "value"} 被消费（setValueConsumesWhenNonNull）</li>
 *   <li>3.2 null 不消费：null 枚举不改变初始值（setValueNullICValueNoOp）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CValueUtilsTests {

    @Getter
    @RequiredArgsConstructor
    private static class ValueHolder implements ICValue<String> {

        private final String value;

    }

    /**
     * 对应测试用例 1.1：正例：{@code ValueHolder("hello")} → {@code "hello"}；null 枚举 → null
     */
    @Test
    public void getValueFromICValue() {

        Assertions.assertEquals("hello", CValueUtils.getValue(new ValueHolder("hello")));
        Assertions.assertNull(CValueUtils.getValue((ICValue<String>) null));

    }

    /**
     * 对应测试用例 2.1：正例：{@code holder → "obj-value"}；obj 为 null → null；函数返回 null → null
     */
    @Test
    public void getValueFromObj() {

        ValueHolder holder = new ValueHolder("obj-value");
        Assertions.assertEquals("obj-value", CValueUtils.getValue(holder, h -> h));

        // obj 为 null 返回 null
        Assertions.assertNull(CValueUtils.getValue((ValueHolder) null, h -> h));
        // 函数返回 null ICValue 返回 null
        Assertions.assertNull(CValueUtils.getValue(holder, h -> (ICValue<String>) null));

    }

    /**
     * 对应测试用例 3.1：非空消费：枚举 {@code "value"} 被消费
     */
    @Test
    public void setValueConsumesWhenNonNull() {

        AtomicReference<String> consumed = new AtomicReference<>();
        CValueUtils.setValue(new ValueHolder("value"), consumed::set);

        Assertions.assertEquals("value", consumed.get());

    }

    /**
     * 对应测试用例 3.2：null 不消费：null 枚举不改变初始值
     */
    @Test
    public void setValueNullICValueNoOp() {

        AtomicReference<String> consumed = new AtomicReference<>("initial");
        CValueUtils.setValue(null, consumed::set);

        Assertions.assertEquals("initial", consumed.get());

    }

}
