package com.c332030.ctool4j.db.test.enums;

import com.c332030.ctool4j.db.enums.CSqlSeparatorEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * <p>
 * Description: CSqlSeparatorEnumTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CSqlSeparatorEnumTests {

    @Test
    public void values() {
        Assertions.assertEquals(3, CSqlSeparatorEnum.values().length);
        Assertions.assertEquals("逗号", CSqlSeparatorEnum.COMMA.getText());
        Assertions.assertEquals("且", CSqlSeparatorEnum.AND.getText());
        Assertions.assertEquals("或", CSqlSeparatorEnum.OR.getText());
    }

    @Test
    public void separator() {
        Assertions.assertEquals(",", CSqlSeparatorEnum.COMMA.getSeparator());
        Assertions.assertEquals("AND", CSqlSeparatorEnum.AND.getSeparator());
        Assertions.assertEquals("OR", CSqlSeparatorEnum.OR.getSeparator());
    }

    @Test
    public void joiningCollector() {
        Assertions.assertEquals(
            "a , b , c",
            Arrays.asList("a", "b", "c").stream()
                .collect(CSqlSeparatorEnum.COMMA.getJoiningCollector())
        );
        Assertions.assertEquals(
            "a AND b",
            Arrays.asList("a", "b").stream()
                .collect(CSqlSeparatorEnum.AND.getJoiningCollector())
        );
        Assertions.assertEquals(
            "a OR b",
            Arrays.asList("a", "b").stream()
                .collect(CSqlSeparatorEnum.OR.getJoiningCollector())
        );
    }

    @Test
    public void joiningCollectorSingle() {
        Assertions.assertEquals(
            "a",
            Collections.singletonList("a").stream()
                .collect(CSqlSeparatorEnum.AND.getJoiningCollector())
        );
    }

    @Test
    public void joiningCollectorEmpty() {
        Assertions.assertEquals(
            "",
            Collections.<String>emptyList().stream()
                .collect(CSqlSeparatorEnum.COMMA.getJoiningCollector())
        );
    }

}
