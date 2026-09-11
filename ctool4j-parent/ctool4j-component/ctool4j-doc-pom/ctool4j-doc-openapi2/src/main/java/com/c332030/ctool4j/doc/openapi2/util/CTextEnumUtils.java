package com.c332030.ctool4j.doc.openapi2.util;

import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.definition.interfaces.ICText;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.lang.Nullable;
import springfox.documentation.service.AllowableListValues;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Description: 枚举 text 展示工具：对实现 {@link ICText} 的枚举，允许值保持可提交的「枚举名」，
 * 可读的「枚举名(text)」说明（如 AUTHORIZATION(鉴权)）作为字段/参数描述展示，供 model 属性与请求参数的枚举展示
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CTextEnumUtils} 提供枚举 text 展示工具，供 model 属性与参数枚举文档展示复用：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>非枚举 / 非 ICText</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>空枚举</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>枚举常量 text 为空</td>
 *     <td>描述拼接为「枚举名()」</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>model 属性枚举与请求参数枚举的文档展示（由 CTextEnumModelPropertyPlugin / CTextEnumParameterPlugin 调用）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>允许值保持枚举名，保证文档值可直接用于请求；text 可读性通过 description 提供。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>判断条件</b></p>
 * <ul>
 *   <li>枚举类（{@code type.isEnum()}）且实现了 {@code ICText}（definition 模块接口，{@code getText()} 提供可读文本）。</li>
 * </ul>
 * <p><b>允许值生成</b></p>
 * <ul>
 *   <li>遍历枚举常量，允许值用枚举名 {@code name()}（保证可提交，与运行时传值一致），允许值类型 {@code LIST}。</li>
 *   <li>可读「枚举名(text)」由 {@code textEnumDescription} 单独生成，避免污染可提交值。</li>
 *   <li>非 ICText 枚举或空枚举返回 null（调用方不覆写，保持默认展示）。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/6
 * @version 1.0
 */
@UtilityClass
public class CTextEnumUtils {

    /**
     * 判断枚举类型是否实现了 {@link ICText}
     * <ul>
     *   <li>{@code isTextEnum(Class)}：判断类型是否为枚举且实现了 {@code ICText}。</li>
     * </ul>
     *
     * @param type 类型
     * @return 是否实现 ICText 的枚举
     */
    public boolean isTextEnum(@Nullable Class<?> type) {
        return null != type && type.isEnum() && ICText.class.isAssignableFrom(type);
    }

    /**
     * 生成「枚举名」的允许值列表（可提交，与运行时传值一致）
     * <ul>
     *   <li>{@code enumAllowableValues(Class)}：生成「枚举名」的 {@code AllowableListValues}（如 AUTHORIZATION），值可提交、与运行时一致。</li>
     * </ul>
     *
     * @param enumClass 枚举类型
     * @return 允许值列表（允许值类型 LIST），非法输入返回 null
     */
    @Nullable
    public AllowableListValues enumAllowableValues(@Nullable Class<?> enumClass) {

        if(!isTextEnum(enumClass)) {
            return null;
        }

        val constants = enumClass.getEnumConstants();
        if(constants.length == 0) {
            return null;
        }

        List<String> values = CCollUtils.convert(
            Arrays.asList(constants),
            constant -> ((Enum<?>) constant).name());

        return new AllowableListValues(values, "LIST");
    }

    /**
     * 生成「枚举名(text)」的可读说明（如 AUTHORIZATION(鉴权)），供字段/参数描述展示
     * <ul>
     *   <li>{@code textEnumDescription(Class)}：生成「枚举名(text)」可读说明（如 AUTHORIZATION(鉴权)），供 description 展示。</li>
     * </ul>
     *
     * @param enumClass 枚举类型
     * @return 以「、」连接的说明，非法输入返回 null
     */
    @Nullable
    public String textEnumDescription(@Nullable Class<?> enumClass) {

        if(!isTextEnum(enumClass)) {
            return null;
        }

        val constants = enumClass.getEnumConstants();
        if(constants.length == 0) {
            return null;
        }

        String[] descriptions = CCollUtils.convert(
            Arrays.asList(constants),
            constant -> {
                val name = ((Enum<?>) constant).name();
                val text = ((ICText) constant).getText();
                return name + "(" + (null == text ? "" : text) + ")";
            }).toArray(new String[0]);

        return CStrUtils.concat("、", descriptions);
    }
}
