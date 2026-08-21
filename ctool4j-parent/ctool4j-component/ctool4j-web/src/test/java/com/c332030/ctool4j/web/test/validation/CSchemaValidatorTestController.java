package com.c332030.ctool4j.web.test.validation;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * Description: CSchema 校验集成测试用 Controller：@RequestBody DTO（@Valid 触发字段校验）接收
 * {@code @CSchema} 标注字段，用于验证接口必填/非必填与字段必填生效
 * </p>
 *
 * @author c332030
 * @see "doc/design/web/CSchemaValidatorTestController.adoc"
 */
@Validated
@RestController
public class CSchemaValidatorTestController {

    /**
     * 接收 @CSchema 标注的 DTO，@Valid 触发字段校验
     *
     * @param dto 请求体
     * @return 成功标识
     */
    @PostMapping("/c-schema-validator/test")
    public String test(@Valid @RequestBody CSchemaValidatorTestDTO dto) {
        return "ok";
    }

}
