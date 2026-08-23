package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.openapi2.plugins.parameter.ICAnnotationExpandedParameterBuilderPlugin;

import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * Description: CNotEmptyAnnotationPlugin
 * </p>
 *
 * @see "doc/design/openapi2/CNotEmptyAnnotationPlugin.adoc"
 * @see "doc/design/openapi2/CNotEmptyAnnotationPluginTests.adoc"
 * @since 2025/12/17
 */
public class CNotEmptyAnnotationPlugin implements ICAnnotationExpandedParameterBuilderPlugin<NotEmpty> {

    /**
     * 获取支持的校验注解类型
     *
     * @return NotEmpty 注解类
     */
    @Override
    public Class<NotEmpty> getAnnotationClass() {
        return NotEmpty.class;
    }

}
