package com.c332030.ctool4j.db.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CSqlSeparatorEnum
 * </p>
 *
 * @see "doc/design/db/CSqlSeparatorEnum.adoc"
 * @see "doc/design/db/CSqlSeparatorEnumTests.adoc"
 * @since 2025/11/11
 */
@Getter
@AllArgsConstructor
public enum CSqlSeparatorEnum {

    COMMA(",", "逗号"),

    AND("AND", "且"),

    OR("OR", "或"),

    ;

    /**
     * 分隔符
     */
    final String separator;

    /**
     * 描述
     */
    final String text;

    /**
     * Joining Collector
     */
    final Collector<CharSequence, ?, String> joiningCollector;

    CSqlSeparatorEnum(String separator, String text) {
        this(separator, text, Collectors.joining(" " + separator + " "));
    }

}
