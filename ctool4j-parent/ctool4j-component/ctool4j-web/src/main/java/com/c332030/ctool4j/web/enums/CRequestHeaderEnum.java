package com.c332030.ctool4j.web.enums;

import com.c332030.ctool4j.core.interfaces.ICRequestHeader;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: RequestHeaderEnum
 * </p>
 *
 * @author c332030
 * @since 2024/3/21
 */
@Getter
@AllArgsConstructor
public enum CRequestHeaderEnum implements ICRequestHeader {

    AUTHORIZATION("鉴权"),

    ACCEPT_LANGUAGE("语言"),

    ACCEPT("内容格式"),

    ;

    /**
     * 描述
     */
    private final String text;

}
