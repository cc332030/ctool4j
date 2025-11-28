package com.c332030.ctool4j.job.xxl.util;

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
 * @since 2025/11/27
 */
@UtilityClass
public class CXxlJobUtils {

    /**
     * 获取任务参数
     * @return 任务参数
     */
    public String getJobParam() {
        return XxlJobHelper.getJobParam();
    }

    /**
     * 获取任务参数
     * @param clazz 任务参数类型
     * @return 任务参数
     */
    public <T> T parseJobParam(Class<T> clazz) {
        return CJsonUtils.fromJson(getJobParam(), clazz);
    }

    /**
     * 获取任务参数
     * @param typeReference 任务参数类型
     * @return 任务参数
     */
    public <T> T parseJobParam(TypeReference<T> typeReference) {
        return CJsonUtils.fromJson(getJobParam(), typeReference);
    }

    /**
     * 获取任务参数
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
     * @return 任务参数
     */
    public <T> List<T> jobParamSplit(CFunction<String, T> convert) {
        return jobParamSplit(null, convert);
    }

    /**
     * 获取任务参数
     * @param separator 分隔符
     * @param convert 转换方法
     * @return 任务参数
     */
    public <T> List<T> jobParamSplit(String separator, CFunction<String, T> convert) {
        return CStrUtils.splitToList(getJobParam(), separator, convert);
    }

}
