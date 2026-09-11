package com.c332030.ctool4j.core.enums;

import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.definition.interfaces.ICText;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * <p>
 * Description: CDataTypeEnum
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CDataTypeEnum} 为数据类型枚举，实现 {@code ICText}，定义 13 种数据类型（INT/LONG/FLOAT/DOUBLE/BOOLEAN/ STRING/DATE/TIME/DATETIME/TIMESTAMP/ENUM/OPTION/MULTI_OPTION），每种带中文描述。</p>
 * <ul>
 *   <li>{@code getText()}：类型描述</li>
 *   <li>{@code DATE_TYPES}：日期相关类型集合（DATE/TIME/DATETIME/TIMESTAMP）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>判断是否日期类型</td>
 *     <td>用 {@code DATE_TYPES.contains}</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>数据类型的标准化枚举（字段类型映射、校验）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>getLowerCase 依赖枚举名；新增枚举需同步维护 DATE_TYPES。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>描述为中文文本，作为展示/传输语义约定。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>日期类型集合</b></p>
 * <ul>
 *   <li>{@code DATE_TYPES} 为不可变 Set（{@code CSet.of}），仅含日期相关类型，便于统一判断"是否日期类型"。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/3/21
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum CDataTypeEnum implements ICText {

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
     * 日期相关类型集合
     */
    public static final Set<CDataTypeEnum> DATE_TYPES = CSet.of(
        DATE,
        TIME,
        DATETIME,
        TIMESTAMP
    );

    /**
     * 描述
     */
    private final String text;

    /**
     * 获取枚举名的小写形式
     *
     * @return 枚举名的小写形式
     */
    public String getLowerCase() {
        return name().toLowerCase();
    }

}
