package com.c332030.ctool4j.doc.openapi2.test.controller;

import com.c332030.ctool4j.doc.openapi2.test.model.CSchemaTestDTO;
import com.c332030.ctool4j.web.doc.annotation.COperation;
import com.c332030.ctool4j.web.doc.annotation.CTag;
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
 * @author c332030
 * @see "doc/design/openapi2/CSchemaTestController.adoc"
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
