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
 * <h2>能力目录</h2>
 * <p>{@code User}：用户测试实体。使用 lombok 生成构造器/builder/访问器。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>lombok {@code @Data}/{@code @SuperBuilder}/{@code @NoArgsConstructor}/{@code @AllArgsConstructor} 生成模型能力。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无特殊兜底（简单数据模型）。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <p>用户测试实体（测试支撑数据模型）。</p>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>简单测试支撑类。</li>
 * </ul>
 *
 * @since 2025/11/21
 * @version 1.0
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
