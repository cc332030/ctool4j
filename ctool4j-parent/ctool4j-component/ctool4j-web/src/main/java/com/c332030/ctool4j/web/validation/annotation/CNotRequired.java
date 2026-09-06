package com.c332030.ctool4j.web.validation.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 请求参数非必填标记注解（仅 request param / 方法参数级生效），标注即文档标记非必填
 * </p>
 *
 * <p>
 * 独立生效（无需同时标注 @CParameter）：标注本注解即文档非必填，由 openapi2 文档插件据此标记非必填；
 * request param 默认必填（与 {@code @RequestParam.required} 默认一致），需要非必填时标注本注解。
 * 实际缺参放行由 {@code @RequestParam(required = false)} 控制（本注解不参与 SpringMVC 绑定）。
 * </p>
 *
 * <p>
 * 纯标记注解，无属性。
 * </p>
 *
 * @see "doc/design/web/CNotRequired.adoc"
 * @since 2026/9/6
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CNotRequired {
}
