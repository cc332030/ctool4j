package com.c332030.ctool4j.definition.enums;

import com.c332030.ctool4j.definition.interfaces.IText;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: CMimeTypeEnum
 * </p>
 *
 * @since 2025/11/17
 */
@Getter
@AllArgsConstructor
public enum CMimeTypeEnum implements IText {

    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel"),

    ;

    /**
     * 描述
     */
    final String mimeType;

    /**
     * 描述
     */
    final String text;

}
