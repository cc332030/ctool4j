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
 * <h2>能力目录</h2>
 * <p>{@code CInnerApi}：内部接口标记注解。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>支持类级（{@code ElementType.TYPE}）与方法级（{@code ElementType.METHOD}）标注：标在方法上仅该接口标记为内部接口；标在类上该类下所有接口方法均标记。</li>
 *   <li>{@code @Inherited}：标注在抽象父类类级时，子类自动继承（{@code isAnnotationPresent} 向上查找），无需在子类重复标注。</li>
 *   <li>纯标记注解，无 {@code value}：IP 白名单由配置类统一提供，所有标注注解的内部接口共用同一份配置，避免逐接口维护 IP。</li>
 *   <li>命名 {@code CInnerApi}（内部接口），与"标记为内部接口"语义一致；IP 白名单是拦截器对内部接口的默认保护，不体现在注解名。</li>
 *   <li>声明放 ctool4j-definition 纯注解模块：不依赖 web/servlet，使 {@code CMpController}（mybatis-base，仅依赖 definition）等基类可直接标注而不引入 web 依赖。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>未配置白名单（{@code allowed-ips} 为空）时，拦截器直接放行，不影响正常内部访问。</li>
 * </ul>
 * <h2>适用范围</h2>
 * <p>内部管理类接口、继承 {@code CMpController} 的具体 Controller，需限制仅内网 IP 调用。</p>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>对外公开接口不应标注；若需接口对外可访问则不应标记为内部接口。</li>
 *   <li>仅作声明标记，无运行时自身行为；是否生效完全取决于消费方拦截器是否装配。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li><b>纯标记注解，不携带 IP</b>：想"给单个接口单独指定 IP"无法通过本注解表达，需靠 {@code CInnerApiConfig} 全局白名单（取舍：所有内部接口共用同一份白名单）。</li>
 *   <li><b>标注 ≠ 立即收紧</b>：注解本身不产生拦截；需依赖 ctool4j-web 的 {@code CInnerApiInterceptor}（被启用时）读取，且白名单非空才真正收紧。</li>
 *   <li><b>{@code @Inherited} 仅对类级标注生效</b>：标在父类类级时子类自动继承；方法级标注不随方法继承传播，需标在具体方法上。</li>
 *   <li>{@code @Target} 仅 {@code TYPE}/{@code METHOD}：标注在字段、参数等其它位置不生效，编译器会报错。</li>
 * </ul>
 * <h2>波及影响</h2>
 * <ul>
 *   <li>注解声明于 ctool4j-definition，可被 {@code CMpController}（mybatis-base）等引用；若将来扩充注解属性或语义，需同步评估对 web 拦截器及各使用方的影响。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CInnerApi {

}
