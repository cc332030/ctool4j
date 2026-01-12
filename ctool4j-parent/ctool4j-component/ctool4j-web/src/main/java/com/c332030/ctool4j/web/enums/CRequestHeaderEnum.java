package com.c332030.ctool4j.web.enums;

import com.c332030.ctool4j.core.enums.CDataTypeEnum;
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

    AUTHORIZATION("鉴权", CDataTypeEnum.STRING, false),

    ACCEPT_LANGUAGE("语言", CDataTypeEnum.STRING, false),

    ACCEPT("内容格式", CDataTypeEnum.STRING, false),

    ;

    /**
     * 描述
     */
    private final String text;

    private final CDataTypeEnum dataType;

    private final boolean required;

}
