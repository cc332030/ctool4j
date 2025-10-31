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
 * Description: JobUtils
 * </p>
 *
 * @since 2025/10/31
 */
@UtilityClass
public class JobUtils {

    @SneakyThrows
    public void dayJobTime(String param, StartEndTimeConsumer consumer) {

        Instant instant = null;
        if(StringUtils.isNotBlank(param)){
            instant = DateUtil.parse(param).toInstant();
        }

        if(null == instant) {
            instant =  Instant.now();
        }

        val startDate = instant
                .atZone(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.DAYS)
                .minusHours(1)
                .toInstant()
                ;

        val endDate = startDate
                .plus(1, ChronoUnit.HOURS)
                .plus(1, ChronoUnit.DAYS);

        consumer.accept(startDate, endDate);

    }

}
