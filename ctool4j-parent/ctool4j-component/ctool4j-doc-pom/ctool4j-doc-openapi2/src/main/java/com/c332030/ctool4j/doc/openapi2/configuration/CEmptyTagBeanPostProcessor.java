package com.c332030.ctool4j.doc.openapi2.configuration;

import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import lombok.CustomLog;
import lombok.val;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CEmptyTagBeanPostProcessor：swagger 模型生成后，清除"声明了却没有任何接口引用"的空分组
 * </p>
 *
 * <p>背景（springfox 的硬编码行为）：swagger 分组名由 {@code WebMvcRequestHandler#groupName()} 直接调用
 * {@code ControllerNamingUtils.controllerNameAsGroup(handlerMethod)} 得到——即控制器类名 splitCamelCase("-") 转小写
 * （{@code WeComController} → {@code we-com-controller}），<b>不经过 {@code ResourceGroupingStrategy} 插件</b>，
 * 故注解与插件都无法把分组名改成 {@code @CTag} 值；而接口自身的 tag 由 {@code @CTag} 决定。两者不一致时，
 * 分组列表里就会出现"有分组名、没有任何接口"的英文空分组（swagger-ui 表现为中文分组 + 英文空分组并存）。</p>
 *
 * <p>本处理器在文档模型生成的最后一步介入（{@code ServiceModelToSwagger2Mapper#mapDocumentation}，
 * 该 bean 是容器 Bean，可被 Spring AOP 代理），仅做一件事：把无人引用的分组声明移除，使分组列表与接口 tag 一致。
 * 只删空分组，接口与其 tag 一律不动。</p>
 *
 * <p>与文档开关的关系：knife4j 关闭文档（生产环境禁用）时其自动配置不装配，容器内没有
 * {@code ServiceModelToSwagger2Mapper} Bean，本处理器不介入、不报错。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>包装 {@code ServiceModelToSwagger2Mapper} Bean，拦截 {@code mapDocumentation} 的返回值。</li>
 *   <li>收集全部接口实际引用的 tag 名（{@code paths[*][*].tags}），清除未被引用的 {@code tags} 声明。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>为什么在模型生成后处理，而不是改分组名：分组名在 {@code WebMvcRequestHandler}（非 Bean，由
 *   provider 直接 new）里由类名产生，AOP 与插件都进不去；而 {@code ServiceModelToSwagger2Mapper}
 *   是 Bean，且其输出即最终 JSON 模型，是这条链路上唯一干净的可拦截点。</li>
 *   <li>不做"改分组名"：那需要重建 {@code Documentation} 的分组键或包装 handler，侵入 springfox 内部；
 *   只删空分组即可让 UI 上只剩接口真正使用的分组（{@code @CTag} 中文分组），信息零丢失。</li>
 *   <li>代理方式：{@code ServiceModelToSwagger2Mapper} 是 MapStruct 生成的<b>抽象类</b>（非接口），
 *   故用 {@code ProxyFactory} + CGLIB（{@code setProxyTargetClass(true)}）代理其实现实例。</li>
 *   <li>调用面：{@code BeanPostProcessor} 由容器对<b>每个 Bean</b> 调用一次（Spring 无"按类型订阅"的钩子），
 *   故本类只做一次 {@code instanceof} 判定：仅目标类型创建代理，其余 Bean 原对象返回（不包装、不代理）；
 *   单次判定在启动期即完成，请求期仅目标 Bean 多一层方法调用，开销可忽略。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>返回值为空，或 {@code tags} / {@code paths} 为 null 或空</td>
 *     <td>原样返回，不做任何处理（无接口信息时不判定引用关系，避免误删）</td>
 *   </tr>
 *   <tr>
 *     <td>没有空分组</td>
 *     <td>不打印日志、不改动模型</td>
 *   </tr>
 *   <tr>
 *     <td>文档 Bean 不存在（knife4j 关闭文档：生产环境禁用时其自动配置不装配，容器内没有
 *     {@code ServiceModelToSwagger2Mapper}）</td>
 *     <td>本处理器不介入、不报错（无 Bean 匹配，全部原样返回）</td>
 *   </tr>
 *   <tr>
 *     <td>代理创建失败（如实现类后续版本变为 final）</td>
 *     <td>记录 warn 并按原 Bean 返回（退化为 springfox 默认，不阻断启动）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>OpenAPI2（springfox）文档：接口 tag 与分组名不一致而产生英文空分组的场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>新项目建议 OpenAPI3（springdoc），其分组模型不依赖此机制。</li>
 *   <li>需要"按分组名排序 / 自定义分组名"的场景本处理器不涉及，只做删除空分组。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 springfox 的 {@code ServiceModelToSwagger2Mapper} 与 swagger-models 的模型结构
 *   （{@code Swagger#tags}、{@code paths[*][*].tags}），升级 springfox/knife4j 大版本时需回归。</li>
 *   <li>删除空分组后，分组在 UI 上的顺序改由接口 tag 决定（不再受 springfox 分组声明顺序影响）。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/16
 * @version 1.1
 */
@CustomLog
public class CEmptyTagBeanPostProcessor implements BeanPostProcessor {

    /**
     * 拦截的方法名：{@code ServiceModelToSwagger2Mapper#mapDocumentation}
     */
    private static final String METHOD_MAP_DOCUMENTATION = "mapDocumentation";

    /**
     * 初始化后处理：包装 swagger 模型映射器，生成文档后清除空分组
     *
     * @param bean     后置处理对象
     * @param beanName Bean 名称
     * @return 包装后的映射器（非映射器原样返回）
     */
    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {

        // BeanPostProcessor 会被容器内每个 Bean 走一次（Spring 无"按类型订阅"的钩子）：此处只做一次 instanceof 判定，
        // 仅目标类型创建代理，其余 Bean 原对象返回，零副作用
        if (!(bean instanceof ServiceModelToSwagger2Mapper)) {
            return bean;
        }

        try {
            val factory = new ProxyFactory(bean);
            // 目标实现类是 MapStruct 生成的抽象类（非接口），只能走 CGLIB 代理
            factory.setProxyTargetClass(true);
            factory.addAdvice((MethodInterceptor) invocation -> {

                val result = invocation.proceed();
                if (result instanceof Swagger && METHOD_MAP_DOCUMENTATION.equals(invocation.getMethod().getName())) {
                    try {
                        removeUnreferencedTags((Swagger) result);
                    } catch (Throwable e) {
                        // 清理失败不应影响文档接口：保留 springfox 原模型返回
                        log.warn("清除无接口引用的空分组失败，已忽略", e);
                    }
                }
                return result;
            });

            log.debug("包装 swagger 模型映射器，启用空分组清理：{}", beanName);
            return factory.getProxy();
        } catch (Throwable e) {
            // 文档增强失败不应阻断启动：原样返回（退化为 springfox 默认，可能出现英文空分组）
            log.warn("包装 swagger 模型映射器失败，本次不清理空分组：{}", beanName, e);
            return bean;
        }
    }

    /**
     * 清除"声明了但没有任何接口引用"的分组声明
     *
     * @param swagger 文档模型
     */
    private static void removeUnreferencedTags(Swagger swagger) {

        val tags = swagger.getTags();
        if (null == tags || tags.isEmpty()) {
            return;
        }

        // 无接口信息（paths 为空）时无法判定引用关系，不做清理（避免误删业务声明的占位分组）
        val paths = swagger.getPaths();
        if (null == paths || paths.isEmpty()) {
            return;
        }

        val referenced = referencedTagNames(swagger);
        val kept = tags.stream()
            .filter(tag -> referenced.contains(tag.getName()))
            .collect(Collectors.toList());

        if (kept.size() == tags.size()) {
            return;
        }

        val removed = tags.stream()
            .filter(tag -> !referenced.contains(tag.getName()))
            .map(Tag::getName)
            .collect(Collectors.toList());

        swagger.setTags(kept);
        log.debug("清除无接口引用的空分组：{}（springfox 分组名由控制器类名硬编码，与接口 @CTag 不一致时会形成空分组）", removed);
    }

    /**
     * 收集接口实际引用的 tag 名称（{@code paths[*][*].tags}）
     *
     * @param swagger 文档模型
     * @return tag 名称集合
     */
    private static Set<String> referencedTagNames(Swagger swagger) {

        val names = new HashSet<String>();

        val paths = swagger.getPaths();
        if (null == paths) {
            return names;
        }

        paths.values().forEach(path -> {
            val operations = path.getOperations();
            if (null == operations) {
                return;
            }
            operations.forEach(operation -> {
                val tags = operation.getTags();
                if (null == tags) {
                    return;
                }
                names.addAll(tags);
            });
        });

        return names;
    }

}
