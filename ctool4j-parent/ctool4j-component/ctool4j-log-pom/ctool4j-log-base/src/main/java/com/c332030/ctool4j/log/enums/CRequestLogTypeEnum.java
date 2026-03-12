package com.c332030.ctool4j.log.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CRequestLogTypeEnum
 * </p>
 *
 * @since 2025/12/20
 */
@Getter
@AllArgsConstructor
public enum CRequestLogTypeEnum {

    AOP("AOP"),

    ADVICE("Advice"),

    INTERCEPTOR("拦截器"),

    ;

    /**
     * 描述
     */
    final String text;

}
