package com.c332030.ctool4j.doc.openapi2.test.controller;

import com.c332030.ctool4j.doc.openapi2.test.model.CSchemaTestDTO;
import com.c332030.ctool4j.doc.annotation.COperation;
import com.c332030.ctool4j.doc.annotation.CTag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * Description: CSchema 集成测试用 Controller：@RequestBody DTO（@Valid 触发字段校验）接收
 * {@code @CSchema} 标注字段，用于验证接口必填/非必填与字段必填生效；标注 @CTag（类级分组，替代 @Api）
 * 与 @COperation 供 springfox 生成接口文档
 * </p>
 *
 * <h2>功能说明</h2>
 * <p>{@code CSchemaTestController} 为 {@code CRequired}/{@code CSchema} 集成测试辅助 Controller（{@code @RestController}，标注 {@code @CTag} 供 Docket 收集）：</p>
 * <ul>
 *   <li>{@code test(@Valid @RequestBody CSchemaTestDTO dto)}：POST {@code /c-schema/test}，接收 {@code @CRequired} 标注的 DTO，{@code @Valid} 触发字段校验（username {@code @CRequired} 必填），标注 {@code @COperation} 供 springfox 生成接口文档</li>
 * </ul>
 * <h2>适用场景</h2>
 * <ul>
 *   <li>{@code CSchemaIntegrationTests} 集成测试，作为真实接口入口验证 {@code @CRequired} 必填校验与字段文档生成。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助类；仅提供单个测试接口。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@Validated
@RestController
@CTag("CSchema 集成测试")
public class CSchemaTestController {

    /**
     * 接收 @CSchema 标注的 DTO，@Valid 触发字段校验（username required=true 必填）
     *
     * @param dto 请求体
     * @return 成功标识
     */
    @COperation("CSchema 校验接口")
    @PostMapping("/c-schema/test")
    public String test(@Valid @RequestBody CSchemaTestDTO dto) {
        return "ok";
    }

}
