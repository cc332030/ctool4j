package com.c332030.ctool4j.mybatis.model.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CPageUtils;
import com.c332030.ctool4j.mybatis.model.ICPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * <p>
 * Description: CPage
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CPage}：分页模型。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>实现 ICPage，pageNum/pageSize/orders 默认值</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>pageNum=1、pageSize=默认、orders=空</p>
 * <h2>适用范围</h2>
 * <p>分页参数载体</p>
 * <h2>不适用与边界场景</h2>
 * <p>字段默认值</p>
 * <h2>已知限制与取舍</h2>
 * <p>字段默认值</p>
 *
 * @since 2025/12/2
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CPage implements ICPage {

    @Builder.Default
    Integer pageNum = 1;

    @Builder.Default
    Integer pageSize = CPageUtils.DEFAULT_PAGE_SIZE;

    @Builder.Default
    List<OrderItem> orders = CList.of();

}
