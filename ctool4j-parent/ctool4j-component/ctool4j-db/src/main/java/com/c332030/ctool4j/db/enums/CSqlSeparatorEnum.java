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
 * <h2>能力目录</h2>
 * <p>{@code CSqlSeparatorEnum} 为 SQL 片段拼接分隔符枚举，定义三种分隔符：</p>
 * <ul>
 *   <li>{@code COMMA}：{@code ,}（逗号，文本"逗号"），用于字段/值列表</li>
 *   <li>{@code AND}：{@code AND}（文本"且"），用于多条件"且"连接</li>
 *   <li>{@code OR}：{@code OR}（文本"或"），用于多条件"或"连接</li>
 * </ul>
 * <p>每个枚举项同时携带一个 {@code joiningCollector}（{@code Collectors.joining(" &lt;分隔符&gt; ")}）， 可直接用于 {@code Stream.collect} 将多个 SQL 片段按对应分隔符拼接。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>空集合 collect</td>
 *     <td>返回空串 {@code ""}</td>
 *   </tr>
 *   <tr>
 *     <td>单元素集合 collect</td>
 *     <td>原样返回该元素</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>配合 {@code CSqlUtils} 等方法，用于生成以逗号/AND/OR 连接的 SQL 片段。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>分隔符仅固定为逗号/AND/OR；其他分隔符场景不适用。</li>
 *   <li>拼接结果带固定前后空格（{@code  a , b } 风格），对拼接结果有精确空格要求的场景需注意。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>枚举值固定不可扩展，新增分隔符需修改枚举本身。</li>
 *   <li>{@code text} 仅为描述性文案，不参与 SQL 生成逻辑。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>{@code separator} 为原始分隔符串（{@code ,}、{@code AND}、{@code OR}）；{@code text} 为中文描述。</li>
 *   <li>{@code joiningCollector} 采用 {@code Collectors.joining(" " + separator + " ")}，即片段间以</li>
 *   <li>{@code " 分隔符 "}（前后各一空格）连接；单元素/空集合时与 {@code Collectors.joining} 行为一致</li>
 *   <li>（单元素原样返回、空集合返回空串）。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>lombok {@code @Getter} + {@code @AllArgsConstructor} 生成 getter 与全参构造器。</li>
 *   <li>显式声明带 3 参的自定义构造器（构造 {@code joiningCollector}）供三个枚举项调用。</li>
 * </ul>
 *
 * @since 2025/11/11
 * @version 1.0
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
