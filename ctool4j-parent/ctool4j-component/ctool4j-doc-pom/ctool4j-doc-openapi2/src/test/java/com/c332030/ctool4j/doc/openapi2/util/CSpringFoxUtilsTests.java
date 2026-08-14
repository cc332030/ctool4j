package com.c332030.ctool4j.doc.openapi2.util;

import com.c332030.ctool4j.core.enums.CDataTypeEnum;
import com.c332030.ctool4j.core.interfaces.ICRequestHeader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CSpringFoxUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
class CSpringFoxUtilsTests {

    @Getter
    @AllArgsConstructor
    private enum Header implements ICRequestHeader {

        AUTHORIZATION(CDataTypeEnum.STRING, true, "鉴权"),
        X_TRACE_ID(CDataTypeEnum.LONG, false, "链路追踪ID"),
        ;

        private final CDataTypeEnum dataType;
        private final boolean required;
        private final String text;
    }

    @Test
    void getDocketBuilder() {
        Docket docket = CSpringFoxUtils.getDocketBuilder();
        Assertions.assertNotNull(docket);
        Assertions.assertEquals(DocumentationType.SWAGGER_2, docket.getDocumentationType());
    }

    @Test
    void getHeaderParameter() {
        Parameter parameter = CSpringFoxUtils.getHeaderParameter(Header.AUTHORIZATION);
        Assertions.assertNotNull(parameter);
        Assertions.assertEquals("Authorization", parameter.getName());
        Assertions.assertEquals("string", parameter.getModelRef().getType());
        Assertions.assertTrue(parameter.isRequired());
        Assertions.assertEquals("header", parameter.getParamType());
        Assertions.assertEquals("鉴权", parameter.getDescription());
    }

    @Test
    void getHeaderParameter_lowerCaseDataType() {
        Parameter parameter = CSpringFoxUtils.getHeaderParameter(Header.X_TRACE_ID);
        Assertions.assertNotNull(parameter);
        Assertions.assertEquals("X-Trace-Id", parameter.getName());
        Assertions.assertEquals("long", parameter.getModelRef().getType());
        Assertions.assertFalse(parameter.isRequired());
    }

    @Test
    void getHeaderParameter_null() {
        Assertions.assertThrowsExactly(NullPointerException.class,
                () -> CSpringFoxUtils.getHeaderParameter(null));
    }

    @Test
    void globalParameterList() {
        List<Parameter> parameters = CSpringFoxUtils.globalParameterList(
                Arrays.asList(Header.AUTHORIZATION, Header.X_TRACE_ID));
        Assertions.assertNotNull(parameters);
        Assertions.assertEquals(2, parameters.size());
        Assertions.assertEquals("Authorization", parameters.get(0).getName());
        Assertions.assertEquals("X-Trace-Id", parameters.get(1).getName());
    }

    @Test
    void globalParameterList_empty() {
        List<Parameter> parameters = CSpringFoxUtils.globalParameterList(Collections.emptyList());
        Assertions.assertNotNull(parameters);
        Assertions.assertTrue(parameters.isEmpty());
    }

    @Test
    void globalParameterList_null() {
        List<Parameter> parameters = CSpringFoxUtils.globalParameterList(null);
        Assertions.assertNotNull(parameters);
        Assertions.assertTrue(parameters.isEmpty());
    }

    @Test
    void globalParameterList_filterNullElement() {
        List<Parameter> parameters = CSpringFoxUtils.globalParameterList(
                Arrays.asList(Header.AUTHORIZATION, null, Header.X_TRACE_ID));
        Assertions.assertNotNull(parameters);
        Assertions.assertEquals(2, parameters.size());
        Assertions.assertEquals("Authorization", parameters.get(0).getName());
        Assertions.assertEquals("X-Trace-Id", parameters.get(1).getName());
    }

}
