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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「普通正则缓存 / URL 通配符」两个维度组织。</li>
 *   <li>普通缓存覆盖：正例匹配、缓存实例复用（assertSame）、自定义编译函数。</li>
 *   <li>URL 通配符覆盖：{@code **}（跨层）、{@code *}（单层）、{@code .} 转义三个通配符规则分支。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对缓存复用与通配符规则的约定。</li>
 *   <li>依据测试方法（分支覆盖/边界值）：{@code **} 跨 {@code /}、{@code *} 不跨 {@code /}、{@code .} 转义。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：普通正则匹配/缓存复用/自定义编译；URL 通配符 {@code **}、{@code *}、{@code .} 转义分支。</li>
 *   <li>未覆盖：{@code **} 与占位符替换相互污染的具体回归样本（当前用例覆盖 {@code **} 后跟 {@code /} 场景，即
 *   {@code getUrlCacheDoubleStar} 的 {@code /api/**} 与 {@code /api/}）。</li>
 * </ul>
 * <h2>普通正则缓存（getCache）</h2>
 * <ul>
 *   <li>1.1 正例：{@code ^\d+$} 匹配 {@code 123}、不匹配 {@code 12a}（getCache）</li>
 *   <li>1.2 缓存复用：同一 regex 返回同一实例（assertSame）（getCacheCached）</li>
 *   <li>1.3 自定义编译：{@code getCache("custom-1", Pattern::compile)} 可匹配（getCacheWithCustom）</li>
 * </ul>
 * <h2>URL 通配符（getUrlCache）</h2>
 * <ul>
 *   <li>2.1 {@code **}：{@code /api/**} 匹配 {@code /api/a/b}、{@code /api/}，不匹配 {@code /api}（getUrlCacheDoubleStar）</li>
 *   <li>2.2 {@code *}：{@code /api/*} 匹配 {@code /api/a}，不匹配 {@code /api/a/b}（getUrlCacheSingleStar）</li>
 *   <li>2.3 {@code .} 转义：{@code /a.*} 匹配 {@code /a.b}，不匹配 {@code /axb}（getUrlCacheEscapesDot）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CPatternUtilsTests {

    /**
     * 对应测试用例 1.1：正例：{@code ^\d+$} 匹配 {@code 123}、不匹配 {@code 12a}
     */
    @Test
    public void getCache() {

        Pattern p1 = CPatternUtils.getCache("^\\d+$");
        Assertions.assertNotNull(p1);
        Assertions.assertTrue(p1.matcher("123").matches());
        Assertions.assertFalse(p1.matcher("12a").matches());

    }

    /**
     * 对应测试用例 1.2：缓存复用：同一 regex 返回同一实例（assertSame）
     */
    @Test
    public void getCacheCached() {

        Pattern p1 = CPatternUtils.getCache("a.b");
        Pattern p2 = CPatternUtils.getCache("a.b");
        Assertions.assertSame(p1, p2);

    }

    /**
     * 对应测试用例 1.3：自定义编译：{@code getCache("custom-1", Pattern::compile)} 可匹配
     */
    @Test
    public void getCacheWithCustom() {

        Pattern p = CPatternUtils.getCache("custom-1", Pattern::compile);
        Assertions.assertNotNull(p);
        Assertions.assertTrue(p.matcher("custom-1").find());

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void getUrlCacheDoubleStar() {

        Pattern p = CPatternUtils.getUrlCache("/api/**");

        Assertions.assertTrue(p.matcher("/api/a/b").matches());
        Assertions.assertTrue(p.matcher("/api/").matches());
        Assertions.assertFalse(p.matcher("/api").matches());

    }

    /**
     * 对应测试用例 2.2：{@code *}：{@code /api/*} 匹配 {@code /api/a}，不匹配 {@code /api/a/b}
     */
    @Test
    public void getUrlCacheSingleStar() {

        Pattern p = CPatternUtils.getUrlCache("/api/*");

        Assertions.assertTrue(p.matcher("/api/a").matches());
        Assertions.assertFalse(p.matcher("/api/a/b").matches());

    }

    /**
     * 对应测试用例 2.3：{@code .} 转义：{@code /a.*} 匹配 {@code /a.b}，不匹配 {@code /axb}
     */
    @Test
    public void getUrlCacheEscapesDot() {

        Pattern p = CPatternUtils.getUrlCache("/a.*");

        Assertions.assertTrue(p.matcher("/a.b").matches());
        Assertions.assertFalse(p.matcher("/axb").matches());

    }

}
