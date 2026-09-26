package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.annotation.AnnotationUtils;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;

import javax.validation.constraints.NotEmpty;

import java.util.Optional;

/**
 * <p>
 * Description: CNotEmptyAnnotationPluginTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖注解类型识别、supports 支持性。</li>
 *   <li>覆盖 apply 分支输出：命中 @NotEmpty 标记必填（正例）、未命中不标记（反例）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"命中 @NotEmpty 标记必填、isRequired 默认 true"的约定。</li>
 *   <li>依据白盒/黑盒原则与分支覆盖：命中/未命中两种分支输出均验证。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getAnnotationClass、supports（含 null）、apply 命中/未命中。</li>
 *   <li>未覆盖：在真实 springfox 文档生成链路上的端到端行为。</li>
 * </ul>
 * <h2>注解识别与支持性</h2>
 * <ul>
 *   <li>1.1 返回 NotEmpty.class（getAnnotationClass）</li>
 *   <li>1.2 支持 SWAGGER_2/12（supports）</li>
 *   <li>1.3 null 文档类型也支持（supports_null）</li>
 * </ul>
 * <h2>apply 分支输出</h2>
 * <ul>
 *   <li>2.1 命中 @NotEmpty 标记参数必填（apply_hitAnnotation）</li>
 *   <li>2.2 未命中 @NotEmpty 不标记必填（apply_missAnnotation）</li>
 * </ul>
 *
 * <p>
 * 是 {@link CNotEmptyAnnotationPlugin} 的测试用例。
 * </p>
 *
 * @since 2026/8/14
 * @version 1.0
 */
class CNotEmptyAnnotationPluginTests {

    private final CNotEmptyAnnotationPlugin plugin = new CNotEmptyAnnotationPlugin();

    /**
     * <p>
     * 对应测试用例 1.1：返回 NotEmpty.class
     */
    @Test
    void getAnnotationClass() {
        Assertions.assertEquals(NotEmpty.class, plugin.getAnnotationClass());
    }

    /**
     * <p>
     * 对应测试用例 1.2：支持 SWAGGER_2/12
     */
    @Test
    void supports() {
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_2));
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_12));
    }

    /**
     * <p>
     * 对应测试用例 1.3：null 文档类型也支持
     */
    @Test
    void supports_null() {
        Assertions.assertTrue(plugin.supports(null));
    }

    /**
     * apply 分支输出：命中 @NotEmpty 注解 → 标记参数必填（正例）
     * <p>
     * 对应测试用例 2.1：命中 @NotEmpty 标记参数必填
     */
    @Test
    void apply_hitAnnotation() {
        val context = Mockito.mock(ParameterExpansionContext.class);
        val parameterBuilder = new ParameterBuilder();
        Mockito.when(context.findAnnotation(NotEmpty.class))
            .thenReturn(Optional.of(AnnotationUtils.synthesizeAnnotation(NotEmpty.class)));
        Mockito.when(context.getParameterBuilder()).thenReturn(parameterBuilder);

        plugin.apply(context);

        Assertions.assertTrue(parameterBuilder.build().isRequired(), "命中 @NotEmpty 应标记参数必填");
    }

    /**
     * apply 分支输出：未命中 @NotEmpty 注解 → 不标记必填（反例）
     * <p>
     * 对应测试用例 2.2：未命中 @NotEmpty 不标记必填
     */
    @Test
    void apply_missAnnotation() {
        val context = Mockito.mock(ParameterExpansionContext.class);
        val parameterBuilder = new ParameterBuilder();
        Mockito.when(context.findAnnotation(NotEmpty.class)).thenReturn(Optional.empty());
        Mockito.when(context.getParameterBuilder()).thenReturn(parameterBuilder);

        plugin.apply(context);

        Assertions.assertFalse(parameterBuilder.build().isRequired(), "未命中 @NotEmpty 不应标记必填");
    }

}
