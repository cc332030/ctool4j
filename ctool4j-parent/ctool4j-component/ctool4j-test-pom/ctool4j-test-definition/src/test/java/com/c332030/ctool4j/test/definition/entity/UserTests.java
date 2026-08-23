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

        /**
     * 对应测试用例 1.1
     */
    @Test
    void noArgsConstructor() {
        User user = new User();
        Assertions.assertNotNull(user);
        Assertions.assertNull(user.getUserName());
        Assertions.assertNull(user.getPassword());
        Assertions.assertNull(user.getAge());
    }

        /**
     * 对应测试用例 1.2
     */
    @Test
    void allArgsConstructor() {
        User user = new User("admin", "pwd", 18);
        Assertions.assertEquals("admin", user.getUserName());
        Assertions.assertEquals("pwd", user.getPassword());
        Assertions.assertEquals(18, user.getAge());
    }

        /**
     * 对应测试用例 1.3
     */
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

        /**
     * 对应测试用例 1.4
     */
    @Test
    void builder_omitOptionalField() {
        User user = User.builder()
                .userName("admin")
                .build();
        Assertions.assertEquals("admin", user.getUserName());
        Assertions.assertNull(user.getPassword());
        Assertions.assertNull(user.getAge());
    }

        /**
     * 对应测试用例 1.5
     */
    @Test
    void equals_sameValues() {
        User a = User.builder().userName("admin").password("pwd").age(18).build();
        User b = User.builder().userName("admin").password("pwd").age(18).build();
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }

        /**
     * 对应测试用例 1.6
     */
    @Test
    void equals_differentValue() {
        User a = User.builder().userName("admin").password("pwd").age(18).build();
        User b = User.builder().userName("admin").password("pwd").age(20).build();
        Assertions.assertNotEquals(a, b);
    }

        /**
     * 对应测试用例 1.7
     */
    @Test
    void equals_null() {
        User a = User.builder().userName("admin").build();
        Assertions.assertNotEquals(a, null);
    }

}
