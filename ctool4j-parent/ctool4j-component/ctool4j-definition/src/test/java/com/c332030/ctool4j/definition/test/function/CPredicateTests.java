package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CPredicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * <p>
 * Description: CPredicateTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CPredicateTests {

    @Test
    public void testNormal() {

        CPredicate<Integer> predicate = i -> i > 0;

        Assertions.assertTrue(predicate.test(1));
        Assertions.assertFalse(predicate.test(-1));

    }

    @Test
    public void testNullInput() {

        CPredicate<String> predicate = s -> true;

        Assertions.assertTrue(predicate.test(null));

    }

    @Test
    public void testSneakyThrowsCheckedException() {

        CPredicate<String> predicate = s -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> predicate.test("input")
        );

    }

    @Test
    public void alwaysTrue() {

        CPredicate<String> predicate = CPredicate.alwaysTrue();

        Assertions.assertTrue(predicate.test(null));
        Assertions.assertTrue(predicate.test("abc"));

    }

    @Test
    public void alwaysFalse() {

        CPredicate<String> predicate = CPredicate.alwaysFalse();

        Assertions.assertFalse(predicate.test(null));
        Assertions.assertFalse(predicate.test("abc"));

    }

    @Test
    public void staticTestNullPredicate() {

        Assertions.assertFalse(CPredicate.test(null, "input"));

    }

    @Test
    public void staticTestNormal() {

        Predicate<Integer> predicate = i -> i > 0;

        Assertions.assertTrue(CPredicate.test(predicate, 1));
        Assertions.assertFalse(CPredicate.test(predicate, -1));

    }

    @Test
    public void convert() {

        CPredicate<Integer> cPredicate = i -> i > 0;

        Predicate<Integer> predicate = CPredicate.convert(cPredicate);

        Assertions.assertTrue(predicate.test(1));
        Assertions.assertFalse(predicate.test(-1));

    }

    @Test
    public void convertNullCPredicate() {

        Predicate<Integer> predicate = CPredicate.convert(null);

        Assertions.assertFalse(predicate.test(1));

    }

}
