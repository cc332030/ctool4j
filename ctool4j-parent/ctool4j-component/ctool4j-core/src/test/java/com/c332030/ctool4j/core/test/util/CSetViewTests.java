package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CSetView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Description: CSetViewTests
 * </p>
 *
 * <p>
 * 是 {@link CSetView} 的测试用例（对应测试文档
 * <code>doc/design/core/CSetViewTests.adoc</code>）。
 * </p>
 *
 * @since 2026/8/31
 */
class CSetViewTests {

    /**
     * 无参构造可变/不可变均非空
 * <p>
 * 对应测试用例 1.1
 */
    @Test
    void constructDefault() {
        CSetView<String> view = CSetView.of();
        Assertions.assertNotNull(view.getMutable());
        Assertions.assertNotNull(view.getImmutable());
    }

    /**
     * 指定可变实例构造生效
 * <p>
 * 对应测试用例 1.2
 */
    @Test
    void constructWithMutable() {
        Set<String> mutable = new HashSet<>();
        mutable.add("a");
        CSetView<String> view = CSetView.of(mutable);
        Assertions.assertSame(mutable, view.getMutable());
        Assertions.assertTrue(view.getImmutable().contains("a"));
    }

    /**
     * 可变与不可变为不同对象
 * <p>
 * 对应测试用例 1.3
 */
    @Test
    void distinctInstance() {
        CSetView<String> view = CSetView.of();
        Assertions.assertNotSame(view.getMutable(), view.getImmutable());
    }

    /**
     * 改可变实例后视图反映最新内容
 * <p>
 * 对应测试用例 2.1
 */
    @Test
    void viewReflectsMutable() {
        CSetView<String> view = CSetView.of();
        view.getMutable().add("a");
        Assertions.assertTrue(view.getImmutable().contains("a"));
    }

    /**
     * 对视图写入抛 UnsupportedOperationException
 * <p>
 * 对应测试用例 3.1
 */
    @Test
    void viewImmutable() {
        CSetView<String> view = CSetView.of();
        Assertions.assertThrowsExactly(
            UnsupportedOperationException.class,
            () -> view.getImmutable().add("a")
        );
    }
}
