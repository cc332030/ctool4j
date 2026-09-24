package com.c332030.ctool4j.mybatis.handler;

/**
 * <p>
 * Description: CCommaLongCollectionTypeHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCommaLongCollectionTypeHandler} 是 {@code List<Long>} 与逗号分割字符串互转的类型处理器（如 1,2,3）。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>只做类型具体化</b>：读写与单值转换统一在 {@link CCommaCollectionTypeHandler}，本类仅声明元素类型为 {@code Long}，
 *   不需要实现任何方法。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>{@code @TableField(typeHandler = CCommaLongCollectionTypeHandler.class)} 标注的 {@code List<Long>} 字段。</li>
 * </ul>
 *
 * @see CCommaCollectionTypeHandler
 * @since 2026/9/24
 * @version 1.0
 */
public class CCommaLongCollectionTypeHandler extends CCommaCollectionTypeHandler<Long> {

}
