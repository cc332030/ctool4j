package com.c332030.ctool4j.web.doc.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 请求参数非必填标记注解（仅 request param / 方法参数级生效），标注即非必填
 * </p>
 *
 * <p>
 * 与文档注解族配合使用：request param 默认必填（与 {@code @RequestParam.required} 默认一致），
 * 需要非必填时标注本注解，同时驱动 springfox 文档非必填与 SpringMVC 绑定放行缺参。
 * </p>
 *
 * <p>
 * 纯标记注解，无属性。示例：{@code @CNotRequired @CParameter("名称") String name}。
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
