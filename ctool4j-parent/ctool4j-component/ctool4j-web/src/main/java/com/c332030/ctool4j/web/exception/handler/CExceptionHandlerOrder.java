package com.c332030.ctool4j.web.exception.handler;

import lombok.experimental.UtilityClass;
import org.springframework.core.Ordered;

/**
 * <p>
 * Description: CExceptionHandlerOrder
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CExceptionHandlerOrder}（{@code @UtilityClass}）集中定义内置异常处理器的 {@code @Order} 档位，供各处理器 {@code @Order(...)} 引用：</p>
 * <ul>
 *   <li>{@code CONCRETE}：具体异常类型处理器（{@code CCBusinessExceptionHandler} 等）。</li>
 *   <li>{@code C_EXCEPTION_FALLBACK}：{@code CException} 兜底（{@code CCExceptionHandler}）。</li>
 *   <li>{@code EXCEPTION_FALLBACK}：{@code Exception}（非项目 {@code CException} 体系）兜底（{@code CExceptionHandler}）。</li>
 *   <li>{@code THROWABLE_FALLBACK}：{@code Throwable} 兜底（{@code CThrowableHandler}）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无</td>
 *     <td>纯常量类，无行为</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>内置异常处理器的 {@code @Order} 声明；业务方对齐或插入档位时参考。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>档位为编译期常量，业务方若需"卡在具体类型档与 {@code CException} 兜底档之间"，需自行选择区间内的显式值。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>为何统一放在兜底区</b></p>
 * <ul>
 *   <li>内置处理器是开箱即用的兜底措施，不应抢占业务方自定义 advice：各档均贴近 {@code Ordered.LOWEST_PRECEDENCE}，
 *   业务方任一显式 {@code @Order}（如 {@code @Order(0)}）都排在其之前、可优先命中。</li>
 *   <li>业务方处理的是<b>子类/超类</b>等非精确类型时，{@code @ConditionalOnMissingExceptionHandler}（按精确类型判断）不会让内置处理器让位，
 *   此时归属由 advice 顺序决定——这正是档位必须可被业务方超越的原因。</li>
 * </ul>
 * <p><b>为何仍需显式档位</b></p>
 * <ul>
 *   <li>Spring 的 {@code ExceptionHandlerExceptionResolver} 按 advice 顺序取首个能匹配的处理器，不跨 advice 比较异常类型精确度。</li>
 *   <li>兜底处理器匹配的是超类（{@code CException}/{@code Exception}/{@code Throwable}），若排在具体类型处理器之前会截走子类异常（如未授权 401 被兜底成 500），
 *   故各档必须严格递增，且档位之间留出间隙便于业务方插入。</li>
 * </ul>
 * <p><b>业务方覆盖路径</b></p>
 * <ul>
 *   <li>存在性覆盖（与顺序无关）：声明同一异常类型的 {@code @ExceptionHandler}，内置处理器因条件装配不生效。</li>
 *   <li>顺序覆盖：处理子类/超类等非精确类型时，用显式 {@code @Order}（值小于对应档位）排在前面；
 *   业务方若要接管 {@code Exception} 本身，其 {@code @Order} 须小于 {@code EXCEPTION_FALLBACK}（否则由 {@code CExceptionHandler} 先命中）。</li>
 * </ul>
 * <p><b>为何显式写 {@code static}</b></p>
 * <ul>
 *   <li>常量被用作 {@code @Order} 的<b>注解值</b>：{@code @UtilityClass} 虽会生成 {@code static}（javac 编译通过），
 *   但 javadoc 直接解析源码、不走 Lombok，省略 {@code static} 会让所有 {@code @Order(CExceptionHandlerOrder.*)} 报
 *   "non-static variable cannot be referenced from a static context"，故此处显式声明（项目其余常量类无注解值引用，故可省略）。</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.2
 */
@UtilityClass
public class CExceptionHandlerOrder {

    /**
     * 具体异常类型处理器档位
     */
    public static final int CONCRETE = Ordered.LOWEST_PRECEDENCE - 100;

    /**
     * {@code CException} 兜底处理器档位
     */
    public static final int C_EXCEPTION_FALLBACK = Ordered.LOWEST_PRECEDENCE - 50;

    /**
     * {@code Exception}（非项目 {@code CException} 体系）兜底处理器档位
     */
    public static final int EXCEPTION_FALLBACK = Ordered.LOWEST_PRECEDENCE - 25;

    /**
     * {@code Throwable} 兜底处理器档位（最后一档）
     */
    public static final int THROWABLE_FALLBACK = Ordered.LOWEST_PRECEDENCE;

}
