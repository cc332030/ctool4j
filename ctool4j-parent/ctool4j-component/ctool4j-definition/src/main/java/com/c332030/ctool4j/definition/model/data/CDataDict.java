package com.c332030.ctool4j.definition.model.data;

import com.c332030.ctool4j.definition.interfaces.ICDataDict;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CDataDict
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CDataDict&lt;T&gt;} 为数据字典，实现 {@code ICDataDict&lt;T&gt;}，含 {@code value} 与 {@code text} 字段。</p>
 * <p>标注 {@code @Data}/{@code @SuperBuilder}/{@code @NoArgsConstructor}/{@code @AllArgsConstructor}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>数据字典项（值 + 展示文本）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>简单数据载体。</li>
 * </ul>
 *
 * @since 2026/1/7
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CDataDict<T> implements ICDataDict<T> {

    T value;

    String text;

}
