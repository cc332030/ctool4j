package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.definition.function.CFunction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: ClassConverter
 * </p>
 *
 * @since 2025/11/20
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClassConverter<From, To> {

    /**
     * 源类
     */
    Class<From> fromClass;

    /**
     * 目标类
     */
    Class<To> toClass;

    /**
     * 转换方法
     */
    CFunction<From, To> converter;

}
