package com.c332030.ctool4j.doc.openapi2.util;

import com.c332030.ctool4j.core.interfaces.ICRequestHeader;
import com.c332030.ctool4j.core.util.CCollUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CSpringFoxUtils
 * </p>
 *
 * @author c332030
 * @since 2024/8/27
 */
@UtilityClass
public class CSpringFoxUtils {

    public Docket getDocketBuilder() {
        return new Docket(DocumentationType.SWAGGER_2)
                .forCodeGeneration(true);
    }

    public Docket getDocket(
            String groupName, Collection<ICRequestHeader> headers,
            Predicate<RequestHandler> apiSelector,
            Predicate<String> pathSelector
    ) {

        val builder = getDocketBuilder()
                .groupName(groupName)
                .globalOperationParameters(globalParameterList(headers))
                .select();

        Optional.ofNullable(apiSelector).ifPresent(builder::apis);
        Optional.ofNullable(pathSelector).ifPresent(builder::paths);

        return builder.build();
    }

    public List<Parameter> globalParameterList(Collection<ICRequestHeader> headers) {

        headers = CCollUtils.filterNull(headers);
        return headers.stream()
                .map(CSpringFoxUtils::getHeaderParameter)
                .collect(Collectors.toList());
    }

    public Parameter getHeaderParameter(ICRequestHeader requestHeader) {
        return new ParameterBuilder().name(requestHeader.getHeaderName())
                .modelRef(new ModelRef(requestHeader.getDataType().getLowerCase()))
                .required(requestHeader.isRequired())
                .parameterType("header")
                .description(requestHeader.getText())
                .build();
    }

}
