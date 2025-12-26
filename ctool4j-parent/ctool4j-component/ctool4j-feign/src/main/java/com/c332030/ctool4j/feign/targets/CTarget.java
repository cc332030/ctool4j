package com.c332030.ctool4j.feign.targets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CTarget
 * </p>
 *
 * @since 2025/12/25
 */
@Data
@SuperBuilder
@AllArgsConstructor
public class CTarget<T> implements ICTarget<T> {

    Class<T> type;

    String name;

    String url;

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String url() {
        return url;
    }

}
