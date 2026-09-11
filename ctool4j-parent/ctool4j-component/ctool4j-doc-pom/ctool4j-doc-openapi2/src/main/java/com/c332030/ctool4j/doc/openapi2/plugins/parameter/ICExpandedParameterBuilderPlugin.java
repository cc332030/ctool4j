package com.c332030.ctool4j.doc.openapi2.plugins.parameter;

import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ExpandedParameterBuilderPlugin;

/**
 * <p>
 * Description: ICExpandedParameterBuilderPlugin
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
 *     <td>任意文档类型/null</td>
 *     <td>默认支持</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>作为项目内 springfox 参数展开插件的统一父接口。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>默认全支持，需要特定类型才支持时由子类覆写。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>supports</b></p>
 * <ul>
 *   <li>默认 {@code supports(DocumentationType)} 恒返回 true（全部支持，含 null）。</li>
 * </ul>
 *
 * @since 2025/12/17
 * @version 1.0
 */
public interface ICExpandedParameterBuilderPlugin extends ExpandedParameterBuilderPlugin {

    /**
     * 是否支持指定文档类型（默认全部支持）
     * @param delimiter 文档类型
     * @return 是否支持
     */
    @Override
    default boolean supports(@NonNull DocumentationType delimiter) {
        return true;
    }

}
