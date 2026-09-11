package com.c332030.ctool4j.mybatisplus.util;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

/**
 * <p>
 * Description: CMpFieldUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMpFieldUtils}：字段工具。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>筛选更新策略非 NEVER 的字段等</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>字段处理</p>
 * <h2>不适用与边界场景</h2>
 * <p>静态工具</p>
 * <h2>已知限制与取舍</h2>
 * <p>静态工具</p>
 *
 * @since 2026/1/6
 * @version 1.0
 */
@UtilityClass
public class CMpFieldUtils {

    /**
     * 更新策略非 NEVER 的字段筛选条件
     */
    public final Predicate<TableFieldInfo> UPDATE_NOT_NEVER =
        fieldInfo -> FieldStrategy.NEVER != fieldInfo.getUpdateStrategy();

}
