package com.c332030.ctool4j.web.test.validation;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * Description: CRequired 校验集成测试用 Controller：@RequestBody DTO（@Valid 触发字段校验）接收
 * {@code @CRequired} 标注字段，用于验证接口必填/非必填与字段必填生效
 * </p>
 *
 * <h2>功能说明</h2>
 * <p>{@code CRequiredValidatorTestController} 为 {@code CRequiredValidatorTests} 集成测试辅助 Controller（{@code @RestController} + {@code @Validated}）：</p>
 * <ul>
 *   <li>{@code test(@Valid @RequestBody CRequiredValidatorTestDTO dto)}：POST {@code /c-required-validator/test}，接收 {@code @CRequired} 标注的 DTO，{@code @Valid} 触发字段校验（username {@code @CRequired} 必填）。</li>
 * </ul>
 * <p>作为真实接口入口，校验失败由全局异常处理器 {@code CMethodArgumentNotValidExceptionHandler} 拼接字段错误并返回 {@code code=500}。</p>
 * <h2>适用场景</h2>
 * <ul>
 *   <li>{@code CRequiredValidatorTests} 集成测试，作为真实接口入口验证 {@code @CRequired} 必填/非必填校验。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助类；仅提供单个测试接口。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@Validated
@RestController
public class CRequiredValidatorTestController {

    /**
     * 接收 @CRequired 标注的 DTO，@Valid 触发字段校验
     *
     * @param dto 请求体
     * @return 成功标识
     */
    @PostMapping("/c-required-validator/test")
    public String test(@Valid @RequestBody CRequiredValidatorTestDTO dto) {
        return "ok";
    }

}
