package com.c332030.ctool4j.test.definition.model;

import com.c332030.ctool4j.test.definition.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * <p>
 * Description: UserDto
 * </p>
 *
 * @since 2025/11/21
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends User {

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
