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
 * <h2>能力目录</h2>
 * <p>{@code CJobUtils}（{@code @UtilityClass}）提供定时任务的时间窗口计算：</p>
 * <p>时间窗口语义：</p>
 * <ul>
 *   <li>{@code endTime}：参数所在日的次日零点（按系统默认时区日边界截断）。</li>
 *   <li>{@code startTime}：{@code endTime} 向前减 1 小时再减 {@code days} 天。</li>
 *   <li>{@code consumer.accept(startTime, endTime)} 接收计算结果。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>param 为空/空白</td>
 *     <td>取当前时间 {@code Instant.now()} 计算</td>
 *   </tr>
 *   <tr>
 *     <td>param 格式非法</td>
 *     <td>抛出 {@code cn.hutool.core.date.DateException}</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>每日/多日定时任务的执行时间窗口计算。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>时区相关计算依赖系统默认时区，跨时区部署时结果可能不同。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>时区按 {@code ZoneId.systemDefault()}，不显式指定。</li>
 *   <li>startTime 故意向前多减 1 小时作为时间窗口缓冲，属设计取舍。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>时间计算</b></p>
 * <ul>
 *   <li>param 非空时用 hutool {@code DateUtil.parse} 解析为 {@code Instant}；param 为空/空白时取当前时间 {@code Instant.now()}。</li>
 *   <li>{@code endTime = 参数时间所在日 次日零点}（{@code truncatedTo(DAYS)} 后 {@code plusDays(1)}）。</li>
 *   <li>{@code startTime = endTime - 1 小时 - days 天}（故意多减 1 小时，向前多覆盖 1 小时，避免任务执行延迟导致边界数据漏处理）。</li>
 * </ul>
 * <p><b>异常处理</b></p>
 * <ul>
 *   <li>param 格式非法时抛异常（{@code DateUtil.parse}），交由任务框架处理，不静默兜底。</li>
 * </ul>
 *
 * @since 2025/10/31
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CJobUtils {

    /**
     * 每日运行任务时间计算处理，一天
     *
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
