package com.c332030.ctool4j.test.definition.model;

import com.c332030.ctool4j.test.definition.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * <p>
 * Description: UserRsp
 * </p>
 *
 * @since 2025/11/21
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserRsp extends User {

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
