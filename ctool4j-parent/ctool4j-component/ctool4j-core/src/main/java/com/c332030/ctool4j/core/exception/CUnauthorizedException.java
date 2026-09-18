package com.c332030.ctool4j.core.exception;

import lombok.experimental.StandardException;

/**
 * <p>
 * Description: CUnauthorizedException
 * </p>
 *
 * <p>未授权异常：语义为「未认证」——无凭据、凭据无效/过期、当前会话缺失或失效；应由 Web 层统一转为
 * 响应体业务码 401 的错误结果（见 ctool4j-web 的 {@code CUnauthorizedExceptionHandler}）。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>继承 {@code CException}，标注 {@code @StandardException}（由 Lombok 生成标准异常构造集合：无参/消息/原因/消息+原因）。</li>
 *   <li>不携带错误码：响应体业务码由统一处理器按 401 判定，避免每个抛出点各自指定码。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无参构造（消息为 null）</td>
 *     <td>处理器回退 401 状态原因短语（业务码仍为 401）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>框架/业务代码判定「当前请求未认证」时抛出，如 {@code CAbstractBaseSessionService#get()} 取当前会话失败。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>已认证但无权限 → 403（Forbidden），不属于本异常。</li>
 *   <li>业务校验/流程失败（非认证语义）→ {@code CBusinessException}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>不携带错误码与扩展信息：更细的未授权原因（凭据过期、签名无效等）用消息区分，不新增错误码。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>置于 ctool4j-core（框架无关）：auth-base 的服务层可抛、ctool4j-web 的处理器可接，双方都不必依赖对方模块。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/17
 * @version 1.1
 * @see "doc/design/web/unauthorized-401.adoc"
 * <p>用例见 {@code CUnauthorizedExceptionTests}（主代码类注释不 {@code @see} 测试类：javadoc 类路径不含测试源）。</p>
 */
@StandardException
public class CUnauthorizedException extends CException {

    private static final long serialVersionUID = 1;

}
