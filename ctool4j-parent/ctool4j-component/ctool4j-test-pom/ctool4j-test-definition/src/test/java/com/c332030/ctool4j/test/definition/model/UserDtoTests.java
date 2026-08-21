package com.c332030.ctool4j.test.definition.model;

import com.c332030.ctool4j.test.definition.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * <p>
 * Description: UserDtoTests
 * </p>
 *
 * @since 2026/8/14
 */
class UserDtoTests {

        /**
     * 对应测试用例 1.1
     */
    @Test
    void noArgsConstructor() {
        UserDto dto = new UserDto();
        Assertions.assertNotNull(dto);
        Assertions.assertNull(dto.getUserName());
        Assertions.assertNull(dto.getSex());
    }

        /**
     * 对应测试用例 1.2
     */
    @Test
    void superBuilder_inheritanceWiring() {
        Map<String, String> tags = new HashMap<>();
        tags.put("k", "v");
        Date createTime = new Date(1700000000000L);
        UserDto dto = UserDto.builder()
                // 父类字段
                .userName("admin")
                .password("pwd")
                .age(18)
                // 子类字段
                .sex(1)
                .status("active")
                .amount(100)
                .score(90)
                .grade(5L)
                .createTime(createTime)
                .updateTime("2026-01-01")
                .roles(Arrays.asList("r1", "r2"))
                .tags(tags)
                .build();
        Assertions.assertEquals("admin", dto.getUserName());
        Assertions.assertEquals("pwd", dto.getPassword());
        Assertions.assertEquals(18, dto.getAge());
        Assertions.assertEquals(1, dto.getSex());
        Assertions.assertEquals("active", dto.getStatus());
        Assertions.assertEquals(100, dto.getAmount());
        Assertions.assertEquals(90, dto.getScore());
        Assertions.assertEquals(5L, dto.getGrade());
        Assertions.assertEquals(createTime, dto.getCreateTime());
        Assertions.assertEquals("2026-01-01", dto.getUpdateTime());
        Assertions.assertEquals(Arrays.asList("r1", "r2"), dto.getRoles());
        Assertions.assertEquals(tags, dto.getTags());
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    void superBuilder_defaultNullFields() {
        UserDto dto = UserDto.builder().userName("admin").build();
        Assertions.assertEquals("admin", dto.getUserName());
        Assertions.assertNull(dto.getPassword());
        Assertions.assertNull(dto.getSex());
        Assertions.assertNull(dto.getCreateTime());
        Assertions.assertNull(dto.getRoles());
        Assertions.assertNull(dto.getTags());
    }

        /**
     * 对应测试用例 1.4
     */
    @Test
    void equals_sameValues() {
        UserDto a = UserDto.builder().userName("admin").sex(1).build();
        UserDto b = UserDto.builder().userName("admin").sex(1).build();
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }

        /**
     * 对应测试用例 1.5
     */
    @Test
    void equals_differentField() {
        UserDto a = UserDto.builder().userName("admin").sex(1).build();
        UserDto b = UserDto.builder().userName("admin").sex(2).build();
        Assertions.assertNotEquals(a, b);
    }

        /**
     * 对应测试用例 1.6
     */
    @Test
    void equals_null() {
        UserDto a = UserDto.builder().userName("admin").build();
        Assertions.assertNotEquals(a, null);
    }

        /**
     * 对应测试用例 1.7
     */
    @Test
    void equals_otherType() {
        UserDto dto = UserDto.builder().userName("admin").sex(1).build();
        User user = User.builder().userName("admin").build();
        Assertions.assertNotEquals(dto, user);
        Assertions.assertNotEquals(dto, "string");
    }

        /**
     * 对应测试用例 1.8
     */
    @Test
    void equals_rolesCollection() {
        UserDto a = UserDto.builder().userName("admin").roles(Arrays.asList("r1", "r2")).build();
        UserDto b = UserDto.builder().userName("admin").roles(Arrays.asList("r1", "r2")).build();
        Assertions.assertEquals(a, b);
    }

        /**
     * 对应测试用例 1.9
     */
    @Test
    void equals_emptyRolesVsNull() {
        UserDto a = UserDto.builder().userName("admin").roles(Collections.emptyList()).build();
        UserDto b = UserDto.builder().userName("admin").build();
        Assertions.assertNotEquals(a, b);
    }

}
