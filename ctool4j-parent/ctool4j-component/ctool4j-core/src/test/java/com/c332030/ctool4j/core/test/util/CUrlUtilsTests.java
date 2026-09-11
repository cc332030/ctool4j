package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CUrlUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CUrlUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「路径 / 协议提取 / 域名替换 / 参数解析」多个维度组织。</li>
 *   <li>用固定测试域名 {@code https://c332030.com} 验证各方法。</li>
 *   <li>协议提取覆盖协议开头、协议前有垃圾、不含协议、空值。</li>
 *   <li>域名替换覆盖带路径/查询/fragment、无路径、空 URL、空新域名。</li>
 *   <li>参数解析覆盖基本参数、值含 {@code =}、fragment 不混入、编码解码、空/无参数。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对协议提取、参数解析（fragment 剥离、第一个 {@code =}、解码）、域名替换语义的约定。</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖）：协议有无、垃圾前缀、空值、fragment、编码值、无路径。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getPath；getUrl（协议开头/垃圾前缀/无协议/空值）；replaceDomain（带路径/查询/fragment、</li>
 *   <li>无路径、空 URL、空新域名）；getParamMap（基本/值含等号/fragment 隔离/解码/空与无参数）。</li>
 *   <li>未覆盖：{@code getURI}/{@code getScheme}/{@code getHost}/{@code getPort}/{@code splitToPath}/{@code firstPath}/{@code lastPath} 等其余入口</li>
 *   <li>（当前测试聚焦核心路径，其余入口行为可后续批次补充）。</li>
 * </ul>
 * <h2>路径</h2>
 * <ul>
 *   <li>1.1 getPath：{@code https://c332030.com/ip} → {@code /ip}（getPath）</li>
 * </ul>
 * <h2>协议提取（getUrl）</h2>
 * <ul>
 *   <li>2.1 协议开头：原样返回（getUrl）</li>
 *   <li>2.2 协议前有垃圾：从协议处截取（getUrlFromProtocolStart）</li>
 *   <li>2.3 不含协议：返回 null（getUrlNoProtocol）</li>
 *   <li>2.4 空/null/空白：返回 null（getUrlEmpty）</li>
 * </ul>
 * <h2>域名替换（replaceDomain）</h2>
 * <ul>
 *   <li>3.1 基本替换：{@code /ip} 路径域名替换（replaceDomain）</li>
 *   <li>3.2 保留查询与 fragment：{@code ?x=1&amp;y=2#frag} 保留（replaceDomainKeepQueryAndFragment）</li>
 *   <li>3.3 无路径：仅返回新域名（replaceDomainNoPath）</li>
 *   <li>3.4 空 URL：返回 null（replaceDomainEmptyUrl）</li>
 *   <li>3.5 空新域名：原样返回原 URL（replaceDomainEmptyNewDomain）</li>
 * </ul>
 * <h2>参数解析（getParamMap）</h2>
 * <ul>
 *   <li>4.1 空/无参数：返回空 Map（getParamMapEmpty）</li>
 *   <li>4.2 基本参数：{@code ?a=1} → {@code {a:1}}（getParamMap）</li>
 *   <li>4.3 值含 {@code =}：{@code ?a=b=c} → {@code {a:"b=c"}}（getParamMapValueContainsEquals）</li>
 *   <li>4.4 fragment 不混入：{@code #frag} 及其后内容不作为参数（getParamMapIgnoreFragment）</li>
 *   <li>4.5 编码解码：{@code b%3Dc} → {@code b=c}、{@code 1%23frag} → {@code 1#frag}（getParamMapDecode）</li>
 * </ul>
 *
 * @since 2025/12/22
 * @version 1.0
 */
public class CUrlUtilsTests {

    static final String DEFAULT_DOMAIN = "https://c332030.com";

    static final String DEFAULT_DOMAIN2 = "https://cc332030.com";

    /**
     * 测试获取 URL 的路径部分
     * 对应测试用例 1.1：{@code https://c332030.com/ip} → {@code /ip}
     */
    @Test
    public void getPath() {

        val url = DEFAULT_DOMAIN + "/ip";
        Assertions.assertEquals("/ip", CUrlUtils.getPath(url));

    }

    /**
     * 测试提取 http(s) 协议开始的 URL：协议开头时原样返回
     * 对应测试用例 2.1：协议开头：原样返回
     */
    @Test
    public void getUrl() {

        Assertions.assertEquals(DEFAULT_DOMAIN + "/ip", CUrlUtils.getUrl(DEFAULT_DOMAIN + "/ip"));
        Assertions.assertEquals("http://c332030.com/ip", CUrlUtils.getUrl("http://c332030.com/ip"));

    }

    /**
     * 测试提取 http(s) 协议开始的 URL：协议前有垃圾内容时从协议处截取
     * 对应测试用例 2.2：协议前有垃圾：从协议处截取
     */
    @Test
    public void getUrlFromProtocolStart() {

        Assertions.assertEquals(DEFAULT_DOMAIN + "/ip", CUrlUtils.getUrl("abc" + DEFAULT_DOMAIN + "/ip"));
        Assertions.assertEquals(DEFAULT_DOMAIN, CUrlUtils.getUrl("xxx " + DEFAULT_DOMAIN));

    }

    /**
     * 测试提取 http(s) 协议开始的 URL：不含协议时返回 null
     * 对应测试用例 2.3：不含协议：返回 null
     */
    @Test
    public void getUrlNoProtocol() {

        Assertions.assertNull(CUrlUtils.getUrl("www.c332030.com/ip"));
        Assertions.assertNull(CUrlUtils.getUrl("ftp://c332030.com"));

    }

    /**
     * 测试提取 http(s) 协议开始的 URL：空值与 null 返回 null
     * 对应测试用例 2.4：空/null/空白：返回 null
     */
    @Test
    public void getUrlEmpty() {

        Assertions.assertNull(CUrlUtils.getUrl(""));
        Assertions.assertNull(CUrlUtils.getUrl(null));
        Assertions.assertNull(CUrlUtils.getUrl("   "));

    }

    /**
     * 测试替换 URL 中的域名
     * 对应测试用例 3.1：基本替换：{@code /ip} 路径域名替换
     */
    @Test
    public void replaceDomain() {

        val url = DEFAULT_DOMAIN + "/ip";
        Assertions.assertEquals(DEFAULT_DOMAIN2 + "/ip", CUrlUtils.replaceDomain(url, DEFAULT_DOMAIN2));

    }

    /**
     * 对应测试用例 3.2：保留查询与 fragment：{@code ?x=1&amp;y=2#frag} 保留
     */
    @Test
    public void replaceDomainKeepQueryAndFragment() {

        val url = DEFAULT_DOMAIN + "/ip?x=1&y=2#frag";
        Assertions.assertEquals(DEFAULT_DOMAIN2 + "/ip?x=1&y=2#frag", CUrlUtils.replaceDomain(url, DEFAULT_DOMAIN2));

    }

    /**
     * 测试替换 URL 中的域名：URL 无路径时仅返回新域名
     * 对应测试用例 3.3：无路径：仅返回新域名
     */
    @Test
    public void replaceDomainNoPath() {

        Assertions.assertEquals(DEFAULT_DOMAIN2, CUrlUtils.replaceDomain(DEFAULT_DOMAIN, DEFAULT_DOMAIN2));

    }

    /**
     * 测试替换 URL 中的域名：URL 为空时返回 null
     * 对应测试用例 3.4：空 URL：返回 null
     */
    @Test
    public void replaceDomainEmptyUrl() {

        Assertions.assertNull(CUrlUtils.replaceDomain("", DEFAULT_DOMAIN2));
        Assertions.assertNull(CUrlUtils.replaceDomain(null, DEFAULT_DOMAIN2));

    }

    /**
     * 测试替换 URL 中的域名：新域名为空时原样返回原 URL
     * 对应测试用例 3.5：空新域名：原样返回原 URL
     */
    @Test
    public void replaceDomainEmptyNewDomain() {

        val url = DEFAULT_DOMAIN + "/ip";
        Assertions.assertEquals(url, CUrlUtils.replaceDomain(url, ""));
        Assertions.assertEquals(url, CUrlUtils.replaceDomain(url, null));

    }

    /**
     * 测试解析 URL 查询参数：空值、无参数返回不可变空 Map
     * 对应测试用例 4.1：空/无参数：返回空 Map
     */
    @Test
    public void getParamMapEmpty() {

        Assertions.assertTrue(CUrlUtils.getParamMap("").isEmpty());
        Assertions.assertTrue(CUrlUtils.getParamMap(null).isEmpty());
        Assertions.assertTrue(CUrlUtils.getParamMap(DEFAULT_DOMAIN + "/ip").isEmpty());

    }

    /**
     * 测试解析 URL 查询参数：基本参数
     * 对应测试用例 4.2：基本参数：{@code ?a=1} → {@code {a:1}}
     */
    @Test
    public void getParamMap() {

        Assertions.assertEquals("1", CUrlUtils.getParamMap(DEFAULT_DOMAIN + "/ip?a=1").get("a"));

    }

    /**
     * 测试解析 URL 查询参数：参数值含 = 时保留完整
     * 对应测试用例 4.3：值含 {@code =}：{@code ?a=b=c} → {@code {a:"b=c"}}
     */
    @Test
    public void getParamMapValueContainsEquals() {

        Assertions.assertEquals("b=c", CUrlUtils.getParamMap(DEFAULT_DOMAIN + "/ip?a=b=c").get("a"));

    }

    /**
     * 测试解析 URL 查询参数：fragment 不混入参数
     * 对应测试用例 4.4：fragment 不混入：{@code #frag} 及其后内容不作为参数
     */
    @Test
    public void getParamMapIgnoreFragment() {

        Assertions.assertEquals("1", CUrlUtils.getParamMap(DEFAULT_DOMAIN + "/ip?a=1#frag").get("a"));

        val map = CUrlUtils.getParamMap(DEFAULT_DOMAIN + "/ip?a=1&b=2#frag");
        Assertions.assertEquals("1", map.get("a"));
        Assertions.assertEquals("2", map.get("b"));
        Assertions.assertNull(map.get("frag"));

        // fragment 后有值（键值对）时不混入参数
        val mapFragmentWithValue = CUrlUtils.getParamMap(DEFAULT_DOMAIN + "/ip?a=1&b=2#x=3");
        Assertions.assertEquals("1", mapFragmentWithValue.get("a"));
        Assertions.assertEquals("2", mapFragmentWithValue.get("b"));
        Assertions.assertNull(mapFragmentWithValue.get("x"));

        // fragment 后带值且仅一个查询参数
        val mapSingleParamWithFragmentValue = CUrlUtils.getParamMap(DEFAULT_DOMAIN + "/ip?a=1#frag=value");
        Assertions.assertEquals("1", mapSingleParamWithFragmentValue.get("a"));
        Assertions.assertNull(mapSingleParamWithFragmentValue.get("frag"));

    }

    /**
     * 测试解析 URL 查询参数：编码值正确解码
     * 对应测试用例 4.5：编码解码：{@code b%3Dc} → {@code b=c}、{@code 1%23frag} → {@code 1#frag}
     */
    @Test
    public void getParamMapDecode() {

        Assertions.assertEquals("b=c", CUrlUtils.getParamMap(DEFAULT_DOMAIN + "/ip?a=b%3Dc").get("a"));
        Assertions.assertEquals("1#frag", CUrlUtils.getParamMap(DEFAULT_DOMAIN + "/ip?a=1%23frag").get("a"));

    }

}
