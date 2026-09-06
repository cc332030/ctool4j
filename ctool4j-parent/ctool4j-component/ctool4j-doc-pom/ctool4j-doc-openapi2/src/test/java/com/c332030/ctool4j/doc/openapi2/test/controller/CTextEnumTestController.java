package com.c332030.ctool4j.doc.openapi2.test.controller;

import com.c332030.ctool4j.doc.annotation.COperation;
import com.c332030.ctool4j.doc.annotation.CTag;
import com.c332030.ctool4j.doc.openapi2.test.model.CTextEnumTestDTO;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Description: 枚举 text 展示集成测试用 Controller：覆盖 model 字段枚举（@RequestBody DTO）
 * 与 query 参数枚举（@RequestParam），验证两者枚举允许值保持可提交的「枚举名」、
 * 可读的「枚举名(text)」说明写入 description
 * </p>
 *
 * @author c332030
 * @see "doc/design/openapi2/CTextEnumIntegrationTests.adoc"
 */
@RestController
@CTag("枚举 text 集成测试")
public class CTextEnumTestController {

    /**
     * body 字段枚举：DTO 含实现 ICText 的枚举字段
     *
     * @param dto 请求体
     * @return 成功标识
     */
    @COperation("枚举 text body")
    @PostMapping("/c-text-enum/body")
    public String body(@RequestBody CTextEnumTestDTO dto) {
        return "ok";
    }

    /**
     * query 参数枚举：@RequestParam 枚举参数
     *
     * @param header 枚举参数
     * @return 成功标识
     */
    @COperation("枚举 text query")
    @PostMapping("/c-text-enum/query")
    public String query(@RequestParam("header") CRequestHeaderEnum header) {
        return "ok";
    }

}
