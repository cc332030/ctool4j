package com.c332030.ctool4j.log.test.mdc;

import com.c332030.ctool4j.log.mdc.CMdc;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CMdcTests
 * </p>
 *
 * <p>
 * 是 {@link CMdc} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖 put/get 基本读写、重复 put 覆盖、remove、clear。</li>
 *   <li>覆盖 containsKey、isEmpty 键判断与空判断。</li>
 *   <li>覆盖异常路径：put 的 null key/value 抛 NPE。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对「TTL 存储、基础读写、键判断、空判断」的约定。</li>
 *   <li>依据白盒/黑盒原则与错误推测法：读写覆盖、空值、null 入参、ConcurrentHashMap null 限制均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：put/get/overwrite/remove/clear、containsKey、isEmpty、null key/value 抛 NPE。</li>
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
 * <h2>键与空判断</h2>
 * <ul>
 *   <li>2.1 containsKey 判断键是否存在（containsKey）</li>
 *   <li>2.2 isEmpty 判断是否为空（isEmpty）</li>
 * </ul>
 * <h2>异常路径</h2>
 * <ul>
 *   <li>3.1 put(null, val) 抛 NPE（putNullKeyThrows）</li>
 *   <li>3.2 put(key, null) 抛 NPE（putNullValueThrows）</li>
 * </ul>
 *
 * @since 2026/8/31
 * @version 1.0
 */
class CMdcTests {

    private CMdc mdc;

    @BeforeEach
    void setUp() {
        mdc = new CMdc();
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
     * 边界：isEmpty 判断是否为空
     * <p>
     * 对应测试用例 2.2：isEmpty 判断是否为空
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
     * 对应测试用例 3.1：put(null, val) 抛 NPE
     */
    @Test
    void putNullKeyThrows() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> mdc.put(null, "v"));
    }

    /**
     * 异常路径：put(key, null) 抛空指针异常（ConcurrentHashMap 不允许 null 值）
     * <p>
     * 对应测试用例 3.2：put(key, null) 抛 NPE
     */
    @Test
    void putNullValueThrows() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> mdc.put("k", null));
    }
}
