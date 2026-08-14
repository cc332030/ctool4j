package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CValueUtils;
import com.c332030.ctool4j.definition.interfaces.ICValue;
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

    private static class ValueHolder implements ICValue<String> {

        private final String value;

        ValueHolder(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

    }

    @Test
    public void getValueFromICValue() {

        Assertions.assertEquals("hello", CValueUtils.getValue(new ValueHolder("hello")));
        Assertions.assertNull(CValueUtils.getValue((ICValue<String>) null));

    }

    @Test
    public void getValueFromObj() {

        ValueHolder holder = new ValueHolder("obj-value");
        Assertions.assertEquals("obj-value", CValueUtils.getValue(holder, h -> h));

        // obj 为 null 返回 null
        Assertions.assertNull(CValueUtils.getValue((ValueHolder) null, h -> h));
        // 函数返回 null ICValue 返回 null
        Assertions.assertNull(CValueUtils.getValue(holder, h -> (ICValue<String>) null));

    }

    @Test
    public void setValueConsumesWhenNonNull() {

        AtomicReference<String> consumed = new AtomicReference<>();
        CValueUtils.setValue(new ValueHolder("value"), consumed::set);

        Assertions.assertEquals("value", consumed.get());

    }

    @Test
    public void setValueNullICValueNoOp() {

        AtomicReference<String> consumed = new AtomicReference<>("initial");
        CValueUtils.setValue(null, consumed::set);

        Assertions.assertEquals("initial", consumed.get());

    }

}
