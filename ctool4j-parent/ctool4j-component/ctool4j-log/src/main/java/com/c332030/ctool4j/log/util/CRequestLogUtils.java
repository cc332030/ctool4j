package com.c332030.ctool4j.log.util;

import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.log.model.CRequestLog;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import com.homecare.common.enums.RequestHeaderEnum;
import com.homecare.common.model.RequestLog;
import com.homecare.common.util.JSONUtils;
import com.homecare.common.util.RequestUtils;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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

    private static final Logger REQUEST_LOGGER = LoggerFactory.getLogger("request-logger");

    private static final ThreadLocal<CRequestLog> REQUEST_LOG_THREAD_LOCAL = new ThreadLocal<>();

    private static final BlockingQueue<CRequestLog> REQUEST_LOG_QUEUE = new LinkedBlockingQueue<>();

    private final static Thread REQUEST_LOG_THREAD = new Thread(CRequestLogUtils::asyncWrite);
    static {
        REQUEST_LOG_THREAD.start();
    }

    public static CRequestLog getRequestLog() {
        return REQUEST_LOG_THREAD_LOCAL.get();
    }

    public static void init(Map<String, Object> reqs) {

        reqs.entrySet().forEach(entry -> {

            val value = entry.getValue();

            var valueNew = value;
            if(value instanceof ServletRequest || value instanceof ServletResponse) {
                valueNew = null;
            }

            if(value instanceof MultipartFile file) {
                valueNew = file.getOriginalFilename() + ":" + file.getSize();
            }
            entry.setValue(valueNew);

        });

        val request = CRequestUtils.getRequest();
        val session = CRequestUtils.getSession();

        val requestLog = CRequestLog.builder()
                .path(request.getRequestURI())
                .reqs(reqs)
                .token(CRequestUtils.getHeader(RequestHeaderEnum.AUTHORIZATION))
                .traceId(session.traceId.getValue())
                .tenantId(session.tenantId.getValue())
                .userId(session.userId.getValue())
                .ip(RequestUtils.getIp(request))
                .beginTimeMillis(System.currentTimeMillis())
                .build();

        REQUEST_LOG_THREAD_LOCAL.set(requestLog);

    }

    public static void write(Object rsp, Throwable throwable) {

        val requestLog = getRequestLog();

        val endTimeMillis = System.currentTimeMillis();

        requestLog.setEndTimeMillis(endTimeMillis);
        requestLog.setRt(endTimeMillis - requestLog.getBeginTimeMillis());
        requestLog.setRsp(rsp);
        if(null != throwable) {
            requestLog.setThrowableMessage(throwable.getMessage());
        }

        val offerResult = REQUEST_LOG_QUEUE.offer(requestLog);
        if(!offerResult) {
            log.error("REQUEST_LOG_THREAD offer error, requestLog: {}", JSONUtils.toJson(requestLog));
        }

        REQUEST_LOG_THREAD_LOCAL.remove();

    }

    public static void asyncWrite() {

        while (true) {
            try {
                val requestLog = REQUEST_LOG_QUEUE.take();
                REQUEST_LOGGER.info("{}", CJsonUtils.toJson(requestLog));
            } catch (Throwable e) {
                log.error("RequestLogUtils asyncWrite error", e);
            }
        }

    }

}
