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
 * @since 2026/8/14
 */
public class CValueUtilsTests {

    @Getter
    @RequiredArgsConstructor
    private static class ValueHolder implements ICValue<String> {

        private final String value;

    }

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void getValueFromICValue() {

        Assertions.assertEquals("hello", CValueUtils.getValue(new ValueHolder("hello")));
        Assertions.assertNull(CValueUtils.getValue((ICValue<String>) null));

    }

    /**
     * 对应测试用例 2.1
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
     * 对应测试用例 3.1
     */
    @Test
    public void setValueConsumesWhenNonNull() {

        AtomicReference<String> consumed = new AtomicReference<>();
        CValueUtils.setValue(new ValueHolder("value"), consumed::set);

        Assertions.assertEquals("value", consumed.get());

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void setValueNullICValueNoOp() {

        AtomicReference<String> consumed = new AtomicReference<>("initial");
        CValueUtils.setValue(null, consumed::set);

        Assertions.assertEquals("initial", consumed.get());

    }

}
