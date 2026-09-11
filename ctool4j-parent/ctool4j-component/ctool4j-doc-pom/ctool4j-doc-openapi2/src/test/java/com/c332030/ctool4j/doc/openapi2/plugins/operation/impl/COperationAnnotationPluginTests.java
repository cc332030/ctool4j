package com.c332030.ctool4j.doc.openapi2.plugins.operation.impl;

import com.c332030.ctool4j.doc.annotation.COperation;
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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖 supports 支持性。</li>
 *   <li>覆盖 apply 分支输出：命中 @COperation 写入 summary/description/deprecated（正例）、未命中不处理（反例）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"summary/description 非空覆盖、deprecated=true 标记废弃"的约定。</li>
 *   <li>依据白盒/黑盒原则与分支覆盖：命中/未命中两种分支输出均验证。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：supports（含 null）、apply 命中/未命中。</li>
 *   <li>未覆盖：在真实 springfox 文档生成链路上的端到端行为。</li>
 * </ul>
 * <h2>支持性</h2>
 * <ul>
 *   <li>1.1 支持 SWAGGER_2/12 与 null（supports）</li>
 * </ul>
 * <h2>apply 分支输出</h2>
 * <ul>
 *   <li>2.1 命中 @COperation 写入 summary/description/deprecated（apply_hitAnnotation）</li>
 *   <li>2.2 未命中 @COperation 不处理（apply_missAnnotation）</li>
 * </ul>
 *
 * <p>
 * 是 {@link COperationAnnotationPlugin} 的测试用例。
 * </p>
 *
 * @since 2026/9/4
 * @version 1.0
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
