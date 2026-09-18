package com.c332030.ctool4j.spring.security.util;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.IntStream;

/**
 * <p>
 * Description: CGrantedAuthorityUtilsTests
 * </p>
 * <p>
 * 验证权限常量池的核心契约：相同 authority 全局复用同一实例（含并发场景），无效值不入池，
 * 键按原文精确匹配，批量获取保持顺序并过滤无效项。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>「同值唯一」用 {@code assertSame} 断言实例同一性（不依赖 equals），并发场景用平行流验证。</li>
 *   <li>常量池无失效机制，用例使用各自独立的权限名，避免用例间相互污染。</li>
 *   <li>键匹配语义（不 trim、区分大小写）用 {@code assertNotSame} + 值断言固化，防止后续被“顺手归一化”改掉。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据「权限判定依赖精确字符串」的约定：归一化会静默改写权限值，故池不做 trim/大小写归一。</li>
 *   <li>依据 {@code SimpleGrantedAuthority} 构造对空白入参抛 {@code IllegalArgumentException} 的既有约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：同值唯一、异值不同、无效值 null、并发同值唯一、批量顺序与过滤、键精确匹配边界。</li>
 *   <li>未覆盖：池容量上限与淘汰（本池不淘汰，无该行为）；池的持久化/序列化（池是进程内对象，不参与持久化）。</li>
 * </ul>
 * <h2>设计取舍</h2>
 * <ul>
 *   <li>并发用例按引用比较（{@code ==}）而非集合去重：语义就是要验证「只有一个实例」，去重会被 equals 掩盖。</li>
 *   <li>无效值用例使用强转后的 null 入参（传 null 字面量会因重载歧义编译失败）。</li>
 * </ul>
 * <h2>权限常量池</h2>
 * <ul>
 *   <li>1 单值获取（对应测试方法 1.1-1.5）</li>
 *   <li>2 批量获取（对应测试方法 2.1-2.2）</li>
 * </ul>
 *
 * @since 2026/9/14
 * @version 1.0
 */
class CGrantedAuthorityUtilsTests {

    /**
     * 并发获取的次数
     */
    private static final int CONCURRENT_TIMES = 64;

    /**
     * 对应测试用例 1.1：相同 authority 返回同一实例
     */
    @Test
    void testGet_sameAuthority_sameInstance() {
        // 正例：常量池核心契约——同值唯一
        val first = CGrantedAuthorityUtils.get("ROLE_POOL_SAME");
        val second = CGrantedAuthorityUtils.get("ROLE_POOL_SAME");

        Assertions.assertNotNull(first);
        Assertions.assertSame(first, second);
    }

    /**
     * 对应测试用例 1.2：不同 authority 为不同实例
     */
    @Test
    void testGet_differentAuthority_differentInstance() {
        // 边界：不同值不复用（避免键冲突导致权限串味）
        Assertions.assertNotSame(
            CGrantedAuthorityUtils.get("ROLE_POOL_X"),
            CGrantedAuthorityUtils.get("ROLE_POOL_Y")
        );
    }

    /**
     * 对应测试用例 1.3：null/空/空白返回 null 且不入池
     */
    @Test
    void testGet_invalidAuthority_returnsNull() {
        // 边界：无效值返回 null，避免把 SimpleGrantedAuthority 的构造断言异常抛给调用方
        Assertions.assertNull(CGrantedAuthorityUtils.get((String) null));
        Assertions.assertNull(CGrantedAuthorityUtils.get(""));
        Assertions.assertNull(CGrantedAuthorityUtils.get("   "));
    }

    /**
     * 对应测试用例 1.4：键按原文精确匹配——不 trim、区分大小写
     */
    @Test
    void testGet_boundaryKey_notNormalized() {
        // 边界：不归一化是刻意取舍（归一化会改写权限值）
        Assertions.assertNotSame(
            CGrantedAuthorityUtils.get("ROLE_A"),
            CGrantedAuthorityUtils.get("role_a")
        );
        Assertions.assertNotSame(
            CGrantedAuthorityUtils.get("ROLE_A"),
            CGrantedAuthorityUtils.get(" ROLE_A")
        );
        // 原文原样还原，不被静默改写
        Assertions.assertEquals(" ROLE_A", CGrantedAuthorityUtils.get(" ROLE_A").getAuthority());
    }

    /**
     * 对应测试用例 1.5：并发获取同一 authority 仍为同一实例
     */
    @Test
    void testGet_concurrent_sameInstance() {
        // 并发：并行获取下池内只创建一个实例（按引用比较，不依赖 equals）
        val expected = CGrantedAuthorityUtils.get("ROLE_POOL_CONCURRENT");

        val allSame = IntStream.range(0, CONCURRENT_TIMES)
            .parallel()
            .allMatch(i -> expected == CGrantedAuthorityUtils.get("ROLE_POOL_CONCURRENT"));

        Assertions.assertTrue(allSame);
    }

    /**
     * 对应测试用例 2.1：批量获取保持顺序、过滤无效项、元素与单值获取同实例
     */
    @Test
    void testGet_collection_keepsOrderAndFiltersInvalid() {
        // 正例：顺序保持 + 无效项跳过
        val authorities = CGrantedAuthorityUtils.get(
            Arrays.asList("ROLE_POOL_A", null, " ", "ROLE_POOL_B")
        );

        Assertions.assertEquals(2, authorities.size());
        Assertions.assertSame(CGrantedAuthorityUtils.get("ROLE_POOL_A"), authorities.get(0));
        Assertions.assertSame(CGrantedAuthorityUtils.get("ROLE_POOL_B"), authorities.get(1));
    }

    /**
     * 对应测试用例 2.2：null/空集合返回空 List
     */
    @Test
    void testGet_emptyCollection_returnsEmpty() {
        // 边界：空入参返回空集合，不抛异常
        val empty = Collections.<String>emptyList();

        Assertions.assertTrue(CGrantedAuthorityUtils.get(empty).isEmpty());
        Assertions.assertTrue(CGrantedAuthorityUtils.get((Collection<String>) null).isEmpty());
    }

}
