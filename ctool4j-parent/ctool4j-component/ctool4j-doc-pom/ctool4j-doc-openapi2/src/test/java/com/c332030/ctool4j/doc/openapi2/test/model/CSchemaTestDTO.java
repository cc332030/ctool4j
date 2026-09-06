package com.c332030.ctool4j.doc.openapi2.test.model;

import com.c332030.ctool4j.web.doc.annotation.CSchema;
import com.c332030.ctool4j.web.validation.annotation.CRequired;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Description: CRequired + CSchema 集成测试用 DTO：字段用 @CRequired（必填）+ @CSchema（描述）标注，
 * 用于验证接口必填/非必填、字段文档、字段必填生效
 * </p>
 *
 * @author c332030
 * @see "doc/design/openapi2/CSchemaTestDTO.adoc"
*/
@Getter
@Setter
public class CSchemaTestDTO {

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
