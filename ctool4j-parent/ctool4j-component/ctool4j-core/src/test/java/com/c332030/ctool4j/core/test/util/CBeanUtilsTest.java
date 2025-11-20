package com.c332030.ctool4j.core.test.util;

import cn.hutool.core.date.DateUtil;
import com.c332030.ctool4j.core.util.CBeanUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * <p>
 * Description: CBeanUtilsTest
 * </p>
 *
 * @since 2025/11/20
 */
public class CBeanUtilsTest {

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class User {
        String userName;
        String password;
        Integer age;
        Integer sex;
        String status;
        Integer amount;
        Integer score;
        Long grade;
        Date createTime;
        String updateTime;
        Collection<String> roles;
        Map<String, String> tags;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UserRsp {
        String userName;
        String password;
        Integer age;
        String sex;
        Integer status;
        BigDecimal amount;
        Long score;
        Integer grade;
        String createTime;
        Date updateTime;
        Collection<String> roles;
        Map<String, String> tags;
    }

    @Test
    public void copyMerge() {

        val username = "c332030";
        val password = "332030";
        val age = 18;

        val user = User.builder()
                .userName(username)
                .age(age)
                .build();

        val passwordOnly = User.builder()
                .password(password)
                .build();

        val userNew = CBeanUtils.copy(user, User.class);
        CBeanUtils.copy(passwordOnly, userNew);

        Assertions.assertEquals(username, userNew.getUserName());
        Assertions.assertEquals(password, userNew.getPassword());
        Assertions.assertEquals(age, userNew.getAge());

    }

    @Test
    public void copyTypeUnmatched() {

        val intValue = 1;
        val bigDecimalValue = BigDecimal.valueOf(intValue);

        val user1 = User.builder()
                .amount(1)
                .build();

        val userRsp1 = CBeanUtils.copy(user1, UserRsp.class);
        Assertions.assertEquals(bigDecimalValue, userRsp1.getAmount());

        val userRsp2 = UserRsp.builder()
                .amount(bigDecimalValue)
                .build();
        val user2 = CBeanUtils.copy(userRsp2, User.class);
        Assertions.assertNull(user2.getAmount());

    }

    @Test
    public void copyTypeConvert() {

        val sexInt = 1;
        val statusInt = 1;
        val score = 99;
        val grade = new Long(5);

        val dateStr = "2025-11-20 00:00:00";

        val user = User.builder()
                .sex(sexInt)
                .status(String.valueOf(statusInt))
                .score(score)
                .grade(grade)
                .createTime(DateUtil.parse(dateStr))
                .updateTime(dateStr)
                .build();

        val userRsp = CBeanUtils.copy(user, UserRsp.class);
        Assertions.assertEquals(String.valueOf(sexInt), userRsp.getSex());
        Assertions.assertEquals(statusInt, userRsp.getStatus());
        Assertions.assertEquals(score, userRsp.getScore());
        Assertions.assertEquals(grade.intValue(), userRsp.getGrade());
        Assertions.assertEquals(dateStr, userRsp.getCreateTime());
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(userRsp.getUpdateTime()));

    }

    @Test
    public void copySkipCollectionAndMap() {

        val roles = CList.of("role1", "role1");
        val tags = CMap.of("tag1", "tag1");
        val user = User.builder()
                .roles(roles)
                .tags(tags)
                .build();

        val userRsp = CBeanUtils.copy(user, UserRsp.class);
        Assertions.assertNull(userRsp.getRoles());
        Assertions.assertNull(userRsp.getTags());

    }

}
