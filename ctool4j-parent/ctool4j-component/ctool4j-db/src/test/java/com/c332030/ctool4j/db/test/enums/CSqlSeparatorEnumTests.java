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
 * <p>
 * 是 {@link CSqlSeparatorEnum} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>分别验证三个枚举项的 {@code text}（描述）、{@code separator}（分隔符）、{@code joiningCollector}（拼接行为）。</li>
 *   <li>joiningCollector 覆盖多元素、单元素、空集合三种集合形态（对应 {@code Collectors.joining} 的分隔符连接、</li>
 *   <li>单元素原样返回、空集合空串语义）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对三个枚举项及其字段语义（separator/text/joiningCollector）的约定。</li>
 *   <li>依据黑盒原则：枚举数量、各字段取值、拼接结果（多/单/空）均取代表性值覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：枚举数量、三个枚举项的描述/分隔符/拼接 collector（多元素、单元素、空集合）。</li>
 *   <li>未覆盖：新增枚举项（本类固定 3 项）；非此三种分隔符的拼接（本枚举不提供）。</li>
 * </ul>
 * <h2>枚举定义</h2>
 * <ul>
 *   <li>1.1 枚举数量与描述：3 个枚举项，text 分别为"逗号/且/或"（values）</li>
 *   <li>1.2 分隔符：COMMA={@code ,}、AND={@code AND}、OR={@code OR}（separator）</li>
 * </ul>
 * <h2>joiningCollector 拼接行为</h2>
 * <ul>
 *   <li>2.1 多元素拼接：{@code a , b , c}（COMMA）、{@code a AND b}（AND）、{@code a OR b}（OR）（joiningCollector）</li>
 *   <li>2.2 单元素：原样返回 {@code a}（joiningCollectorSingle）</li>
 *   <li>2.3 空集合：返回空串 {@code ""}（joiningCollectorEmpty）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CSqlSeparatorEnumTests {

    /**
     * 对应测试用例 1.1：枚举数量与描述
     */
    @Test
    public void values() {
        Assertions.assertEquals(3, CSqlSeparatorEnum.values().length);
        Assertions.assertEquals("逗号", CSqlSeparatorEnum.COMMA.getText());
        Assertions.assertEquals("且", CSqlSeparatorEnum.AND.getText());
        Assertions.assertEquals("或", CSqlSeparatorEnum.OR.getText());
    }

    /**
     * 对应测试用例 1.2：分隔符取值
     */
    @Test
    public void separator() {
        Assertions.assertEquals(",", CSqlSeparatorEnum.COMMA.getSeparator());
        Assertions.assertEquals("AND", CSqlSeparatorEnum.AND.getSeparator());
        Assertions.assertEquals("OR", CSqlSeparatorEnum.OR.getSeparator());
    }

    /**
     * 对应测试用例 2.1：多元素拼接
     */
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

    /**
     * 对应测试用例 2.2：单元素拼接
     */
    @Test
    public void joiningCollectorSingle() {
        Assertions.assertEquals(
            "a",
            Collections.singletonList("a").stream()
                .collect(CSqlSeparatorEnum.AND.getJoiningCollector())
        );
    }

    /**
     * 对应测试用例 2.3：空集合拼接
     */
    @Test
    public void joiningCollectorEmpty() {
        Assertions.assertEquals(
            "",
            Collections.<String>emptyList().stream()
                .collect(CSqlSeparatorEnum.COMMA.getJoiningCollector())
        );
    }

}
