package com.c332030.ctool4j.doc.openapi2.plugins.operation.impl;

import com.c332030.ctool4j.doc.annotation.COperation;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * <p>
 * Description: COperationAnnotationPlugin：识别方法上的 @COperation 注解，
 * 将 summary/description/operationId/deprecated 写入 springfox 的 operation
 * （替代原生 {@code @ApiOperation}；分组 tag 由类级 @CTag 的 CTagAnnotationPlugin 统一处理）
 * </p>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>summary/description 为空</td>
 *     <td>不覆盖</td>
 *   </tr>
 *   <tr>
 *     <td>deprecated=false</td>
 *     <td>不标记废弃</td>
 *   </tr>
 *   <tr>
 *     <td>无 @COperation</td>
 *     <td>不处理</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>用 {@code @COperation} 标注接口方法后，springfox 文档展示摘要/说明/废弃标记。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>springfox 为 operation 自动生成 id，{@code operationId} 不在此映射。</li>
 *   <li>依赖 {@code @COperation}（ctool4j-definition 模块）注解定义。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>注解读取</b></p>
 * <ul>
 *   <li>用 {@code context.findAnnotation(COperation.class)} 取方法级注解。</li>
 * </ul>
 * <p><b>处理</b></p>
 * <ul>
 *   <li>{@code @COperation.value} 非空 → {@code operationBuilder.summary(...)}。</li>
 *   <li>description 非空 → {@code operationBuilder.notes(...)}。</li>
 *   <li>deprecated=true → {@code operationBuilder.deprecated("true")}。</li>
 *   <li>分组 tag 由类级 {@code CTagAnnotationPlugin} 统一处理，此处不处理 tags。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class COperationAnnotationPlugin implements OperationBuilderPlugin {

    /**
     * 将 {@code @COperation} 的 value/description/deprecated 写入 operation 摘要、描述与弃用标记。
     *
     * <p>注解缺失时不做任何修改；value/description 为空白（null 或全空白）时对应字段不写入，
     * 保留既有的摘要/描述。</p>
     *
     * @param context operation 构建上下文，用于查找注解并写入字段
     */
    @Override
    public void apply(@NonNull OperationContext context) {

        val annotationOpt = context.findAnnotation(COperation.class);
        annotationOpt.ifPresent(cOperation -> {
            val operationBuilder = context.operationBuilder();

            if (hasText(cOperation.value())) {
                operationBuilder.summary(cOperation.value());
            }
            if (hasText(cOperation.description())) {
                operationBuilder.notes(cOperation.description());
            }
            if (cOperation.deprecated()) {
                operationBuilder.deprecated(Boolean.TRUE.toString());
            }
        });
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
