package com.c332030.ctool4j.job.util;

import cn.hutool.core.date.DateUtil;
import com.c332030.ctool4j.core.function.StartEndTimeConsumer;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * <p>
 * Description: CJobUtils
 * </p>
 *
 * @since 2025/10/31
 */
@UtilityClass
public class CJobUtils {

    public void dayJobTime(String param, StartEndTimeConsumer consumer) {
        dayJobTime(param, 1, consumer);
    }

    @SneakyThrows
    public void dayJobTime(String param, int days, StartEndTimeConsumer consumer) {

        Instant instant = null;
        if(StringUtils.isNotBlank(param)){
            instant = DateUtil.parse(param).toInstant();
        }

        if(null == instant) {
            instant =  Instant.now();
        }

        val endTime = instant
                .atZone(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.DAYS)
                .plusDays(1)
                .toInstant()
                ;

        val startTime = endTime
                .minus(1, ChronoUnit.HOURS)
                .minus(days, ChronoUnit.DAYS);

        consumer.accept(startTime, endTime);

    }

}
