package com.c332030.ctool4j.job.xxljob.util;

import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * <p>
 * Description: CXxlJobUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CXxlJobUtils}（{@code @UtilityClass}）提供 xxl-job 任务参数获取与解析：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>任务参数为空</td>
 *     <td>由 CJsonUtils/CStrUtils 相应兜底（null/空）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>xxl-job 任务参数获取、解析与拆分。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 xxl-job 运行时上下文（XxlJobHelper）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>解析</b></p>
 * <ul>
 *   <li>经 {@code CJsonUtils.fromJson} 解析 JSON。</li>
 *   <li>{@code jobParamSplit} 经 {@code CStrUtils.splitToList} 拆分，支持分隔符与转换函数。</li>
 * </ul>
 *
 * @since 2025/11/27
 * @version 1.0
 */
@UtilityClass
public class CXxlJobUtils {

    /**
     * 获取任务参数
     * <ul>
     *   <li>{@code getJobParam()}：获取原始任务参数字符串（{@code XxlJobHelper.getJobParam}）。</li>
     * </ul>
     *
     * @return 任务参数
     */
    public String getJobParam() {
        return XxlJobHelper.getJobParam();
    }

    /**
     * 获取任务参数
     * <ul>
     *   <li>{@code parseJobParam(Class)} / {@code parseJobParam(TypeReference)}：解析为对象。</li>
     * </ul>
     *
     * @param clazz 任务参数类型
     * @param <T> 任务参数类型
     * @return 任务参数
     */
    public <T> T parseJobParam(Class<T> clazz) {
        return CJsonUtils.fromJson(getJobParam(), clazz);
    }

    /**
     * 获取任务参数
     * @param typeReference 任务参数类型
     * @param <T> 任务参数类型
     * @return 任务参数
     */
    public <T> T parseJobParam(TypeReference<T> typeReference) {
        return CJsonUtils.fromJson(getJobParam(), typeReference);
    }

    /**
     * 获取任务参数
     * <ul>
     *   <li>{@code jobParamSplit(...)}：按分隔符拆分任务参数为列表（支持转换函数）。</li>
     * </ul>
     *
     * @return 任务参数
     */
    public List<String> jobParamSplit() {
        return jobParamSplit((String) null);
    }

    /**
     * 获取任务参数
     * @param separator 分隔符
     * @return 任务参数
     */
    public List<String> jobParamSplit(String separator) {
        return jobParamSplit(separator, CFunction.self());
    }

    /**
     * 获取任务参数
     * @param convert 转换方法
     * @param <T> 任务参数类型
     * @return 任务参数
     */
    public <T> List<T> jobParamSplit(CFunction<String, T> convert) {
        return jobParamSplit(null, convert);
    }

    /**
     * 获取任务参数
     * @param separator 分隔符
     * @param convert 转换方法
     * @param <T> 任务参数类型
     * @return 任务参数
     */
    public <T> List<T> jobParamSplit(String separator, CFunction<String, T> convert) {
        return CStrUtils.splitToList(getJobParam(), separator, convert);
    }

}
