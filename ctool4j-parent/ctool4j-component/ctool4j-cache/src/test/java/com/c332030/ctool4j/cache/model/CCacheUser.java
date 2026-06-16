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
 * @since 2026/6/16
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
