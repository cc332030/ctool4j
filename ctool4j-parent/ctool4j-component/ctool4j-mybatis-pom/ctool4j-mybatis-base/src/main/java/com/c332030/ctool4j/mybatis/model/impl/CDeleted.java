package com.c332030.ctool4j.mybatis.model.impl;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.c332030.ctool4j.mybatis.model.ICDeleted;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CDeleted
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CDeleted}：删除标识实现。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>实现 ICDeleted，@TableLogic 逻辑删除字段，lombok 生成访问器</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>逻辑删除字段载体</p>
 * <h2>不适用与边界场景</h2>
 * <p>依赖 mybatis-plus</p>
 * <h2>已知限制与取舍</h2>
 * <p>依赖 mybatis-plus</p>
 *
 * @since 2025/12/16
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CDeleted implements ICDeleted {

    @TableLogic
    Boolean deleted;

}
