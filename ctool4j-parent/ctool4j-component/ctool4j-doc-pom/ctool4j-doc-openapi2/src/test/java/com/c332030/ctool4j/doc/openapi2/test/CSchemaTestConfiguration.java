package com.c332030.ctool4j.doc.openapi2.test;

import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * <p>
 * Description: CSchema 集成测试用配置：激活 springfox Swagger2 文档端点（/v2/api-docs），
 * 供测试验证字段文档生效。springfox 2.10.5 使用 {@code @EnableSwagger2WebMvc} 激活（替代 2.9 的 @EnableSwagger2）
 * </p>
 *
 * @author c332030
 * @see doc/design/openapi2/CSchemaTestConfiguration.adoc
*/
@Configuration
@EnableSwagger2WebMvc
public class CSchemaTestConfiguration {

}
