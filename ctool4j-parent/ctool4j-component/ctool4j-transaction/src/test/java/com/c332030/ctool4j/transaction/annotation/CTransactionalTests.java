package com.c332030.ctool4j.transaction.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Description: CTransactionalTests
 * </p>
 *
 * @since 2026/8/14
 */
class CTransactionalTests {

    @CTransactional
    static class DefaultConfig {
        void method() {
        }
    }

    @CTransactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED,
            readOnly = true,
            rollbackFor = IllegalStateException.class)
    static class CustomConfig {
        void method() {
        }
    }

    static class CustomMethodConfig {

        @CTransactional(
                propagation = Propagation.MANDATORY,
                isolation = Isolation.REPEATABLE_READ,
                readOnly = true,
                rollbackFor = IllegalArgumentException.class)
        void method() {
        }
    }

    @Test
    void metaAnnotationResolved_defaultValues() {
        Transactional t = AnnotatedElementUtils.findMergedAnnotation(DefaultConfig.class, Transactional.class);
        Assertions.assertNotNull(t, "@CTransactional 应通过 @AliasFor 映射为 @Transactional");
        Assertions.assertEquals(Propagation.REQUIRED, t.propagation());
        Assertions.assertEquals(Isolation.DEFAULT, t.isolation());
        Assertions.assertFalse(t.readOnly());
        Assertions.assertEquals(1, t.rollbackFor().length);
        Assertions.assertEquals(Exception.class, t.rollbackFor()[0]);
    }

    @Test
    void metaAnnotationResolved_customValues() {
        Transactional t = AnnotatedElementUtils.findMergedAnnotation(CustomConfig.class, Transactional.class);
        Assertions.assertNotNull(t);
        Assertions.assertEquals(Propagation.REQUIRES_NEW, t.propagation());
        Assertions.assertEquals(Isolation.READ_COMMITTED, t.isolation());
        Assertions.assertTrue(t.readOnly());
        Assertions.assertEquals(1, t.rollbackFor().length);
        Assertions.assertEquals(IllegalStateException.class, t.rollbackFor()[0]);
    }

    @Test
    void metaAnnotationResolved_methodLevel() throws NoSuchMethodException {
        Transactional t = AnnotatedElementUtils.findMergedAnnotation(
                CustomMethodConfig.class.getDeclaredMethod("method"), Transactional.class);
        Assertions.assertNotNull(t);
        Assertions.assertEquals(Propagation.MANDATORY, t.propagation());
        Assertions.assertEquals(Isolation.REPEATABLE_READ, t.isolation());
        Assertions.assertTrue(t.readOnly());
        Assertions.assertEquals(1, t.rollbackFor().length);
        Assertions.assertEquals(IllegalArgumentException.class, t.rollbackFor()[0]);
    }

    @Test
    void annotationRetainedAtRuntime() {
        Assertions.assertTrue(CTransactional.class.isAnnotationPresent(Transactional.class),
                "@CTransactional 应被 @Transactional 元注解标注");
    }

}
