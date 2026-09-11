package com.c332030.ctool4j.mybatis.model.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CPageUtils;
import com.c332030.ctool4j.doc.annotation.CSchema;
import com.c332030.ctool4j.mybatis.model.ICPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * <p>
 * Description: CPageReq
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CPageReq}：分页查询参数模型。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>实现 ICPage，含泛型查询参数 req</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>同 CPage 默认值</p>
 * <h2>适用范围</h2>
 * <p>分页查询载体</p>
 * <h2>不适用与边界场景</h2>
 * <p>泛型 T 查询参数</p>
 * <h2>已知限制与取舍</h2>
 * <p>泛型 T 查询参数</p>
 *
 * @since 2026/1/20
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CPageReq<T> implements ICPage {

    @Builder.Default
    Integer pageNum = 1;

    @Builder.Default
    Integer pageSize = CPageUtils.DEFAULT_PAGE_SIZE;

    @Builder.Default
    List<OrderItem> orders = CList.of();

    @CSchema("查询参数")
    T req;

}
