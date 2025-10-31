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

    /**
     * 每日运行任务时间计算处理，一天
     * @param param 定时任务参数
     * @param consumer 开始、结束时间消费器
     */
    public void dayJobTime(String param, StartEndTimeConsumer consumer) {
        dayJobTime(param, 1, consumer);
    }

    /**
     * 每日运行任务时间计算处理
     * @param param 定时任务参数
     * @param days 天数
     * @param consumer 开始、结束时间消费器
     */
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
