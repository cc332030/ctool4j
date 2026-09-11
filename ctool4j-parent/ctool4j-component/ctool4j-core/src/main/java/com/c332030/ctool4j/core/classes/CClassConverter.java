package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.definition.function.CFunction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CClassConverter
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CClassConverter&lt;From, To&gt;} 为类型转换器定义类，含：</p>
 * <ul>
 *   <li>{@code fromClass}：源类</li>
 *   <li>{@code toClass}：目标类</li>
 *   <li>{@code converter}：转换函数（CFunction&lt;From, To&gt;）</li>
 * </ul>
 * <p>标注 {@code @Data}/{@code @SuperBuilder}/{@code @NoArgsConstructor}/{@code @AllArgsConstructor}，支持 builder 与三种构造。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无参构造</td>
 *     <td>三字段均为 null</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>描述源类型→目标类型的转换关系，供 CConvertUtils 注册与查找。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>纯数据结构，无行为逻辑。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>简单数据载体，配合 builder 便捷构造。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>结构</b></p>
 * <ul>
 *   <li>通过 Lombok 生成 getter/setter、builder、无参/全参构造，供 {@code CConvertUtils} 注册转换器使用。</li>
 * </ul>
 *
 * @since 2025/11/20
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CClassConverter<From, To> {

    /**
     * 源类
     */
    Class<From> fromClass;

    /**
     * 目标类
     */
    Class<To> toClass;

    /**
     * 转换方法
     */
    CFunction<From, To> converter;

}
