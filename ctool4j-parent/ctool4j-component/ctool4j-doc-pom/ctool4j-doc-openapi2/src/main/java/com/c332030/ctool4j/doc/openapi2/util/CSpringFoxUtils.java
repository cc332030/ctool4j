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
 * <h2>能力目录</h2>
 * <p>{@code CSpringFoxUtils}（{@code @UtilityClass}）提供 springfox（OpenAPI2）的工具能力：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>globalParameterList 入参 null</td>
 *     <td>返回空列表</td>
 *   </tr>
 *   <tr>
 *     <td>集合含 null 元素</td>
 *     <td>过滤 null 元素</td>
 *   </tr>
 *   <tr>
 *     <td>getHeaderParameter 入参 null</td>
 *     <td>抛 NPE（访问 requestHeader.getHeaderName()）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>springfox Docket 构建、全局请求头参数配置。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>基于 springfox 的 OpenAPI2，不适用于 OpenAPI3。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>getHeaderParameter 对 null 入参不防御，抛 NPE。</li>
 *   <li>依赖 springfox 的 Parameter/ModelRef 类型。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>Docket 构建</b></p>
 * <ul>
 *   <li>{@code new Docket(DocumentationType.SWAGGER_2).forCodeGeneration(true)}。</li>
 * </ul>
 * <p><b>参数转换</b></p>
 * <ul>
 *   <li>请求头定义的 {@code headerName}、{@code dataType.getLowerCase()}、{@code isRequired()}、{@code text} 映射到 Parameter 的 name/modelRef/required/description。</li>
 *   <li>{@code parameterType("header")}。</li>
 *   <li>{@code globalParameterList} 先过滤 null 元素。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/8/27
 * @version 1.0
 */
@UtilityClass
public class CSpringFoxUtils {

    /**
     * 获取 Docket 构建器
     * <ul>
     *   <li>{@code getDocketBuilder()}：构建 {@code Docket}（SWAGGER_2）。</li>
     * </ul>
     *
     * @return Docket
     */
    public Docket getDocketBuilder() {
        return new Docket(DocumentationType.SWAGGER_2)
                .forCodeGeneration(true);
    }

    /**
     * 构建全局参数列表（过滤 null）
     * <ul>
     *   <li>{@code globalParameterList(Collection&lt;ICRequestHeader&gt;)}：请求头定义集合转 Swagger 全局参数列表（过滤 null 元素）。</li>
     * </ul>
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
     * <ul>
     *   <li>{@code getHeaderParameter(ICRequestHeader)}：单个请求头定义转 Swagger {@code Parameter}。</li>
     * </ul>
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
