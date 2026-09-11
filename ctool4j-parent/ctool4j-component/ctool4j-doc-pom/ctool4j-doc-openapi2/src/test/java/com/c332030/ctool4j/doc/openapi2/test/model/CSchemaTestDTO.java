package com.c332030.ctool4j.doc.openapi2.test.model;

import com.c332030.ctool4j.doc.annotation.CSchema;
import com.c332030.ctool4j.web.validation.annotation.CRequired;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Description: CRequired + CSchema 集成测试用 DTO：字段用 @CRequired（必填）+ @CSchema（描述）标注，
 * 用于验证接口必填/非必填、字段文档、字段必填生效
 * </p>
 *
 * <h2>功能说明</h2>
 * <p>{@code CSchemaTestDTO} 为 {@code CRequired}/{@code CSchema} 集成测试辅助 DTO，字段用 {@code @CRequired}（必填）+ {@code @CSchema}（描述）标注：</p>
 * <ul>
 *   <li>{@code username}：必填字段（{@code @CRequired}，含描述 {@code @CSchema("用户名")}）</li>
 *   <li>{@code remark}：非必填字段（含描述）</li>
 *   <li>{@code other}：无描述的非必填字段</li>
 * </ul>
 * <p>用于验证接口必填/非必填、字段文档生成、字段必填校验生效。</p>
 * <h2>适用场景</h2>
 * <ul>
 *   <li>{@code CSchemaIntegrationTests} 集成测试，验证 {@code @CRequired}/{@code @CSchema} 注解在真实接口（{@code CSchemaTestController}）上的必填校验与字段文档。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助类；字段设计覆盖必填/非必填/有描述/无描述多种形态。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
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
