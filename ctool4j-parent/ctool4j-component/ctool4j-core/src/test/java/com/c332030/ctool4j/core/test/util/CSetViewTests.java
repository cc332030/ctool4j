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
 * 是 {@link CSetView} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖无参/指定可变实例两种构造。</li>
 *   <li>覆盖「改可变实例 → 视图实时反映」核心语义。</li>
 *   <li>覆盖视图只读（写入抛异常）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对「双实例、视图为实时视图、视图只读」的约定。</li>
 *   <li>依据白盒/黑盒原则：构造、视图实时性、只读性均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：两种构造、改可变视图反映、视图只读抛异常。</li>
 *   <li>未覆盖：并发场景（默认非线程安全，未专门并发测试）。</li>
 * </ul>
 * <h2>构造</h2>
 * <ul>
 *   <li>1.1 无参构造可变/不可变均非空（constructDefault）</li>
 *   <li>1.2 指定可变实例构造生效（constructWithMutable）</li>
 *   <li>1.3 可变与不可变为不同对象（distinctInstance）</li>
 * </ul>
 * <h2>视图实时性</h2>
 * <ul>
 *   <li>2.1 改可变实例后视图反映最新内容（viewReflectsMutable）</li>
 * </ul>
 * <h2>视图只读</h2>
 * <ul>
 *   <li>3.1 对视图写入抛 UnsupportedOperationException（viewImmutable）</li>
 * </ul>
 *
 * @since 2026/8/31
 * @version 1.0
 */
class CSetViewTests {

    /**
     * 无参构造可变/不可变均非空
     * <p>
     * 对应测试用例 1.1：无参构造可变/不可变均非空
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
     * 对应测试用例 1.2：指定可变实例构造生效
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
     * 对应测试用例 1.3：可变与不可变为不同对象
     */
    @Test
    void distinctInstance() {
        CSetView<String> view = CSetView.of();
        Assertions.assertNotSame(view.getMutable(), view.getImmutable());
    }

    /**
     * 改可变实例后视图反映最新内容
     * <p>
     * 对应测试用例 2.1：改可变实例后视图反映最新内容
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
     * 对应测试用例 3.1：对视图写入抛 UnsupportedOperationException
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
