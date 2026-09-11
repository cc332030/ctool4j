package com.c332030.ctool4j.log.advice;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.web.advice.ICBaseResponseBodyAdvice;
import com.c332030.ctool4j.web.util.CRequestLogUtils;
import lombok.CustomLog;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Description: CLogResponseBodyAdvice
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLogResponseBodyAdvice}（{@code @ControllerAdvice}）实现 {@code ICBaseResponseBodyAdvice&lt;Object&gt;}，在响应体写出前调用 {@code CRequestLogUtils.setRsp(body, null, response)} 采集响应体、响应状态码与响应头到请求日志上下文。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>采集过程抛异常</td>
 *     <td>捕获并记录 error 日志，返回原 body</td>
 *   </tr>
 *   <tr>
 *     <td>请求日志未启用</td>
 *     <td>跳过采集</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Web 接口响应体的统一日志采集。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>依赖 {@code ICBaseResponseBodyAdvice} 的调用链（web 模块）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅采集，日志打印由 {@code CRequestLogHandlerInterceptor.afterCompletion} 统一执行。</li>
 *   <li>采集异常静默（仅记录），响应日志可能缺失而不易察觉。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>采集时机</b></p>
 * <ul>
 *   <li>{@code beforeBodyWrite} 在响应写出前执行，一次采集响应体 + 响应状态码 + 响应头，供输出响应报文头。</li>
 * </ul>
 * <p><b>开关控制</b></p>
 * <ul>
 *   <li>受 {@code CRequestLogUtils.isEnable()} 总开关控制。</li>
 *   <li>采集异常捕获后记录 error 日志。</li>
 * </ul>
 *
 * @author c332030
 * @since 2025/12/20
 * @version 1.0
 */
@CustomLog
@ControllerAdvice
public class CLogResponseBodyAdvice implements ICBaseResponseBodyAdvice<Object> {

    /**
     * 响应体写出前记录响应体到请求日志（仅记录，日志打印由 CRequestLogHandlerInterceptor.afterCompletion 统一执行）
     *
     * @param body                  响应体
     * @param returnType            返回类型
     * @param selectedContentType   选定的内容类型
     * @param selectedConverterType 选定的转换器类型
     * @param request               请求
     * @param response              响应
     * @return 原响应体
     */
    @Nullable
    @Override
    public Object beforeBodyWrite(
            @Nullable Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        if(BooleanUtil.isTrue(CRequestLogUtils.isEnable())) {
            try {
                // 一次采集响应体 + 响应状态码 + 响应头，供输出响应报文头
                CRequestLogUtils.setRsp(body, null, response);
            } catch (Throwable e) {
                log.error("setRsp failure", e);
            }
        }

        return body;
    }

}
