package com.c332030.ctool.core.mapstruct;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * <p>
 * Description: CMapStructConfig
 * </p>
 *
 * @since 2025/4/18
 */
@MapperConfig(
        componentModel = "spring",
        uses = {CMapStructConvert.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CMapStructConfig {

}
