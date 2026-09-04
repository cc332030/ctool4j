package com.c332030.ctool4j.doc.openapi2.plugins.operation.impl;

import com.c332030.ctool4j.web.validation.annotation.COperation;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.util.Optional;

/**
 * <p>
 * Description: COperationAnnotationPluginTests
 * </p>
 *
 * @since 2026/9/4
 *
 * <p>
 * 是 {@link COperationAnnotationPlugin} 的测试用例（对应测试文档 <code>doc/design/openapi2/COperationAnnotationPluginTests.adoc</code>）。
 * </p>
 */
class COperationAnnotationPluginTests {

    private final COperationAnnotationPlugin plugin = new COperationAnnotationPlugin();

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
     * apply：命中 @COperation → summary/description/deprecated 写入 operation（正例）
     * <p>对应测试用例 2.1</p>
     */
    @Test
    void apply_hitAnnotation() throws Exception {
        val context = Mockito.mock(OperationContext.class);
        val operationBuilder = new OperationBuilder(prefix -> "op");

        Mockito.when(context.findAnnotation(COperation.class))
            .thenReturn(Optional.of(findCOperation("full")));
        Mockito.when(context.operationBuilder()).thenReturn(operationBuilder);

        plugin.apply(context);

        val operation = operationBuilder.build();
        Assertions.assertEquals("查询用户", operation.getSummary());
        Assertions.assertEquals("按 id 查询用户", operation.getNotes());
        Assertions.assertEquals(Boolean.TRUE.toString(), operation.getDeprecated());
    }

    /**
     * apply：未命中 @COperation → 不处理（反例）
     * <p>对应测试用例 2.2</p>
     */
    @Test
    void apply_missAnnotation() {
        val context = Mockito.mock(OperationContext.class);
        val operationBuilder = new OperationBuilder(prefix -> "op");

        Mockito.when(context.findAnnotation(COperation.class)).thenReturn(Optional.empty());
        Mockito.when(context.operationBuilder()).thenReturn(operationBuilder);

        plugin.apply(context);

        val operation = operationBuilder.build();
        Assertions.assertNull(operation.getSummary());
        Assertions.assertNull(operation.getNotes());
    }

    private static COperation findCOperation(String fixture) throws NoSuchMethodException {
        return Fixture.class.getDeclaredMethod(fixture).getAnnotation(COperation.class);
    }

    private static class Fixture {

        // value 与 summary 等效（简写）
        @COperation(value = "查询用户", description = "按 id 查询用户", deprecated = true)
        void full() {
        }
    }
}
