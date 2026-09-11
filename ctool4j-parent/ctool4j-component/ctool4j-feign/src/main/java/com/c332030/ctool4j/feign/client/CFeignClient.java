package com.c332030.ctool4j.feign.client;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.core.util.CCharsets;
import com.c332030.ctool4j.core.util.CThreadLocalUtils;
import com.c332030.ctool4j.feign.config.CFeignClientLogConfig;
import com.c332030.ctool4j.feign.util.CFeignUtils;
import com.c332030.ctool4j.web.util.CCommUtils;
import feign.Client;
import feign.Request;
import feign.Response;
import feign.Util;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.val;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Description: CFeignClient
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CFeignClient} 实现 feign {@code Client}，包装默认客户端。开启日志时记录响应日志并重新缓冲响应体， 否则透传默认客户端执行。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>日志处理异常</td>
 *     <td>记录 error 日志，不中断（响应已重建返回）</td>
 *   </tr>
 *   <tr>
 *     <td>无响应体</td>
 *     <td>日志输出 {@code [no response body]}</td>
 *   </tr>
 *   <tr>
 *     <td>非文本 body</td>
 *     <td>日志输出 {@code [no response text body]}</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要记录 Feign 响应日志（含响应头/体）的客户端包装。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>日志关闭时零开销透传；响应体过大时日志会占用内存。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>读响应体到内存用于日志，大响应体有内存开销。</li>
 *   <li>重建 Response 未保留 protocolVersion（低版本兼容）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>{@code feignLogConfig.enable} 为 false 时直接透传默认客户端，不产生日志开销。</li>
 *   <li>日志开启时：读响应头 + 响应体字节，拼接到线程局部 HTTP 日志，并 log.info 输出。</li>
 *   <li>响应体已读取，需重建 {@code Response} 返回（body 重新包装），避免下游拿不到响应体。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>基于 {@code feign.Util.toByteArray} 读响应流；{@code CCommUtils} 拼 header/判断文本 body。</li>
 *   <li>用 try-with-resources 关闭响应，重建 Response 返回。</li>
 * </ul>
 *
 * @since 2025/9/21
 * @version 1.0
 */
@CustomLog
@AllArgsConstructor
public class CFeignClient implements Client {

    final Client defaultClient;

    final CFeignClientLogConfig feignLogConfig;

    /**
     * 执行请求：开启日志时记录响应日志并重新缓冲响应体，否则透传默认客户端
     * <ul>
     *   <li>{@code execute(request, options)}：执行请求；日志开启时读取响应体、记录响应日志，再重建响应体返回。</li>
     * </ul>
     *
     * @param request 请求
     * @param options 请求选项
     * @return 响应
     */
    @Override
    public Response execute(Request request, Request.Options options) throws IOException {

        if(!BooleanUtil.isTrue(feignLogConfig.getEnable())) {
            return defaultClient.execute(request, options);
        }

        // 执行原始请求，获取响应
        try(val response = defaultClient.execute(request, options)) {

            val headers = response.headers();
            val bodyBytes = Util.toByteArray(response.body().asInputStream());

            try {
                dealLog(headers, bodyBytes);
            } catch (Throwable e) {
                log.error("处理响应日志失败", e);
            }

            // 重新构建响应体（因为原流已被读取）
            return Response.builder()
                    .requestTemplate(request.requestTemplate())
                    // 低版本不支持
//                    .protocolVersion(response.protocolVersion())
                    .status(response.status())
                    .reason(response.reason())
                    .request(request)
                    .headers(headers)
                    .body(bodyBytes)
                    .build();
        }
    }

    private void dealLog(Map<String, Collection<String>> headers, byte[] bodyBytes) {

        val httpLog = CThreadLocalUtils.getThenRemove(CFeignUtils.HTTP_LOG_THREAD_LOCAL);

        if(BooleanUtil.isTrue(feignLogConfig.getEnableHeader())) {
            httpLog.append("\n\n");
            httpLog.append(CCommUtils.getFullHeaderStr(headers));
        }

        httpLog.append("\n\n");
        if(ArrayUtil.isEmpty(bodyBytes)) {

            httpLog.append("[no response body]");
        } else {

            if(CCommUtils.isTextBody(headers)) {

                val responseBody = new String(bodyBytes, CCharsets.UTF_8);
                httpLog.append(responseBody);
            } else {
                httpLog.append("[no response text body]");
            }

        }

        log.info("{}", httpLog::toString);

    }

}
