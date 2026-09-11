package com.c332030.ctool4j.log4j.test.mdc;

import com.c332030.ctool4j.log4j.mdc.CMdcLog4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * <p>
 * Description: CMdcLog4jTests
 * </p>
 *
 * <p>
 * 是 {@link CMdcLog4j} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖 put/get 基本读写、重复 put 覆盖、remove、clear。</li>
 *   <li>覆盖 containsKey、isEmpty、getCopy 副本隔离、getImmutableMapOrNull 空与非空形态。</li>
 *   <li>覆盖异常路径：put/get 的 null key/value 抛 NPE。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"TTL 存储、副本隔离、不可变副本"的约定。</li>
 *   <li>依据白盒/黑盒原则与错误推测法：读写覆盖、空值、副本隔离、null 入参、ConcurrentHashMap null 限制均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：put/get/overwrite/remove/clear、containsKey、isEmpty、副本隔离、不可变副本、null key/value 抛 NPE。</li>
 *   <li>未覆盖：TTL 跨线程传递行为（依赖 TTL 机制，未在单测断言跨线程）。</li>
 * </ul>
 * <h2>基本读写</h2>
 * <ul>
 *   <li>1.1 put 后可 get 到对应值（putAndGet）</li>
 *   <li>1.2 重复 put 覆盖旧值（putOverwrite）</li>
 *   <li>1.3 remove 后 get 返回 null（remove）</li>
 *   <li>1.4 未 put 过的 key 返回 null（getNonExistentReturnsNull）</li>
 *   <li>1.5 clear 清空所有值（clearEmpties）</li>
 * </ul>
 * <h2>键判断</h2>
 * <ul>
 *   <li>2.1 containsKey 判断键是否存在（containsKey）</li>
 * </ul>
 * <h2>上下文副本</h2>
 * <ul>
 *   <li>3.1 getCopy 返回副本，修改不影响原 MDC（getCopyIsCopy）</li>
 * </ul>
 * <h2>不可变副本</h2>
 * <ul>
 *   <li>4.1 空 MDC 时 getImmutableMapOrNull 返回 null（getImmutableMapOrNullEmpty）</li>
 *   <li>4.2 非空时返回不可变副本（getImmutableMapOrNullNonEmpty）</li>
 * </ul>
 * <h2>空判断</h2>
 * <ul>
 *   <li>5.1 isEmpty 判断是否为空（isEmpty）</li>
 * </ul>
 * <h2>异常路径</h2>
 * <ul>
 *   <li>6.1 put(null, val) 抛 NPE（putNullKeyThrows）</li>
 *   <li>6.2 put(key, null) 抛 NPE（putNullValueThrows）</li>
 * </ul>
 *
 * @since 2026/8/31
 * @version 1.0
 */
class CMdcLog4jTests {

    private CMdcLog4j mdc;

    @BeforeEach
    void setUp() {
        mdc = new CMdcLog4j();
        mdc.clear();
    }

    /**
     * 正常路径：put 后可 get 到对应值
     * <p>
     * 对应测试用例 1.1：put 后可 get 到对应值
     */
    @Test
    void putAndGet() {
        mdc.put("key", "value");
        Assertions.assertEquals("value", mdc.get("key"));
    }

    /**
     * 正常路径：重复 put 覆盖旧值
     * <p>
     * 对应测试用例 1.2：重复 put 覆盖旧值
     */
    @Test
    void putOverwrite() {
        mdc.put("key", "v1");
        mdc.put("key", "v2");
        Assertions.assertEquals("v2", mdc.get("key"));
    }

    /**
     * 正常路径：remove 后 get 返回 null
     * <p>
     * 对应测试用例 1.3：remove 后 get 返回 null
     */
    @Test
    void remove() {
        mdc.put("key", "value");
        mdc.remove("key");
        Assertions.assertNull(mdc.get("key"));
    }

    /**
     * 边界：未 put 过的 key 返回 null
     * <p>
     * 对应测试用例 1.4：未 put 过的 key 返回 null
     */
    @Test
    void getNonExistentReturnsNull() {
        Assertions.assertNull(mdc.get("not-exist"));
    }

    /**
     * 边界：clear 清空所有值
     * <p>
     * 对应测试用例 1.5：clear 清空所有值
     */
    @Test
    void clearEmpties() {
        mdc.put("a", "1");
        mdc.put("b", "2");
        mdc.clear();
        Assertions.assertNull(mdc.get("a"));
        Assertions.assertNull(mdc.get("b"));
    }

    /**
     * 正常路径：containsKey 判断键是否存在
     * <p>
     * 对应测试用例 2.1：containsKey 判断键是否存在
     */
    @Test
    void containsKey() {
        mdc.put("a", "1");
        Assertions.assertTrue(mdc.containsKey("a"));
        Assertions.assertFalse(mdc.containsKey("not-exist"));
    }

    /**
     * 正常路径：getCopy 返回副本，修改副本不影响原 MDC
     * <p>
     * 对应测试用例 3.1：getCopy 返回副本，修改不影响原 MDC
     */
    @Test
    void getCopyIsCopy() {
        mdc.put("a", "1");
        Map<String, String> copy = mdc.getCopy();
        copy.put("b", "2");
        Assertions.assertNull(mdc.get("b"));
        Assertions.assertEquals("1", mdc.get("a"));
    }

    /**
     * 边界：空 MDC 时 getImmutableMapOrNull 返回 null
     * <p>
     * 对应测试用例 4.1：空 MDC 时 getImmutableMapOrNull 返回 null
     */
    @Test
    void getImmutableMapOrNullEmpty() {
        Assertions.assertNull(mdc.getImmutableMapOrNull());
    }

    /**
     * 正常路径：非空 MDC 时 getImmutableMapOrNull 返回不可变副本
     * <p>
     * 对应测试用例 4.2：非空时返回不可变副本
     */
    @Test
    void getImmutableMapOrNullNonEmpty() {
        mdc.put("a", "1");
        Map<String, String> map = mdc.getImmutableMapOrNull();
        Assertions.assertNotNull(map);
        Assertions.assertEquals("1", map.get("a"));
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> map.put("b", "2"));
    }

    /**
     * 边界：isEmpty 判断是否为空
     * <p>
     * 对应测试用例 5.1：isEmpty 判断是否为空
     */
    @Test
    void isEmpty() {
        Assertions.assertTrue(mdc.isEmpty());
        mdc.put("a", "1");
        Assertions.assertFalse(mdc.isEmpty());
    }

    /**
     * 异常路径：put(null, val) 抛空指针异常
     * <p>
     * 对应测试用例 6.1：put(null, val) 抛 NPE
     */
    @Test
    void putNullKeyThrows() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> mdc.put(null, "v"));
    }

    /**
     * 异常路径：put(key, null) 抛空指针异常（ConcurrentHashMap 不允许 null 值）
     * <p>
     * 对应测试用例 6.2：put(key, null) 抛 NPE
     */
    @Test
    void putNullValueThrows() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> mdc.put("k", null));
    }
}
