package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.web.doc.annotation.CParameter;
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
     * apply：命中 @CParameter → name/description/required/example 写入参数（正例）
     * <p>对应测试用例 2.1</p>
     */
    @Test
    void apply_hitAnnotation() throws NoSuchMethodException {
        val context = Mockito.mock(ParameterContext.class);
        val resolvedMethodParameter = Mockito.mock(ResolvedMethodParameter.class);
        val parameterBuilder = new ParameterBuilder();

        Mockito.when(resolvedMethodParameter.findAnnotation(CParameter.class))
            .thenReturn(Optional.of(findCParameter()));
        Mockito.when(context.resolvedMethodParameter()).thenReturn(resolvedMethodParameter);
        Mockito.when(context.parameterBuilder()).thenReturn(parameterBuilder);

        plugin.apply(context);

        val parameter = parameterBuilder.build();
        Assertions.assertEquals("userId", parameter.getName());
        Assertions.assertEquals("用户 id", parameter.getDescription());
        Assertions.assertTrue(parameter.isRequired(), "命中 @CParameter(required=true) 应标记必填");
    }

    /**
     * apply：未命中 @CParameter → 不处理（反例）
     * <p>对应测试用例 2.2</p>
     */
    @Test
    void apply_missAnnotation() {
        val context = Mockito.mock(ParameterContext.class);
        val resolvedMethodParameter = Mockito.mock(ResolvedMethodParameter.class);
        val parameterBuilder = new ParameterBuilder();

        Mockito.when(resolvedMethodParameter.findAnnotation(CParameter.class))
            .thenReturn(Optional.empty());
        Mockito.when(context.resolvedMethodParameter()).thenReturn(resolvedMethodParameter);
        Mockito.when(context.parameterBuilder()).thenReturn(parameterBuilder);

        plugin.apply(context);

        Assertions.assertFalse(parameterBuilder.build().isRequired(), "未命中 @CParameter 不应标记必填");
    }

    private static CParameter findCParameter() throws NoSuchMethodException {
        return Fixture.class
            .getDeclaredMethod("find", String.class)
            .getParameters()[0].getAnnotation(CParameter.class);
    }

    private static class Fixture {

        @SuppressWarnings("unused")
        void find(@CParameter(value = "用户 id", name = "userId", required = true, example = "1") String id) {
        }
    }
}
