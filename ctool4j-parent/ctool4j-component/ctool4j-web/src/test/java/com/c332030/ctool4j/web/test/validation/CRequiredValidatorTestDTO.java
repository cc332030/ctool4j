package com.c332030.ctool4j.web.test.validation;

import com.c332030.ctool4j.web.doc.annotation.CSchema;
import com.c332030.ctool4j.web.validation.annotation.CRequired;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Description: CRequired 校验集成测试用 DTO：字段用 {@code @CRequired}（必填）+ {@code @CSchema}（描述）标注，
 * 用于在启动 Spring 容器后经真实接口（MockMvc、@Valid @RequestBody）验证接口必填/非必填生效
 * </p>
 *
 * @author c332030
 * @see "doc/design/web/CRequiredValidatorTestDTO.adoc"
 */
@Getter
@Setter
public class CRequiredValidatorTestDTO {

    /**
     * 必填字段（含描述）
     */
    @CRequired
    @CSchema("用户名")
    private String username;

    /**
     * 非必填字段（含描述）
     */
    @CSchema("备注")
    private String remark;

    /**
     * 无描述的非必填字段
     */
    @CSchema
    private String other;

}
