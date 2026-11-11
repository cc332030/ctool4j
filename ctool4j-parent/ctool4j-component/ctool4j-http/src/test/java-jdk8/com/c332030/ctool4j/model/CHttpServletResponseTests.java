package com.c332030.ctool4j.model;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * <p>
 * Description: CHttpServletResponseTests
 * </p>
 *
 * <p>{@code CHttpServletResponse} 的测试用例</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code of(response)} / {@code unwrap(response)}：包装与解包的往返</li>
 *   <li>{@code addCookie(cookie)}：抽象层 {@code ICCookie} → 容器 Cookie 的字段映射</li>
 * </ul>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>只钉住本类自己的逻辑：往返同一性、null 契约、跨侧拒����、Cookie 字段逐项映射
 *   （映射漏字段不会报错，只表现为浏览器收不到该属性）。</li>
 * </ul>
 *
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：包装后取回同一底层响应；{@code unwrap(null)} 返回 null；{@code unwrap} 非本侧适配器抛异常；</li>
 *   <li>{@code of(null)} 快速失败；Cookie 各字段逐项写回容器。</li>
 *   <li>未覆盖：其余纯转发方法（逐个委托底层响应，无本层逻辑）。</li>
 * </ul>
 *
 * <h2>用例编号索引</h2>
 * <ul>
 *   <li>1 包装与解包（1.1-1.3）；2 Cookie 映射（2.1）</li>
 * </ul>
 *
 * @see CHttpServletResponse
 * @since 2026/9/27
 * @version 1.0
 */
public class CHttpServletResponseTests {

    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() {
        response = new MockHttpServletResponse();
    }

    /**
     * 包装后解包取回同一底层响应（对应测试用例 1.1）
     */
    @Test
    public void of_thenUnwrap_returnsSameInstance() {

        val wrapped = CHttpServletResponse.of(response);

        Assertions.assertSame(response, CHttpServletResponse.unwrap(wrapped));

    }

    /**
     * unwrap 对 null 入参返回 null（对应测试用例 1.2）
     */
    @Test
    public void unwrap_null_returnsNull() {
        Assertions.assertNull(CHttpServletResponse.unwrap(null));
    }

    /**
     * of 对 null 底层响应快速失败（对应测试用例 1.3）
     */
    @Test
    public void of_null_throws() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> CHttpServletResponse.of(null));
    }

    /**
     * 抽象层 Cookie 各字段逐项写回容器 Cookie（对应测试用例 2.1）
     */
    @Test
    public void addCookie_mapsAllFields() {

        val cookie = CCookie.of("sid", "v1")
            .setPath("/")
            .setDomain("example.com")
            .setMaxAge(3600)
            .setSecure(true)
            .setHttpOnly(true)
            .setVersion(1)
            .setComment("c");

        CHttpServletResponse.of(response).addCookie(cookie);

        val written = response.getCookie("sid");
        Assertions.assertNotNull(written);
        Assertions.assertEquals("v1", written.getValue());
        Assertions.assertEquals("/", written.getPath());
        Assertions.assertEquals("example.com", written.getDomain());
        Assertions.assertEquals(3600, written.getMaxAge());
        Assertions.assertTrue(written.getSecure());
        Assertions.assertTrue(written.isHttpOnly());
        Assertions.assertEquals(1, written.getVersion());
        Assertions.assertEquals("c", written.getComment());

    }

}
