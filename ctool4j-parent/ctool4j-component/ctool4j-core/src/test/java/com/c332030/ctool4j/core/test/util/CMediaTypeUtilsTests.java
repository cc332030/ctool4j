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

    /**
     * 判断文本类型：各匹配分支正例
     * <p>匹配规则：完整匹配 / 以 key+"/" 开头 / 含 "/"+key / 含 "+"+key / 含 "-"+key</p>
     */
    @Test
    public void isTextPositive() {

        // 完整匹配
        Assertions.assertTrue(CMediaTypeUtils.isText("text"));
        Assertions.assertTrue(CMediaTypeUtils.isText("plain"));
        Assertions.assertTrue(CMediaTypeUtils.isText("html"));
        Assertions.assertTrue(CMediaTypeUtils.isText("json"));
        Assertions.assertTrue(CMediaTypeUtils.isText("xml"));
        Assertions.assertTrue(CMediaTypeUtils.isText("form"));

        // 以 key/ 开头
        Assertions.assertTrue(CMediaTypeUtils.isText("text/html"));
        Assertions.assertTrue(CMediaTypeUtils.isText("text/plain"));
        Assertions.assertTrue(CMediaTypeUtils.isText("text/event-stream"));
        Assertions.assertTrue(CMediaTypeUtils.isText("json/any"));

        // 含 /key
        Assertions.assertTrue(CMediaTypeUtils.isText("application/json"));
        Assertions.assertTrue(CMediaTypeUtils.isText("application/xml"));

        // 含 +key（结构化后缀语法）
        Assertions.assertTrue(CMediaTypeUtils.isText("application/activity+json"));
        Assertions.assertTrue(CMediaTypeUtils.isText("application/atom+xml"));
        Assertions.assertTrue(CMediaTypeUtils.isText("application/xhtml+xml"));

        // 含 -key
        Assertions.assertTrue(CMediaTypeUtils.isText("application/x-www-form-urlencoded"));

    }

    /**
     * 判断文本类型：非文本类型反例
     */
    @Test
    public void isTextNegative() {

        Assertions.assertFalse(CMediaTypeUtils.isText("image/png"));
        Assertions.assertFalse(CMediaTypeUtils.isText("application/octet-stream"));
        Assertions.assertFalse(CMediaTypeUtils.isText("video/mp4"));
        Assertions.assertFalse(CMediaTypeUtils.isText("audio/mpeg"));
        Assertions.assertFalse(CMediaTypeUtils.isText("font/woff2"));
        Assertions.assertFalse(CMediaTypeUtils.isText("application/pdf"));

    }

    /**
     * 判断文本类型：子串不误匹配（Q39 修复核心回归）
     * <p>关键字出现在子串中但无 /、+、- 分隔时，不应误判为文本类型</p>
     */
    @Test
    public void isTextNoSubstringFalseMatch() {

        Assertions.assertFalse(CMediaTypeUtils.isText("uniform"));      // 含 form
        Assertions.assertFalse(CMediaTypeUtils.isText("information"));  // 含 form
        Assertions.assertFalse(CMediaTypeUtils.isText("transform"));    // 含 form
        Assertions.assertFalse(CMediaTypeUtils.isText("xhtml"));        // 含 html
        Assertions.assertFalse(CMediaTypeUtils.isText("plaintext"));    // 含 plain
        Assertions.assertFalse(CMediaTypeUtils.isText("json-schema"));  // json 开头但无分隔

    }

    /**
     * 判断文本类型：大小写不敏感
     */
    @Test
    public void isTextCaseInsensitive() {

        Assertions.assertTrue(CMediaTypeUtils.isText("TEXT/HTML"));
        Assertions.assertTrue(CMediaTypeUtils.isText("Application/JSON"));
        Assertions.assertTrue(CMediaTypeUtils.isText("APPLICATION/XML"));
        Assertions.assertTrue(CMediaTypeUtils.isText("Application/X-WWW-Form-Urlencoded"));

    }

    /**
     * 判断文本类型：带 charset 等参数仍可识别
     */
    @Test
    public void isTextWithParameter() {

        Assertions.assertTrue(CMediaTypeUtils.isText("text/html; charset=utf-8"));
        Assertions.assertTrue(CMediaTypeUtils.isText("application/json;charset=UTF-8"));
        Assertions.assertTrue(CMediaTypeUtils.isText("text/plain; boundary=xxx"));

    }

    /**
     * 判断文本类型：空白输入返回 false
     */
    @Test
    public void isTextBlank() {

        Assertions.assertFalse(CMediaTypeUtils.isText(null));
        Assertions.assertFalse(CMediaTypeUtils.isText(""));
        Assertions.assertFalse(CMediaTypeUtils.isText("   "));
        Assertions.assertFalse(CMediaTypeUtils.isText("\t\n"));

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
