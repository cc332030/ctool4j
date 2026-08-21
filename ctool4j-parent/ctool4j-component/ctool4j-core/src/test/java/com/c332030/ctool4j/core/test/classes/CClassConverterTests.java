package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CClassConverter;
import com.c332030.ctool4j.definition.function.CFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CClassConverterTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CClassConverterTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void build() {

        CFunction<String, Integer> converter = Integer::valueOf;
        CClassConverter<String, Integer> c = CClassConverter.<String, Integer>builder()
                .fromClass(String.class)
                .toClass(Integer.class)
                .converter(converter)
                .build();

        Assertions.assertEquals(String.class, c.getFromClass());
        Assertions.assertEquals(Integer.class, c.getToClass());
        Assertions.assertEquals(123, c.getConverter().apply("123"));

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void allArgsConstructor() {

        CFunction<String, Integer> converter = Integer::valueOf;
        CClassConverter<String, Integer> c = new CClassConverter<>(String.class, Integer.class, converter);

        Assertions.assertEquals(String.class, c.getFromClass());
        Assertions.assertEquals(Integer.class, c.getToClass());
        Assertions.assertEquals(123, c.getConverter().apply("123"));

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void noArgsConstructor() {

        CClassConverter<String, Integer> c = new CClassConverter<>();
        Assertions.assertNull(c.getFromClass());
        Assertions.assertNull(c.getToClass());
        Assertions.assertNull(c.getConverter());

    }

}
