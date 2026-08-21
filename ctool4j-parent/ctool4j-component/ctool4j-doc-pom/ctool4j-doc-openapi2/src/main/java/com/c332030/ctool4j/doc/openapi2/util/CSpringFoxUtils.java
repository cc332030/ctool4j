package com.c332030.ctool4j.doc.openapi2.util;

import com.c332030.ctool4j.core.interfaces.ICRequestHeader;
import com.c332030.ctool4j.core.util.CCollUtils;
import lombok.experimental.UtilityClass;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CSpringFoxUtils
 * </p>
 *
 * @author c332030
 * @see doc/design/openapi2/CSpringFoxUtils.adoc
 * @see doc/design/openapi2/CSpringFoxUtilsTests.adoc
 * @since 2024/8/27
 */
@UtilityClass
public class CSpringFoxUtils {

    /**
     * 获取 Docket 构建器
     *
     * @return Docket
     */
    public Docket getDocketBuilder() {
        return new Docket(DocumentationType.SWAGGER_2)
                .forCodeGeneration(true);
    }

    /**
     * 构建全局参数列表（过滤 null）
     *
     * @param headers 请求头定义集合
     * @return 参数列表
     */
    public List<Parameter> globalParameterList(Collection<? extends ICRequestHeader> headers) {

        headers = CCollUtils.filterNull(headers);
        return headers.stream()
                .map(CSpringFoxUtils::getHeaderParameter)
                .collect(Collectors.toList());
    }

    /**
     * 请求头定义转 Swagger 参数
     *
     * @param requestHeader 请求头定义
     * @return 参数
     */
    public Parameter getHeaderParameter(ICRequestHeader requestHeader) {
        return new ParameterBuilder().name(requestHeader.getHeaderName())
                .modelRef(new ModelRef(requestHeader.getDataType().getLowerCase()))
                .required(requestHeader.isRequired())
                .parameterType("header")
                .description(requestHeader.getText())
                .build();
    }

}
