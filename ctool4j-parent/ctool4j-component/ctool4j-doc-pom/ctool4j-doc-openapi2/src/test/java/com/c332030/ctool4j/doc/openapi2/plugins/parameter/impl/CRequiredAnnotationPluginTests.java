package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.web.validation.annotation.CRequired;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.annotation.AnnotationUtils;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;

import java.util.Optional;

/**
 * <p>
 * Description: CRequiredAnnotationPluginTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖注解类型识别、supports 支持性。</li>
 *   <li>覆盖 apply 分支输出：命中 @CRequired 标记必填（正例）、未命中不标记（反例）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"命中 @CRequired 标记必填、isRequired 默认 true（标注即必填）"的约定。</li>
 *   <li>依据分支覆盖：命中/未命中两种分支输出均验证（apply 与 isRequired 由
 *   {@code ICAnnotationExpandedParameterBuilderPlugin} 默认实现提供）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getAnnotationClass、supports（含 null）、apply 命中/未命中。</li>
 *   <li>未覆盖：在真实 springfox 文档生成链路上的端到端行为（由 {@code CSchemaIntegrationTests} 间接覆盖）。</li>
 * </ul>
 * <h2>注解识别与支持性</h2>
 * <ul>
 *   <li>1.1 返回 CRequired.class（getAnnotationClass）</li>
 *   <li>1.2 支持 SWAGGER_2/12（supports）</li>
 *   <li>1.3 null 文档类型也支持（supports_null）</li>
 * </ul>
 * <h2>apply 分支输出</h2>
 * <ul>
 *   <li>2.1 命中 @CRequired 标记参数必填（apply_hitAnnotation）</li>
 *   <li>2.2 未命中 @CRequired 不标记必填（apply_missAnnotation）</li>
 * </ul>
 *
 * <p>
 * 是 {@link CRequiredAnnotationPlugin} 的测试用例。
 * </p>
 *
 * @since 2026/9/16
 * @version 1.0
 */
class CRequiredAnnotationPluginTests {

    private final CRequiredAnnotationPlugin plugin = new CRequiredAnnotationPlugin();

    /**
     * <p>
     * 对应测试用例 1.1：返回 CRequired.class
     */
    @Test
    void getAnnotationClass() {
        Assertions.assertEquals(CRequired.class, plugin.getAnnotationClass());
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
     * apply 分支输出：命中 @CRequired 注解 → 标记参数必填（正例）
     * <p>
     * 对应测试用例 2.1：命中 @CRequired 标记参数必填
     */
    @Test
    void apply_hitAnnotation() {
        val context = Mockito.mock(ParameterExpansionContext.class);
        val parameterBuilder = new ParameterBuilder();
        Mockito.when(context.findAnnotation(CRequired.class))
            .thenReturn(Optional.of(AnnotationUtils.synthesizeAnnotation(CRequired.class)));
        Mockito.when(context.getParameterBuilder()).thenReturn(parameterBuilder);

        plugin.apply(context);

        Assertions.assertTrue(parameterBuilder.build().isRequired(), "命中 @CRequired 应标记参数必填");
    }

    /**
     * apply 分支输出：未命中 @CRequired 注解 → 不标记必填（反例）
     * <p>
     * 对应测试用例 2.2：未命中 @CRequired 不标记必填
     */
    @Test
    void apply_missAnnotation() {
        val context = Mockito.mock(ParameterExpansionContext.class);
        val parameterBuilder = new ParameterBuilder();
        Mockito.when(context.findAnnotation(CRequired.class)).thenReturn(Optional.empty());
        Mockito.when(context.getParameterBuilder()).thenReturn(parameterBuilder);

        plugin.apply(context);

        Assertions.assertFalse(parameterBuilder.build().isRequired(), "未命中 @CRequired 不应标记必填");
    }

}
