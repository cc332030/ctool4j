package com.c332030.ctool4j.model;

import com.c332030.ctool4j.interfaces.CHttpRequest;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * <p>
 * Description: CHttpServletRequestTests
 * </p>
 *
 * <p>{@code CHttpServletRequest} 的测试用例</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code of(request)} / {@code unwrap(request)}：包装与解包的往返</li>
 *   <li>{@code getCookies()}：容器 Cookie → 抽象层 {@code ICCookie} 的字段映射</li>
 * </ul>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>适配器是"包装 + 解包 + 字段映射"三类动作，纯转发方法由容器保证、不重复测；
 *   本类只钉住这三类中**属本类自己的逻辑**：往返同一性、null 契约、跨侧/非法入参的拒绝、
 *   Cookie 字段逐项映射（映射漏字段不会报错，只表现为取不到值）。</li>
 *   <li>用 Spring 的 {@code MockHttpServletRequest} / {@code MockCookie} 提供容器对象，避免手写 Servlet 实现，
 *   源码不出现 {@code javax.servlet} / {@code jakarta.servlet} 的 import（其实现随 Spring 版本切包，
 *   业务模块（含测试）据此不绑定容器包）。</li>
 * </ul>
 *
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：包装后可取回同一底层对象；{@code unwrap(null)} 返回 null；{@code unwrap} 非本侧适配器抛异常；</li>
 *   <li>{@code of(null)} 快速失败；无 Cookie 返回空列表（不返回 null）；Cookie 各字段逐项映射。</li>
 *   <li>未覆盖：其余纯转发方法（逐个委托底层请求，无本层逻辑）；{@code getRequestDispatcher} 的分支由容器决定。</li>
 * </ul>
 *
 * <h2>用例编号索引</h2>
 * <ul>
 *   <li>1 包装与解包（1.1-1.4）；2 Cookie 映射（2.1-2.2）</li>
 * </ul>
 *
 * @see CHttpServletRequest
 * @since 2026/9/27
 * @version 1.1
 */
public class CHttpServletRequestTests {

    private MockHttpServletRequest request;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest();
    }

    /**
     * 包装后解包取回同一底层请求（对应测试用例 1.1）
     */
    @Test
    public void of_thenUnwrap_returnsSameInstance() {

        val wrapped = CHttpServletRequest.of(request);

        Assertions.assertSame(request, CHttpServletRequest.unwrap(wrapped));

    }

    /**
     * unwrap 对 null 入参返回 null（对应测试用例 1.2）
     */
    @Test
    public void unwrap_null_returnsNull() {
        Assertions.assertNull(CHttpServletRequest.unwrap(null));
    }

    /**
     * unwrap 传入非本侧适配器时抛 IllegalArgumentException（对应测试用例 1.3）
     */
    @Test
    public void unwrap_foreignImplementation_throws() {

        // 用本侧的另一适配器冒充"非本侧"：类型不同即应被拒绝，不需真的引入 jakarta 侧
        // 传入非本侧适配器的实现：类型不符即应被拒绝（用现有测试桩之外的极简实现表达）
        CHttpRequest foreign = Mockito.mock(CHttpRequest.class);

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CHttpServletRequest.unwrap(foreign)
        );

    }

    /**
     * of 对 null 底层请求快速失败（对应测试用例 1.4）
     */
    @Test
    public void of_null_throws() {
        Assertions.assertThrowsExactly(NullPointerException.class, () -> CHttpServletRequest.of(null));
    }

    /**
     * 未携带 Cookie 时返回空列表而非 null（对应测试用例 2.1）
     */
    @Test
    public void getCookies_withoutCookies_returnsEmptyList() {

        val wrapped = CHttpServletRequest.of(request);

        Assertions.assertNotNull(wrapped.getCookies());
        Assertions.assertTrue(wrapped.getCookies().isEmpty());

    }

    /**
     * Cookie 各字段逐项映射到抽象层 Cookie（对应测试用例 2.2）
     */
    @Test
    public void getCookies_mapsAllFields() {

        val cookie = new MockCookie("sid", "v1");
        cookie.setPath("/");
        cookie.setDomain("example.com");
        cookie.setMaxAge(3600);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setVersion(1);
        cookie.setComment("c");
        request.setCookies(cookie);

        val wrapped = CHttpServletRequest.of(request).getCookies();

        Assertions.assertEquals(1, wrapped.size());
        val mapped = wrapped.get(0);
        Assertions.assertEquals("sid", mapped.getName());
        Assertions.assertEquals("v1", mapped.getValue());
        Assertions.assertEquals("/", mapped.getPath());
        Assertions.assertEquals("example.com", mapped.getDomain());
        Assertions.assertEquals(3600, mapped.getMaxAge());
        Assertions.assertTrue(mapped.isSecure());
        Assertions.assertTrue(mapped.isHttpOnly());
        Assertions.assertEquals(1, mapped.getVersion());
        Assertions.assertEquals("c", mapped.getComment());

    }

}
