package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.openapi2.plugins.parameter.ICAnnotationExpandedParameterBuilderPlugin;
import com.c332030.ctool4j.web.validation.annotation.CRequired;

/**
 * <p>
 * Description: CRequiredAnnotationPlugin：识别 @CRequired 注解，标注即必填（标记参数必填）
 * </p>
 *
 * @see "doc/design/openapi2/CRequiredAnnotationPlugin.adoc"
 * @author c332030
 */
public class CRequiredAnnotationPlugin implements ICAnnotationExpandedParameterBuilderPlugin<CRequired> {

    /**
     * 获取支持的校验注解类型
     *
     * @return CRequired 注解类
     */
    @Override
    public Class<CRequired> getAnnotationClass() {
        return CRequired.class;
    }

}
