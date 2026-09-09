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

/**
 * <p>
 * Description: CIdUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CIdUtilsTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void UUID() {

        String uuid = CIdUtils.UUID();

        Assertions.assertNotNull(uuid);
        Assertions.assertEquals(36, uuid.length());
        Assertions.assertTrue(uuid.contains("-"));

        // UUID v7 格式特征：版本位（下标14）为 '7'，variant 位（下标19）为 8/9/a/b
        Assertions.assertEquals('7', uuid.charAt(14));
        Assertions.assertTrue("89ab".indexOf(uuid.charAt(19)) >= 0);

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void simpleUUID() {

        String uuid = CIdUtils.simpleUUID();

        Assertions.assertNotNull(uuid);
        Assertions.assertEquals(32, uuid.length());
        Assertions.assertFalse(uuid.contains("-"));

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void UUID_concurrent_unique() throws InterruptedException {

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
                            if (!uuids.add(CIdUtils.UUID())) {
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
     * 对应测试用例 2.1
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
     * 对应测试用例 3.1
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
     * 对应测试用例 4.1
     */
    @Test
    public void nextIdWithPrefix() {

        String id = CIdUtils.nextIdWithPrefix("P-");

        Assertions.assertTrue(id.startsWith("P-"));
        Assertions.assertTrue(Long.parseLong(id.substring(2)) > 0);

    }

    /**
     * 对应测试用例 4.2
     */
    @Test
    public void nextIdWithPrefixByClass() {

        String id = CIdUtils.nextIdWithPrefix(CIdUtilsTests.class);

        Assertions.assertTrue(id.startsWith("CIUT"));
        Assertions.assertTrue(Long.parseLong(id.substring(4)) > 0);

    }

    /**
     * 对应测试用例 4.3
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
