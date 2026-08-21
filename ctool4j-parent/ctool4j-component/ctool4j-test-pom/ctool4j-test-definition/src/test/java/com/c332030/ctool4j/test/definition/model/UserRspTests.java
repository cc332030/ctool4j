package com.c332030.ctool4j.test.definition.model;

import com.c332030.ctool4j.test.definition.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * Description: UserRspTests
 * </p>
 *
 * @since 2026/8/14
 */
class UserRspTests {

        /**
     * 对应测试用例 1.1
     */
    @Test
    void noArgsConstructor() {
        UserRsp rsp = new UserRsp();
        Assertions.assertNotNull(rsp);
        Assertions.assertNull(rsp.getUserName());
        Assertions.assertNull(rsp.getSex());
    }

        /**
     * 对应测试用例 1.2
     */
    @Test
    void superBuilder_inheritanceWiring() {
        Date updateTime = new Date(1700000000000L);
        UserRsp rsp = UserRsp.builder()
                // 父类字段
                .userName("admin")
                .password("pwd")
                .age(18)
                // 子类字段
                .sex("male")
                .status(1)
                .amount(new BigDecimal("100.50"))
                .score(90L)
                .grade(5)
                .createTime("2026-01-01")
                .updateTime(updateTime)
                .build();
        Assertions.assertEquals("admin", rsp.getUserName());
        Assertions.assertEquals("pwd", rsp.getPassword());
        Assertions.assertEquals(18, rsp.getAge());
        Assertions.assertEquals("male", rsp.getSex());
        Assertions.assertEquals(1, rsp.getStatus());
        Assertions.assertEquals(new BigDecimal("100.50"), rsp.getAmount());
        Assertions.assertEquals(90L, rsp.getScore());
        Assertions.assertEquals(5, rsp.getGrade());
        Assertions.assertEquals("2026-01-01", rsp.getCreateTime());
        Assertions.assertEquals(updateTime, rsp.getUpdateTime());
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    void superBuilder_defaultNullFields() {
        UserRsp rsp = UserRsp.builder().userName("admin").build();
        Assertions.assertEquals("admin", rsp.getUserName());
        Assertions.assertNull(rsp.getPassword());
        Assertions.assertNull(rsp.getSex());
        Assertions.assertNull(rsp.getAmount());
    }

        /**
     * 对应测试用例 1.4
     */
    @Test
    void equals_sameValues() {
        UserRsp a = UserRsp.builder().userName("admin").sex("male").build();
        UserRsp b = UserRsp.builder().userName("admin").sex("male").build();
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }

        /**
     * 对应测试用例 1.5
     */
    @Test
    void equals_differentField() {
        UserRsp a = UserRsp.builder().userName("admin").sex("male").build();
        UserRsp b = UserRsp.builder().userName("admin").sex("female").build();
        Assertions.assertNotEquals(a, b);
    }

        /**
     * 对应测试用例 1.6
     */
    @Test
    void equals_null() {
        UserRsp a = UserRsp.builder().userName("admin").build();
        Assertions.assertNotEquals(a, null);
    }

        /**
     * 对应测试用例 1.7
     */
    @Test
    void equals_otherType() {
        UserRsp rsp = UserRsp.builder().userName("admin").sex("male").build();
        User user = User.builder().userName("admin").build();
        Assertions.assertNotEquals(rsp, user);
        Assertions.assertNotEquals(rsp, "string");
    }

        /**
     * 对应测试用例 1.8
     */
    @Test
    void equals_sameValueButDifferentSubclassNotEqual() {
        UserRsp rsp = UserRsp.builder().userName("admin").build();
        UserDto dto = UserDto.builder().userName("admin").build();
        Assertions.assertNotEquals(rsp, dto);
    }

}
