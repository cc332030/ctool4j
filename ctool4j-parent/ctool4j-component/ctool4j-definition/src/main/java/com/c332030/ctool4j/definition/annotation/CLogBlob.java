package com.c332030.ctool4j.definition.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 标识长文本/大对象字段，日志打印时按规模决定打印真实内容或占位符
 * </p>
 * <p>标记在 DTO 字段（或 getter）上，经 CJsonUtils.toJsonLog（日志专用 mapper）序列化时：
 * 值的规模（字符串字符数、集合元素数、Map 条目数、数组长度）不超过 {@link #maxSize()} 时打印真实内容，
 * 超过则输出占位符（如 &lt;BLOB:chars=1024&gt;、&lt;BLOB:list=500&gt;，只暴露形状与规模），
 * 避免 base64、文件流、大集合等内容刷屏日志；规模无法评估的值（如流、自定义对象）始终输出 &lt;BLOB&gt;；
 * 全局 ObjectMapper 无该行为，业务序列化输出真实内容</p>
 * <p>阈值通过 {@link #maxSize()} 配置，默认 {@link #DEFAULT_MAX_SIZE}；字符串与集合/Map/数组共用同一阈值口径</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLogBlob} 为日志长文本/大对象字段的阈值占位注解。</p>
 * <ul>
 *   <li>字段/getter 级注解，仅日志专用 mapper 生效（配合 {@code CLogBlobSerializer}），全局 mapper 输出真实内容。</li>
 *   <li>{@code maxSize}：规模阈值（含），不超过打印真实内容、超过输出占位符并附规模。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>规模不超过 {@code maxSize}（含等于）</td>
 *     <td>打印真实内容</td>
 *   </tr>
 *   <tr>
 *     <td>规模超过 {@code maxSize}</td>
 *     <td>输出占位符并附规模（如 {@code &lt;BLOB:chars=1024&gt;}）</td>
 *   </tr>
 *   <tr>
 *     <td>规模无法评估（流、自定义对象）</td>
 *     <td>输出 {@code &lt;BLOB&gt;}</td>
 *   </tr>
 *   <tr>
 *     <td>全局 mapper 序列化</td>
 *     <td>输出真实内容</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>请求/响应体中的长文本、集合、Map 等大字段的日志打印。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>注解仅声明，实际行为由消费方（如 {@code CLogBlobSerializer}）实现。</li>
 *   <li>规模不超过阈值时会打印真实内容（便于排障），敏感字段请配合 {@code @CLogSensitive} 使用。</li>
 * </ul>
 *
 * @since 2026/8/13
 * @version 1.3
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CLogBlob {

    /**
     * 默认规模阈值
     */
    int DEFAULT_MAX_SIZE = 10;

    /**
     * 规模阈值（含）：值的规模不超过该值时打印真实内容，超过则输出占位符
     * <p>规模口径：{@code CharSequence} 为字符数、{@code Collection} 为元素数、{@code Map} 为条目数、数组为长度</p>
     *
     * @return 规模阈值
     */
    int maxSize() default DEFAULT_MAX_SIZE;

}
