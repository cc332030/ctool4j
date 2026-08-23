package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.definition.enums.CDbOperateEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Description: CSetTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CSetTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void ofEmpty() {

        Assertions.assertTrue(CSet.<String>of().isEmpty());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void ofGeneric() {

        Set<String> set = CSet.of("a", "b", "a");
        Assertions.assertEquals(new HashSet<>(Arrays.asList("a", "b")), set);

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void ofGenericNullFiltered() {

        Set<String> set = CSet.of(null, "b", null);
        Assertions.assertEquals(new HashSet<>(Collections.singletonList("b")), set);

        Assertions.assertTrue(CSet.of((String) null, null).isEmpty());

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void ofGenericUnmodifiable() {

        Set<String> set = CSet.of("a", "b");
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> set.add("c"));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void ofEnum() {

        Set<CDbOperateEnum> set = CSet.of(CDbOperateEnum.INSERT, CDbOperateEnum.DELETE);
        Assertions.assertEquals(2, set.size());
        Assertions.assertTrue(set.contains(CDbOperateEnum.INSERT));
        Assertions.assertTrue(set.contains(CDbOperateEnum.DELETE));

    }

    /**
     * 对应测试用例 3.2
     */
    @Test
    public void ofEnumUnmodifiable() {

        Set<CDbOperateEnum> set = CSet.of(CDbOperateEnum.INSERT, CDbOperateEnum.DELETE);
        Assertions.assertThrowsExactly(UnsupportedOperationException.class,
                () -> set.add(CDbOperateEnum.UPDATE));

    }

}
