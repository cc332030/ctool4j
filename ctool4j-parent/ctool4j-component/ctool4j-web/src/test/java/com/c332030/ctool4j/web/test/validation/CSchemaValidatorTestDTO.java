package com.c332030.ctool4j.web.test.validation;

import com.c332030.ctool4j.web.doc.annotation.CSchema;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Description: CSchema 校验集成测试用 DTO：字段用 {@code @CSchema} 标注（required=true 必填、默认非必填），
 * 用于在启动 Spring 容器后经真实接口（MockMvc、@Valid @RequestBody）验证接口必填/非必填生效
 * </p>
 *
 * @author c332030
 * @see "doc/design/web/CSchemaValidatorTestDTO.adoc"
 */
@Getter
@Setter
public class CSchemaValidatorTestDTO {

    /**
     * 必填字段（含描述）
     */
    @CSchema(value = "用户名", required = true)
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
