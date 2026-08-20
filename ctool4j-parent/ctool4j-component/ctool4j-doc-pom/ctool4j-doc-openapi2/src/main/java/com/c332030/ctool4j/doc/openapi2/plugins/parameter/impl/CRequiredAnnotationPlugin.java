package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.openapi2.plugins.parameter.ICAnnotationExpandedParameterBuilderPlugin;
import com.c332030.ctool4j.web.validation.annotation.CRequired;

/**
 * <p>
 * Description: CRequiredAnnotationPlugin：识别 @CRequired 注解，将参数标记为必填
 * </p>
 *
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
