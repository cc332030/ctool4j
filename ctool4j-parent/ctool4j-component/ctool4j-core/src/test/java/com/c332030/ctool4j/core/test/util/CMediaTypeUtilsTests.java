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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「文本判断 / JSON5 追加」两个维度组织；文本判断是重点，按匹配规则拆分为多组用例。</li>
 *   <li>文本判断分别覆盖：关键字集合内容、各匹配分支正例（完整/{@code key/}开头/{@code /key}/{@code +key}/{@code -key}）、</li>
 *   <li>非文本反例（真实非文本类型 + 笔误 + 捏造非法值）、子串不误匹配（Q39 回归）、大小写不敏感、</li>
 *   <li>带参数、空白输入。</li>
 *   <li>JSON5 追加覆盖 set 与 list 两个入口，list 验证 JSON5 位于末尾。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 isText 段匹配规则（完整/{@code /}/{@code +}/{@code -} 分隔）与空白返回 false 的约定。</li>
 *   <li>依据测试方法（白盒分支覆盖/黑盒等价类/错误推测/边界值）：各匹配分支、任意非法输入、子串误匹配</li>
 *   <li>边界、大小写变体、空白边界。</li>
 *   <li>Q39 修复核心（子串不误匹配）作为回归重点单列验证。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：TEXT_KEYS 内容；isText 全部匹配分支正例、非文本反例（含笔误/捏造非法值）、子串不误匹配、</li>
 *   <li>大小写、带 charset 参数、空白；JSON5 追加 set/list、list 顺序。</li>
 *   <li>未覆盖：非 {@code TEXT_KEYS} 关键字但语义为文本的类型（关键字集合外的文本类型不识别，属设计边界）。</li>
 * </ul>
 * <h2>文本判断（isText）</h2>
 * <ul>
 *   <li>1.1 关键字集合内容：含 text/plain/html/json/xml/form（textKeys）</li>
 *   <li>1.2 匹配分支正例：完整匹配（text/plain/html/json/xml/form）（isTextPositive）</li>
 *   <li>1.3 匹配分支正例：{@code key/} 开头（text/html、text/event-stream、json/any）（isTextPositive）</li>
 *   <li>1.4 匹配分支正例：含 {@code /key}（application/json、application/xml）（isTextPositive）</li>
 *   <li>1.5 匹配分支正例：含 {@code +key} 结构化后缀（application/activity+json、application/atom+xml、application/xhtml+xml）（isTextPositive）</li>
 *   <li>1.6 匹配分支正例：含 {@code -key}（application/x-www-form-urlencoded）（isTextPositive）</li>
 *   <li>1.7 反例：真实非文本类型（image/png、application/octet-stream、video/mp4、audio/mpeg、font/woff2、application/pdf）（isTextNegative）</li>
 *   <li>1.8 反例：笔误值（vldeo、vldeo/mp4、txet、jsno）（isTextNegative）</li>
 *   <li>1.9 反例：随意捏造非法值（txso、txso/anything、abc、application/xyz、foo+bar）（isTextNegative）</li>
 *   <li>1.10 边界：子串不误匹配（uniform/information/transform 含 form、xhtml 含 html、plaintext 含 plain、json-schema）（isTextNoSubstringFalseMatch，Q39 回归）</li>
 *   <li>1.11 分支：大小写不敏感（TEXT/HTML、Application/JSON、APPLICATION/XML 等）（isTextCaseInsensitive）</li>
 *   <li>1.12 分支：带 charset 等参数仍可识别（text/html; charset=utf-8 等）（isTextWithParameter）</li>
 *   <li>1.13 边界：空白/null 返回 false（isTextBlank）</li>
 * </ul>
 * <h2>JSON5 追加</h2>
 * <ul>
 *   <li>2.1 getSetWithJson5：保留原元素并追加 JSON5（getSetWithJson5）</li>
 *   <li>2.2 getListWithJson5：返回列表且 JSON5 位于末尾（getListWithJson5）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CMediaTypeUtilsTests {

    /**
     * 对应测试用例 1.1：关键字集合内容：含 text/plain/html/json/xml/form
     */
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
     * 对应测试用例 1.2 / 1.3 / 1.4 / 1.5 / 1.6
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
     * 判断文本类型：非文本类型反例，异常输入不限定范围
     * <p>覆盖笔误值（vldeo）、随意捏造且不在关键字范围内的值（txso）等任意非法输入</p>
     * 对应测试用例 1.7 / 1.8 / 1.9
     */
    @Test
    public void isTextNegative() {

        // 真实存在的非文本类型
        Assertions.assertFalse(CMediaTypeUtils.isText("image/png"));
        Assertions.assertFalse(CMediaTypeUtils.isText("application/octet-stream"));
        Assertions.assertFalse(CMediaTypeUtils.isText("video/mp4"));
        Assertions.assertFalse(CMediaTypeUtils.isText("audio/mpeg"));
        Assertions.assertFalse(CMediaTypeUtils.isText("font/woff2"));
        Assertions.assertFalse(CMediaTypeUtils.isText("application/pdf"));

        // 笔误值
        Assertions.assertFalse(CMediaTypeUtils.isText("vldeo"));
        Assertions.assertFalse(CMediaTypeUtils.isText("vldeo/mp4"));
        Assertions.assertFalse(CMediaTypeUtils.isText("txet"));
        Assertions.assertFalse(CMediaTypeUtils.isText("jsno"));

        // 随意捏造、不在关键字范围内的值
        Assertions.assertFalse(CMediaTypeUtils.isText("txso"));
        Assertions.assertFalse(CMediaTypeUtils.isText("txso/anything"));
        Assertions.assertFalse(CMediaTypeUtils.isText("abc"));
        Assertions.assertFalse(CMediaTypeUtils.isText("application/xyz"));
        Assertions.assertFalse(CMediaTypeUtils.isText("foo+bar"));

    }

    /**
     * 判断文本类型：子串不误匹配（Q39 修复核心回归）
     * <p>关键字出现在子串中但无 /、+、- 分隔时，不应误判为文本类型</p>
     * 对应测试用例 1.10：边界：子串不误匹配（uniform/information/transform 含 form、xhtml 含 html、plaintext 含 plain、json-schema）（isTextNoSubstringFalseMatch，Q39 回归）
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
     * 对应测试用例 1.11：分支：大小写不敏感（TEXT/HTML、Application/JSON、APPLICATION/XML 等）
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
     * 对应测试用例 1.12：分支：带 charset 等参数仍可识别（text/html; charset=utf-8 等）
     */
    @Test
    public void isTextWithParameter() {

        Assertions.assertTrue(CMediaTypeUtils.isText("text/html; charset=utf-8"));
        Assertions.assertTrue(CMediaTypeUtils.isText("application/json;charset=UTF-8"));
        Assertions.assertTrue(CMediaTypeUtils.isText("text/plain; boundary=xxx"));

    }

    /**
     * 判断文本类型：空白输入返回 false
     * 对应测试用例 1.13：边界：空白/null 返回 false
     */
    @Test
    public void isTextBlank() {

        Assertions.assertFalse(CMediaTypeUtils.isText(null));
        Assertions.assertFalse(CMediaTypeUtils.isText(""));
        Assertions.assertFalse(CMediaTypeUtils.isText("   "));
        Assertions.assertFalse(CMediaTypeUtils.isText("\t\n"));

    }

    /**
     * 对应测试用例 2.1：保留原元素并追加 JSON5
     */
    @Test
    public void getSetWithJson5() {

        Set<MediaType> set = CMediaTypeUtils.getSetWithJson5(
                Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));

        Assertions.assertTrue(set.contains(MediaType.APPLICATION_JSON));
        Assertions.assertTrue(set.contains(MediaType.TEXT_PLAIN));
        Assertions.assertTrue(set.contains(CMimeTypeEnum.JSON5.getMimeType()));

    }

    /**
     * 对应测试用例 2.2：返回列表且 JSON5 位于末尾
     */
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
