package com.c332030.ctool4j.doc.openapi2.plugins.property.impl;

import com.c332030.ctool4j.doc.annotation.CSchema;
import com.c332030.ctool4j.web.validation.annotation.CRequired;
import com.fasterxml.classmate.TypeResolver;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import springfox.documentation.builders.ModelPropertyBuilder;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

/**
 * <p>
 * Description: CSchemaAnnotationModelPropertyPlugin 测试：验证 @CRequired 标注时标记属性必填，
 * <h2>设计思路</h2>
 * <ul>
 *   <li>覆盖 apply 分支输出：@CRequired+@CSchema(value)（必填+描述）、@CRequired 无 @CSchema(value)（仅必填）、</li>
 *   <li>仅 @CSchema 无 @CRequired（不标记必填）、均未标注（不处理）、@CRequired 重复标注容器场景（仍必填）。</li>
 *   <li>覆盖 supports 支持性。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对"@CRequired 标注即必填、@CSchema.value 非空写描述"的约定。</li>
 *   <li>依据白盒/黑盒原则与分支覆盖：必填与描述的解耦组合、未标注场景均覆盖。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：@CRequired+@CSchema 组合、仅 @CRequired、仅 @CSchema、未标注、supports。</li>
 *   <li>未覆盖：通过 getter 查找注解的路径、真实 springfox 文档生成链路。</li>
 * </ul>
 * <h2>apply 分支输出</h2>
 * <ul>
 *   <li>1.1 @CRequired + @CSchema(value)，必填+描述（apply_fieldRequiredWithValue）</li>
 *   <li>1.2 @CRequired 不带 @CSchema(value)，仅必填描述为空（apply_fieldRequiredWithoutValue）</li>
 *   <li>1.3 仅 @CSchema 无 @CRequired，不标记必填（apply_fieldNotRequired）</li>
 *   <li>1.4 均未标注不处理（apply_unAnnotatedField）</li>
 *   <li>1.5 @CRequired 按不同 groups 重复标注（容器注解）仍标记必填（apply_fieldRequiredRepeated）</li>
 * </ul>
 * <h2>supports</h2>
 * <ul>
 *   <li>2.1 支持 SWAGGER_2/12（supports）</li>
 *   <li>2.2 null 文档类型也支持（supports_null）</li>
 * </ul>
 *
 * </p>
 *
 * <p>
 * 是 {@link CSchemaAnnotationModelPropertyPlugin} 的测试用例。
 * </p>
 *
 * @CSchema 在 value 非空时写入字段描述（描述与必填解耦）
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
class CSchemaAnnotationModelPropertyPluginTests {

    private final CSchemaAnnotationModelPropertyPlugin plugin = new CSchemaAnnotationModelPropertyPlugin();

    /**
     * <p>
     * 对应测试用例 1.1：@CRequired + @CSchema(value)，必填+描述
     */
    @Test
    void apply_fieldRequiredWithValue() throws NoSuchFieldException {
        // 正例：@CRequired + @CSchema(value)，必填 + 描述
        val property = applyOnField("username");
        Assertions.assertTrue(property.isRequired());
        Assertions.assertEquals("用户名", property.getDescription());
    }

    /**
     * <p>
     * 对应测试用例 1.2：@CRequired 不带 @CSchema(value)，仅必填描述为空
     */
    @Test
    void apply_fieldRequiredWithoutValue() throws NoSuchFieldException {
        // 边界：@CRequired 不带 @CSchema(value)，仅必填，描述保持为空
        val property = applyOnField("password");
        Assertions.assertTrue(property.isRequired());
        Assertions.assertNull(property.getDescription());
    }

    /**
     * <p>
     * 对应测试用例 1.3：仅 @CSchema 无 @CRequired，不标记必填
     */
    @Test
    void apply_fieldNotRequired() throws NoSuchFieldException {
        // 边界：仅 @CSchema 无 @CRequired，不标记必填，但 value 非空仍写入描述
        val property = applyOnField("remark");
        Assertions.assertFalse(property.isRequired());
        Assertions.assertEquals("备注", property.getDescription());
    }

    /**
     * <p>
     * 对应测试用例 1.4：均未标注不处理
     */
    @Test
    void apply_unAnnotatedField() throws NoSuchFieldException {
        // 边界：未标注注解，不设置必填也不写描述
        val property = applyOnField("extra");
        Assertions.assertFalse(property.isRequired());
        Assertions.assertNull(property.getDescription());
    }

    /**
     * <p>
     * 对应测试用例 1.5：@CRequired 按不同 groups 重复标注（容器注解）仍标记必填
     */
    @Test
    void apply_fieldRequiredRepeated() throws NoSuchFieldException {
        // 边界：@Repeatable 容器注解场景，同一字段按不同 groups 重复标注 @CRequired，仍应标记必填
        val property = applyOnField("usernameRepeated");
        Assertions.assertTrue(property.isRequired(), "@CRequired 容器注解（@CRequired.List）也应标记必填");
    }

    /**
     * <p>
     * 对应测试用例 2.1：支持 SWAGGER_2/12
     */
    @Test
    void supports() {
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_2));
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_12));
    }

    /**
     * <p>
     * 对应测试用例 2.2：null 文档类型也支持
     */
    @Test
    void supports_null() {
        Assertions.assertTrue(plugin.supports(null));
    }

    private ModelProperty applyOnField(String fieldName) throws NoSuchFieldException {
        val builder = new ModelPropertyBuilder();
        val context = new ModelPropertyContext(
            builder, TestModel.class.getDeclaredField(fieldName), new TypeResolver(), DocumentationType.SWAGGER_2);
        plugin.apply(context);
        return builder.build();
    }

    /**
     * 测试用 model
     */
    private static class TestModel {

        @CRequired
        @CSchema("用户名")
        private String username;

        /**
         * 重复标注 @CRequired（不同 groups）场景：注解以容器 @CRequired.List 存在
         */
        @CRequired(groups = Group1.class)
        @CRequired(groups = Group2.class)
        private String usernameRepeated;

        @CRequired
        private String password;

        @CSchema("备注")
        private String remark;

        private String extra;
    }

    /**
     * 分组 1（重复标注测试用）
     */
    private interface Group1 {
    }

    /**
     * 分组 2（重复标注测试用）
     */
    private interface Group2 {
    }
}
