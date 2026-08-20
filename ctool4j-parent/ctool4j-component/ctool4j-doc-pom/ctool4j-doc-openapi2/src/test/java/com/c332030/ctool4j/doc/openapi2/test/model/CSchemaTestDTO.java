package com.c332030.ctool4j.doc.openapi2.test.model;

import com.c332030.ctool4j.web.validation.annotation.CSchema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Description: CSchema 集成测试用 DTO：字段用 @CSchema 标注（required=true 必填、默认非必填），
 * 用于验证接口必填/非必填、字段文档、字段必填生效
 * </p>
 *
 * @author c332030
 */
@Getter
@Setter
public class CSchemaTestDTO {

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
