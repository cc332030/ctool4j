package com.c332030.ctool4j.log.util;

import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.core.util.CObjUtils;
import com.c332030.ctool4j.log.model.CRequestLog;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

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
            var newValue = value;
            val vClass = CObjUtils.convert(value, Object::getClass);

            if(value == null) {
                newValue = "[null]";
            } else if(value instanceof MultipartFile) {
                val file = (MultipartFile) value;
                newValue = file.getOriginalFilename() + ":" + file.getSize();
            } else if(!CLogUtils.isJsonLog(vClass)) {
                newValue = "[not json class: " + vClass + "]";
            }

            entry.setValue(newValue);

        });

        val request = CRequestUtils.getRequest();
        val traceId = CTraceUtils.getTraceId();

        val requestLog = CRequestLog.builder()
                .path(request.getRequestURI())
                .token(CRequestUtils.getHeader(HttpHeaders.AUTHORIZATION))
                .reqs(reqs)
                .traceId(traceId)
                .ip(CRequestUtils.getIp(request))
                .beginTimeMillis(System.currentTimeMillis())
                .build();

        REQUEST_LOG_THREAD_LOCAL.set(requestLog);

    }

    public static void write(Object rsp, Throwable throwable) {

        try {

            val requestLog = getRequestLog();
            if(null == requestLog) {
                log.warn("write failure because requestLog is null");
                return;
            }

            val endTimeMillis = System.currentTimeMillis();

            requestLog.setEndTimeMillis(endTimeMillis);
            requestLog.setRt(endTimeMillis - requestLog.getBeginTimeMillis());
            requestLog.setRsp(rsp);
            if(null != throwable) {
                requestLog.setThrowableMessage(throwable.getMessage());
            }

            val offerResult = REQUEST_LOG_QUEUE.offer(requestLog);
            if(!offerResult) {
                log.error("REQUEST_LOG_THREAD offer error, requestLog: {}", requestLog);
            }
        } finally {
            REQUEST_LOG_THREAD_LOCAL.remove();
        }

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
