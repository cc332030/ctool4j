package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CPatternUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

/**
 * <p>
 * Description: CPatternUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CPatternUtilsTests {

    @Test
    public void getCache() {

        Pattern p1 = CPatternUtils.getCache("^\\d+$");
        Assertions.assertNotNull(p1);
        Assertions.assertTrue(p1.matcher("123").matches());
        Assertions.assertFalse(p1.matcher("12a").matches());

    }

    @Test
    public void getCacheCached() {

        Pattern p1 = CPatternUtils.getCache("a.b");
        Pattern p2 = CPatternUtils.getCache("a.b");
        Assertions.assertSame(p1, p2);

    }

    @Test
    public void getCacheWithCustom() {

        Pattern p = CPatternUtils.getCache("custom-1", Pattern::compile);
        Assertions.assertNotNull(p);
        Assertions.assertTrue(p.matcher("custom-1").find());

    }

    @Test
    public void getUrlCacheDoubleStar() {

        Pattern p = CPatternUtils.getUrlCache("/api/**");

        Assertions.assertTrue(p.matcher("/api/a/b").matches());
        Assertions.assertTrue(p.matcher("/api/").matches());
        Assertions.assertFalse(p.matcher("/api").matches());

    }

    @Test
    public void getUrlCacheSingleStar() {

        Pattern p = CPatternUtils.getUrlCache("/api/*");

        Assertions.assertTrue(p.matcher("/api/a").matches());
        Assertions.assertFalse(p.matcher("/api/a/b").matches());

    }

    @Test
    public void getUrlCacheEscapesDot() {

        Pattern p = CPatternUtils.getUrlCache("/a.*");

        Assertions.assertTrue(p.matcher("/a.b").matches());
        Assertions.assertFalse(p.matcher("/axb").matches());

    }

}
