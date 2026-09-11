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
 * <h2>能力目录</h2>
 * <p>{@code CMimeTypeEnum} 为媒体类型枚举，实现 {@code ICText}，定义 JSON5/XLS/XLSX，各含：</p>
 * <ul>
 *   <li>{@code mimeTypeStr}：媒体类型字符串</li>
 *   <li>{@code mimeType}：MediaType 对象（由字符串解析）</li>
 *   <li>{@code text}：描述</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>统一媒体类型常量（JSON5/XLS/XLSX）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>MediaType 由构造时解析字符串生成。</li>
 * </ul>
 *
 * @since 2025/11/17
 * @version 1.0
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
