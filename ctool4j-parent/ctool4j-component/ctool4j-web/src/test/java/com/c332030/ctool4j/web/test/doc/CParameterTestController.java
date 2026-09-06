package com.c332030.ctool4j.web.test.doc;

import com.c332030.ctool4j.web.doc.annotation.CNotRequired;
import com.c332030.ctool4j.web.doc.annotation.CParameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Description: CParameterMethodArgumentResolver 集成测试用 Controller：仅标注 @CParameter（不标 @RequestParam），
 * 验证默认必填、叠加 @CNotRequired 非必填（required 由 CNotRequired 决定）
 * </p>
 *
 * @author c332030
 * @see "doc/design/web/CParameterMethodArgumentResolverTests.adoc"
 */
@RestController
public class CParameterTestController {

    /**
     * 默认必填（未标 @CNotRequired）：缺参应报错
     *
     * @param name 参数名
     * @return 成功标识
     */
    @GetMapping("/c-parameter/required-true")
    public String requiredTrue(
        @CParameter("名称") String name
    ) {
        return name;
    }

    /**
     * 非必填（标 @CNotRequired）：缺参放行返回 null
     *
     * @param name 参数名
     * @return 成功标识
     */
    @GetMapping("/c-parameter/required-false")
    public String requiredFalse(
        @CNotRequired
        @CParameter("名称") String name
    ) {
        return name;
    }

    /**
     * 不写任何非必填标记：默认必填（与 @RequestParam.required 默认一致）
     *
     * @param name 参数名
     * @return 成功标识
     */
    @GetMapping("/c-parameter/required-default")
    public String requiredDefault(
        @CParameter("名称") String name
    ) {
        return name;
    }

    /**
     * 已有 @RequestParam 时：@CParameter 仅作文档，由 @RequestParam 控制绑定
     *
     * @param name 参数名
     * @return 成功标识
     */
    @GetMapping("/c-parameter/with-request-param")
    public String withRequestParam(
        @org.springframework.web.bind.annotation.RequestParam(value = "name", required = false)
        @CParameter("名称") String name
    ) {
        return name;
    }

}
