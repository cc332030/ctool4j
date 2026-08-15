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
 * @since 2025/12/22
 */
public class CUrlUtilsTests {

    static final String DEFAULT_DOMAIN = "https://c332030.com";

    static final String DEFAULT_DOMAIN2 = "https://cc332030.com";

    /**
     * 测试获取 URL 的路径部分
     */
    @Test
    public void getPath() {

        val url = DEFAULT_DOMAIN + "/ip";
        Assertions.assertEquals("/ip", CUrlUtils.getPath(url));

    }

    /**
     * 测试提取 http(s) 协议开始的 URL：协议开头时原样返回
     */
    @Test
    public void getUrl() {

        Assertions.assertEquals(DEFAULT_DOMAIN + "/ip", CUrlUtils.getUrl(DEFAULT_DOMAIN + "/ip"));
        Assertions.assertEquals("http://c332030.com/ip", CUrlUtils.getUrl("http://c332030.com/ip"));

    }

    /**
     * 测试提取 http(s) 协议开始的 URL：协议前有垃圾内容时从协议处截取
     */
    @Test
    public void getUrlFromProtocolStart() {

        Assertions.assertEquals(DEFAULT_DOMAIN + "/ip", CUrlUtils.getUrl("abc" + DEFAULT_DOMAIN + "/ip"));
        Assertions.assertEquals(DEFAULT_DOMAIN, CUrlUtils.getUrl("xxx " + DEFAULT_DOMAIN));

    }

    /**
     * 测试提取 http(s) 协议开始的 URL：不含协议时返回 null
     */
    @Test
    public void getUrlNoProtocol() {

        Assertions.assertNull(CUrlUtils.getUrl("www.c332030.com/ip"));
        Assertions.assertNull(CUrlUtils.getUrl("ftp://c332030.com"));

    }

    /**
     * 测试提取 http(s) 协议开始的 URL：空值与 null 返回 null
     */
    @Test
    public void getUrlEmpty() {

        Assertions.assertNull(CUrlUtils.getUrl(""));
        Assertions.assertNull(CUrlUtils.getUrl(null));
        Assertions.assertNull(CUrlUtils.getUrl("   "));

    }

    /**
     * 测试替换 URL 中的域名
     */
    @Test
    public void replaceDomain() {

        val url = DEFAULT_DOMAIN + "/ip";
        Assertions.assertEquals(DEFAULT_DOMAIN2 + "/ip", CUrlUtils.replaceDomain(url, DEFAULT_DOMAIN2));

    }

    @Test
    public void replaceDomainKeepQueryAndFragment() {

        val url = DEFAULT_DOMAIN + "/ip?x=1&y=2#frag";
        Assertions.assertEquals(DEFAULT_DOMAIN2 + "/ip?x=1&y=2#frag", CUrlUtils.replaceDomain(url, DEFAULT_DOMAIN2));

    }

    /**
     * 测试替换 URL 中的域名：URL 无路径时仅返回新域名
     */
    @Test
    public void replaceDomainNoPath() {

        Assertions.assertEquals(DEFAULT_DOMAIN2, CUrlUtils.replaceDomain(DEFAULT_DOMAIN, DEFAULT_DOMAIN2));

    }

    /**
     * 测试替换 URL 中的域名：URL 为空时返回 null
     */
    @Test
    public void replaceDomainEmptyUrl() {

        Assertions.assertNull(CUrlUtils.replaceDomain("", DEFAULT_DOMAIN2));
        Assertions.assertNull(CUrlUtils.replaceDomain(null, DEFAULT_DOMAIN2));

    }

    /**
     * 测试替换 URL 中的域名：新域名为空时原样返回原 URL
     */
    @Test
    public void replaceDomainEmptyNewDomain() {

        val url = DEFAULT_DOMAIN + "/ip";
        Assertions.assertEquals(url, CUrlUtils.replaceDomain(url, ""));
        Assertions.assertEquals(url, CUrlUtils.replaceDomain(url, null));

    }

}
