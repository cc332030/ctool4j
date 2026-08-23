package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICEnumNameValue
 * </p>
 *
 * @see "doc/design/definition/ICEnumNameValue.adoc"
 * @since 2025/12/23
 */
public interface ICEnumNameValue extends ICEnumName, ICValue<String> {

    /**
     * 获取值（默认返回枚举名称）
     * @return 值
     */
    @Override
    default String getValue() {
        return name();
    }

}
