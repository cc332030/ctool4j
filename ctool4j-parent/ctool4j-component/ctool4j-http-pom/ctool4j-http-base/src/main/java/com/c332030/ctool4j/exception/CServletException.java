package com.c332030.ctool4j.exception;

import lombok.experimental.StandardException;

/**
 * <p>
 * Description: CServletException
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CServletException}：跨 Servlet 包边界的异常包装类，标注 {@code @StandardException}
 * （Lombok 生成完整构造集合：无参/消息/原因/消息+原因）。</p>
 * <p>用途：{@code javax.servlet.ServletException} 与 {@code jakarta.servlet.ServletException} 是两套包里的<b>不同类</b>，
 * 抽象层无法在同一个方法签名上同时表达两者；故由两侧适配器捕获底层 Servlet 异常后包装为本类抛出，
 * 抽象层（{@code com.c332030.ctool4j.interfaces}）只暴露本类，使用方不接触任何 Servlet 包。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>运行时异常</b>：继承 {@code RuntimeException}，与项目异常体系一致（{@code CException} 家族均为运行时异常），
 *   抽象层的方法签名因此保持干净、不必声明 {@code throws}。</li>
 *   <li><b>保留原因</b>：底层异常经 {@code cause} 原样保留，需要判定具体失败原因时用 {@link Throwable#getCause()} 取回。</li>
 *   <li><b>零依赖</b>：不继承 {@code CException}——本模块（ctool4j-http-base）不依赖 ctool4j-core，
 *   抽象层保持不引入任何模块依赖。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：本类只承载失败事实与原因，不改写消息、不记日志（记日志属调用方的判断）。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>跨 javax/jakarta 的 Servlet 抽象层中，底层 Servlet 异常的统一出口（当前用于 {@code login}/{@code logout}）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>包装后原异常的 <b>checked 约束消失</b>：底层 {@code ServletException} 是受检异常、调用方本须处理，
 *   本类为运行时异常，调用方不再被编译器强制处理——需要保留失败可见性时按 {@code @throws} 文档处理并自行记录。</li>
 *   <li>只包装"两侧类型不同"的异常：{@code java.io.IOException} 两侧同为 JDK 类型，直接沿用、不包装。</li>
 * </ul>
 *
 * @since 2026/9/24
 * @version 1.0
 */
@StandardException
public class CServletException extends RuntimeException {

    private static final long serialVersionUID = 1L;

}
