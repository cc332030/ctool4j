package com.c332030.ctool4j.doc.openapi2.plugins.property.impl;

import com.c332030.ctool4j.doc.annotation.CSchema;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import io.swagger.annotations.ApiModelProperty;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import springfox.documentation.builders.ModelPropertyBuilder;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: CTextEnumModelPropertyPlugin 测试：实现 {@code ICText} 的 model 字段枚举，允许值保持可提交的
 * 「枚举名」、可读的「枚举名(text)」写入 description；已自带描述（{@code @CSchema.value} / 存量
 * {@code @ApiModelProperty.value}）时不写入 text 说明（描述由对应注解插件负责）
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>Mock {@code ModelPropertyContext}（被测逻辑只依赖 beanPropertyDefinition 的类型与 annotatedElement 的注解），
 *   builder 用真实 {@code ModelPropertyBuilder}，不启容器。</li>
 *   <li>枚举用本仓库既有的 {@code CRequestHeaderEnum}（实现 ICText：AUTHORIZATION(鉴权)），与集成用例口径一致。</li>
 *   <li>描述断言只针对本插件职责：写入「枚举名(text)」或（已自带描述时）不写入；描述本身由注解插件写入，不在本用例断言范围。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：支持性（含 null）、正例写允许值与 text 说明、已自带描述（@CSchema/@ApiModelProperty）不写 text、非 text 枚举不介入。</li>
 *   <li>未覆盖：真实 springfox 文档生成链路（由 CTextEnumIntegrationTests 覆盖）。</li>
 * </ul>
 * <h2>支持性</h2>
 * <ul>
 *   <li>1.1 支持 SWAGGER_2/12 与 null（supports）</li>
 * </ul>
 * <h2>apply 分支输出</h2>
 * <ul>
 *   <li>2.1 命中 text 枚举：写入可提交允许值与「枚举名(text)」描述（apply_textEnum_writesAllowableValuesAndDescription）</li>
 *   <li>2.2 自带 @CSchema(value)：不写 text 说明，允许值仍写入（apply_keepCSchemaDescription）</li>
 *   <li>2.3 自带存量 @ApiModelProperty(value)：不写 text 说明（apply_keepApiModelPropertyDescription）</li>
 *   <li>2.4 非 text 枚举（未实现 ICText）：不覆写（apply_nonTextEnum_noOverride）</li>
 * </ul>
 *
 * <p>
 * 是 {@link CTextEnumModelPropertyPlugin} 的测试用例。
 * </p>
 *
 * @author c332030
 * @since 2026/9/16
 * @version 1.0
 */
class CTextEnumModelPropertyPluginTests {

    private final CTextEnumModelPropertyPlugin plugin = new CTextEnumModelPropertyPlugin();

    /**
     * <p>对应测试用例 1.1</p>
     */
    @Test
    void supports() {
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_2));
        Assertions.assertTrue(plugin.supports(DocumentationType.SWAGGER_12));
        Assertions.assertTrue(plugin.supports(null));
    }

    /**
     * <p>对应测试用例 2.1：命中 text 枚举，写入允许值与 text 说明</p>
     */
    @Test
    void apply_textEnum_writesAllowableValuesAndDescription() throws NoSuchFieldException {
        ModelProperty property = applyOnField(CRequestHeaderEnum.class, "header");

        Assertions.assertTrue(allowableValues(property).contains("AUTHORIZATION"),
            "允许值应为可提交的枚举名，实际 " + property.getAllowableValues());
        Assertions.assertTrue(property.getDescription().contains("AUTHORIZATION(鉴权)"),
            "description 应展示「枚举名(text)」，实际 " + property.getDescription());
    }

    /**
     * <p>对应测试用例 2.2：自带 @CSchema(value) 时不写 text 说明，允许值仍写入</p>
     */
    @Test
    void apply_keepCSchemaDescription() throws NoSuchFieldException {
        ModelProperty property = applyOnField(CRequestHeaderEnum.class, "headerWithSchema");

        Assertions.assertNull(property.getDescription(),
            "属性已自带描述（@CSchema.value）时不应写入 text 说明，实际 " + property.getDescription());
        Assertions.assertTrue(allowableValues(property).contains("AUTHORIZATION"), "允许值不受描述影响，仍应写入");
    }

    /**
     * <p>对应测试用例 2.3：自带存量 @ApiModelProperty(value) 时不写 text 说明</p>
     */
    @Test
    void apply_keepApiModelPropertyDescription() throws NoSuchFieldException {
        ModelProperty property = applyOnField(CRequestHeaderEnum.class, "headerWithApiModelProperty");

        Assertions.assertNull(property.getDescription(),
            "属性已自带描述（@ApiModelProperty.value）时不应写入 text 说明，实际 " + property.getDescription());
    }

    /**
     * <p>对应测试用例 2.4：非 text 枚举（未实现 ICText）不覆写</p>
     */
    @Test
    void apply_nonTextEnum_noOverride() throws NoSuchFieldException {
        ModelProperty property = applyOnField(PlainEnum.class, "plain");

        Assertions.assertNull(property.getAllowableValues(), "非 text 枚举不应写入允许值");
        Assertions.assertNull(property.getDescription(), "非 text 枚举不应写入描述");
    }

    /**
     * 在指定字段上执行插件（字段提供注解来源，类型由参数显式给出）
     *
     * @param type      属性类型
     * @param fieldName 字段名（取注解用）
     * @return 构建结果
     */
    private ModelProperty applyOnField(Class<?> type, String fieldName) throws NoSuchFieldException {
        val builder = new ModelPropertyBuilder();
        val field = TestModel.class.getDeclaredField(fieldName);

        val beanProperty = Mockito.mock(BeanPropertyDefinition.class);
        Mockito.doReturn(type).when(beanProperty).getRawPrimaryType();

        val context = Mockito.mock(ModelPropertyContext.class);
        Mockito.doReturn(Optional.of(beanProperty)).when(context).getBeanPropertyDefinition();
        Mockito.doReturn(Optional.of(field)).when(context).getAnnotatedElement();
        Mockito.doReturn(builder).when(context).getBuilder();

        plugin.apply(context);
        return builder.build();
    }

    /**
     * 取允许值列表（非列表类型时断言失败）
     *
     * @param property model 属性
     * @return 允许值列表
     */
    private static List<String> allowableValues(ModelProperty property) {
        AllowableValues values = property.getAllowableValues();
        Assertions.assertInstanceOf(AllowableListValues.class, values, "允许值应为列表类型，实际 " + values);
        return ((AllowableListValues) values).getValues();
    }

    /**
     * 测试用 model：提供字段上的注解来源
     */
    private static class TestModel {

        @SuppressWarnings("unused")
        private CRequestHeaderEnum header;

        @CSchema("鉴权头")
        @SuppressWarnings("unused")
        private CRequestHeaderEnum headerWithSchema;

        @ApiModelProperty("鉴权头")
        @SuppressWarnings("unused")
        private CRequestHeaderEnum headerWithApiModelProperty;

        @SuppressWarnings("unused")
        private PlainEnum plain;
    }

    /**
     * 未实现 ICText 的枚举
     */
    private enum PlainEnum {

        ONLY
    }

}
