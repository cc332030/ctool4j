package com.c332030.ctool4j.doc.openapi2.util;

import com.c332030.ctool4j.definition.interfaces.ICText;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.lang.Nullable;
import springfox.documentation.service.AllowableListValues;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: 枚举 text 展示工具：对实现 {@link ICText} 的枚举，生成「枚举名(text)」的允许值列表，
 * 使接口文档中的枚举值可读（如 AUTHORIZATION(鉴权)），用于 model 属性与请求参数的枚举展示
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
     * 生成「枚举名(text)」的允许值列表
     *
     * @param enumClass 枚举类型
     * @return 允许值列表（允许值类型 LIST），非法输入返回 null
     */
    @Nullable
    public AllowableListValues textEnumAllowableValues(@Nullable Class<?> enumClass) {

        if(!isTextEnum(enumClass)) {
            return null;
        }

        val constants = enumClass.getEnumConstants();
        if(constants.length == 0) {
            return null;
        }

        List<String> values = Arrays.stream(constants)
            .map(constant -> {
                val name = ((Enum<?>) constant).name();
                val text = ((ICText) constant).getText();
                return name + "(" + text + ")";
            })
            .collect(Collectors.toList());

        return new AllowableListValues(values, "LIST");
    }
}
