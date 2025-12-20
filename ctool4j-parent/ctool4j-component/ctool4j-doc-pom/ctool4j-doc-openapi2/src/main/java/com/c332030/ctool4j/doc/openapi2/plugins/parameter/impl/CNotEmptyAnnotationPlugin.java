package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.openapi2.plugins.parameter.ICAnnotationExpandedParameterBuilderPlugin;

import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * Description: CNotEmptyAnnotationPlugin
 * </p>
 *
 * @since 2025/12/17
 */
public class CNotEmptyAnnotationPlugin implements ICAnnotationExpandedParameterBuilderPlugin<NotEmpty> {

    @Override
    public Class<NotEmpty> getAnnotationClass() {
        return NotEmpty.class;
    }

}
