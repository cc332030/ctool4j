package com.c332030.ctool4j.cache.model;

import com.c332030.ctool4j.cache.annotation.CCacheId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CCacheUser
 * </p>
 *
 * <h2>功能说明</h2>
 * <p>{@code CCacheUser} 为缓存测试辅助实体，标注 {@code @Data}/{@code @Accessors(chain)}/{@code @SuperBuilder}/{@code @NoArgsConstructor}/{@code @AllArgsConstructor}：</p>
 * <ul>
 *   <li>{@code id}：带 {@code @CCacheId} 注解的主键字段，作为缓存键</li>
 * </ul>
 * <p>供缓存功能测试（{@code CCacheAspectTests} 等）作为测试实体使用。</p>
 * <h2>适用场景</h2>
 * <ul>
 *   <li>缓存功能测试的辅助实体，验证 {@code @CCacheId} 键提取与缓存读写。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助类，非业务实体；仅含 {@code id} 单字段。</li>
 * </ul>
 *
 * @since 2026/6/16
 * @version 1.0
 */
@Data
@Accessors(chain = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CCacheUser {

    @CCacheId
    Long id;

}
