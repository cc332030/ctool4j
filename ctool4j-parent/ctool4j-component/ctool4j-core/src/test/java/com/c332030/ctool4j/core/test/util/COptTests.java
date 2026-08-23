package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.COpt;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: COptTests
 * </p>
 *
 * @since 2025/12/6
 */
public class COptTests {

    /**
     * 测试 of 对 null 抛 NPE
     * 对应测试用例 1.1
     */
    @Test
    public void of() {

        Assertions.assertThrowsExactly(NullPointerException.class, () -> COpt.of(null));

    }

    /**
     * 测试 empty 与 ofNullable(null) 等价
     * 对应测试用例 1.2
     */
    @Test
    public void empty() {

        Assertions.assertEquals(COpt.empty(), COpt.ofNullable(null));

    }


    /**
     * 测试空集合、空 Map、空字符串视为空值
     * 对应测试用例 1.3
     */
    @Test
    public void ofEmptyAble() {

        Assertions.assertFalse(COpt.ofEmptyAble(CList.of()).isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(CList.of(1)).isPresent());

        Assertions.assertFalse(COpt.ofEmptyAble(CMap.of()).isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(CMap.of(1, 1)).isPresent());

        Assertions.assertFalse(COpt.ofEmptyAble("").isPresent());
        Assertions.assertTrue(COpt.ofEmptyAble(" ").isPresent());

    }

    /**
     * 测试空白字符串视为空值
     * 对应测试用例 1.4
     */
    @Test
    public void ofBlankAble() {

        Assertions.assertFalse(COpt.ofBlankAble(" ").isPresent());
        Assertions.assertTrue(COpt.ofBlankAble("1").isPresent());

    }

    /**
     * 测试值存在判断
     * 对应测试用例 2.1
     */
    @Test
    public void isPresent() {

        Assertions.assertTrue(COpt.ofNullable(1).isPresent());
        Assertions.assertFalse(COpt.ofNullable(null).isPresent());

    }

    /**
     * 测试获取值
     * 对应测试用例 3.1
     */
    @Test
    public void get() {

        val result = COpt.of(7)
                .get()
                ;
        Assertions.assertEquals(7, result);

    }

    /**
     * 测试空值时返回默认值
     * 对应测试用例 3.2
     */
    @Test
    public void orElse() {

        val result = COpt.ofNullable(null)
                .orElse(33)
                ;
        Assertions.assertEquals(33, result);

    }

    /**
     * 测试空值时通过供应商获取默认值
     * 对应测试用例 3.3
     */
    @Test
    public void orElseGet() {

        val result = COpt.ofNullable(null)
                .orElseGet(() -> 33)
                ;
        Assertions.assertEquals(33, result);

        // 值存在时 supplier 不应执行（惰性语义）
        val called = new boolean[] {false};
        val presentResult = COpt.of(7)
                .orElseGet(() -> {
                    called[0] = true;
                    return 33;
                })
                ;
        Assertions.assertEquals(7, presentResult);
        Assertions.assertFalse(called[0]);

    }

    /**
     * 测试空值时抛出指定异常
     * 对应测试用例 3.4
     */
    @Test
    public void orElseThrow() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> COpt.ofNullable(null)
                        .orElseThrow(IllegalArgumentException::new));

    }

    /**
     * 测试值转换
     * 对应测试用例 4.1
     */
    @Test
    public void map() {

        val result = COpt.of(7)
                .map(String::valueOf)
                .get()
                ;
        Assertions.assertEquals("7", result);

    }

    /**
     * 测试扁平化值转换
     * 对应测试用例 4.2
     */
    @Test
    public void flatMap() {

        val result = COpt.of(7)
                .flatMap(e -> COpt.of(String.valueOf(e)))
                .get()
                ;
        Assertions.assertEquals("7", result);

    }

}
