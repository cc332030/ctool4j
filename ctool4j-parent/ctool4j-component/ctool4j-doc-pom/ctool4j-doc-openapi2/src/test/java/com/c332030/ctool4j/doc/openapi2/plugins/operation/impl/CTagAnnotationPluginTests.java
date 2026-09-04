package com.c332030.ctool4j.doc.openapi2.plugins.operation.impl;

import com.c332030.ctool4j.web.doc.annotation.COperation;
import com.c332030.ctool4j.web.doc.annotation.CTag;
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
 * @since 2026/9/4
 *
 * <p>
 * 是 {@link CTagAnnotationPlugin} 的测试用例（对应测试文档 <code>doc/design/openapi2/CTagAnnotationPluginTests.adoc</code>）。
 * </p>
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
