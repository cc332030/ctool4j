package com.c332030.ctool4j.definition.enums;

import com.c332030.ctool4j.definition.interfaces.IText;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.MediaType;

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

    JSON5("application/json5", "json5"),

    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel"),

    ;

    /**
     * 描述
     */
    final String mimeTypeStr;

    /**
     * 描述
     */
    final MediaType mimeType;

    /**
     * 描述
     */
    final String text;

    CMimeTypeEnum(String mimeTypeStr, String text) {
        this(mimeTypeStr, MediaType.parseMediaType(mimeTypeStr), text);
    }

}
