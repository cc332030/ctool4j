package com.c332030.core.mapstruct;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * <p>
 * Description: MapStructConfig
 * </p>
 *
 * @since 2025/4/18
 */
@MapperConfig(
        componentModel = "spring",
        uses = {MapStructConvert.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MapStructConfig {

}
