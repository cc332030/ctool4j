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
import java.util.stream.Stream;

/**
 * <p>
 * Description: CResultUtils
 * </p>
 *
 * @since 2025/2/11
 */
@CustomLog
@UtilityClass
public class CResultUtils {

    private static final String EXCEPTION_MESSAGE_TEMPLATE = "[{}] {}";

    /**
     * 成功状态码集合（不可变）
     */
    public final Set<String> SUCCESS_CODES =
        Stream.of(
                0,
                HttpStatus.OK.value(),
                "000000"
        ).map(String::valueOf)
        .collect(CCollectors.toUnmodifiableSet());

    /**
     * 判断结果是否成功
     *
     * @param result 结果
     * @return 是否成功，结果为 null 或 code 为空时返回 false
     */
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

    /**
     * 判断结果是否失败
     *
     * @param result 结果
     * @return 是否失败
     */
    public static boolean isNotSuccess(@Nullable ICBaseResult<?, ?> result) {
        return !isSuccess(result);
    }

    @SneakyThrows
    private static void throwException(ICBaseResult<?, ?> result) {

        val message = StrUtil.format(EXCEPTION_MESSAGE_TEMPLATE, result.getCode(), result.getMessage());
        throw CExceptionUtils.newBusinessException(null, message);
    }

    @SneakyThrows
    /**
     * 断言结果成功，失败时抛出业务异常
     *
     * @param result 结果
     * @throws Throwable 结果为 null 或失败时抛出业务异常
     */
    public static void assertSuccess(@Nullable ICBaseResult<?, ?> result) {

        if(null == result) {
            throw CExceptionUtils.newBusinessException(null, "未返回数据");
        }

        if (isNotSuccess(result)) {
            throwException(result);
        }
    }

    /**
     * 断言成功并获取结果数据
     *
     * @param result 结果
     * @param <T>    数据类型
     * @return 数据，为 null 时返回 null
     */
    public static <T> T getData(@Nullable ICBaseResult<?, T> result) {
        return getData(result, null);
    }

    /**
     * 断言成功并获取结果数据，为 null 时返回空列表
     *
     * @param result 结果
     * @param <T>    数据类型
     * @return 数据或空列表
     */
    public static <T> List<T> getDataDefaultEmptyList(@Nullable ICBaseResult<?, List<T>> result) {
        return getData(result, CList.of());
    }

    /**
     * 断言成功并获取结果数据，为 null 时返回默认值
     *
     * @param result       结果
     * @param defaultValue 默认值
     * @param <T>          数据类型
     * @return 数据或默认值
     */
    public static <T> T getData(@Nullable ICBaseResult<?, T> result, T defaultValue) {

        assertSuccess(result);
        return ObjUtil.defaultIfNull(result.getData(), defaultValue);
    }

}
