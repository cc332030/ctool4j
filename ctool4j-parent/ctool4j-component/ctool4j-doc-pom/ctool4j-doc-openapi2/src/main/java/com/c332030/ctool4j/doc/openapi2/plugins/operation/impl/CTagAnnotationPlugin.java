package com.c332030.ctool4j.doc.openapi2.plugins.operation.impl;

import com.c332030.ctool4j.doc.annotation.COperation;
import com.c332030.ctool4j.doc.annotation.CTag;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * Description: CTagAnnotationPlugin：识别类上的 @CTag 注解，将该接口的分组（tag）应用到其所有 operation，
 * 并合并方法上 @COperation.tags 指定的额外分组；由类级 @CTag 替代原生 {@code @Api} 的分组作用
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CTagAnnotationPlugin} 实现 {@code OperationBuilderPlugin}，识别类上 {@code @CTag} 注解，将类级分组（tag）应用到该类的所有 operation，并合并方法上 {@code @COperation.tags} 指定的额外分组（替代类级原生 {@code @Api} 的分组作用）。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无 @CTag</td>
 *     <td>不处理</td>
 *   </tr>
 *   <tr>
 *     <td>name/tags 均为空</td>
 *     <td>不设置分组</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>用 {@code @CTag} 标注 Controller 后，springfox 按该类分组生成接口文档。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code @CTag}/{@code @COperation}（ctool4j-definition 模块）注解定义。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>注解读取</b></p>
 * <ul>
 *   <li>用 {@code context.findControllerAnnotation(CTag.class)} 取类级 {@code @CTag}，用 {@code context.findAnnotation(COperation.class)} 取方法级分组。</li>
 * </ul>
 * <p><b>处理</b></p>
 * <ul>
 *   <li>类级分组名取 {@code @CTag.value}。</li>
 *   <li>分组 tag = 类级分组名 并集 方法 {@code @COperation.tags}（均为空则不处理）。</li>
 *   <li>通过 {@code context.operationBuilder().tags(...)} 写入。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CTagAnnotationPlugin implements OperationBuilderPlugin {

    /**
     * 将 {@code @CTag} 的 name 写入 operation 的标签集合。
     *
     * <p>注解缺失、名字为空白或标签已存在时不重复写入。</p>
     *
     * @param context operation 构建上下文，用于查找注解并写入标签
     */
    @Override
    public void apply(@NonNull OperationContext context) {

        val classTagOpt = context.findControllerAnnotation(CTag.class);
        val methodOperationOpt = context.findAnnotation(COperation.class);

        Set<String> tags = new LinkedHashSet<>();

        classTagOpt.ifPresent(cTag -> {
            if (hasText(cTag.value())) {
                tags.add(cTag.value());
            }
        });

        methodOperationOpt.ifPresent(cOperation -> {
            if (cOperation.tags().length > 0) {
                Arrays.stream(cOperation.tags())
                    .filter(CTagAnnotationPlugin::hasText)
                    .forEach(tags::add);
            }
        });

        if (!tags.isEmpty()) {
            context.operationBuilder().tags(tags);
        }
    }

    /**
     * 是否支持该文档类型。
     *
     * @param delimiter 文档类型（本插件不区分类型）
     * @return 恒为 true，对所有文档类型生效
     */
    @Override
    public boolean supports(@NonNull DocumentationType delimiter) {
        return true;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
