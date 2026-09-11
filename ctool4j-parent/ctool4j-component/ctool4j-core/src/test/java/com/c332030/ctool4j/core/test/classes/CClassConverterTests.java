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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「builder / 全参构造 / 无参构造」三个构造入口组织。</li>
 *   <li>builder 与全参构造验证字段正确且转换函数可用；无参构造验证字段为 null。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对三种构造方式的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：builder 构造字段与转换；全参构造；无参构造字段 null。</li>
 *   <li>未覆盖：无（覆盖了全部构造）。</li>
 * </ul>
 * <h2>构造</h2>
 * <ul>
 *   <li>1.1 builder：fromClass/toClass/converter 正确（build）</li>
 *   <li>1.2 全参构造：字段正确且转换可用（allArgsConstructor）</li>
 *   <li>1.3 无参构造：字段为 null（noArgsConstructor）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.0
 */
public class CClassConverterTests {

    /**
     * 对应测试用例 1.1：fromClass/toClass/converter 正确
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
     * 对应测试用例 1.2：全参构造：字段正确且转换可用
     */
    @Test
    public void allArgsConstructor() {

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
     * 对应测试用例 1.3：无参构造：字段为 null
     */
    @Test
    public void noArgsConstructor() {

        CClassConverter<String, Integer> c = new CClassConverter<>();
        Assertions.assertNull(c.getFromClass());
        Assertions.assertNull(c.getToClass());
        Assertions.assertNull(c.getConverter());

    }

}
