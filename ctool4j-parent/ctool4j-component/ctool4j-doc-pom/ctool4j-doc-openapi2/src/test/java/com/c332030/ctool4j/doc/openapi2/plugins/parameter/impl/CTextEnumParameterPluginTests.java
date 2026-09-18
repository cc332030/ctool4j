package com.c332030.ctool4j.doc.openapi2.plugins.parameter.impl;

import com.c332030.ctool4j.doc.annotation.CParameter;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import com.fasterxml.classmate.ResolvedType;
import io.swagger.annotations.ApiParam;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.ParameterContext;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: CTextEnumParameterPlugin 测试：实现 {@code ICText} 的枚举参数，允许值保持可提交的「枚举名」、
 * 可读的「枚举名(text)」写入 description；已自带描述（{@code @CParameter.value} / 存量 {@code @ApiParam.value}）
 * 时不写入 text 说明（描述由对应注解插件负责）
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>Mock {@code ParameterContext} / {@code ResolvedMethodParameter}，只驱动被测分支，不启容器。</li>
 *   <li>枚举用本仓库既有的 {@code CRequestHeaderEnum}（实现 ICText：AUTHORIZATION(鉴权)），与集成用例口径一致。</li>
 *   <li>描述断言只针对本插件职责：写入「枚举名(text)」或（已自带描述时）不写入；描述本身由注解插件写入，不在本用例断言范围。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：支持性（含 null）、正例写允许值与 text 说明、已自带描述（@CParameter/@ApiParam）不写 text、非 text 枚举不介入。</li>
 *   <li>未覆盖：真实 springfox 文档生成链路（由 CTextEnumIntegrationTests 覆盖）。</li>
 * </ul>
 * <h2>支持性</h2>
 * <ul>
 *   <li>1.1 支持 SWAGGER_2/12 与 null（supports）</li>
 * </ul>
 * <h2>apply 分支输出</h2>
 * <ul>
 *   <li>2.1 命中 text 枚举：写入可提交允许值与「枚举名(text)」描述（apply_textEnum_writesAllowableValuesAndDescription）</li>
 *   <li>2.2 自带 @CParameter(value)：不写 text 说明，允许值仍写入（apply_keepCParameterDescription）</li>
 *   <li>2.3 自带 @ApiParam(value)：不写 text 说明（apply_keepApiParamDescription）</li>
 *   <li>2.4 非 text 枚举（非枚举/未实现 ICText）：不覆写（apply_nonTextEnum_noOverride）</li>
 * </ul>
 *
 * <p>
 * 是 {@link CTextEnumParameterPlugin} 的测试用例。
 * </p>
 *
 * @author c332030
 * @since 2026/9/16
 * @version 1.0
 */
class CTextEnumParameterPluginTests {

    private final CTextEnumParameterPlugin plugin = new CTextEnumParameterPlugin();

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
    void apply_textEnum_writesAllowableValuesAndDescription() {
        val parameterBuilder = new ParameterBuilder();
        val context = context(parameterBuilder, CRequestHeaderEnum.class, Optional.empty(), Optional.empty());

        plugin.apply(context);

        val parameter = parameterBuilder.build();
        Assertions.assertTrue(allowableValues(parameter).contains("AUTHORIZATION"),
            "允许值应为可提交的枚举名，实际 " + parameter.getAllowableValues());
        Assertions.assertTrue(parameter.getDescription().contains("AUTHORIZATION(鉴权)"),
            "description 应展示「枚举名(text)」，实际 " + parameter.getDescription());
    }

    /**
     * <p>对应测试用例 2.2：自带 @CParameter(value) 时不写 text 说明，允许值仍写入</p>
     */
    @Test
    void apply_keepCParameterDescription() throws NoSuchMethodException {
        val parameterBuilder = new ParameterBuilder();
        val context = context(parameterBuilder, CRequestHeaderEnum.class, Optional.of(findCParameter()), Optional.empty());

        plugin.apply(context);

        val parameter = parameterBuilder.build();
        Assertions.assertNull(parameter.getDescription(),
            "参数已自带描述（@CParameter.value）时不应写入 text 说明，实际 " + parameter.getDescription());
        Assertions.assertTrue(allowableValues(parameter).contains("AUTHORIZATION"), "允许值不受描述影响，仍应写入");
    }

    /**
     * <p>对应测试用例 2.3：自带存量 @ApiParam(value) 时不写 text 说明</p>
     */
    @Test
    void apply_keepApiParamDescription() throws NoSuchMethodException {
        val parameterBuilder = new ParameterBuilder();
        val context = context(parameterBuilder, CRequestHeaderEnum.class, Optional.empty(), Optional.of(findApiParam()));

        plugin.apply(context);

        val parameter = parameterBuilder.build();
        Assertions.assertNull(parameter.getDescription(),
            "参数已自带描述（@ApiParam.value）时不应写入 text 说明，实际 " + parameter.getDescription());
    }

    /**
     * <p>对应测试用例 2.4：非 text 枚举（非枚举 / 未实现 ICText）不覆写</p>
     */
    @Test
    void apply_nonTextEnum_noOverride() {
        val parameterBuilder = new ParameterBuilder();
        val context = context(parameterBuilder, String.class, Optional.empty(), Optional.empty());

        plugin.apply(context);

        val parameter = parameterBuilder.build();
        Assertions.assertNull(parameter.getAllowableValues(), "非 text 枚举不应写入允许值");
        Assertions.assertNull(parameter.getDescription(), "非 text 枚举不应写入描述");
    }

    /**
     * 构造参数上下文（仅桩出被测逻辑用到的调用）
     *
     * @param parameterBuilder 参数构建器
     * @param parameterType    参数类型
     * @param cParameter       @CParameter 注解（无则 empty）
     * @param apiParam         @ApiParam 注解（无则 empty）
     * @return 参数上下文
     */
    private static ParameterContext context(
        ParameterBuilder parameterBuilder, Class<?> parameterType,
        Optional<CParameter> cParameter, Optional<ApiParam> apiParam
    ) {
        val resolvedType = Mockito.mock(ResolvedType.class);
        // getErasedType() 返回 Class<?>，泛型捕获导致 thenReturn 不适用，故用 doReturn 形式
        Mockito.doReturn(parameterType).when(resolvedType).getErasedType();

        val resolvedMethodParameter = Mockito.mock(ResolvedMethodParameter.class);
        Mockito.doReturn(resolvedType).when(resolvedMethodParameter).getParameterType();
        Mockito.doReturn(cParameter).when(resolvedMethodParameter).findAnnotation(CParameter.class);
        Mockito.doReturn(apiParam).when(resolvedMethodParameter).findAnnotation(ApiParam.class);

        val context = Mockito.mock(ParameterContext.class);
        Mockito.doReturn(resolvedMethodParameter).when(context).resolvedMethodParameter();
        Mockito.doReturn(parameterBuilder).when(context).parameterBuilder();
        return context;
    }

    /**
     * 取允许值列表（非列表类型时断言失败）
     *
     * @param parameter 参数
     * @return 允许值列表
     */
    private static List<String> allowableValues(Parameter parameter) {
        AllowableValues values = parameter.getAllowableValues();
        Assertions.assertInstanceOf(AllowableListValues.class, values, "允许值应为列表类型，实际 " + values);
        return ((AllowableListValues) values).getValues();
    }

    private static CParameter findCParameter() throws NoSuchMethodException {
        return AnnotatedFixture.class.getDeclaredMethod("cParam", String.class)
            .getParameters()[0].getAnnotation(CParameter.class);
    }

    private static ApiParam findApiParam() throws NoSuchMethodException {
        return AnnotatedFixture.class.getDeclaredMethod("apiParam", String.class)
            .getParameters()[0].getAnnotation(ApiParam.class);
    }

    /**
     * 注解夹具：提供带 @CParameter / @ApiParam 的参数（反射取注解实例）
     */
    private static class AnnotatedFixture {

        @SuppressWarnings("unused")
        void cParam(@CParameter("鉴权头") String header) {
        }

        @SuppressWarnings("unused")
        void apiParam(@ApiParam("鉴权头") String header) {
        }
    }

}
