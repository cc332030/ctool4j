package com.c332030.ctool4j.web.cors;

import com.c332030.ctool4j.spring.test.annotation.CTool4jSpringBootTest;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * Description: CCorsConfigBootTests
 * </p>
 *
 * <p>是 {@link CCorsConfig} 与 {@link CCorsOriginConfig} 在真实 Spring 容器下的<b>配置绑定</b>集成测试用例：
 * 验证 {@code cors.origins[<域名>].*} 能正确绑定到 {@code Map<String, CCorsOriginConfig>}，
 * 且域名级未配置的项保持 {@code null}（默认值不写在域名配置里、由处理逻辑兜底）</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>纯单元测试直接构造配置对象，无法证明"配置文件里的写法能绑上"；本用例通过真实容器 +
 *   {@code @TestPropertySource} 提供属性，验证绑定链路本身（键映射、嵌套类型、kebab-case 到驼峰的转换）。</li>
 *   <li>断言点选在可观测结果上：绑到的域名 key 与域名级各项取值，含"未配置即 null"这一关键契约。</li>
 *   <li><b>域名 key 必须用中括号形式</b>（{@code cors.origins[a.example.com].enable}）：点号形式
 *   （{@code cors.origins.a.example.com.enable}）会被 Spring 当作层级分隔符，在 {@code origins} 下多建层级、
 *   绑不到 Map 的 key 上（实测 {@code origins} 为空）。</li>
 *   <li><b>域名 key 不支持含 {@code :}（端口）</b>：{@code cors.origins[c.example.com:8081].enable} 实测被
 *   规范化成 {@code cexamplecom}（点号与冒号均被吞掉），既绑不到期望的 key、不同域名还会互相覆盖。
 *   故域名 key 只支持<b>纯 host</b>（见 1.1 与 {@code CCorsUtils.handleDo} 的匹配口径）。</li>
 *   <li>覆盖取舍：不覆盖 Spring Boot 自身的类型转换（非本模块职责）；只覆盖"能绑上"与"未配置即 null"
 *   两条本模块的契约，不复制框架的绑定细节用例。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：见下方编号索引。</li>
 *   <li>未覆盖：实际 HTTP 请求下的跨域响应头（由 {@code CCorsUtilsTests} 与各上游组件用例覆盖）。</li>
 * </ul>
 * <h2>CCorsConfig 配置绑定</h2>
 * <ul>
 *   <li>1.1 bindOrigins（bindOrigins）</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.0
 */
@CTool4jSpringBootTest
@TestPropertySource(properties = {
    "cors.enable=true",
    "cors.origins[a.example.com].enable=true",
    "cors.origins[a.example.com].credentials=true",
    "cors.origins[a.example.com].expose-headers=true",
    "cors.origins[a.example.com].allowed-methods=GET,POST",
    "cors.origins[b.example.com].enable=true",
})
public class CCorsConfigBootTests {

    @Autowired
    private CCorsConfig config;

    /**
     * 对应测试用例 1.1：bindOrigins
     */
    @Test
    public void bindOrigins() {
        // 正例：全局开关与两条域名配置均按 cors.origins[<域名>].* 绑定成功
        Assertions.assertTrue(config.getEnable());
        Assertions.assertEquals(2, config.getOrigins().size());

        val a = config.getOrigins().get("a.example.com");
        Assertions.assertNotNull(a);
        Assertions.assertTrue(a.getEnable());
        Assertions.assertTrue(a.getCredentials());
        Assertions.assertTrue(a.getExposeHeaders());
        Assertions.assertEquals(new LinkedHashSet<>(Arrays.asList("GET", "POST")), a.getAllowedMethods());

        // 未配置的项须保持 null（默认值不写在域名配置里，由处理逻辑兜底）
        Assertions.assertNull(a.getAllowedHeaders());
        Assertions.assertNull(a.getExposedHeaders());

        // 仅配了 enable 的域名：其余项保持 null，等待处理逻辑回落默认值
        val b = config.getOrigins().get("b.example.com");
        Assertions.assertNotNull(b);
        Assertions.assertTrue(b.getEnable());
        Assertions.assertNull(b.getCredentials());
        Assertions.assertNull(b.getExposeHeaders());
        Assertions.assertNull(b.getAllowedMethods());
        Assertions.assertNull(b.getAllowedHeaders());

        // 全局默认值仍可用（域名级未配置时回落至此）
        Set<String> defaultMethods = config.getAllowedMethods();
        Assertions.assertTrue(defaultMethods.contains(CCorsConfig.ALL));
    }

}
