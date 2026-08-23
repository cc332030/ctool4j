package com.c332030.ctool4j.doc.openapi2.plugins.parameter;

import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ExpandedParameterBuilderPlugin;

/**
 * <p>
 * Description: ICExpandedParameterBuilderPlugin
 * </p>
 *
 * @see "doc/design/openapi2/ICExpandedParameterBuilderPlugin.adoc"
 * @since 2025/12/17
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
