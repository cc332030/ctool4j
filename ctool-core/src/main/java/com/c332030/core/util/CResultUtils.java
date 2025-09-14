package com.c332030.core.util;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.core.exception.CExceptionUtils;
import com.c332030.core.model.ICResult;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Description: ResultUtils
 * </p>
 *
 * @since 2025/2/11
 */
@CustomLog
@UtilityClass
public class CResultUtils {

    public Set<String> SUCCESS_CODES = Stream.of(
            0,
            HttpStatus.OK.value(),
            "000000"
    ).map(String::valueOf).collect(Collectors.toSet());

    public static boolean isSuccess(ICResult<?, ?> result) {
        return SUCCESS_CODES.contains(StrUtil.toStringOrNull(result.getCode()));
    }

    public static boolean isNotSuccess(ICResult<?, ?> result) {
        return !isSuccess(result);
    }

    @SneakyThrows
    private static void throwException(ICResult<?, ?> result) {
        throw CExceptionUtils.newBusinessException(
                result.getCode()
                + "："
                + result.getMessage()
        );
    }

    @SneakyThrows
    public static void assertSuccess(ICResult<?, ?> result) {

        if(null == result) {
            throw CExceptionUtils.newBusinessException("未返回数据");
        }

        if (isNotSuccess(result)) {
            throwException(result);
        }
    }

    public static <T> T getData(ICResult<?, T> result) {
        return getData(result, null);
    }

    public static <T> List<T> getDataDefaultEmptyList(ICResult<?, List<T>> result) {
        return getData(result, CList.of());
    }

    public static <T> T getData(ICResult<?, T> result, T defaultValue) {

        assertSuccess(result);
        return ObjUtil.defaultIfNull(result.getData(), defaultValue);
    }

}
