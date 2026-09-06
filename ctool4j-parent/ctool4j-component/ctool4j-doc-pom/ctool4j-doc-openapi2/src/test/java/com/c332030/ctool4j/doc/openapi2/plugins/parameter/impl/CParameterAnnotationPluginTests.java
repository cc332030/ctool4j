package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.annotation.CParameter;
import com.c332030.ctool4j.web.validation.annotation.CNotRequired;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.ParameterContext;

import java.util.Optional;

/**
 * <p>
 * Description: CParameterAnnotationPluginTests
 * </p>
 *
 * @since 2026/9/4
 *
 * <p>
 * 是 {@link CParameterAnnotationPlugin} 的测试用例（对应测试文档 <code>doc/design/openapi2/CParameterAnnotationPluginTests.adoc</code>）。
 * </p>
 */
class CParameterAnnotationPluginTests {

    private final CParameterAnnotationPlugin plugin = new CParameterAnnotationPlugin();

    /**
     * <p>对应测试用例 1.1</p>
     */
    @Test
    void supports() {
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_2));
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_12));
        Assertions.assertTrue(plugin.supports(null));
    }

    /**
     * apply：命中 @CParameter 且未标 @CNotRequired → name/description/required/example 写入参数（正例，默认必填）
     * <p>对应测试用例 2.1</p>
     */
    @Test
    void apply_hitAnnotation_requiredByDefault() throws NoSuchMethodException {
        val context = Mockito.mock(ParameterContext.class);
        val resolvedMethodParameter = Mockito.mock(ResolvedMethodParameter.class);
        val parameterBuilder = new ParameterBuilder();

        Mockito.when(resolvedMethodParameter.findAnnotation(CParameter.class))
            .thenReturn(Optional.of(findCParameter()));
        Mockito.when(resolvedMethodParameter.hasParameterAnnotation(CNotRequired.class))
            .thenReturn(false);
        Mockito.when(context.resolvedMethodParameter()).thenReturn(resolvedMethodParameter);
        Mockito.when(context.parameterBuilder()).thenReturn(parameterBuilder);

        plugin.apply(context);

        val parameter = parameterBuilder.build();
        Assertions.assertEquals("userId", parameter.getName());
        Assertions.assertEquals("用户 id", parameter.getDescription());
        Assertions.assertTrue(parameter.isRequired(), "未标 @CNotRequired 应默认标记必填");
    }

    /**
     * apply：命中 @CParameter 且标注 @CNotRequired → 不标记必填（反例，非必填）
     * <p>对应测试用例 2.2</p>
     */
    @Test
    void apply_hitAnnotation_notRequired() throws NoSuchMethodException {
        val context = Mockito.mock(ParameterContext.class);
        val resolvedMethodParameter = Mockito.mock(ResolvedMethodParameter.class);
        val parameterBuilder = new ParameterBuilder();

        Mockito.when(resolvedMethodParameter.findAnnotation(CParameter.class))
            .thenReturn(Optional.of(findCParameter()));
        Mockito.when(resolvedMethodParameter.hasParameterAnnotation(CNotRequired.class))
            .thenReturn(true);
        Mockito.when(context.resolvedMethodParameter()).thenReturn(resolvedMethodParameter);
        Mockito.when(context.parameterBuilder()).thenReturn(parameterBuilder);

        plugin.apply(context);

        val parameter = parameterBuilder.build();
        Assertions.assertFalse(parameter.isRequired(), "标注 @CNotRequired 不应标记必填");
    }

    /**
     * apply：未命中 @CParameter → 不处理（反例）
     * <p>对应测试用例 2.3</p>
     */
    @Test
    void apply_missAnnotation() {
        val context = Mockito.mock(ParameterContext.class);
        val resolvedMethodParameter = Mockito.mock(ResolvedMethodParameter.class);
        val parameterBuilder = new ParameterBuilder();

        Mockito.when(resolvedMethodParameter.findAnnotation(CParameter.class))
            .thenReturn(Optional.empty());
        Mockito.when(resolvedMethodParameter.hasParameterAnnotation(CNotRequired.class))
            .thenReturn(false);
        Mockito.when(context.resolvedMethodParameter()).thenReturn(resolvedMethodParameter);
        Mockito.when(context.parameterBuilder()).thenReturn(parameterBuilder);

        plugin.apply(context);

        Assertions.assertFalse(parameterBuilder.build().isRequired(), "未命中 @CParameter 不应标记必填");
    }

    /**
     * apply：仅标注 @CNotRequired（无 @CParameter）→ 独立生效，不标记必填
     * <p>对应测试用例 2.4</p>
     */
    @Test
    void apply_onlyNotRequired() {
        val context = Mockito.mock(ParameterContext.class);
        val resolvedMethodParameter = Mockito.mock(ResolvedMethodParameter.class);
        val parameterBuilder = new ParameterBuilder();

        Mockito.when(resolvedMethodParameter.findAnnotation(CParameter.class))
            .thenReturn(Optional.empty());
        Mockito.when(resolvedMethodParameter.hasParameterAnnotation(CNotRequired.class))
            .thenReturn(true);
        Mockito.when(context.resolvedMethodParameter()).thenReturn(resolvedMethodParameter);
        Mockito.when(context.parameterBuilder()).thenReturn(parameterBuilder);

        plugin.apply(context);

        Assertions.assertFalse(parameterBuilder.build().isRequired(),
            "仅标注 @CNotRequired（无 @CParameter）也应标记非必填");
    }

    private static CParameter findCParameter() throws NoSuchMethodException {
        return Fixture.class
            .getDeclaredMethod("find", String.class)
            .getParameters()[0].getAnnotation(CParameter.class);
    }

    private static class Fixture {

        @SuppressWarnings("unused")
        void find(@CParameter(value = "用户 id", name = "userId", example = "1") String id) {
        }
    }
}
