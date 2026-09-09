package com.c332030.ctool4j.definition.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Description: 内部接口标记注解
 * </p>
 *
 * <p>标注在 Controller 类或其接口方法上，将该接口标记为"内部接口"。内部接口默认仅允许配置的内部
 * IP/IP 段（CIDR）访问，由 ctool4j-web 的 {@code CInnerApiInterceptor} 在请求进入时读取并做 IP 白名单校验。</p>
 *
 * <p>白名单规则通过 {@code CInnerApiConfig} 配置类统一提供（所有标注本注解的内部接口共用），
 * 未配置白名单（为空）时全部放行；注解本身不带 IP 值，仅作"内部接口"的声明标记。</p>
 *
 * <p>标注位置说明：标注在方法上仅该接口标记为内部接口；标注在类上该类下所有接口方法均标记。
 * 标注于类级时配合 {@code @Inherited}，若标在父类（抽象基类）类上，继承它的子类也会被识别为内部接口。</p>
 *
 * @author c332030
 * @see "doc/design/definition/CInnerApi.adoc"
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CInnerApi {

}
