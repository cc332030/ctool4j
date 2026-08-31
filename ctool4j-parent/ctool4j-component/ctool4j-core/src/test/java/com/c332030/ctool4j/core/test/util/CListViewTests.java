package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CListView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Description: CListViewTests
 * </p>
 *
 * <p>
 * 是 {@link CListView} 的测试用例（对应测试文档
 * <code>doc/design/core/CListViewTests.adoc</code>）。
 * </p>
 *
 * @since 2026/8/31
 */
class CListViewTests {

    /**
     * 无参构造可变/不可变均非空
 * <p>
 * 对应测试用例 1.1
 */
    @Test
    void constructDefault() {
        CListView<String> view = CListView.of();
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
        List<String> mutable = new ArrayList<>();
        mutable.add("a");
        CListView<String> view = CListView.of(mutable);
        Assertions.assertSame(mutable, view.getMutable());
        Assertions.assertEquals("a", view.getImmutable().get(0));
    }

    /**
     * 可变与不可变为不同对象
 * <p>
 * 对应测试用例 1.3
 */
    @Test
    void distinctInstance() {
        CListView<String> view = CListView.of();
        Assertions.assertNotSame(view.getMutable(), view.getImmutable());
    }

    /**
     * 改可变实例后视图反映最新内容
 * <p>
 * 对应测试用例 2.1
 */
    @Test
    void viewReflectsMutable() {
        CListView<String> view = CListView.of();
        view.getMutable().add("a");
        Assertions.assertEquals("a", view.getImmutable().get(0));
    }

    /**
     * 对视图写入抛 UnsupportedOperationException
 * <p>
 * 对应测试用例 3.1
 */
    @Test
    void viewImmutable() {
        CListView<String> view = CListView.of();
        Assertions.assertThrowsExactly(
            UnsupportedOperationException.class,
            () -> view.getImmutable().add("a")
        );
    }
}
