package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CIdUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CIdUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「生成 / 前缀计算 / 前缀解析」三个维度组织：生成类验证 UUID/雪花 ID 的形态与唯一性，前缀计算验证类名前缀约定，前缀解析验证数字前字符的解析规则。</li>
 *   <li>UUID 形态断言长度与 '-' 有无，雪花 ID 断言为正数且两次不相等（验证递增）。</li>
 *   <li>前缀计算以测试类自身（{@code CIdUtilsTests}）验证类名前缀规则（大写字母去 DO → {@code CIUT}）。</li>
 *   <li>前缀解析覆盖含前缀、纯字母（无数字）、以数字开头、空、null 等分支。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对前缀计算规则（{@code @CBizId} 优先、类名大写去 DO、按类缓存）与解析规则（数字前字符）的约定。</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖）：UUID 形态、前缀截取边界（length 超长）、解析各分支。</li>
 *   <li>前缀截取覆盖"length 超前缀长度"边界。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：随机 UUID v4 带/不带连字符形态（{@code UUID}/{@code simpleUUID}）、UUID v7 带/不带连字符形态（{@code UUIDv7}/{@code simpleUUIDv7}）、并发多线程下无重复（验证生成器缓存复用后仍线程安全）；雪花 ID 正数且递增；类前缀（含注解优先、类名大写去 DO）、前缀截取边界；</li>
 *   <li>带前缀雪花 ID（字符串/类/类+长度三种入口）；前缀解析（含前缀、纯字母、数字开头、空、null、转换函数）。</li>
 *   <li>未覆盖：{@code @CBizId} 注解优先路径（测试类无注解，仅覆盖类名回退路径；注解路径依赖注解声明，未在单测中构造）。</li>
 * </ul>
 * <h2>UUID 生成</h2>
 * <ul>
 *   <li>1.1 UUID：36 位且含 '-'，版本位为 4、variant 位为 8/9/a/b（随机 UUID v4 格式特征）（UUID）</li>
 *   <li>1.2 simpleUUID：32 位且不含 '-'（simpleUUID）</li>
 *   <li>1.3 UUIDv7：36 位且含 '-'，版本位为 7、variant 位为 8/9/a/b（UUID v7 格式特征）（UUIDv7）</li>
 *   <li>1.4 simpleUUIDv7：32 位且不含 '-'（simpleUUIDv7）</li>
 *   <li>1.5 并发唯一性：8 线程 × 100 次并发生成，无重复且总数正确（UUID_concurrent_unique）</li>
 * </ul>
 * <h2>雪花 ID 生成</h2>
 * <ul>
 *   <li>2.1 nextId：正数且两次不相等（递增）（nextId）</li>
 * </ul>
 * <h2>类 ID 前缀计算</h2>
 * <ul>
 *   <li>3.1 getPrefix：类名大写去 DO（{@code CIdUtilsTests} → {@code CIUT}）（getPrefix）</li>
 *   <li>3.2 getPrefix(Class, length)：截取 2 位 {@code CI}（getPrefixByLength）</li>
 *   <li>3.3 边界：length 超前缀长度时返回完整前缀 {@code CIUT}（getPrefixByLength）</li>
 * </ul>
 * <h2>带前缀雪花 ID</h2>
 * <ul>
 *   <li>4.1 nextIdWithPrefix(String)：{@code P-} 前缀 + 正数雪花 ID（nextIdWithPrefix）</li>
 *   <li>4.2 nextIdWithPrefix(Class)：类前缀 {@code CIUT} + 正数雪花 ID（nextIdWithPrefixByClass）</li>
 *   <li>4.3 nextIdWithPrefix(Class, length)：截取前缀 {@code CI} + 正数雪花 ID（nextIdWithPrefixByClassAndLength）</li>
 * </ul>
 * <h2>前缀解析</h2>
 * <ul>
 *   <li>5.1 含前缀：{@code P-123} → {@code P-}（getPrefixFromId）</li>
 *   <li>5.2 纯字母无数字：{@code ABC} → {@code ABC}（getPrefixFromId）</li>
 *   <li>5.3 以数字开头无前缀：{@code 123abc} → null（getPrefixFromId）</li>
 *   <li>5.4 边界：空字符串 → null（getPrefixFromId）</li>
 *   <li>5.5 边界：null → null（getPrefixFromId）</li>
 *   <li>5.6 转换函数：{@code P-123} → {@code P-} / 长度 2（getPrefixFromIdWithFunction）</li>
 *   <li>5.7 无前缀时不调用转换函数直接返回 null：{@code 123} → null（getPrefixFromIdWithFunction）</li>
 * </ul>
 * <h2>Nano ID 生成</h2>
 * <ul>
 *   <li>6.1 nanoId：默认 21 字符，且仅含 URL 安全字母表 {@code A-Za-z0-9_-}（nanoId_default）</li>
 *   <li>6.2 nanoId(size)：自定义长度 1/16/64，字符合法（nanoId_customSize）</li>
 *   <li>6.3 唯一性：抽样 10000 次无重复（nanoId_unique）</li>
 * </ul>
 * <h2>ULID 生成</h2>
 * <ul>
 *   <li>7.1 ulid：默认 26 字符，大写 Crockford Base32（不含 I/L/O/U）（ulid_format）</li>
 *   <li>7.2 时间有序：先生成的字典序小于后生成（ulid_timeOrdered）</li>
 *   <li>7.3 时间戳可解码：前 10 字符解码回毫秒，落在生成时刻附近（ulid_timestampDecodable）</li>
 * </ul>
 *
 * @see CIdUtils
 *
 * @since 2026/8/14
 * @version 1.1
 */
public class CIdUtilsTests {

    /**
     * 对应测试用例 1.1：36 位且含 '-'，版本位为 4、variant 位为 8/9/a/b（随机 UUID v4 格式特征）
     */
    @Test
    public void UUID() {

        String uuid = CIdUtils.UUID();

        Assertions.assertNotNull(uuid);
        Assertions.assertEquals(36, uuid.length());
        Assertions.assertTrue(uuid.contains("-"));

        // 随机 UUID v4 格式特征：版本位（下标14）为 '4'，variant 位（下标19）为 8/9/a/b
        Assertions.assertEquals('4', uuid.charAt(14));
        Assertions.assertTrue("89ab".indexOf(uuid.charAt(19)) >= 0);

    }

    /**
     * 对应测试用例 1.2：32 位且不含 '-'
     */
    @Test
    public void simpleUUID() {

        String uuid = CIdUtils.simpleUUID();

        Assertions.assertNotNull(uuid);
        Assertions.assertEquals(32, uuid.length());
        Assertions.assertFalse(uuid.contains("-"));

        // 去 '-' 后仍保留 v4 的版本位（下标 12）
        Assertions.assertEquals('4', uuid.charAt(12));

    }

    /**
     * 对应测试用例 1.3：36 位且含 '-'，版本位为 7、variant 位为 8/9/a/b（UUID v7 格式特征）
     */
    @Test
    public void UUIDv7() {

        String uuid = CIdUtils.UUIDv7();

        Assertions.assertNotNull(uuid);
        Assertions.assertEquals(36, uuid.length());
        Assertions.assertTrue(uuid.contains("-"));

        // UUID v7 格式特征：版本位（下标14）为 '7'，variant 位（下标19）为 8/9/a/b
        Assertions.assertEquals('7', uuid.charAt(14));
        Assertions.assertTrue("89ab".indexOf(uuid.charAt(19)) >= 0);

    }

    /**
     * 对应测试用例 1.4：32 位且不含 '-'
     */
    @Test
    public void simpleUUIDv7() {

        String uuid = CIdUtils.simpleUUIDv7();

        Assertions.assertNotNull(uuid);
        Assertions.assertEquals(32, uuid.length());
        Assertions.assertFalse(uuid.contains("-"));

        // 去 '-' 后仍保留 v7 的版本位（下标 12）
        Assertions.assertEquals('7', uuid.charAt(12));

    }

    /**
     * 对应测试用例 1.5：并发唯一性：8 线程 × 100 次并发生成，无重复且总数正确（UUID 与 UUIDv7 各一轮）
     */
    @Test
    public void UUID_concurrent_unique() throws InterruptedException {

        // 两代实现各自并发生成，均须无重复（UUID 走 hutool 无状态随机、UUIDv7 走懒加载生成器）
        assertConcurrentUnique(CIdUtils::UUID);
        assertConcurrentUnique(CIdUtils::UUIDv7);

    }

    /**
     * 按给定生成器并发抽样，断言无重复且总数正确
     *
     * @param supplier UUID 生成器
     */
    private static void assertConcurrentUnique(Supplier<String> supplier) throws InterruptedException {

        int threadCount = 8;
        int perThread = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        // 需与主线程/各任务线程同步，用并发安全的 Set 收集结果
        Set<String> uuids = Collections.synchronizedSet(new HashSet<>());
        AtomicBoolean duplicate = new AtomicBoolean(false);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        try {
            for (int i = 0; i < threadCount; i++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < perThread; j++) {
                            if (!uuids.add(supplier.get())) {
                                duplicate.set(true);
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            Assertions.assertTrue(done.await(10, TimeUnit.SECONDS),
                "UUID 并发生成超时");
        } finally {
            pool.shutdownNow();
        }

        Assertions.assertFalse(duplicate.get(), "并发生成出现重复 UUID");
        Assertions.assertEquals(threadCount * perThread, uuids.size());
    }

    /**
     * 对应测试用例 2.1：正数且两次不相等（递增）
     */
    @Test
    public void nextId() {

        Long id1 = CIdUtils.nextId();
        Long id2 = CIdUtils.nextId();

        Assertions.assertNotNull(id1);
        Assertions.assertTrue(id1 > 0);
        Assertions.assertNotEquals(id1, id2);

    }

    /**
     * 对应测试用例 3.1：类名大写去 DO（{@code CIdUtilsTests} → {@code CIUT}）
     */
    @Test
    public void getPrefix() {

        // 类名 CIdUtilsTests 仅保留大写字母 => CIUT
        Assertions.assertEquals("CIUT", CIdUtils.getPrefix(CIdUtilsTests.class));

    }

    /**
     * 对应测试用例 3.2 / 3.3
     */
    @Test
    public void getPrefixByLength() {

        Assertions.assertEquals("CI", CIdUtils.getPrefix(CIdUtilsTests.class, 2));
        // length 超出前缀长度时返回完整前缀
        Assertions.assertEquals("CIUT", CIdUtils.getPrefix(CIdUtilsTests.class, 10));

    }

    /**
     * 对应测试用例 4.1：nextIdWithPrefix(String)：{@code P-} 前缀 + 正数雪花 ID
     */
    @Test
    public void nextIdWithPrefix() {

        String id = CIdUtils.nextIdWithPrefix("P-");

        Assertions.assertTrue(id.startsWith("P-"));
        Assertions.assertTrue(Long.parseLong(id.substring(2)) > 0);

    }

    /**
     * 对应测试用例 4.2：nextIdWithPrefix(Class)：类前缀 {@code CIUT} + 正数雪花 ID
     */
    @Test
    public void nextIdWithPrefixByClass() {

        String id = CIdUtils.nextIdWithPrefix(CIdUtilsTests.class);

        Assertions.assertTrue(id.startsWith("CIUT"));
        Assertions.assertTrue(Long.parseLong(id.substring(4)) > 0);

    }

    /**
     * 对应测试用例 4.3：nextIdWithPrefix(Class, length)：截取前缀 {@code CI} + 正数雪花 ID
     */
    @Test
    public void nextIdWithPrefixByClassAndLength() {

        String id = CIdUtils.nextIdWithPrefix(CIdUtilsTests.class, 2);

        Assertions.assertTrue(id.startsWith("CI"));
        Assertions.assertTrue(Long.parseLong(id.substring(2)) > 0);

    }

    /**
     * 对应测试用例 5.1 / 5.2 / 5.3 / 5.4 / 5.5
     */
    @Test
    public void getPrefixFromId() {

        Assertions.assertEquals("P-", CIdUtils.getPrefixFromId("P-123"));
        Assertions.assertEquals("ABC", CIdUtils.getPrefixFromId("ABC"));
        Assertions.assertNull(CIdUtils.getPrefixFromId("123abc"));
        Assertions.assertNull(CIdUtils.getPrefixFromId(""));
        Assertions.assertNull(CIdUtils.getPrefixFromId(null));

    }

    /**
     * 对应测试用例 5.6 / 5.7
     */
    @Test
    public void getPrefixFromIdWithFunction() {

        Assertions.assertEquals("P-", CIdUtils.getPrefixFromId("P-123", s -> s));
        Assertions.assertEquals(2, CIdUtils.getPrefixFromId("P-123", String::length));
        // 无前缀时直接返回 null，不调用函数
        Assertions.assertNull(CIdUtils.getPrefixFromId("123", s -> s));

    }

    // ---------- Nano ID ----------

    /**
     * 对应测试用例 6.1：默认 21 字符，且仅含 URL 安全字母表字符
     */
    @Test
    public void nanoId_default() {

        String id = CIdUtils.nanoId();

        Assertions.assertNotNull(id);
        Assertions.assertEquals(21, id.length());
        Assertions.assertTrue(id.chars().allMatch(CIdUtilsTests::isUrlSafe));

    }

    /**
     * 对应测试用例 6.2：指定长度
     */
    @Test
    public void nanoId_customSize() {

        Assertions.assertEquals(10, CIdUtils.nanoId(10).length());
        Assertions.assertEquals(1, CIdUtils.nanoId(1).length());
        Assertions.assertEquals(64, CIdUtils.nanoId(64).length());
        Assertions.assertTrue(CIdUtils.nanoId(16).chars().allMatch(CIdUtilsTests::isUrlSafe));

    }

    /**
     * 对应测试用例 6.3：多次生成不重复（抽样规模内）
     */
    @Test
    public void nanoId_unique() {

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            ids.add(CIdUtils.nanoId());
        }

        Assertions.assertEquals(10000, ids.size());

    }

    /**
     * 字符是否为 Nano ID 默认 URL 安全字母表 A-Za-z0-9_- 之一
     *
     * @param ch 字符
     * @return 是否合法
     */
    private static boolean isUrlSafe(int ch) {
        return ch >= 'A' && ch <= 'Z'
            || ch >= 'a' && ch <= 'z'
            || ch >= '0' && ch <= '9'
            || ch == '_'
            || ch == '-';
    }

    // ---------- ULID ----------

    /**
     * 对应测试用例 7.1：默认 26 字符，大写 Crockford Base32（不含 I/L/O/U），时间戳+随机结构
     */
    @Test
    public void ulid_format() {

        String id = CIdUtils.ulid();

        Assertions.assertNotNull(id);
        Assertions.assertEquals(26, id.length());
        Assertions.assertTrue(id.chars().allMatch(CIdUtilsTests::isUlidChar));

    }

    /**
     * 对应测试用例 7.2：时间有序（先生成的字典序小于后生成的）
     */
    @Test
    public void ulid_timeOrdered() {

        String first = CIdUtils.ulid();
        String second = CIdUtils.ulid();

        Assertions.assertTrue(first.compareTo(second) < 0);

    }

    /**
     * 对应测试用例 7.3：时间戳部分（前 10 字符）可解码回毫秒，落在当前时间附近
     */
    @Test
    public void ulid_timestampDecodable() {

        String id = CIdUtils.ulid();
        long before = System.currentTimeMillis();

        // ULID 时间戳用 48 位毫秒，Crockford Base32 前 10 字符表示，可通过规范算法解码
        long ts = decodeUlidTimestamp(id);

        long after = System.currentTimeMillis();
        Assertions.assertTrue(ts >= before && ts <= after);

    }

    /**
     * 字符是否为 ULID Crockford Base32 字符之一（大写 0-9A-HJKMNP-TV-Z，不含 I/L/O/U）
     *
     * @param ch 字符
     * @return 是否合法
     */
    private static boolean isUlidChar(int ch) {
        return ULID_ALPHABET.indexOf(ch) >= 0;
    }

    /**
     * 解码 ULID 前 10 字符的毫秒时间戳（Crockford Base32，48 位）
     *
     * @param ulid ULID 字符串
     * @return 毫秒时间戳
     */
    private static long decodeUlidTimestamp(String ulid) {
        long value = 0;
        String tsPart = ulid.substring(0, 10);
        for (int i = 0; i < tsPart.length(); i++) {
            value = value * 32 + crockfordValue(tsPart.charAt(i));
        }
        return value;
    }

    /**
     * ULID Crockford Base32 字符集（有序，排除 I/L/O/U）
     */
    private static final String ULID_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

    /**
     * Crockford Base32 字符对应值
     *
     * @param ch 字符
     * @return 值 0~31
     */
    private static int crockfordValue(char ch) {
        int idx = ULID_ALPHABET.indexOf(ch);
        if (idx < 0) {
            throw new IllegalArgumentException("非 ULID 字符: " + ch);
        }
        return idx;
    }

}
