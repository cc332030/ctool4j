package com.c332030.ctool4j.doc.openapi2.plugins.property.impl;

import com.c332030.ctool4j.web.validation.annotation.CSchema;
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
 * Description: CSchemaAnnotationModelPropertyPlugin 测试：验证 @CSchema 在 required=true 时标记属性必填，
 * 并在 value 非空时写入字段描述
 * </p>
 *
 * @author c332030
 */
class CSchemaAnnotationModelPropertyPluginTests {

    private final CSchemaAnnotationModelPropertyPlugin plugin = new CSchemaAnnotationModelPropertyPlugin();

    @Test
    void apply_fieldRequiredWithValue() throws NoSuchFieldException {
        // 正例：required=true + value，必填 + 描述
        val property = applyOnField("username");
        Assertions.assertTrue(property.isRequired());
        Assertions.assertEquals("用户名", property.getDescription());
    }

    @Test
    void apply_fieldRequiredWithoutValue() throws NoSuchFieldException {
        // 边界：required=true 不带 value，仅必填，描述保持为空
        val property = applyOnField("password");
        Assertions.assertTrue(property.isRequired());
        Assertions.assertNull(property.getDescription());
    }

    @Test
    void apply_fieldNotRequired() throws NoSuchFieldException {
        // 边界：required=false（默认），不标记必填，但 value 非空仍写入描述
        val property = applyOnField("remark");
        Assertions.assertFalse(property.isRequired());
        Assertions.assertEquals("备注", property.getDescription());
    }

    @Test
    void apply_unAnnotatedField() throws NoSuchFieldException {
        // 边界：未标注 @CSchema，不设置必填也不写描述
        val property = applyOnField("extra");
        Assertions.assertFalse(property.isRequired());
        Assertions.assertNull(property.getDescription());
    }

    @Test
    void supports() {
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_2));
    }

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

        @CSchema(value = "用户名", required = true)
        private String username;

        @CSchema(required = true)
        private String password;

        @CSchema("备注")
        private String remark;

        private String extra;
    }
}
