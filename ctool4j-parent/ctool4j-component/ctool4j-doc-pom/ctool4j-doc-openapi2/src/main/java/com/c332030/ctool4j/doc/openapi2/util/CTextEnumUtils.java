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
 * @author c332030
 * @see "doc/design/openapi2/CTextEnumUtils.adoc"
 * @since 2026/9/6
 */
@UtilityClass
public class CTextEnumUtils {

    /**
     * 判断枚举类型是否实现了 {@link ICText}
     *
     * @param type 类型
     * @return 是否实现 ICText 的枚举
     */
    public boolean isTextEnum(@Nullable Class<?> type) {
        return null != type && type.isEnum() && ICText.class.isAssignableFrom(type);
    }

    /**
     * 生成「枚举名」的允许值列表（可提交，与运行时传值一致）
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
