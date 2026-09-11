package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.openapi2.plugins.parameter.ICAnnotationExpandedParameterBuilderPlugin;
import com.c332030.ctool4j.web.validation.annotation.CRequired;

/**
 * <p>
 * Description: CRequiredAnnotationPlugin：识别 @CRequired 注解，标注即必填（标记参数必填）
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
 *     <td>未命中 @CRequired</td>
 *     <td>不标记必填</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>springfox 文档生成时按 {@code @CRequired} 标记方法参数（展开）必填。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 {@code @CRequired}（web 模块）注解定义。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>注解识别</b></p>
 * <ul>
 *   <li>{@code isRequired} 走默认（恒 true）：{@code @CRequired} 标注即必填。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
public class CRequiredAnnotationPlugin implements ICAnnotationExpandedParameterBuilderPlugin<CRequired> {

    /**
     * 获取支持的校验注解类型
     * <ul>
     *   <li>{@code getAnnotationClass()} 返回 {@code CRequired.class}。</li>
     * </ul>
     *
     * @return CRequired 注解类
     */
    @Override
    public Class<CRequired> getAnnotationClass() {
        return CRequired.class;
    }

}
