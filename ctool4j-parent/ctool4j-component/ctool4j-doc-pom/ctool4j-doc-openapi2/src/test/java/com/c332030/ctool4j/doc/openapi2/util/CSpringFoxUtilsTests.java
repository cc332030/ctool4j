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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖 Docket 构建（类型为 SWAGGER_2）。</li>
 *   <li>覆盖单个请求头转换（必填/非必填、数据类型小写化）。</li>
 *   <li>覆盖全局参数列表：多元素、空集合、null 集合、过滤 null 元素。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"SWAGGER_2、header 参数、过滤 null"的约定。</li>
 *   <li>依据白盒/黑盒原则与错误推测法：必填/非必填、类型大小写、空/null/含 null 元素均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：Docket 构建、单个参数转换（2 种请求头形态）、全局列表多形态。</li>
 *   <li>未覆盖：{@code getHeaderParameter} 传入空/异常请求头定义（枚举实例）之外的自定义实现。</li>
 * </ul>
 * <h2>Docket 构建</h2>
 * <ul>
 *   <li>1.1 返回 SWAGGER_2 类型 Docket（getDocketBuilder）</li>
 * </ul>
 * <h2>单个请求头转换</h2>
 * <ul>
 *   <li>2.1 必填 string 类型参数（getHeaderParameter）</li>
 *   <li>2.2 非必填 long 类型参数（getHeaderParameter_lowerCaseDataType）</li>
 *   <li>2.3 null 入参抛 NPE（getHeaderParameter_null）</li>
 * </ul>
 * <h2>全局参数列表</h2>
 * <ul>
 *   <li>3.1 多元素列表（globalParameterList）</li>
 *   <li>3.2 空列表返回空（globalParameterList_empty）</li>
 *   <li>3.3 null 集合返回空（globalParameterList_null）</li>
 *   <li>3.4 过滤 null 元素（globalParameterList_filterNullElement）</li>
 * </ul>
 *
 * <p>
 * 是 {@link CSpringFoxUtils} 的测试用例。
 * </p>
 *
 * @since 2026/8/14
 * @version 1.0
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

    /**
     * <p>
     * 对应测试用例 1.1：返回 SWAGGER_2 类型 Docket
     */
    @Test
    void getDocketBuilder() {
        Docket docket = CSpringFoxUtils.getDocketBuilder();
        Assertions.assertNotNull(docket);
        Assertions.assertEquals(DocumentationType.SWAGGER_2, docket.getDocumentationType());
    }

    /**
     * <p>
     * 对应测试用例 2.1：必填 string 类型参数
     */
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

    /**
     * <p>
     * 对应测试用例 2.2：非必填 long 类型参数
     */
    @Test
    void getHeaderParameter_lowerCaseDataType() {
        Parameter parameter = CSpringFoxUtils.getHeaderParameter(Header.X_TRACE_ID);
        Assertions.assertNotNull(parameter);
        Assertions.assertEquals("X-Trace-Id", parameter.getName());
        Assertions.assertEquals("long", parameter.getModelRef().getType());
        Assertions.assertFalse(parameter.isRequired());
    }

    /**
     * <p>
     * 对应测试用例 2.3：null 入参抛 NPE
     */
    @Test
    void getHeaderParameter_null() {
        Assertions.assertThrowsExactly(NullPointerException.class,
                () -> CSpringFoxUtils.getHeaderParameter(null));
    }

    /**
     * <p>
     * 对应测试用例 3.1：多元素列表
     */
    @Test
    void globalParameterList() {
        List<Parameter> parameters = CSpringFoxUtils.globalParameterList(
                Arrays.asList(Header.AUTHORIZATION, Header.X_TRACE_ID));
        Assertions.assertNotNull(parameters);
        Assertions.assertEquals(2, parameters.size());
        Assertions.assertEquals("Authorization", parameters.get(0).getName());
        Assertions.assertEquals("X-Trace-Id", parameters.get(1).getName());
    }

    /**
     * <p>
     * 对应测试用例 3.2：空列表返回空
     */
    @Test
    void globalParameterList_empty() {
        List<Parameter> parameters = CSpringFoxUtils.globalParameterList(Collections.emptyList());
        Assertions.assertNotNull(parameters);
        Assertions.assertTrue(parameters.isEmpty());
    }

    /**
     * <p>
     * 对应测试用例 3.3：null 集合返回空
     */
    @Test
    void globalParameterList_null() {
        List<Parameter> parameters = CSpringFoxUtils.globalParameterList(null);
        Assertions.assertNotNull(parameters);
        Assertions.assertTrue(parameters.isEmpty());
    }

    /**
     * <p>
     * 对应测试用例 3.4：过滤 null 元素
     */
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
