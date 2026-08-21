package com.c332030.ctool4j.job.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.definition.function.StartEndTimeConsumer;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * <p>
 * Description: CJobUtils
 * </p>
 *
 * @see doc/design/job/CJobUtils.adoc
 * @see doc/design/job/CJobUtilsTests.adoc
 * @since 2025/10/31
 */
@CustomLog
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
    public void dayJobTime(String param, int days, StartEndTimeConsumer consumer) {

        Instant instant = null;
        if(StrUtil.isNotBlank(param)){
            // 任务参数来自外部配置，格式非法时抛异常，交由任务框架处理，不静默兜底
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

        // 故意多减 1 小时（时间窗口向前多覆盖 1 小时），避免任务执行延迟导致边界数据漏处理
        val startTime = endTime
                .minus(1, ChronoUnit.HOURS)
                .minus(days, ChronoUnit.DAYS);

        consumer.accept(startTime, endTime);

    }

}
