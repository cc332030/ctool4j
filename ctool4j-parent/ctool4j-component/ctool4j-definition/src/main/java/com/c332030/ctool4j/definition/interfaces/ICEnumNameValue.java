package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICEnumNameValue
 * </p>
 *
 * @since 2025/12/23
 */
public interface ICEnumNameValue extends IEnumName, IValue<String> {

    @Override
    default String getValue() {
        return name();
    }

}
