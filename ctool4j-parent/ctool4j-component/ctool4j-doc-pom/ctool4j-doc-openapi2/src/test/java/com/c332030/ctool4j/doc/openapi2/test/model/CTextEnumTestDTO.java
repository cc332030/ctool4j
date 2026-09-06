package com.c332030.ctool4j.doc.openapi2.test.model;

import com.c332030.ctool4j.doc.annotation.CSchema;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Description: 枚举 text 展示集成测试用 DTO：含实现 ICText 的枚举字段，
 * 验证 model 字段枚举允许值保持可提交的「枚举名」、description 展示「枚举名(text)」
 * </p>
 *
 * @author c332030
 * @see "doc/design/openapi2/CTextEnumIntegrationTests.adoc"
 */
@Getter
@Setter
public class CTextEnumTestDTO {

    /**
     * 实现 ICText 的枚举字段（无 @CSchema：text 说明应写入 description）
     */
    private CRequestHeaderEnum header;

    /**
     * 实现 ICText 的枚举字段（带 @CSchema 自定义描述：description 保持自定义，不被 text 覆盖）
     */
    @CSchema("带描述枚举头")
    private CRequestHeaderEnum headerWithSchema;

}
