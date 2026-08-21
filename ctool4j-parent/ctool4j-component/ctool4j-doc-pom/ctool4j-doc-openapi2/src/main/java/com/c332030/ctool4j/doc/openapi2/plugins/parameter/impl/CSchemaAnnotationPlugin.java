package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.openapi2.plugins.parameter.ICAnnotationExpandedParameterBuilderPlugin;
import com.c332030.ctool4j.web.validation.annotation.CSchema;

import java.lang.annotation.Annotation;

/**
 * <p>
 * Description: CSchemaAnnotationPlugin：识别 @CSchema 注解，在 required=true 时将参数标记为必填
 * </p>
 *
 * @see "doc/design/openapi2/CSchemaAnnotationPlugin.adoc"
 * @author c332030
 */
public class CSchemaAnnotationPlugin implements ICAnnotationExpandedParameterBuilderPlugin<CSchema> {

    /**
     * 获取支持的校验注解类型
     *
     * @return CSchema 注解类
     */
    @Override
    public Class<CSchema> getAnnotationClass() {
        return CSchema.class;
    }

    /**
     * 按注解的 required 属性决定是否必填
     *
     * @param annotation 命中的注解
     * @return 是否必填
     */
    @Override
    public boolean isRequired(Annotation annotation) {
        return ((CSchema) annotation).required();
    }

}
