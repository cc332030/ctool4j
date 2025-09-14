package com.c332030.core.test.exception;

import com.c332030.core.exception.IBusinessExceptionProvider;

import java.util.function.BiFunction;

/**
 * <p>
 * Description: TestBusinessExceptionProvider
 * </p>
 *
 * @since 2025/9/14
 */
public class TestBusinessExceptionProvider implements IBusinessExceptionProvider<TestException> {

    @Override
    public BiFunction<String, Throwable, TestException> getExceptionFunction() {
        return TestException::new;
    }

}
