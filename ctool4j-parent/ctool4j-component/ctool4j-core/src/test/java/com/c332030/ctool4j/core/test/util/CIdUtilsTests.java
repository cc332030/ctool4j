package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CIdUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CIdUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CIdUtilsTests {

    @Test
    public void stringUUID() {

        String uuid = CIdUtils.stringUUID();

        Assertions.assertNotNull(uuid);
        Assertions.assertEquals(36, uuid.length());
        Assertions.assertTrue(uuid.contains("-"));

    }

    @Test
    public void stringUUIDNoHyphen() {

        String uuid = CIdUtils.stringUUIDNoHyphen();

        Assertions.assertNotNull(uuid);
        Assertions.assertEquals(32, uuid.length());
        Assertions.assertFalse(uuid.contains("-"));

    }

    @Test
    public void nextId() {

        Long id1 = CIdUtils.nextId();
        Long id2 = CIdUtils.nextId();

        Assertions.assertNotNull(id1);
        Assertions.assertTrue(id1 > 0);
        Assertions.assertNotEquals(id1, id2);

    }

    @Test
    public void getPrefix() {

        // 类名 CIdUtilsTests 仅保留大写字母 => CIUT
        Assertions.assertEquals("CIUT", CIdUtils.getPrefix(CIdUtilsTests.class));

    }

    @Test
    public void getPrefixByLength() {

        Assertions.assertEquals("CI", CIdUtils.getPrefix(CIdUtilsTests.class, 2));
        // length 超出前缀长度时返回完整前缀
        Assertions.assertEquals("CIUT", CIdUtils.getPrefix(CIdUtilsTests.class, 10));

    }

    @Test
    public void nextIdWithPrefix() {

        String id = CIdUtils.nextIdWithPrefix("P-");

        Assertions.assertTrue(id.startsWith("P-"));
        Assertions.assertTrue(Long.parseLong(id.substring(2)) > 0);

    }

    @Test
    public void nextIdWithPrefixByClass() {

        String id = CIdUtils.nextIdWithPrefix(CIdUtilsTests.class);

        Assertions.assertTrue(id.startsWith("CIUT"));
        Assertions.assertTrue(Long.parseLong(id.substring(4)) > 0);

    }

    @Test
    public void nextIdWithPrefixByClassAndLength() {

        String id = CIdUtils.nextIdWithPrefix(CIdUtilsTests.class, 2);

        Assertions.assertTrue(id.startsWith("CI"));
        Assertions.assertTrue(Long.parseLong(id.substring(2)) > 0);

    }

    @Test
    public void getPrefixFromId() {

        Assertions.assertEquals("P-", CIdUtils.getPrefixFromId("P-123"));
        Assertions.assertEquals("ABC", CIdUtils.getPrefixFromId("ABC"));
        Assertions.assertNull(CIdUtils.getPrefixFromId("123abc"));
        Assertions.assertNull(CIdUtils.getPrefixFromId(""));
        Assertions.assertNull(CIdUtils.getPrefixFromId(null));

    }

    @Test
    public void getPrefixFromIdWithFunction() {

        Assertions.assertEquals("P-", CIdUtils.getPrefixFromId("P-123", s -> s));
        Assertions.assertEquals(2, CIdUtils.getPrefixFromId("P-123", String::length));
        // 无前缀时直接返回 null，不调用函数
        Assertions.assertNull(CIdUtils.getPrefixFromId("123", s -> s));

    }

}
