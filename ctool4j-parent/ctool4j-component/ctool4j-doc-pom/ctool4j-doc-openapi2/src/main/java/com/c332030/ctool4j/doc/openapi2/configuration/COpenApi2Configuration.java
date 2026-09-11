package com.c332030.ctool4j.doc.openapi2.configuration;

import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.doc.annotation.CTag;
import com.c332030.ctool4j.doc.openapi2.config.CDocOpenApi2Config;
import com.c332030.ctool4j.doc.openapi2.plugins.operation.impl.COperationAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.operation.impl.CTagAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CNotEmptyAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CParameterAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CRequiredAnnotationPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl.CTextEnumParameterPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.property.impl.CSchemaAnnotationModelPropertyPlugin;
import com.c332030.ctool4j.doc.openapi2.plugins.property.impl.CTextEnumModelPropertyPlugin;
import com.c332030.ctool4j.doc.openapi2.util.CSpringFoxUtils;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import io.swagger.annotations.Api;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: COpenApi2Configuration
 * </p>
 *
 * <p>
 * 本模块基于 springfox（OpenAPI2/Swagger 注解形态）实现。springfox 已停止维护，Knife4j 4.x 建议迁移 OpenAPI3
 * （springdoc-openapi + Knife4j 4.x），保留此实现是为兼容老项目对 OpenAPI2 的依赖，不重复造轮子，
 * 新项目建议直接使用 OpenAPI3；迁移属大工程需排期
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code COpenApi2Configuration}（{@code @Configuration}）提供 OpenAPI2（springfox）文档的自动配置：</p>
 * <ul>
 *   <li>注册 {@code @NotEmpty}、{@code @CSchema} 及 {@code @CTag}/{@code @COperation}/{@code @CParameter} 等参数/属性/操作插件 Bean。</li>
 *   <li>注册 Swagger {@code Docket}（收集标注 {@code @Api} 或 {@code @CTag} 注解的接口：{@code @CTag} 为本族新注解，{@code @Api} 为存量 Swagger 注解，二者兼容纳入）。</li>
 *   <li>注册 BeanPostProcessor 修复 springfox 的 handlerMappings 空指针问题。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>基于 springfox（OpenAPI2），springfox 已停止维护；Knife4j 4.x 建议迁移 OpenAPI3（springdoc-openapi）。保留此实现为兼容老项目，不重复造轮子，新项目建议用 OpenAPI3。</li>
 * </ul>
 * <p><b>插件 Bean</b></p>
 * <ul>
 *   <li>参数/属性/操作/枚举插件 Bean：</li>
 *   <li>{@code cExpanderNotEmpty}、{@code cExpanderCRequired}（参数必填，@CRequired）</li>
 *   <li>{@code cModelPropertyCSchema}（model 属性描述与必填：@CSchema 写描述、@CRequired 标必填）</li>
 *   <li>{@code cModelPropertyTextEnum}（枚举 model 属性：允许值保持枚举名，text 进 description）</li>
 *   <li>{@code cParameterTextEnum}（枚举参数：允许值保持枚举名，text 进 description）</li>
 *   <li>{@code cOperationCOperation}、{@code cOperationCTag}、{@code cParameterCParameter}</li>
 * </ul>
 * <p><b>Docket</b></p>
 * <ul>
 *   <li>{@code @ConditionalOnMissingBean(Docket.class)}，{@code cDocket(config)} 通过 {@code CSpringFoxUtils} 构建。</li>
 *   <li>收集标注 {@code @Api} 或 {@code @CTag} 注解的接口（二者任一命中即纳入，兼容存量 {@code @Api} 与迁移 {@code @CTag}）；全局参数含 {@code AUTHORIZATION} 请求头。</li>
 * </ul>
 * <p><b>springfox 空指针修复</b></p>
 * <ul>
 *   <li>{@code cSpringfoxHandlerProviderBeanPostProcessor}：对 {@code WebMvcRequestHandlerProvider} 过滤掉含 {@code PatternParser} 的 mapping，避免空指针。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>handlerMappings 修复异常</td>
 *     <td>捕获记录 debug 日志</td>
 *   </tr>
 *   <tr>
 *     <td>handlerMappings 字段不存在</td>
 *     <td>返回空列表</td>
 *   </tr>
 *   <tr>
 *     <td>已有 Docket Bean</td>
 *     <td>{@code @ConditionalOnMissingBean} 跳过</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>老项目 OpenAPI2 文档生成。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>新项目建议迁移 OpenAPI3，迁移属大工程需排期。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>springfox 已停维护，长期需迁移 OpenAPI3。</li>
 *   <li>空指针修复依赖反射访问私有字段 {@code handlerMappings}。</li>
 * </ul>
 *
 * @since 2025/12/16
 * @version 1.0
 */
@CustomLog
@Configuration
@Import(value = {
    BeanValidatorPluginsConfiguration.class
})
public class COpenApi2Configuration {

    /**
     * 非空注解插件
     *
     * @return 插件
     */
    @Bean
    public CNotEmptyAnnotationPlugin cExpanderNotEmpty() {
        return new CNotEmptyAnnotationPlugin();
    }

    /**
     * 必填注解插件（@CRequired，方法参数）
     *
     * @return 插件
     */
    @Bean
    public CRequiredAnnotationPlugin cExpanderCRequired() {
        return new CRequiredAnnotationPlugin();
    }

    /**
     * model 属性文档插件：@CSchema 写字段/getter 描述（description），@CRequired 标注即标记必填
     *
     * @return 插件
     */
    @Bean
    public CSchemaAnnotationModelPropertyPlugin cModelPropertyCSchema() {
        return new CSchemaAnnotationModelPropertyPlugin();
    }

    /**
     * 枚举 text 展示 model 属性插件（实现 ICText 的枚举字段，允许值保持枚举名，text 说明进 description）
     *
     * @return 插件
     */
    @Bean
    public CTextEnumModelPropertyPlugin cModelPropertyTextEnum() {
        return new CTextEnumModelPropertyPlugin();
    }

    /**
     * 枚举 text 展示参数插件（实现 ICText 的枚举参数，允许值保持枚举名，text 说明进 description）
     *
     * @return 插件
     */
    @Bean
    public CTextEnumParameterPlugin cParameterTextEnum() {
        return new CTextEnumParameterPlugin();
    }

    /**
     * 操作注解插件（@COperation，方法摘要/说明/operationId）
     *
     * @return 插件
     */
    @Bean
    public COperationAnnotationPlugin cOperationCOperation() {
        return new COperationAnnotationPlugin();
    }

    /**
     * 分组注解插件（@CTag，类级分组 tag）
     *
     * @return 插件
     */
    @Bean
    public CTagAnnotationPlugin cOperationCTag() {
        return new CTagAnnotationPlugin();
    }

    /**
     * 参数注解插件（@CParameter，方法参数 name/description/required/example）
     *
     * @return 插件
     */
    @Bean
    public CParameterAnnotationPlugin cParameterCParameter() {
        return new CParameterAnnotationPlugin();
    }

    /**
     * Swagger Docket（收集标注 {@code @CTag} 注解的接口，替代原生 {@code @Api}）
     *
     * @param config 配置
     * @return Docket
     */
    @Bean
    @ConditionalOnMissingBean(Docket.class)
    public Docket cDocket(CDocOpenApi2Config config) {
        return CSpringFoxUtils.getDocketBuilder()
            .groupName(null)
            .pathMapping(config.getPathMapping())
            .globalOperationParameters(CSpringFoxUtils.globalParameterList(CList.of(
                CRequestHeaderEnum.AUTHORIZATION
            )))
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(Api.class)
                // 兼容：@CTag 为本族新注解；@Api 为存量 Swagger 注解，二者都纳入文档，避免存量接口漏收集
                .or(RequestHandlerSelectors.withClassAnnotation(CTag.class)))
            .build()
            ;
    }

    /**
     * 避免 springfox 报空指针
     *
     * @return BeanPostProcessor
     */
    @Bean
    public static BeanPostProcessor cSpringfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {

            /**
             * 初始化后处理：修复 springfox 的 handlerMappings 空指针问题
             *
             * @param bean     后置处理对象
             * @param beanName Bean 名称
             * @return 处理后的对象
             */
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    try {
                        customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                    } catch (Throwable ex) {
                        log.debug("could not customize springfox handler mappings", ex);
                    }
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                val copy = mappings.stream()
                    .filter(mapping -> mapping.getPatternParser() == null)
                    .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            @SneakyThrows
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                val field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                if (null == field) {
                    return Collections.emptyList();
                }
                field.setAccessible(true);
                return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
            }
        };
    }

}
