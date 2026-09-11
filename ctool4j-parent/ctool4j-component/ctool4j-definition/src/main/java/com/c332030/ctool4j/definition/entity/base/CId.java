package com.c332030.ctool4j.definition.entity.base;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * <p>
 * Description: CId
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CId&lt;ID extends Serializable&gt;} 为泛型 ID 实体基类，实现 {@code ICId&lt;ID&gt;}，含 {@code id} 字段（标注 {@code @TableId}）。</p>
 * <p>标注 {@code @Data}/{@code @SuperBuilder}/{@code @NoArgsConstructor}/{@code @AllArgsConstructor}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要主键 ID 的实体基类；CIntegerId/CLongId/CStringId 继承它固定 ID 类型。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>ID 泛型化，子类可指定具体类型（Long/String/Integer）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>结构</b></p>
 * <ul>
 *   <li>单一 {@code id} 字段，作为 mybatis-plus 主键（@TableId）。</li>
 *   <li>Lombok 生成 getter/setter、builder、构造、equals/hashCode/toString。</li>
 * </ul>
 *
 * @since 2025/5/26
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CId<ID extends Serializable> implements ICId<ID> {

    @TableId
    ID id;

}
