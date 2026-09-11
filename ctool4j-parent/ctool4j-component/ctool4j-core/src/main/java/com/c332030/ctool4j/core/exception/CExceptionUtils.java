package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.core.util.CSpiUtils;
import com.c332030.ctool4j.definition.function.CRunnable;
import com.c332030.ctool4j.definition.function.CSupplier;
import com.c332030.ctool4j.definition.function.CTriFunction;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.util.LinkedHashSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CExceptionUtils
 * </p>
 *
 * <p>异常工具类，围绕业务异常提供者 {@link ICBusinessExceptionProvider} 提供：</p>
 * <ul>
 *   <li>提供者与构造函数：{@link #BUSINESS_EXCEPTION_PROVIDER}（经 {@code CSpiUtils} 取首个自定义 SPI 实现，无则用默认实现
 *       {@link CBusinessExceptionProvider}）、{@link #getBusinessExceptionFunction()}；</li>
 *   <li>构造业务异常：{@code newBusinessException} 系列，按错误码 {@link ICRes}、附加信息 message、原因 cause 组合构造
 *       （不抛出，返回异常对象）；</li>
 *   <li>抛出业务异常：{@code throwBusinessException} 系列，按同上组合构造并抛出，message 支持直接传值或经
 *       {@link java.util.function.Supplier} 延迟求值；</li>
 *   <li>忽略异常：{@code ignore} 系列，执行 {@code CRunnable} / {@code CSupplier}，异常记日志后吞掉不向上抛，无结果时返回 null；</li>
 * </ul>
 *
 * <p><b>约定</b>：业务异常的具体类型由 SPI 提供者决定，本类只负责按错误码与附加信息调用其构造函数；附加信息与原因可为 null，
 * 由提供者实现决定是否纳入异常内容。异常信息拼接口径见 {@code getMessageWithCause} 的方法说明。</p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CExceptionUtils} 为异常工具类，提供：</p>
 * <ul>
 *   <li>业务异常提供者：{@code BUSINESS_EXCEPTION_PROVIDER}（优先取自定义 SPI 实现，默认 CBusinessExceptionProvider）</li>
 *   <li>创建：{@code newBusinessException}（多重载：error/message/cause）</li>
 *   <li>抛出：{@code throwBusinessException}（多重载，@SneakyThrows）</li>
 *   <li>忽略执行：{@code ignore}（runnable/supplier，失败记录日志返回 null）</li>
 *   <li>异常链信息：{@code getMessageWithCause}（拼接异常及其 cause 消息）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>ignore 执行失败</td>
 *     <td>记录 error 日志；supplier 返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>getMessageWithCause(null)</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>getMessageWithCause 异常链成环</td>
 *     <td>去重停止，避免死循环</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>业务异常统一创建/抛出、非关键操作忽略异常、异常链信息展示。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>ignore 会吞掉异常（仅记录日志），需要失败处理时不适用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>ignore 吞异常换取不中断，牺牲失败可见性。</li>
 *   <li>SPI 提供者支持自定义业务异常类型，灵活可扩展。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>SPI 提供者</b></p>
 * <ul>
 *   <li>{@code BUSINESS_EXCEPTION_PROVIDER} 经 {@code CSpiUtils.getFirstCustomImplOrDefault} 取自定义 SPI 实现，</li>
 *   <li>默认 {@code CBusinessExceptionProvider}，可通过 SPI 扩展自定义业务异常类型。</li>
 * </ul>
 * <p><b>创建与抛出</b></p>
 * <ul>
 *   <li>{@code throwBusinessException} 经 @SneakyThrows 抛出业务异常。</li>
 * </ul>
 * <p><b>异常链信息</b></p>
 * <ul>
 *   <li>{@code getMessageWithCause}：用 LinkedHashSet 遍历异常链（防环），拼接各层 {@code getMessage}（{@code \ncause by} 分隔）。</li>
 * </ul>
 *
 * @since 2025/9/14
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CExceptionUtils {

    /**
     * 业务异常提供者（优先取自定义 SPI 实现）
     */
    @SuppressWarnings("unchecked")
    public static final ICBusinessExceptionProvider<? extends Throwable> BUSINESS_EXCEPTION_PROVIDER =
            CSpiUtils.getFirstCustomImplOrDefault(ICBusinessExceptionProvider.class, CBusinessExceptionProvider.class);

    /**
     * 获取业务异常构造函数
     * <ul>
     *   <li>{@code newBusinessException} 委托 {@code getBusinessExceptionFunction()}（error/msgExtend/cause 三元构造）。</li>
     * </ul>
     *
     * @return 业务异常构造函数（错误码、附加信息、原因构造异常）
     */
    public CTriFunction<ICRes<?>, String, Throwable, ? extends Throwable> getBusinessExceptionFunction() {
        return BUSINESS_EXCEPTION_PROVIDER.getExceptionFunction();
    }

    /**
     * 根据错误码构造业务异常
     *
     * @param error 错误码定义
     * @param <T>   异常类型
     * @return 业务异常
     */
    public <T extends Throwable> T newBusinessException(ICRes<?> error) {
        return newBusinessException(error, null);
    }

    /**
     * 根据信息构造业务异常
     *
     * @param message 异常信息
     * @param <T>     异常类型
     * @return 业务异常
     */
    public <T extends Throwable> T newBusinessException(String message) {
        return newBusinessException(null, message);
    }

    /**
     * 根据错误码和信息构造业务异常
     *
     * @param error   错误码定义
     * @param message 异常信息
     * @param <T>     异常类型
     * @return 业务异常
     */
    public <T extends Throwable> T newBusinessException(ICRes<?> error, String message) {
        return newBusinessException(error, message, null);
    }

    /**
     * 根据错误码、信息和原因构造业务异常
     *
     * @param error   错误码定义
     * @param message 异常信息
     * @param cause   异常原因
     * @param <T>     异常类型
     * @return 业务异常
     */
    @SuppressWarnings("unchecked")
    public <T extends Throwable> T newBusinessException(ICRes<?> error, String message, Throwable cause) {
        return (T) getBusinessExceptionFunction().apply(error, message, cause);
    }

    /**
     * 抛出业务异常
     *
     * @param error 错误码定义
     */
    @SneakyThrows
    public void throwBusinessException(ICRes<?> error) {
        throw newBusinessException(error);
    }

    /**
     * 抛出业务异常
     *
     * @param message 异常信息
     */
    @SneakyThrows
    public void throwBusinessException(String message) {
        throw newBusinessException(message);
    }

    /**
     * 抛出业务异常
     *
     * @param error   错误码定义
     * @param message 异常信息
     */
    @SneakyThrows
    public void throwBusinessException(ICRes<?> error, String message) {
        throw newBusinessException(error, message);
    }

    /**
     * 抛出业务异常
     *
     * @param error   错误码定义
     * @param message 异常信息
     * @param cause   异常原因
     */
    @SneakyThrows
    public void throwBusinessException(ICRes<?> error, String message, Throwable cause) {
        throw newBusinessException(error, message, cause);
    }

    /**
     * 抛出业务异常
     *
     * @param messageSupplier 异常信息供应商
     */
    public void throwBusinessException(Supplier<String> messageSupplier) {
        throwBusinessException(messageSupplier.get());
    }

    /**
     * 抛出业务异常
     *
     * @param error           错误码定义
     * @param messageSupplier 异常信息供应商
     */
    public void throwBusinessException(ICRes<?> error, Supplier<String> messageSupplier) {
        throwBusinessException(error, messageSupplier.get());
    }

    /**
     * 抛出业务异常
     *
     * @param error           错误码定义
     * @param messageSupplier 异常信息供应商
     * @param cause           异常原因
     */
    public void throwBusinessException(ICRes<?> error, Supplier<String> messageSupplier, Throwable cause) {
        throwBusinessException(error, messageSupplier.get(), cause);
    }

    /**
     * 忽略异常执行（失败仅记录日志）
     * <ul>
     *   <li>{@code ignore(runnable/supplier)}：try-catch(Throwable) 记录 error 日志；supplier 失败返回 null。</li>
     * </ul>
     *
     * @param runnable 待执行操作
     */
    public void ignore(CRunnable runnable) {
        ignore(runnable, "处理失败");
    }

    /**
     * 忽略异常执行（失败仅记录日志）
     *
     * @param runnable 待执行操作
     * @param message  失败日志信息
     */
    public void ignore(CRunnable runnable, String message) {
        try {
            runnable.run();
        } catch (Throwable e) {
            log.error(message, e);
        }
    }

    /**
     * 忽略异常执行（失败仅记录日志）
     *
     * @param supplier 结果供应商
     * @param <T>      结果类型
     * @return 执行结果，失败时返回 null
     */
    public <T> T ignore(CSupplier<T> supplier) {
        return ignore(supplier, "处理失败");
    }

    /**
     * 忽略异常执行（失败仅记录日志）
     *
     * @param supplier 结果供应商
     * @param message  失败日志信息
     * @param <T>      结果类型
     * @return 执行结果，失败时返回 null
     */
    public <T> T ignore(CSupplier<T> supplier, String message) {
        try {
            return CSupplier.get(supplier);
        } catch (Throwable e) {
            log.error(message, e);
            return null;
        }
    }

    /**
     * 获取异常信息及其所有 cause 信息
     * <ul>
     *   <li>异常信息拼接：{@link #getMessageWithCause(Throwable)} 拼接异常 message 与 cause 链。</li>
     * </ul>
     *
     * @param throwable 异常
     * @return 异常链信息；异常为 null 时返回 null
     */
    public String getMessageWithCause(Throwable throwable) {

        if(null == throwable) {
            return null;
        }

        val throwableSet = new LinkedHashSet<Throwable>();

        var throwableNew = throwable;
        while (true) {

            throwableSet.add(throwableNew);
            val cause = throwableNew.getCause();
            if(null == cause || throwableSet.contains(cause)) {
                break;
            }
            throwableNew = cause;

        }

        return throwableSet.stream()
            .map(Throwable::getMessage)
            .collect(Collectors.joining("\ncause by "));
    }

}
