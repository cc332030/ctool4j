package com.c332030.ctool4j.log.advice;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.c332030.ctool4j.web.advice.ICBaseRequestBodyAdvice;
import com.c332030.ctool4j.web.util.CRequestLogUtils;
import lombok.CustomLog;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.lang.reflect.Type;

/**
 * <p>
 * Description: CLogRequestBodyAdvice
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLogRequestBodyAdvice}（{@code @ControllerAdvice}）实现 {@code ICBaseRequestBodyAdvice}，在请求体读取后调用 {@code CRequestLogUtils.setRequestBodyReq(body)} 采集请求体到请求日志上下文。</p>
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
 *   <li>Web 接口请求体的统一日志采集。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>依赖 {@code ICBaseRequestBodyAdvice} 的调用链（web 模块）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>采集异常静默（仅记录），请求日志可能缺失请求体而不易察觉。</li>
 *   <li>依赖请求日志总开关。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>采集时机</b></p>
 * <ul>
 *   <li>{@code afterBodyRead} 在请求体反序列化完成后执行，将 body 存入请求日志上下文。</li>
 * </ul>
 * <p><b>开关控制</b></p>
 * <ul>
 *   <li>受 {@code CRequestLogUtils.isEnable()} 总开关控制。</li>
 *   <li>采集异常捕获后记录 error 日志，不影响主流程。</li>
 * </ul>
 *
 * @author c332030
 * @since 2025/12/20
 * @version 1.0
 */
@CustomLog
@ControllerAdvice
public class CLogRequestBodyAdvice implements ICBaseRequestBodyAdvice {

    /**
     * 请求体读取后记录日志
     *
     * @param body          请求体
     * @param inputMessage  输入消息
     * @param parameter     方法参数
     * @param targetType    目标类型
     * @param converterType 消息转换器类型
     * @return 原请求体
     */
    @Override
    public Object afterBodyRead(
            Object body,
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {

        if(BooleanUtil.isTrue(CRequestLogUtils.isEnable())) {
            try {
                CRequestLogUtils.setRequestBodyReq(body);
            } catch (Throwable e) {
                log.error("setReq failure, url: {}", CRequestUtils.getRequestURIDefaultNull(), e);
            }
        }

        return body;
    }

}
