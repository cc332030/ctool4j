package com.c332030.ctool4j.doc.openapi2.plugins.grouping.impl;

import com.c332030.ctool4j.doc.annotation.CTag;
import io.swagger.annotations.Api;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

/**
 * <p>
 * Description: CTagGroupingStrategyTests
 * </p>
 *
 * <p>验证 {@link CTagGroupingStrategy} 的分组名/分组描述取值与回退：{@code @CTag} 优先、兼容存量 {@code @Api}、
 * 都无则回退 springfox 默认（类名 kebab）；并固化"单分组"（不出现英文空分组）。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接调用策略方法（不启动 springfox/容器）：分组名与描叙的取值链是纯注解解析，与文档生成链路无关。</li>
 *   <li>以真实 {@code HandlerMethod}（控制器实例 + 方法）为输入，覆写点与 springfox 调用一致。</li>
 *   <li>"单分组"单独断言：这是本策略存在的目的——修掉"中文分组 + 英文空分组"并存。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据 {@link CTagGroupingStrategy} 的 javadoc：{@code @CTag.value} → {@code @Api.tags} → springfox 默认；
 *   描述同理（{@code @CTag.description} → {@code @Api.value} → 默认）。</li>
 *   <li>依据 springfox 默认策略行为：{@code splitCamelCase(类名, "-").toLowerCase()}（如 {@code PlainController}
 *   → {@code plain-controller}）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：{@code @CTag} 取值（名 + 描述）、{@code @Api} 兼容取值、两者皆无时回退默认、{@code @CTag} 空白时继续取
 *   {@code @Api}、分组为单元素。</li>
 *   <li>未覆盖：在真实 springfox 文档生成链路上 {@code /v2/api-docs} 的分组输出（需容器级集成）。</li>
 * </ul>
 * <h2>分组名与描述</h2>
 * <ul>
 *   <li>1.1 类上 {@code @CTag} 作为分组名与描述，且仅一个分组（getResourceGroups_classCTag）</li>
 *   <li>1.2 仅存量 {@code @Api} 时按 {@code @Api.tags}/{@code @Api.value} 取值（getResourceGroups_apiTags）</li>
 *   <li>1.3 {@code @CTag} 与 {@code @Api} 皆无时回退 springfox 默认（getResourceGroups_defaultFallback）</li>
 *   <li>1.4 {@code @CTag.value} 空白时继续取 {@code @Api.tags}（getResourceGroups_blankCTagFallsBackToApi）</li>
 * </ul>
 *
 * @since 2026/9/15
 * @version 1.0
 * @see CTagGroupingStrategy
 */
class CTagGroupingStrategyTests {

    private final CTagGroupingStrategy strategy = new CTagGroupingStrategy();

    /**
     * 对应测试用例 1.1：类上 {@code @CTag} 作为分组名与描述，且仅一个分组
     */
    @Test
    void getResourceGroups_classCTag() throws Exception {
        val handlerMethod = handlerMethodOf(CTaggedController.class);

        val groups = strategy.getResourceGroups(null, handlerMethod);

        // 正例：分组名/描述取 @CTag，且只有一个分组（否则 swagger-ui 会出现英文空分组）
        Assertions.assertEquals(1, groups.size(), "应只产出一个分组");
        Assertions.assertEquals("企微", groups.iterator().next().getGroupName());
        Assertions.assertEquals("企业微信接口", strategy.getResourceDescription(null, handlerMethod));
    }

    /**
     * 对应测试用例 1.2：仅存量 {@code @Api} 时按 {@code @Api.tags}/{@code @Api.value} 取值
     */
    @Test
    void getResourceGroups_apiTags() throws Exception {
        val handlerMethod = handlerMethodOf(ApiTaggedController.class);

        val groups = strategy.getResourceGroups(null, handlerMethod);

        Assertions.assertEquals("客户", groups.iterator().next().getGroupName());
        Assertions.assertEquals("客户接口", strategy.getResourceDescription(null, handlerMethod));
    }

    /**
     * 对应测试用例 1.3：{@code @CTag} 与 {@code @Api} 皆无时回退 springfox 默认（类名 kebab）
     */
    @Test
    void getResourceGroups_defaultFallback() throws Exception {
        val handlerMethod = handlerMethodOf(PlainController.class);

        val groups = strategy.getResourceGroups(null, handlerMethod);

        // 边界：未声明分组时不改变原有行为——默认实现按类名 splitCamelCase("-") 小写
        Assertions.assertEquals(1, groups.size());
        Assertions.assertEquals("plain-controller", groups.iterator().next().getGroupName());
        Assertions.assertEquals("Plain Controller", strategy.getResourceDescription(null, handlerMethod));
    }

    /**
     * 对应测试用例 1.4：{@code @CTag.value} 空白时继续取 {@code @Api.tags}
     */
    @Test
    void getResourceGroups_blankCTagFallsBackToApi() throws Exception {
        val handlerMethod = handlerMethodOf(BlankCTagWithApiController.class);

        val groups = strategy.getResourceGroups(null, handlerMethod);

        Assertions.assertEquals("订单配置", groups.iterator().next().getGroupName());
    }

    private static HandlerMethod handlerMethodOf(Class<?> controllerClass) throws Exception {
        val controller = controllerClass.getDeclaredConstructor().newInstance();
        val method = controllerClass.getDeclaredMethod("read");
        return new HandlerMethod(controller, method);
    }

    /**
     * 新注解用法：{@code @CTag} 声明分组名与描述
     */
    @CTag(value = "企微", description = "企业微信接口")
    static class CTaggedController {

        void read() {
        }

    }

    /**
     * 存量用法：原生 {@code @Api} 声明分组
     */
    @Api(tags = "客户", value = "客户接口")
    static class ApiTaggedController {

        void read() {
        }

    }

    /**
     * 未声明分组：应回退 springfox 默认（类名转 kebab）
     */
    static class PlainController {

        void read() {
        }

    }

    /**
     * {@code @CTag.value} 空白 + 存量 {@code @Api}：应继续取 {@code @Api.tags}
     */
    @CTag(description = "描述不参与分组名")
    @Api(tags = "订单配置")
    static class BlankCTagWithApiController {

        void read() {
        }

    }

}
