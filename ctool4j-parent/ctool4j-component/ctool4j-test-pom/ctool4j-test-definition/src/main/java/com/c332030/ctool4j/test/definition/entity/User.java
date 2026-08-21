package com.c332030.ctool4j.test.definition.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: User
 * </p>
 *
 * @since 2025/11/21
 * @see doc/design/test/User.adoc
 * @see doc/design/test/UserTests.adoc
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    String userName;
    String password;
    Integer age;

}
