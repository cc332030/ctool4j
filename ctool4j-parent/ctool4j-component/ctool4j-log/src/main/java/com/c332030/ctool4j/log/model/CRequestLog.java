package com.c332030.ctool4j.log.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * <p>
 * Description: CRequestLog
 * </p>
 *
 * @author c332030
 * @since 2024/5/6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CRequestLog {

    String path;
    String token;

    String traceId;
    String tenantId;
    String userId;

    String ip;

    Map<String, Object> reqs;
    Object rsp;

    String throwableMessage;

    long beginTimeMillis;
    long endTimeMillis;
    long rt;

}
