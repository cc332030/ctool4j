package com.c332030.ctool4j.definition.enums.business;

import com.c332030.ctool4j.definition.interfaces.ICValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CCountryCodeEnum
 * </p>
 *
 * @since 2026/3/17
 * @see "doc/design/core/CCountryCodeEnum.adoc"
 * @see "doc/design/core/CCountryCodeEnumTests.adoc"
 */
@Getter
@AllArgsConstructor
public enum CCountryCodeEnum implements ICValue<Integer> {

    CHN(86, "中国"),

    ;

    /**
     * 描述
     */
    final Integer value;

    /**
     * 描述
     */
    final String text;

}
