package com.c332030.ctool4j.cache.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CCacheId
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCacheId} 为字段级标记注解，标注在对象的一个字段上，表示该字段作为缓存 ID 来源 （配合 {@code @CCacheable} 使用，由 {@code CCacheAspect} 通过 MethodHandle 读取该字段值作为缓存 key 一部分）。</p>
 * <p>无任何属性，纯标记。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>对象上没有 {@code @CCacheId} 字段</td>
 *     <td>{@code CCacheAspect} 取不到 handle，cacheId 为 null，走默认 idConverter 兜底</td>
 *   </tr>
 *   <tr>
 *     <td>JDK 类对象</td>
 *     <td>跳过 {@code @CCacheId} 字段读取，cacheId 为 null</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要以对象某字段值作为缓存 key 的缓存方法入参对象。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅单个字段可作为缓存 ID；复合 ID 场景需自行在 idConverter 中组装。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>同一对象多 {@code @CCacheId} 字段时只取第一个，语义由调用方保证。</li>
 *   <li>不提供 key 前缀/格式配置，具体缓存 key 由 idConverter 生成。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>仅作用于字段（{@code @Target(ElementType.FIELD)}），运行时保留（{@code RUNTIME}）、可继承、可被文档记录。</li>
 *   <li>一个对象上通常只标注一个 {@code @CCacheId} 字段；{@code CCacheAspect.CACHE_ID_HANDLE_CLASS_VALUE}</li>
 *   <li>只取第一个带该注解的字段（经 {@code getAllFieldMap} 值流 {@code findFirst}）。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>纯注解声明，无业务逻辑；由切面在运行时反射读取注解与字段值。</li>
 * </ul>
 *
 * @since 2026/6/16
 * @version 1.0
 */
@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CCacheId {

}
