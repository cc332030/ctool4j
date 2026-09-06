package com.c332030.ctool4j.web.doc;

import com.c332030.ctool4j.web.doc.annotation.CParameter;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;

/**
 * <p>
 * Description: CParameterMethodArgumentResolver：将标注 {@code @CParameter} 的方法参数按
 * {@code @RequestParam} 语义解析（name/required 取自 {@code @CParameter}，与 {@code @RequestParam} 一致），
 * 使文档注解同时驱动 SpringMVC 参数绑定，避免重复标注 required
 * </p>
 *
 * <p>
 * 仅接管「标注 {@code @CParameter}、未标注 {@code @RequestParam}、简单类型」的参数；
 * 其余参数（含已有 {@code @RequestParam}、复杂对象等）交由默认解析器处理。
 * </p>
 *
 * @see "doc/design/web/CParameterMethodArgumentResolver.adoc"
 * @see "doc/design/web/CParameterMethodArgumentResolverTests.adoc"
 * @since 2026/9/6
 */
public class CParameterMethodArgumentResolver extends RequestParamMethodArgumentResolver {

    /**
     * 构造解析器（不使用默认解析兜底，仅处理标注 @CParameter 的参数）
     */
    public CParameterMethodArgumentResolver() {
        super(false);
    }

    /**
     * 是否支持解析该参数
     *
     * @param parameter 方法参数
     * @return 是否支持
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        if (!parameter.hasParameterAnnotation(CParameter.class)) {
            return false;
        }
        // 已有 @RequestParam 时交给标准绑定注解解析，@CParameter 仅作文档
        if (parameter.hasParameterAnnotation(RequestParam.class)) {
            return false;
        }

        parameter = parameter.nestedIfOptional();
        return BeanUtils.isSimpleProperty(parameter.getNestedParameterType());
    }

    /**
     * 构造参数名/必填/默认值信息
     *
     * @param parameter 方法参数
     * @return 参数名/必填/默认值信息
     */
    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        val cParameter = parameter.getParameterAnnotation(CParameter.class);
        // name 为空时由父类按参数名兜底（与 @RequestParam 不写 name 一致）
        return new NamedValueInfo(cParameter.name(), cParameter.required(), null);
    }

}
