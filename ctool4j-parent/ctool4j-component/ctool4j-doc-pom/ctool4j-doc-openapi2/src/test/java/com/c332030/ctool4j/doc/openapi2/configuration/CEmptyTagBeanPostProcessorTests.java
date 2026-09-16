package com.c332030.ctool4j.doc.openapi2.configuration;

import io.swagger.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Documentation;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: CEmptyTagBeanPostProcessor 单元测试：swagger 模型生成后清除「无任何接口引用」的分组声明，
 * 只删空分组、接口与其 tag 不动；非目标类型 Bean 原样返回
 * </p>
 *
 * <ul>
 *   <li>1.1 非目标类型 Bean 原样返回（postProcess_nonTargetBeanReturnedAsIs）</li>
 *   <li>2.1 无接口引用的分组被清除、被引用的保留（mapDocumentation_removesUnreferencedTags）</li>
 *   <li>2.2 分组均被引用时不做改动（mapDocumentation_keepsAllWhenAllReferenced）</li>
 *   <li>2.3 无接口信息（paths 为空）时不清理、不报错（mapDocumentation_noPathsKeepsTagsWithoutError）</li>
 * </ul>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>桩映射器只实现 mapDocumentation（被测逻辑唯一的入口），其余抽象方法空实现，不依赖 Spring 容器。</li>
 *   <li>经被测处理器包装后再调用 mapDocumentation，走与容器内一致的代理链路。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/16
 * @version 1.0
 */
class CEmptyTagBeanPostProcessorTests {

    /**
     * 被测处理器
     */
    private final CEmptyTagBeanPostProcessor processor = new CEmptyTagBeanPostProcessor();

    /**
     * 对应测试用例 1.1：非目标类型 Bean（非 ServiceModelToSwagger2Mapper）原样返回
     */
    @Test
    void postProcess_nonTargetBeanReturnedAsIs() {
        Object bean = new Object();

        Assertions.assertSame(bean, processor.postProcessAfterInitialization(bean, "notMapper"),
            "非目标类型 Bean 应原样返回，不包装、不代理");
    }

    /**
     * 对应测试用例 2.1：无接口引用的分组被清除，被引用的分组保留
     */
    @Test
    void mapDocumentation_removesUnreferencedTags() throws Exception {
        Swagger swagger = swaggerWithTags(
            Arrays.asList(new Tag().name("we-com-controller"), new Tag().name("客户")),
            Collections.singletonMap("/customer", pathWithOperationTags("客户")));

        List<Tag> tags = generate(swagger).getTags();

        Assertions.assertNotNull(tags);
        Assertions.assertEquals(1, tags.size(), "空分组应被清除，实际 " + tags);
        Assertions.assertEquals("客户", tags.get(0).getName(), "接口引用的 tag 应保留");
    }

    /**
     * 对应测试用例 2.2：分组均被接口引用时不做任何改动
     */
    @Test
    void mapDocumentation_keepsAllWhenAllReferenced() throws Exception {
        Swagger swagger = swaggerWithTags(
            Collections.singletonList(new Tag().name("客户")),
            Collections.singletonMap("/customer", pathWithOperationTags("客户")));

        List<Tag> tags = generate(swagger).getTags();

        Assertions.assertEquals(1, tags.size());
        Assertions.assertEquals("客户", tags.get(0).getName());
    }

    /**
     * 对应测试用例 2.3：无接口信息（paths 为空）时不清理、不报错
     *
     * <p>无接口信息时无法判定引用关系，按兜底语义原样返回，避免误删业务声明的占位分组。</p>
     */
    @Test
    void mapDocumentation_noPathsKeepsTagsWithoutError() throws Exception {
        Swagger swagger = swaggerWithTags(Collections.singletonList(new Tag().name("we-com-controller")), null);

        List<Tag> tags = generate(swagger).getTags();

        Assertions.assertNotNull(tags);
        Assertions.assertEquals(1, tags.size(), "无接口信息时不清理（不误删），且不抛异常，实际 " + tags);
    }

    /**
     * 经被测处理器包装后生成文档（等价容器内的代理链路）
     *
     * @param swagger 预置文档模型
     * @return 生成结果
     */
    private Swagger generate(Swagger swagger) throws Exception {
        ServiceModelToSwagger2Mapper mapper = (ServiceModelToSwagger2Mapper)
            processor.postProcessAfterInitialization(new StubSwagger2Mapper(swagger), "swagger2Mapper");

        return mapper.mapDocumentation(null);
    }

    /**
     * 构造文档模型
     *
     * @param tags  分组声明
     * @param paths 接口定义
     * @return 文档模型
     */
    private static Swagger swaggerWithTags(List<Tag> tags, Map<String, Path> paths) {
        Swagger swagger = new Swagger();
        swagger.setTags(tags);
        swagger.setPaths(paths);
        return swagger;
    }

    /**
     * 构造带 tag 的接口路径
     *
     * @param tags 接口 tag
     * @return 路径
     */
    private static Path pathWithOperationTags(String... tags) {
        Operation operation = new Operation();
        operation.setTags(Arrays.asList(tags));

        Path path = new Path();
        path.setGet(operation);
        return path;
    }

    /**
     * 桩映射器：mapDocumentation 返回预置模型，其余抽象方法空实现
     */
    public static class StubSwagger2Mapper extends ServiceModelToSwagger2Mapper {

        private final Swagger swagger;

        public StubSwagger2Mapper(Swagger swagger) {
            this.swagger = swagger;
        }

        @Override
        public Swagger mapDocumentation(Documentation from) {
            return swagger;
        }

        @Override
        protected Info mapApiInfo(ApiInfo from) {
            return null;
        }

        @Override
        protected io.swagger.models.Contact map(springfox.documentation.service.Contact from) {
            return null;
        }

        @Override
        protected Operation mapOperation(springfox.documentation.service.Operation from) {
            return null;
        }

        @Override
        protected Tag mapTag(springfox.documentation.service.Tag from) {
            return null;
        }
    }

}
