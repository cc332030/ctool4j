package com.c332030.ctool4j.doc.openapi2.plugins.operation.impl;

import com.c332030.ctool4j.doc.annotation.COperation;
import com.c332030.ctool4j.doc.annotation.CTag;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * <p>
 * Description: CTagAnnotationPluginTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖 supports 支持性。</li>
 *   <li>覆盖 apply 分支输出：类级 @CTag 作为分组（正例）、合并方法级 @COperation.tags、无 @CTag 不处理（反例）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"分组 = 类级 name 并集方法级 tags"的约定。</li>
 *   <li>依据分支覆盖：有/无 @CTag、是否合并方法级分组均验证。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：supports（含 null）、apply 类级分组/合并/无注解。</li>
 *   <li>未覆盖：在真实 springfox 文档生成链路上的端到端分组行为。</li>
 * </ul>
 * <h2>支持性</h2>
 * <ul>
 *   <li>1.1 支持 SWAGGER_2/12 与 null（supports）</li>
 * </ul>
 * <h2>apply 分支输出</h2>
 * <ul>
 *   <li>2.1 类级 @CTag.name 作为分组 tag（apply_classTag）</li>
 *   <li>2.2 合并方法 @COperation.tags 到分组（apply_mergeOperationTags）</li>
 *   <li>2.3 无 @CTag 不设置分组（apply_noAnnotation）</li>
 * </ul>
 *
 * <p>
 * 是 {@link CTagAnnotationPlugin} 的测试用例。
 * </p>
 *
 * @since 2026/9/4
 * @version 1.0
 */
class CTagAnnotationPluginTests {

    private final CTagAnnotationPlugin plugin = new CTagAnnotationPlugin();

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
     * apply：类级 @CTag.name 作为分组 tag（正例）
     * <p>对应测试用例 2.1</p>
     */
    @Test
    void apply_classTag() throws Exception {
        val context = Mockito.mock(OperationContext.class);
        val operationBuilder = new OperationBuilder(prefix -> "op");

        Mockito.when(context.findControllerAnnotation(CTag.class))
            .thenReturn(Optional.of(readCTag()));
        Mockito.when(context.findAnnotation(COperation.class))
            .thenReturn(Optional.empty());
        Mockito.when(context.operationBuilder()).thenReturn(operationBuilder);

        plugin.apply(context);

        val operation = operationBuilder.build();
        Assertions.assertTrue(operation.getTags().contains("用户接口"), "类级 @CTag.name 应作为分组 tag");
    }

    /**
     * apply：合并方法 @COperation.tags 到分组 tag
     * <p>对应测试用例 2.2</p>
     */
    @Test
    void apply_mergeOperationTags() throws Exception {
        val context = Mockito.mock(OperationContext.class);
        val operationBuilder = new OperationBuilder(prefix -> "op");

        Mockito.when(context.findControllerAnnotation(CTag.class))
            .thenReturn(Optional.of(readCTag()));
        Mockito.when(context.findAnnotation(COperation.class))
            .thenReturn(Optional.of(readCOperationWithTags()));
        Mockito.when(context.operationBuilder()).thenReturn(operationBuilder);

        plugin.apply(context);

        val tags = operationBuilder.build().getTags();
        Assertions.assertTrue(tags.contains("用户接口"), "应包含类级分组");
        Assertions.assertTrue(tags.contains("额外分组"), "应合并方法级分组");
    }

    /**
     * apply：无 @CTag 时不做处理
     * <p>对应测试用例 2.3</p>
     */
    @Test
    void apply_noAnnotation() {
        val context = Mockito.mock(OperationContext.class);
        val operationBuilder = new OperationBuilder(prefix -> "op");

        Mockito.when(context.findControllerAnnotation(CTag.class)).thenReturn(Optional.empty());
        Mockito.when(context.findAnnotation(COperation.class)).thenReturn(Optional.empty());
        Mockito.when(context.operationBuilder()).thenReturn(operationBuilder);

        plugin.apply(context);

        Assertions.assertTrue(operationBuilder.build().getTags().isEmpty(), "无 @CTag 不应设置分组");
    }

    // 简写：@CTag("内容") 等价于 name="内容"
    @CTag("用户接口")
    private static class FixtureController {
        void read() {
        }
    }

    private static class FixtureMethod {
        @COperation(value = "x", tags = "额外分组")
        void read() {
        }
    }

    private static CTag readCTag() {
        return FixtureController.class.getAnnotation(CTag.class);
    }

    private static COperation readCOperationWithTags() throws NoSuchMethodException {
        Method method = FixtureMethod.class.getDeclaredMethod("read");
        return method.getAnnotation(COperation.class);
    }
}
