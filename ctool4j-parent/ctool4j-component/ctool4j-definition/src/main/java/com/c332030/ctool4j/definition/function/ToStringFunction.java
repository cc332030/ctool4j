package com.c332030.ctool4j.definition.function;

/**
 * <p>
 * Description: ToStringFunction
 * </p>
 *
 * @since 2024/12/2
 * @see doc/design/core/ToStringFunction.adoc
 * @see doc/design/core/ToStringFunctionTests.adoc
 */
@FunctionalInterface
public interface ToStringFunction<T> extends CFunction<T, String> {

}
