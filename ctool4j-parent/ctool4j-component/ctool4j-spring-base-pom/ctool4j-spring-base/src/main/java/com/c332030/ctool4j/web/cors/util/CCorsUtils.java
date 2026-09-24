package com.c332030.ctool4j.web.cors.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CBoolUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.core.util.CUrlUtils;
import com.c332030.ctool4j.core.validation.CValidUtils;
import com.c332030.ctool4j.definition.constant.CConstants;
import com.c332030.ctool4j.interfaces.CHttpRequest;
import com.c332030.ctool4j.interfaces.CHttpResponse;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import com.c332030.ctool4j.web.cors.CCorsConfig;
import com.c332030.ctool4j.web.cors.CCorsOriginConfig;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * Description: CCorsUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCorsUtils}（{@code @UtilityClass} + {@code @CAutowiredScan}）为跨域处理的静态工具类，
 * 持有 {@code @CAutowired} 注入的 {@code CCorsConfig config}，供 {@code CCorsFilter}/{@code CCorsInterceptor}/
 * {@code CCorsResponseBodyAdvice} 复用。</p>
 * <p>核心方法：</p>
 * <ul>
 *   <li>{@code handleOptions(request, response)}：全局与域名级开关均开启且为 OPTIONS 预检请求时，
 *   以 204 结束预检并返回 true；否则返回 false。预检的 CORS 响应头由 {@code handle}/{@code handleDo}
 *   在处理链中先行设置。</li>
 *   <li>{@code handle(request, response)}：{@code enable=true} 时委托 {@code handleDo} 设置跨域响应头。</li>
 *   <li>{@code handleAndContinue(request, response)}：按 {@code handle} → {@code handleOptions} 的顺序处理，
 *   返回是否继续后续处理（过滤器与拦截器两个接入点共用的编排）。</li>
 *   <li>{@code handleDo(request, response)}：按<b>域名级配置</b>校验并设置跨域响应头。</li>
 *   <li>{@code getOriginConfig(origin)}：按域名（{@code host:port} → 纯 {@code host}）取域名级配置。</li>
 *   <li>{@code setHeaderIfNotEmpty} / {@code joinHeaders}：头集合为空则不设置、含 {@code *} 用通配拼接。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <p><b>面向抽象层（与 javax/jakarta 无关）</b></p>
 * <ul>
 *   <li>入参用 {@link CHttpRequest} / {@link CHttpResponse} 而非某个 Servlet 包的请求/响应：
 *   CORS 的判定与写出只用到两边公共面（读头、读方法、写头、写状态码），故本类不随容器切换而改，
 *   由调用侧（Filter/Interceptor/Advice）负责把各自容器的对象包装成抽象层对象。</li>
 *   <li>状态码取 Spring 的 {@link HttpStatus#NO_CONTENT}，不用某一侧 Servlet 包的 {@code SC_*} 常量。</li>
 * </ul>
 * <p><b>开关语义（配置了"不等于"启用了）</b></p>
 * <ul>
 *   <li>三层开关：全局 {@code cors.enable}、域名级 {@code cors.origins.<域名>.enable}；
 *   两项都显式为 {@code true} 才处理（未配置即 {@code false}），避免"删掉配置后残留行为"。</li>
 *   <li>凭据（{@code Access-Control-Allow-Credentials}）与响应头暴露（{@code Access-Control-Expose-Headers}）
 *   各由域名级的独立开关控制，<b>默认禁用</b>，需显式开启。</li>
 *   <li>各开关的取值兜底统一由 {@code CBoolUtils.isTrue} 完成（null 视为 false），不在配置类写默认值。</li>
 * </ul>
 * <p><b>域名级取值兜底</b></p>
 * <ul>
 *   <li>域名级配置项的回落<b>就在取值处完成</b>，不另写"取值 + 回落"的 getter——那层方法只是把单行兜底换个名字，
 *   读代码时多一跳、取值形态还分叉成两种。集合取 {@code CollUtil.defaultIfEmpty}（未配置与空集合一律回落默认）；
 *   请求方法与暴露响应头取 {@code ObjectUtil.defaultIfNull}（<b>仅 null 才回落</b>，保留"显式空集合"的语义：
 *   不允许任何方法、不暴露任何头）。</li>
 * </ul>
 * <p><b>异常兜底</b></p>
 * <ul>
 *   <li>{@code handle}/{@code handleOptions} 外层 try-catch 捕获 Throwable 记录 error 日志，避免跨域处理异常影响主流程。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>配置未注入（config 为 null）</td>
 *     <td>NPE 被外层 try-catch 捕获，记录日志，跨域头不输出</td>
 *   </tr>
 *   <tr>
 *     <td>{@code handleDo} 校验链不通过（非同源/域名未配置/域名未启用/方法不允许）</td>
 *     <td>不设置任何跨域头，直接返回</td>
 *   </tr>
 *   <tr>
 *     <td>请求无 {@code Origin} 头</td>
 *     <td>视为非跨域请求，直接返回</td>
 *   </tr>
 *   <tr>
 *     <td>域名级未配置 {@code allowedMethods}/{@code allowedHeaders}/{@code exposedHeaders}</td>
 *     <td>取 {@code CCorsConfig} 的同名默认值（默认值只在配置类声明一处）</td>
 *   </tr>
 *   <tr>
 *     <td>最终取到的头集合为 null 或空</td>
 *     <td>经 {@code setHeaderIfNotEmpty} 不设置对应响应头（避免空指针，空即不声明）</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>跨域请求需要动态回显 Origin、按<b>域名</b>白名单放行来源与方法、并按域名独立开启凭据与响应头暴露的场景。</li>
 *   <li>javax 与 jakarta 两套容器下的调用方共用同一份跨域逻辑（调用方各做一次包装）。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>全局 {@code enable=false} 或该域名未配置/未启用时不做处理；{@code Origin} 为 null（非浏览器跨域）不处理。</li>
 *   <li>需要读写 Cookie、二进制流等抽象层未暴露能力时，须由调用方在包装前自行处理。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>采用"回显 Origin + 域名白名单校验"而非 {@code *} 通配，因此支持 {@code Allow-Credentials: true}（带凭据跨域）。</li>
 *   <li>{@code CUrlUtils.getHostWithPort} 归一化 Origin 后再与 HOST 比较判断是否同源，并作为域名配置的匹配值
 *   （匹配时先按 {@code host:port}、再回落纯 {@code host}，见 {@link #getOriginConfig}）。</li>
 *   <li>预检请求的 CORS 头依赖 {@code handle}/{@code handleDo} 在处理链中先行设置，{@code handleOptions} 本身不再设置（有意设计）。</li>
 * </ul>
 *
 * @since 2026/1/9
 * @version 1.4
 */
@CustomLog
@UtilityClass
@CAutowiredScan
public class CCorsUtils {

    @Setter
    @CAutowired
    CCorsConfig config;

    /**
     * 处理 OPTIONS 预检请求，直接返回 204
     *
     * <p>预检请求的 CORS 响应头由 {@link #handle} / {@link #handleDo} 在处理链中先行设置，
     * 此处仅负责以 204 状态码结束预检请求，不再重复设置响应头（有意设计）</p>
     * <ul>
     *   <li>{@code handleOptions(request, response)}：{@code enable=true} 且为 OPTIONS 预检请求时，以 204 状态码结束预检并返回 true；否则返回 false。</li>
     * </ul>
     *
     * @param request  请求
     * @param response 响应
     * @return true 表示本次为预检请求且已处理
     */
    public boolean handleOptions(CHttpRequest request, CHttpResponse response) {
        try {
            if (CBoolUtils.isTrue(config.getEnable())) {
                if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
                    log.debug("deal OPTIONS request");
                    response.setStatus(HttpStatus.NO_CONTENT.value());
                    return true;
                }
            }
        } catch (Throwable e) {
            log.error("deal OPTIONS failure", e);
        }

        return false;
    }

    /**
     * 处理跨域并按预检结果决定是否继续后续处理
     *
     * <p>把 {@link #handle} 与 {@link #handleOptions} 的调用序收在一处：过滤器与拦截器两个接入点共用同一编排，
     * 差异只在「不继续时如何结束」（过滤器直接返回、拦截器返回 false 中断处理链）。</p>
     *
     * @param request  请求
     * @param response 响应
     * @return true 表示继续后续处理；false 表示本次为已处理的 OPTIONS 预检（调用方不应再放行）
     */
    public boolean handleAndContinue(CHttpRequest request, CHttpResponse response) {
        handle(request, response);
        return !handleOptions(request, response);
    }

    /**
     * 处理跨域请求，开启跨域时设置响应头
     * <ul>
     *   <li>{@code handle(request, response)}：{@code enable=true} 时调用 {@code handleDo} 设置跨域响应头。</li>
     * </ul>
     *
     * @param request  请求
     * @param response 响应
     */
    public void handle(CHttpRequest request, CHttpResponse response) {
        try {
            if (CBoolUtils.isTrue(config.getEnable())) {
                handleDo(request, response);
            }
        } catch (Throwable e) {
            log.error("cors handle failure", e);
        }

    }

    /**
     * 按域名级配置校验并设置跨域响应头
     *
     * <h2>handleDo 校验链</h2>
     * <ul>
     *   <li>校验请求来源 header {@code Origin} 是否为空 → 是否同源 → 该域名是否在 {@code origins} 中 →</li>
     *   <li>该域名是否显式开启（{@code origins.<域名>.enable=true}）→</li>
     *   <li>请求方法是否在允许方法（域名级优先，缺省取 {@code CCorsConfig.allowedMethods}，含 {@code *} 通配）中。</li>
     *   <li>全部通过后设置响应头：</li>
     *   <li>{@code Access-Control-Allow-Origin} 回显请求 Origin</li>
     *   <li>{@code Access-Control-Allow-Methods}：回显当前请求方法</li>
     *   <li>{@code Access-Control-Allow-Headers}：头集合经统一私有方法 {@code setHeaderIfNotEmpty}
     *   （集合为 null/空则不设置）拼接 {@code joinHeaders}（含 {@code *} 用 {@code *}，否则逗号连接）</li>
     *   <li>{@code Access-Control-Allow-Credentials}：仅该域名 {@code credentials=true} 时设置固定 {@code true}（默认禁用）</li>
     *   <li>{@code Access-Control-Expose-Headers}：仅该域名 {@code exposeHeaders=true} 且集合非空时设置（默认禁用）</li>
     * </ul>
     *
     * @param request  请求
     * @param response 响应
     */
    public void handleDo(CHttpRequest request, CHttpResponse response) {

        val origin = request.getHeader(HttpHeaders.ORIGIN);
        if (StrUtil.isEmpty(origin)) {
            log.debug("Not cors");
            return;
        }

        val newOrigin = CUrlUtils.getHostWithPort(origin);
        // 同源，避免打印日志误导
        if (Objects.equals(request.getHeader(HttpHeaders.HOST), newOrigin)) {
            return;
        }

        val originConfig = getOriginConfig(newOrigin);
        if (CValidUtils.isNotValid(originConfig)) {
            log.debug("Not allow origin: {}", origin);
            return;
        }

        // 域名级开关：未配置视为 false，须显式开启（避免"删掉配置后残留行为"）
        if (!CBoolUtils.isTrue(originConfig.getEnable())) {
            log.debug("origin disabled: {}", newOrigin);
            return;
        }

        val method = request.getMethod();
        // 域名级未配置（含空集合）时回落全局默认；显式空集合即"不允许任何方法"
        val allowedMethods = ObjectUtil.defaultIfNull(originConfig.getAllowedMethods(), config.getAllowedMethods());
        if (!CCollUtils.containsAny(allowedMethods, CConstants.STAR, method)) {
            log.info("Not allow origin with method: {} {}", method, origin);
            return;
        }

        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        // 允许当前请求方法类型
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, method);

        // 域名级未配置或空集合时回落全局默认
        val allowedHeaders = CollUtil.defaultIfEmpty(originConfig.getAllowedHeaders(), config.getAllowedHeaders());
        setHeaderIfNotEmpty(response, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowedHeaders);
        // 暴露给浏览器脚本可读的响应头（默认仅简单响应头可读，如 Authorization 需显式暴露）；默认禁用，需显式开启
        if (CBoolUtils.isTrue(originConfig.getExposeHeaders())) {
            // 域名级未配置时回落全局默认；显式空集合即"不暴露任何头"
            val exposedHeaders = ObjectUtil.defaultIfNull(originConfig.getExposedHeaders(), config.getExposedHeaders());
            setHeaderIfNotEmpty(response, HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeaders);
        }
        // 是否允许携带凭据；默认禁用，需显式开启
        if (CBoolUtils.isTrue(originConfig.getCredentials())) {
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

    }

    /**
     * 取域名对应的配置：先按 {@code host:port} 原样匹配，未命中再按纯 {@code host} 匹配
     *
     * <p>配置文件的域名 key<b>只支持纯 host</b>——{@code cors.origins[host:port]} 在 Spring 绑定阶段
     * 会被规范化成不含点号与冒号的形态（实测 {@code a.example.com:8081} 绑不到、且不同域名会互相覆盖），
     * 而请求侧归一化结果 {@code CUrlUtils.getHostWithPort} 带端口，故此处按两种形态依次匹配：
     * 先精确匹配 {@code host:port}（兼容直接设置的 Map），再回落到纯 host。</p>
     *
     * @param newOrigin 归一化后的来源（{@code host} 或 {@code host:port}）
     * @return 域名级配置；均未命中时返回 null
     */
    public CCorsOriginConfig getOriginConfig(String newOrigin) {
        val origins = config.getOrigins();
        val exact = CMapUtils.get(origins, newOrigin);
        if (Objects.nonNull(exact)) {
            return exact;
        }

        // 配置 key 只支持纯 host：去掉端口后再匹配（getHost 对无协议的 host:port 返回 null，不能用它裁剪）
        val idx = newOrigin.lastIndexOf(':');
        if (idx < 0) {
            return null;
        }
        return CMapUtils.get(origins, newOrigin.substring(0, idx));
    }

    /**
     * 头集合非空时，拼接（含 {@link CConstants#STAR} 用 {@code *}）并设置响应头；集合为 null 或空则均不设置
     *
     * @param response   响应
     * @param headerName 响应头名
     * @param headers    头集合
     */
    public void setHeaderIfNotEmpty(
        CHttpResponse response,
        String headerName,
        Set<String> headers
    ) {
        if (CollUtil.isEmpty(headers)) {
            return;
        }
        response.setHeader(headerName, joinHeaders(headers));
    }

    /**
     * 将头集合转为逗号分隔的头值；集合含 {@link CConstants#STAR} 时直接使用 {@code *} 通配
     *
     * @param headers 头集合（已保证非 null 非空，由 {@link #setHeaderIfNotEmpty} 调用）
     * @return 头值
     */
    public String joinHeaders(Set<String> headers) {
        return headers.contains(CConstants.STAR)
            ? CConstants.STAR
            : CollUtil.join(headers, ",");
    }

}
