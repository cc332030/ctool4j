package com.c332030.ctool4j.core.mapstruct;

import com.c332030.ctool4j.core.classes.CClassConvert;
import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * <p>
 * Description: CMapStructConfig
 * </p>
 * <p>
 * MapStruct 全局映射配置：组件模型为 spring，复用 CClassConvert，忽略 null 属性映射
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMapStructConfig} 为 MapStruct 全局映射配置接口，标注 {@code @MapperConfig}：</p>
 * <ul>
 *   <li>{@code componentModel = "spring"}：生成 Spring 组件模型</li>
 *   <li>{@code uses = CClassConvert.class}：复用 {@code CClassConvert} 的转换方法</li>
 *   <li>{@code nullValuePropertyMappingStrategy = IGNORE}：忽略 null 属性映射</li>
 * </ul>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>其他 Mapper 通过 {@code @Mapper(config = CMapStructConfig.class)} 复用全局映射配置。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>全局配置对所有引用它的 Mapper 生效，具体 Mapper 可局部覆盖。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>全局约定</b></p>
 * <ul>
 *   <li>组件模型为 Spring，映射器作为 Spring Bean 注入。</li>
 *   <li>复用 CClassConvert 提供类型转换能力。</li>
 *   <li>源属性为 null 时不映射到目标（忽略），避免覆盖目标既有值。</li>
 * </ul>
 *
 * @since 2025/4/18
 * @version 1.0
 */
@MapperConfig(
        componentModel = "spring",
        uses = {CClassConvert.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CMapStructConfig {

}
