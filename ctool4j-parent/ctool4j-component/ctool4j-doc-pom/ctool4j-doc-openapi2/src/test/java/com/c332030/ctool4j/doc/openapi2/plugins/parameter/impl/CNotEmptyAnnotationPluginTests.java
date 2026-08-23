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
 * @since 2026/8/14
 *
 * <p>
 * 是 {@link CNotEmptyAnnotationPlugin} 的测试用例（对应测试文档 <code>doc/design/openapi2/CNotEmptyAnnotationPluginTests.adoc</code>）。
 * </p>
 */
class CNotEmptyAnnotationPluginTests {

    private final CNotEmptyAnnotationPlugin plugin = new CNotEmptyAnnotationPlugin();

    /**
     * <p>
     * 对应测试用例 1.1
     */
    @Test
    void getAnnotationClass() {
        Assertions.assertEquals(NotEmpty.class, plugin.getAnnotationClass());
    }

    /**
     * <p>
     * 对应测试用例 1.2
     */
    @Test
    void supports() {
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_2));
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_12));
    }

    /**
     * <p>
     * 对应测试用例 1.3
     */
    @Test
    void supports_null() {
        Assertions.assertTrue(plugin.supports(null));
    }

    /**
     * apply 分支输出：命中 @NotEmpty 注解 → 标记参数必填（正例）
     * <p>
     * 对应测试用例 2.1
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
     * 对应测试用例 2.2
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
