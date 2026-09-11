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
 * <h2>能力目录</h2>
 * <p>{@code CResultUtils} 为响应结果工具类，提供：</p>
 * <ul>
 *   <li>{@code SUCCESS_CODES}：成功状态码集合（{@code 0}、{@code 200}、{@code 000000}，不可变）</li>
 *   <li>{@code isSuccess} / {@code isNotSuccess}：判断结果是否成功</li>
 *   <li>{@code assertSuccess}：断言成功，失败抛业务异常</li>
 *   <li>{@code getData} / {@code getDataDefaultEmptyList}：断言成功并获取数据（可带默认值）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>isSuccess：result 为 null / code 为空</td>
 *     <td>返回 false</td>
 *   </tr>
 *   <tr>
 *     <td>assertSuccess：result 为 null</td>
 *     <td>抛业务异常（"未返回数据"）</td>
 *   </tr>
 *   <tr>
 *     <td>assertSuccess：失败结果</td>
 *     <td>抛业务异常（[code] message）</td>
 *   </tr>
 *   <tr>
 *     <td>getData：数据为 null</td>
 *     <td>返回默认值（无默认值返回 null）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>远程调用/接口返回结果统一判断成功与取数，避免分散判断。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅支持 {@code ICBaseResult&lt;?,?&gt;} 类型结果；其他响应模型需适配。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>成功码集合固定（0/200/000000），业务自定义成功码需扩展集合。</li>
 *   <li>断言失败抛业务异常，保证调用方明确感知失败。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>成功判断</b></p>
 * <ul>
 *   <li>code 经 {@code Opt.ofNullable(result).map(getCode).map(toStringOrNull)} 提取并转为字符串。</li>
 *   <li>code 为空白（null/空）返回 false；否则判断是否在 {@code SUCCESS_CODES} 集合中。</li>
 *   <li>{@code isNotSuccess} = {@code !isSuccess}。</li>
 * </ul>
 * <p><b>断言与异常</b></p>
 * <ul>
 *   <li>{@code assertSuccess}：result 为 null 抛业务异常（"未返回数据"）；isNotSuccess 时抛业务异常</li>
 *   <li>（格式 {@code [code] message}）。</li>
 *   <li>异常构造经 {@code CExceptionUtils.newBusinessException(null, message)}（@SneakyThrows 上抛）。</li>
 * </ul>
 * <p><b>数据获取</b></p>
 * <ul>
 *   <li>{@code ObjUtil.defaultIfNull(result.getData(), defaultValue)}——数据为 null 时返回默认值。</li>
 * </ul>
 *
 * @since 2025/2/11
 * @version 1.0
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
