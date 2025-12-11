package com.c332030.ctool4j.core.util;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.exception.CExceptionUtils;
import com.c332030.ctool4j.definition.model.result.ICBaseResult;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

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

    private static final String EXCEPTION_MESSAGE_TEMPLATE = "错误码：{}，错误信息：{}";

    public Set<String> SUCCESS_CODES = Stream.of(
            0,
            HttpStatus.OK.value(),
            "000000"
    ).map(String::valueOf).collect(Collectors.toSet());

    public static boolean isSuccess(@Nullable ICBaseResult<?, ?> result) {

        val code = Opt.ofNullable(result)
            .map(ICBaseResult::getCode)
            .map(StrUtil::toStringOrNull)
            .orElse(null);
        if (StrUtil.isBlank(code)) {
            return false;
        }

        return SUCCESS_CODES.contains(code);
    }

    public static boolean isNotSuccess(@Nullable ICBaseResult<?, ?> result) {
        return !isSuccess(result);
    }

    @SneakyThrows
    private static void throwException(ICBaseResult<?, ?> result) {

        val message = StrUtil.format(EXCEPTION_MESSAGE_TEMPLATE, result.getCode(), result.getMessage());
        throw CExceptionUtils.newBusinessException(null, message);
    }

    @SneakyThrows
    public static void assertSuccess(@Nullable ICBaseResult<?, ?> result) {

        if(null == result) {
            throw CExceptionUtils.newBusinessException(null, "未返回数据");
        }

        if (isNotSuccess(result)) {
            throwException(result);
        }
    }

    public static <T> T getData(@Nullable ICBaseResult<?, T> result) {
        return getData(result, null);
    }

    public static <T> List<T> getDataDefaultEmptyList(@Nullable ICBaseResult<?, List<T>> result) {
        return getData(result, CList.of());
    }

    public static <T> T getData(@Nullable ICBaseResult<?, T> result, T defaultValue) {

        assertSuccess(result);
        return ObjUtil.defaultIfNull(result.getData(), defaultValue);
    }

}
