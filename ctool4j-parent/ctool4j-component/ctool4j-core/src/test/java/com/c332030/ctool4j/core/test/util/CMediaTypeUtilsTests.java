package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CMediaTypeUtils;
import com.c332030.ctool4j.definition.enums.CMimeTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Description: CMediaTypeUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CMediaTypeUtilsTests {

    @Test
    public void textKeys() {

        Set<String> keys = CMediaTypeUtils.TEXT_KEYS;
        Assertions.assertTrue(keys.contains("text"));
        Assertions.assertTrue(keys.contains("plain"));
        Assertions.assertTrue(keys.contains("html"));
        Assertions.assertTrue(keys.contains("json"));
        Assertions.assertTrue(keys.contains("xml"));
        Assertions.assertTrue(keys.contains("form"));

    }

    @Test
    public void isText() {

        Assertions.assertTrue(CMediaTypeUtils.isText("text/html"));
        Assertions.assertTrue(CMediaTypeUtils.isText("application/json"));
        Assertions.assertTrue(CMediaTypeUtils.isText("application/xml"));
        Assertions.assertTrue(CMediaTypeUtils.isText("application/x-www-form-urlencoded"));
        Assertions.assertFalse(CMediaTypeUtils.isText("image/png"));
        Assertions.assertFalse(CMediaTypeUtils.isText("application/octet-stream"));

    }

    @Test
    public void getSetWithJson5() {

        Set<MediaType> set = CMediaTypeUtils.getSetWithJson5(
                Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));

        Assertions.assertTrue(set.contains(MediaType.APPLICATION_JSON));
        Assertions.assertTrue(set.contains(MediaType.TEXT_PLAIN));
        Assertions.assertTrue(set.contains(CMimeTypeEnum.JSON5.getMimeType()));

    }

    @Test
    public void getListWithJson5() {

        List<MediaType> list = CMediaTypeUtils.getListWithJson5(
                Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));

        Assertions.assertTrue(list.contains(MediaType.APPLICATION_JSON));
        Assertions.assertTrue(list.contains(CMimeTypeEnum.JSON5.getMimeType()));
        // JSON5 在末尾
        Assertions.assertEquals(CMimeTypeEnum.JSON5.getMimeType(), list.get(list.size() - 1));

    }

}
