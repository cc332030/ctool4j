package com.c332030.ctool4j.mybatis.handler;

/**
 * <p>
 * Description: CCommaStringCollectionTypeHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCommaStringCollectionTypeHandler} 是 {@code List<String>} 与逗号分割字符串互转的类型处理器（如 {@code a,b,c}）。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>只做类型具体化</b>：读写与转换统一在 {@link CCommaCollectionTypeHandler}，本类仅声明元素类型为 {@code String}。</li>
 *   <li><b>读回会归一化</b>：{@code String} 元素写入时原样，读回时经 {@code CStrUtils.splitToList}（内部 trim、去首尾引号，空串元素被丢弃），故首尾空格与引号不保真；
 *   值本身含逗号时无法还原（分隔符冲突），此类场景应改用 JSON 序列化的处理器。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>{@code @TableField(typeHandler = CCommaStringCollectionTypeHandler.class)} 标注的 {@code List<String>} 字段，如标签、编码集合。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>元素值可能包含逗号（如自由文本、URL 查询串）时不适用——会被误切成多个元素。</li>
 * </ul>
 *
 * @see CCommaCollectionTypeHandler
 * @since 2026/9/24
 * @version 1.0
 */
public class CCommaStringCollectionTypeHandler extends CCommaCollectionTypeHandler<String> {

}
