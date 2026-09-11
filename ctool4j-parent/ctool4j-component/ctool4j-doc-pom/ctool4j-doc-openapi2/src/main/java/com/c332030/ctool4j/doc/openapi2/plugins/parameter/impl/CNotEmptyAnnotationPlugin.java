package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.openapi2.plugins.parameter.ICAnnotationExpandedParameterBuilderPlugin;

import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * Description: CNotEmptyAnnotationPlugin
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
 *     <td>未命中 @NotEmpty</td>
 *     <td>不标记必填</td>
 *   </tr>
 *   <tr>
 *     <td>supports 任意文档类型/null</td>
 *     <td>均返回 true</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>springfox 文档生成时将 {@code @NotEmpty} 参数标记为必填。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅处理 {@code @NotEmpty}，其他校验注解由对应插件处理。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>注解识别</b></p>
 * <ul>
 *   <li>通过 {@code ICAnnotationExpandedParameterBuilderPlugin} 的默认 {@code apply} 逻辑：命中注解且 {@code isRequired} 为 true 时标记必填。</li>
 * </ul>
 *
 * @since 2025/12/17
 * @version 1.0
 */
public class CNotEmptyAnnotationPlugin implements ICAnnotationExpandedParameterBuilderPlugin<NotEmpty> {

    /**
     * 获取支持的校验注解类型
     * <ul>
     *   <li>{@code getAnnotationClass()} 返回 {@code NotEmpty.class}。</li>
     * </ul>
     *
     * @return NotEmpty 注解类
     */
    @Override
    public Class<NotEmpty> getAnnotationClass() {
        return NotEmpty.class;
    }

}
