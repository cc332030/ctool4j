package com.c332030.ctool4j.log.util;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.log.CLog;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.log.config.CRequestLogConfig;
import com.c332030.ctool4j.log.enums.CRequestLogTypeEnum;
import com.c332030.ctool4j.log.model.CRequestLog;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>
 * Description: RequestLoggerUtils
 * </p>
 *
 * @author c332030
 * @since 2024/3/6
 */
@CustomLog
@UtilityClass
public class CRequestLogUtils {

    public final String REQUEST_LOG_STR = "request-log";

    public final String REQUEST_BODY = "requestBody";

    final CLog REQUEST_LOG = CLogUtils.getLog(REQUEST_LOG_STR);

    final ThreadLocal<CRequestLog> REQUEST_LOG_THREAD_LOCAL = new ThreadLocal<>();

    final BlockingQueue<CRequestLog> REQUEST_LOG_QUEUE = new LinkedBlockingQueue<>();

    final Map<String, Object> EMPTY_REQS = getRequestBodyMap("[no request body]");

    final Thread REQUEST_LOG_THREAD = new Thread(CRequestLogUtils::logWrite, REQUEST_LOG_STR + "-thread");
    static {
        REQUEST_LOG_THREAD.start();
    }

    @Setter
    CRequestLogConfig requestLogConfig;

    public boolean isEnable() {
        val enable = CObjUtils.convert(requestLogConfig, CRequestLogConfig::getEnable);
        return BooleanUtil.isTrue(enable);
    }

    public boolean isAopEnable() {
        return isEnable() && CRequestLogTypeEnum.AOP.equals(requestLogConfig.getType());
    }

    public boolean isAdviceEnable() {
        return isEnable() && CRequestLogTypeEnum.ADVICE.equals(requestLogConfig.getType());
    }

    public boolean isInterceptorEnable() {
        return isEnable() && CRequestLogTypeEnum.INTERCEPTOR.equals(requestLogConfig.getType());
    }

    public CRequestLog get() {
        return REQUEST_LOG_THREAD_LOCAL.get();
    }

    public CRequestLog getThenRemove() {
        val requestLog = get();
        remove();
        return requestLog;
    }

    public void init() {

        val request = CRequestUtils.getRequest();
        val traceId = CTraceUtils.getTraceId();

        val requestLog = CRequestLog.builder()
            .traceId(traceId)
            .path(request.getRequestURI())
            .token(CRequestUtils.getHeader(HttpHeaders.AUTHORIZATION))
            .reqs(EMPTY_REQS)
            .ip(CRequestUtils.getIp(request))
            .beginTimeMillis(System.currentTimeMillis())
            .build();

        REQUEST_LOG_THREAD_LOCAL.set(requestLog);

    }

    public void remove() {
        REQUEST_LOG_THREAD_LOCAL.remove();
    }

    public Map<String, Object> getRequestBodyMap(Object requestBody) {
        return CMap.of(
            REQUEST_BODY, requestBody
        );
    }

    public void setRequestBodyReq(Object req) {
        setPrintAbleReqs(getRequestBodyMap(req));
    }

    public void setPrintAbleReqs(Map<String, Object> reqs) {

        val reqMap = CMapUtils.mapValue(
            reqs,
            CLogUtils::getPrintAble
        );
        setReqs(reqMap);
    }

    public void setReqs(Map<String, Object> reqs) {
        get().setReqs(reqs);
    }

    public void write(Object rsp, Throwable throwable) {

        val requestLog = get();
        if (null == requestLog) {
            log.debug("write failure because requestLog is null");
            return;
        }

        val endTimeMillis = System.currentTimeMillis();

        requestLog.setEndTimeMillis(endTimeMillis);
        requestLog.setRt(endTimeMillis - requestLog.getBeginTimeMillis());
        requestLog.setRsp(rsp);
        if (null != throwable) {
            requestLog.setThrowableMessage(throwable.getMessage());
        }

        val offerResult = REQUEST_LOG_QUEUE.offer(requestLog);
        if (!offerResult) {
            log.error("REQUEST_LOG_QUEUE offer error, requestLog: {}", requestLog);
        }

    }

    public void logWrite() {

        while (true) {
            try {

                val requestLog = REQUEST_LOG_QUEUE.take();
                REQUEST_LOG.infoNonNull("{}", requestLog);
            } catch (Throwable e) {
                log.error("logWrite error", e);
            }
        }

    }

}
