package com.c332030.ctool4j.definition.enums;

import com.c332030.ctool4j.definition.interfaces.ICText;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.MediaType;

/**
 * <p>
 * Description: CMimeTypeEnum
 * </p>
 *
 * @since 2025/11/17
 * @see "doc/design/core/CMimeTypeEnum.adoc"
 * @see "doc/design/core/CMimeTypeEnumTests.adoc"
 */
@Getter
@AllArgsConstructor
public enum CMimeTypeEnum implements ICText {

    JSON5("application/json5", "json5"),

    XLS("application/vnd.ms-excel", "Excel xls"),

    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel xlsx"),

    ;

    /**
     * MIME 类型字符串（如 application/json5）
     */
    final String mimeTypeStr;

    /**
     * MIME 类型对应的 Spring MediaType
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
