package com.c332030.ctool4j.doc.openapi2.test;

import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * <p>
 * Description: CSchema 集成测试用配置：激活 springfox Swagger2 文档端点（/v2/api-docs），
 * 供测试验证字段文档生效。springfox 2.10.5 使用 {@code @EnableSwagger2WebMvc} 激活（替代 2.9 的 @EnableSwagger2）
 * </p>
 *
 * <h2>适用场景</h2>
 * <ul>
 *   <li>{@code CSchemaIntegrationTests} 集成测试，激活 Swagger2 文档端点以验证字段文档生成。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助配置；依赖 springfox 2.10.5 的注解激活方式。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@Configuration
@EnableSwagger2WebMvc
public class CSchemaTestConfiguration {

}
