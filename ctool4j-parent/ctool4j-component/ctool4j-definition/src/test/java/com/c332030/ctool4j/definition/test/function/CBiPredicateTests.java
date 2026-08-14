package com.c332030.ctool4j.definition.test.function;

import com.c332030.ctool4j.definition.function.CBiPredicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.BiPredicate;

/**
 * <p>
 * Description: CBiPredicateTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CBiPredicateTests {

    @Test
    public void testNormal() {

        CBiPredicate<Integer, Integer> predicate = (a, b) -> a > b;

        Assertions.assertTrue(predicate.test(2, 1));
        Assertions.assertFalse(predicate.test(1, 2));

    }

    @Test
    public void testNullInputs() {

        CBiPredicate<String, String> predicate = (a, b) -> true;

        Assertions.assertTrue(predicate.test(null, null));

    }

    @Test
    public void testSneakyThrowsCheckedException() {

        CBiPredicate<String, String> predicate = (a, b) -> {
            throw new IOException("io error");
        };

        Assertions.assertThrowsExactly(
            IOException.class,
            () -> predicate.test("a", "b")
        );

    }

    @Test
    public void alwaysTrue() {

        CBiPredicate<String, String> predicate = CBiPredicate.alwaysTrue();

        Assertions.assertTrue(predicate.test(null, null));
        Assertions.assertTrue(predicate.test("a", "b"));

    }

    @Test
    public void alwaysFalse() {

        CBiPredicate<String, String> predicate = CBiPredicate.alwaysFalse();

        Assertions.assertFalse(predicate.test(null, null));
        Assertions.assertFalse(predicate.test("a", "b"));

    }

    @Test
    public void staticTestNullPredicate() {

        Assertions.assertFalse(CBiPredicate.test(null, "a", "b"));

    }

    @Test
    public void staticTestNormal() {

        BiPredicate<Integer, Integer> predicate = (a, b) -> a > b;

        Assertions.assertTrue(CBiPredicate.test(predicate, 2, 1));
        Assertions.assertFalse(CBiPredicate.test(predicate, 1, 2));

    }

    @Test
    public void convert() {

        CBiPredicate<Integer, Integer> cPredicate = (a, b) -> a > b;

        BiPredicate<Integer, Integer> predicate = CBiPredicate.convert(cPredicate);

        Assertions.assertTrue(predicate.test(2, 1));
        Assertions.assertFalse(predicate.test(1, 2));

    }

    @Test
    public void convertNullCBiPredicate() {

        BiPredicate<Integer, Integer> predicate = CBiPredicate.convert(null);

        Assertions.assertFalse(predicate.test(1, 2));

    }

}
