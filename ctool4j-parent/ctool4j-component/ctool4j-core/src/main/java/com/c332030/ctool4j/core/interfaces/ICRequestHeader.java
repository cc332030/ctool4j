package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.core.enums.CDataTypeEnum;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.definition.interfaces.ICEnumName;
import com.c332030.ctool4j.definition.interfaces.ICText;

/**
 * <p>
 * Description: ICRequestHeader
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICRequestHeader} 为请求报文头定义接口，继承 {@code ICText}（描述）与 {@code ICEnumName}（枚举名），提供默认行为：</p>
 * <ul>
 *   <li>{@code getDataType()}：数据类型，默认 {@code CDataTypeEnum.STRING}</li>
 *   <li>{@code isRequired()}：是否必输，默认 false</li>
 *   <li>{@code getHeaderName()}：报文头名，默认 {@code CStrUtils.upperUnderscoreToHeaderName(name())}（大写下划线转 Header 风格）</li>
 * </ul>
 * <p>实现类可覆盖 dataType/required。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未覆盖 getDataType</td>
 *     <td>返回 STRING</td>
 *   </tr>
 *   <tr>
 *     <td>未覆盖 isRequired</td>
 *     <td>返回 false</td>
 *   </tr>
 *   <tr>
 *     <td>未覆盖 getHeaderName</td>
 *     <td>由 name() 推导 Header 名</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>定义请求报文头枚举，统一描述、数据类型、必输、Header 名。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>依赖 {@code name()} 推导 Header 名，枚举名需为大写下划线风格。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>默认 STRING 非必输，覆盖需要时再定制，减少重复声明。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>报文头名推导</b></p>
 * <ul>
 *   <li>基于枚举 {@code name()}（如 {@code TRACE_ID}）经 {@code upperUnderscoreToHeaderName} 转 {@code Trace-Id}、{@code X_TOKEN} → {@code X-Token}。</li>
 * </ul>
 * <p><b>默认语义</b></p>
 * <ul>
 *   <li>数据类型默认 STRING、必输默认 false，多数报文头直接继承默认值，仅需覆盖有特殊需求的字段。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/3/21
 * @version 1.0
 */
public interface ICRequestHeader extends ICText, ICEnumName {

    /**
     * 数据类型
     * @return 数据类型
     */
    default CDataTypeEnum getDataType() {
        return CDataTypeEnum.STRING;
    }

    /**
     * 是否必输
     * @return 是否必输
     */
    default boolean isRequired() {
        return false;
    }

    /**
     * 报文头名
     * @return 报文头名
     */
    default String getHeaderName() {
        return CStrUtils.upperUnderscoreToHeaderName(name());
    }

}
