package com.c332030.ctool4j.logback.test.mdc;

import com.c332030.ctool4j.logback.mdc.CMdcLogback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: CMdcLogbackTests
 * </p>
 *
 * <p>
 * 是 {@link CMdcLogback} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖 put/get 基本读写、重复 put 覆盖、remove、clear。</li>
 *   <li>覆盖 getCopyOfContextMap 副本隔离、空 MDC 返回空 map。</li>
 *   <li>覆盖 setContextMap 的 ConcurrentMap/普通 Map/null 三种形态。</li>
 *   <li>覆盖异常路径：put/get 的 null key/value 抛 NPE。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"TTL 存储、setContextMap 三种形态、null 清空"的约定。</li>
 *   <li>依据白盒/黑盒原则与错误推测法：读写覆盖、空值、副本隔离、null 入参、ConcurrentHashMap null 限制均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：put/get/overwrite/remove/clear、副本隔离、空 map、setContextMap 三形态、null key/value 抛 NPE。</li>
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
 * <h2>上下文副本</h2>
 * <ul>
 *   <li>2.1 getCopyOfContextMap 返回副本，修改不影响原 MDC（getCopyOfContextMapIsCopy）</li>
 *   <li>2.2 空 MDC 时返回空 map 而非 null（getCopyOfContextMapEmpty）</li>
 * </ul>
 * <h2>setContextMap</h2>
 * <ul>
 *   <li>3.1 ConcurrentMap 拷贝内容并生效（setContextMapConcurrent）</li>
 *   <li>3.2 普通 Map 拷贝内容并生效（setContextMapNormal）</li>
 *   <li>3.3 null 相当于清空（setContextMapNullClears）</li>
 * </ul>
 * <h2>异常路径</h2>
 * <ul>
 *   <li>4.1 put(null, val) 抛 NPE（putNullKeyThrows）</li>
 *   <li>4.2 put(key, null) 抛 NPE（putNullValueThrows）</li>
 *   <li>4.3 get(null) 抛 NPE（getNullKeyThrows）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/8/14
 * @version 1.0
 */
class CMdcLogbackTests {

    private CMdcLogback mdc;

    @BeforeEach
    void setUp() {
        mdc = new CMdcLogback();
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
     * 正常路径：getCopyOfContextMap 返回副本，修改副本不影响原 MDC
     * <p>
     * 对应测试用例 2.1：getCopyOfContextMap 返回副本，修改不影响原 MDC
     */
    @Test
    void getCopyOfContextMapIsCopy() {
        mdc.put("a", "1");
        Map<String, String> copy = mdc.getCopyOfContextMap();
        copy.put("b", "2");
        Assertions.assertNull(mdc.get("b"));
        Assertions.assertEquals("1", mdc.get("a"));
    }

    /**
     * 边界：空 MDC 时 getCopyOfContextMap 返回空 map 而非 null
     * <p>
     * 对应测试用例 2.2：空 MDC 时返回空 map 而非 null
     */
    @Test
    void getCopyOfContextMapEmpty() {
        Assertions.assertNotNull(mdc.getCopyOfContextMap());
        Assertions.assertTrue(mdc.getCopyOfContextMap().isEmpty());
    }

    /**
     * 正常路径：setContextMap(ConcurrentMap) 拷贝内容并生效
     * <p>
     * 对应测试用例 3.1：ConcurrentMap 拷贝内容并生效
     */
    @Test
    void setContextMapConcurrent() {
        Map<String, String> map = new ConcurrentHashMap<>();
        map.put("k", "v");
        mdc.setContextMap(map);
        Assertions.assertEquals("v", mdc.get("k"));
    }

    /**
     * 正常路径：setContextMap(普通 Map) 拷贝内容并生效
     * <p>
     * 对应测试用例 3.2：普通 Map 拷贝内容并生效
     */
    @Test
    void setContextMapNormal() {
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");
        mdc.setContextMap(map);
        Assertions.assertEquals("v", mdc.get("k"));
    }

    /**
     * 边界：setContextMap(null) 相当于清空
     * <p>
     * 对应测试用例 3.3：null 相当于清空
     */
    @Test
    void setContextMapNullClears() {
        mdc.put("k", "v");
        mdc.setContextMap(null);
        Assertions.assertNull(mdc.get("k"));
    }

    /**
     * 异常路径：put(null, val) 抛空指针异常
     * <p>
     * 对应测试用例 4.1：put(null, val) 抛 NPE
     */
    @Test
    void putNullKeyThrows() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> mdc.put(null, "v"));
    }

    /**
     * 异常路径：put(key, null) 抛空指针异常（ConcurrentHashMap 不允许 null 值）
     * <p>
     * 对应测试用例 4.2：put(key, null) 抛 NPE
     */
    @Test
    void putNullValueThrows() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> mdc.put("k", null));
    }

    /**
     * 异常路径：get(null) 抛空指针异常
     * <p>
     * 对应测试用例 4.3：get(null) 抛 NPE
     */
    @Test
    void getNullKeyThrows() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> mdc.get(null));
    }
}
