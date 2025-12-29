package com.c332030.ctool4j.definition.enums;

import com.c332030.ctool4j.definition.interfaces.IText;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Description: DataTypeEnum
 * </p>
 *
 * @author c332030
 * @since 2024/3/21
 */
@Getter
@AllArgsConstructor
public enum CDataTypeEnum implements IText {

    INT("整形"),

    LONG("长整形"),

    FLOAT("浮点型"),

    DOUBLE("双精度浮点型"),

    BOOLEAN("布尔"),

    STRING("字符串"),

    DATE("日期"),

    TIME("时间"),

    DATETIME("日期时间"),

    TIMESTAMP("时间戳"),

    ENUM("枚举"),

    OPTION("选项"),

    MULTI_OPTION("选项-多选"),

    ;

    /**
     * 描述
     */
    private final String text;

    public String getLowerCase() {
        return name().toLowerCase();
    }

}
