package com.c332030.ctool4j.log.util;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.log.CLog;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.log.config.CRequestLogConfig;
import com.c332030.ctool4j.log.enums.CRequestLogTypeEnum;
import com.c332030.ctool4j.log.model.CRequestLog;
import com.c332030.ctool4j.spring.util.CRequestUtils;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
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

    private static final CLog REQUEST_LOGGER = CLogUtils.getLog("request-log");

    private static final ThreadLocal<CRequestLog> REQUEST_LOG_THREAD_LOCAL = new ThreadLocal<>();

    private static final BlockingQueue<CRequestLog> REQUEST_LOG_QUEUE = new LinkedBlockingQueue<>();

    private final static Thread REQUEST_LOG_THREAD = new Thread(CRequestLogUtils::asyncWrite);
    static {
        REQUEST_LOG_THREAD.start();
    }

    @Setter
    private static CRequestLogConfig requestLogConfig;

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
                .path(request.getRequestURI())
                .token(CRequestUtils.getHeader(HttpHeaders.AUTHORIZATION))
                .traceId(traceId)
                .ip(CRequestUtils.getIp(request))
                .beginTimeMillis(System.currentTimeMillis())
                .build();

        REQUEST_LOG_THREAD_LOCAL.set(requestLog);

    }

    public void remove() {
        REQUEST_LOG_THREAD_LOCAL.remove();
    }

    public void setReqs(Map<String, Object> reqs) {

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

        val requestLog = get();
        requestLog.setReqs(reqs);

    }

    public void write(Object rsp, Throwable throwable) {

        val requestLog = get();
        if(null == requestLog) {
            log.debug("write failure because requestLog is null");
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

    }

    public void asyncWrite() {

        while (true) {
            try {

                val requestLog = REQUEST_LOG_QUEUE.take();
                REQUEST_LOGGER.infoNonNull("{}", requestLog);
            } catch (Throwable e) {
                log.error("RequestLogUtils asyncWrite error", e);
            }
        }

    }

}
