package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CMapView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CMapViewTests
 * </p>
 *
 * <p>
 * 是 {@link CMapView} 的测试用例（对应测试文档
 * <code>doc/design/core/CMapViewTests.adoc</code>）。
 * </p>
 *
 * @since 2026/8/31
 */
class CMapViewTests {

    /**
     * 无参构造可变/不可变均非空
 * <p>
 * 对应测试用例 1.1
 */
    @Test
    void constructDefault() {
        CMapView<String, String> view = CMapView.of();
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
        Map<String, String> mutable = new HashMap<>();
        mutable.put("k", "v");
        CMapView<String, String> view = CMapView.of(mutable);
        Assertions.assertSame(mutable, view.getMutable());
        Assertions.assertEquals("v", view.getImmutable().get("k"));
    }

    /**
     * 可变与不可变为不同对象
 * <p>
 * 对应测试用例 1.3
 */
    @Test
    void distinctInstance() {
        CMapView<String, String> view = CMapView.of();
        Assertions.assertNotSame(view.getMutable(), view.getImmutable());
    }

    /**
     * 改可变实例后视图反映最新内容
 * <p>
 * 对应测试用例 2.1
 */
    @Test
    void viewReflectsMutable() {
        CMapView<String, String> view = CMapView.of();
        view.getMutable().put("a", "1");
        Assertions.assertEquals("1", view.getImmutable().get("a"));
    }

    /**
     * 对视图写入抛 UnsupportedOperationException
 * <p>
 * 对应测试用例 3.1
 */
    @Test
    void viewImmutable() {
        CMapView<String, String> view = CMapView.of();
        Assertions.assertThrowsExactly(
            UnsupportedOperationException.class,
            () -> view.getImmutable().put("a", "1")
        );
    }
}
