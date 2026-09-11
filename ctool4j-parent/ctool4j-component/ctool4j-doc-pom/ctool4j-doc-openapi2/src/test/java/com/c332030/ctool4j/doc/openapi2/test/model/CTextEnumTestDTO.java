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
 * <h2>功能说明</h2>
 * <p>{@code CTextEnumTestDTO} 为枚举 text 展示集成测试辅助 DTO，含实现 {@code ICText} 的枚举字段（类型 {@code CRequestHeaderEnum}）：</p>
 * <ul>
 *   <li>{@code header}：未标注 @CSchema，验证允许值保持可提交「枚举名」、description 展示「枚举名(text)」。</li>
 *   <li>{@code headerWithSchema}：标注 {@code @CSchema("带描述枚举头")}，验证自定义描述不被 text 说明覆盖。</li>
 * </ul>
 * <h2>适用场景</h2>
 * <ul>
 *   <li>{@code CTextEnumIntegrationTests} 集成测试，验证 model 字段枚举（@RequestBody DTO）文档展示。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助类。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
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
