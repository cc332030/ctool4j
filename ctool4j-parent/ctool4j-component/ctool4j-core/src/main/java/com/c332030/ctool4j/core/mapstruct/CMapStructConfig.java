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
 * @since 2025/4/18
 */
@MapperConfig(
        componentModel = "spring",
        uses = {CClassConvert.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CMapStructConfig {

}
