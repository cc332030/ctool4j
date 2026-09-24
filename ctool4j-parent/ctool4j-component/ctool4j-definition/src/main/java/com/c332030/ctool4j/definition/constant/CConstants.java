package com.c332030.ctool4j.definition.constant;

import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CConstants
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CConstants} 为跨模块通用的<b>字面量常量</b>：只描述字符本身，不绑定某一处的业务语义。</p>
 * <ul>
 *   <li>{@code STAR}：半角星号，表示星号这一字符——既可作为"全部/任意"的通配取值
 *   （如跨域允许全部来源、方法或头），也可作为普通字符出现在任意语句中</li>
 *   <li>{@code COMMA}：半角逗号，列表型取值的分隔符（如 {@code 1,2,3}）；其他场景按所在语句的语义使用</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>常量只按<b>字符形态</b>定名（星号、逗号），不按使用场景定名：同一个字符在不同场景含义不同
 *   （通配、乘号、占位等），按场景定名会让同一取值分裂成多个别名，改取值时必漏其余。</li>
 *   <li>{@code @UtilityClass}（lombok）：生成私有构造器，声明字段时无需再写 {@code static}；
 *   纯常量类，不含任何逻辑。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>常量为编译期字面量，无运行时兜底。</li>
 *   <li>常量为非空字面量，不承载"无值"语义——调用方按自身契约处理未配置项。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>放：跨模块通用的字面量常量。</li>
 *   <li>不放：项目自身的标识类常量（如基础包名），由 {@link CTool4jConstants} 承载；
 *   某一模块专有的常量留在该模块内，不上收至此。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>常量值以编译期字面量内联到调用方，修改后调用方须重新编译方可生效。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/23
 * @version 1.1
 */
@UtilityClass
public class CConstants {

    /**
     * 半角星号字符
     * <p>跨域场景中作为"允许全部来源、方法或头"的通配取值；其他场景按所在语句的语义使用。</p>
     */
    public final String STAR = "*";

    /**
     * 半角逗号
     * <p>列表型取值的分隔符（如 {@code 1,2,3}）；其他场景按所在语句的语义使用。</p>
     */
    public final String COMMA = ",";

}
