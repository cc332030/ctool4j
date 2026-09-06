package com.c332030.ctool4j.doc.openapi2.test.model;

import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Description: 枚举 text 展示集成测试用 DTO：含实现 ICText 的枚举字段，
 * 验证 model 字段枚举值在文档中显示为「枚举名(text)」
 * </p>
 *
 * @author c332030
 * @see "doc/design/openapi2/CTextEnumIntegrationTests.adoc"
 */
@Getter
@Setter
public class CTextEnumTestDTO {

    /**
     * 实现 ICText 的枚举字段
     */
    private CRequestHeaderEnum header;

}
