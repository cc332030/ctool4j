package com.c332030.ctool4j.web.test.validation;

import com.c332030.ctool4j.web.validation.annotation.CRequired;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Description: CRequired 校验集成测试用 DTO：必填字段用 {@code @CRequired} 标注，
 * 用于在启动 Spring 容器后经真实接口（MockMvc、@Valid @RequestBody）验证接口必填/非必填生效
 * </p>
 *
 * <h2>功能说明</h2>
 * <p>{@code CRequiredValidatorTestDTO} 为 {@code CRequiredValidatorTests} 集成测试辅助 DTO：</p>
 * <ul>
 *   <li>{@code username}：必填字符串（{@code @CRequired}）</li>
 *   <li>{@code remark}：非必填字符串</li>
 *   <li>{@code other}：非必填字符串</li>
 * </ul>
 * <h2>适用场景</h2>
 * <ul>
 *   <li>{@code CRequiredValidatorTests} 集成测试，验证 {@code @CRequired} 注解在真实接口（{@code CRequiredValidatorTestController}）上的必填校验。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助类；字段设计覆盖必填/非必填多种形态，对应真实接口必填与非必填场景。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@Getter
@Setter
public class CRequiredValidatorTestDTO {

    /**
     * 必填字段
     */
    @CRequired
    private String username;

    /**
     * 非必填字段
     */
    private String remark;

    /**
     * 非必填字段
     */
    private String other;

}
