package com.c332030.ctool4j.doc.openapi2.plugins.parameter;

import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ExpandedParameterBuilderPlugin;

/**
 * <p>
 * Description: ICExpandedParameterBuilderPlugin
 * </p>
 *
 * @since 2025/12/17
 */
public interface ICExpandedParameterBuilderPlugin extends ExpandedParameterBuilderPlugin {

    @Override
    default boolean supports(@NonNull DocumentationType delimiter) {
        return true;
    }

}
