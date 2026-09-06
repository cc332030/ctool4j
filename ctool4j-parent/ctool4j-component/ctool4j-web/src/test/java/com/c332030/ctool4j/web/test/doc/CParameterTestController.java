package com.c332030.ctool4j.web.test.doc;

import com.c332030.ctool4j.web.doc.annotation.CParameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Description: CParameterMethodArgumentResolver 集成测试用 Controller：仅标注 @CParameter（不标 @RequestParam），
 * 验证 required 同时驱动 SpringMVC 绑定
 * </p>
 *
 * @author c332030
 * @see "doc/design/web/CParameterMethodArgumentResolverTests.adoc"
 */
@RestController
public class CParameterTestController {

    /**
     * required=true（默认必填）：缺参应报错
     *
     * @param name 参数名
     * @return 成功标识
     */
    @GetMapping("/c-parameter/required-true")
    public String requiredTrue(
        @CParameter(value = "名称", required = true) String name
    ) {
        return name;
    }

    /**
     * required=false（非必填）：缺参放行返回 null
     *
     * @param name 参数名
     * @return 成功标识
     */
    @GetMapping("/c-parameter/required-false")
    public String requiredFalse(
        @CParameter(value = "名称", required = false) String name
    ) {
        return name;
    }

    /**
     * 不写 required：默认必填（与 @RequestParam.required 默认一致）
     *
     * @param name 参数名
     * @return 成功标识
     */
    @GetMapping("/c-parameter/required-default")
    public String requiredDefault(
        @CParameter(value = "名称") String name
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
        @CParameter(value = "名称") String name
    ) {
        return name;
    }

}
