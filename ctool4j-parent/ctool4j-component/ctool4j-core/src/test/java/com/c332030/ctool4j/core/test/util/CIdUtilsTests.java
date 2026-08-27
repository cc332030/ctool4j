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

}
