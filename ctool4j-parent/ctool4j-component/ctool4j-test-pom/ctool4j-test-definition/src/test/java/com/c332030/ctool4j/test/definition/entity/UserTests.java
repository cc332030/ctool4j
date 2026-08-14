package com.c332030.ctool4j.test.definition.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: UserTests
 * </p>
 *
 * @since 2026/8/14
 */
class UserTests {

    @Test
    void noArgsConstructor() {
        User user = new User();
        Assertions.assertNotNull(user);
        Assertions.assertNull(user.getUserName());
        Assertions.assertNull(user.getPassword());
        Assertions.assertNull(user.getAge());
    }

    @Test
    void allArgsConstructor() {
        User user = new User("admin", "pwd", 18);
        Assertions.assertEquals("admin", user.getUserName());
        Assertions.assertEquals("pwd", user.getPassword());
        Assertions.assertEquals(18, user.getAge());
    }

    @Test
    void superBuilder() {
        User user = User.builder()
                .userName("admin")
                .password("pwd")
                .age(18)
                .build();
        Assertions.assertEquals("admin", user.getUserName());
        Assertions.assertEquals("pwd", user.getPassword());
        Assertions.assertEquals(18, user.getAge());
    }

    @Test
    void builder_omitOptionalField() {
        User user = User.builder()
                .userName("admin")
                .build();
        Assertions.assertEquals("admin", user.getUserName());
        Assertions.assertNull(user.getPassword());
        Assertions.assertNull(user.getAge());
    }

    @Test
    void equals_sameValues() {
        User a = User.builder().userName("admin").password("pwd").age(18).build();
        User b = User.builder().userName("admin").password("pwd").age(18).build();
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentValue() {
        User a = User.builder().userName("admin").password("pwd").age(18).build();
        User b = User.builder().userName("admin").password("pwd").age(20).build();
        Assertions.assertNotEquals(a, b);
    }

    @Test
    void equals_null() {
        User a = User.builder().userName("admin").build();
        Assertions.assertNotEquals(a, null);
    }

}
