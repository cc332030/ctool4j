package com.c332030.ctool4j.core.test.exception;

import com.c332030.ctool4j.core.classes.CMethodHandleUtils;
import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.exception.CException;
import com.c332030.ctool4j.core.exception.CExceptionUtils;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.CustomLog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * Description: CExceptionUtilsTest
 * </p>
 * <p>`com.c332030.ctool4j.core.exception.CExceptionUtils`（CExceptionUtils）的测试用例</p>
 *
 * <p><b>用例设计思路</b>：按「创建业务异常 / 抛出业务异常 / 忽略执行 / 异常链信息」四个维度组织：</p>
 * <ul>
 *   <li>创建：覆盖四种入参组合（错误码、信息、错误码+信息、错误码+信息+原因），断言实际异常类型与承载字段；</li>
 *   <li>抛出：以 {@code assertThrowsExactly} 精确断言抛出的异常类型与 {@code cause} 传递；</li>
 *   <li>忽略执行：覆盖 {@code CRunnable} 与 {@code CSupplier} 两个入口的正常路径（无异常）与异常路径（吞异常），
 *   异常路径断言不向上抛且 {@code CSupplier} 返回 null；</li>
 *   <li>异常链：覆盖 null、单层、多层 cause、自引用 cause（防死循环）。</li>
 * </ul>
 *
 * <p><b>设计依据</b>：依据功能设计对默认提供者返回 {@link CBusinessException}、{@code ignore} 吞异常并返回 null、
 * {@code getMessageWithCause} 拼接全链且对 null 返回 null 的约定；依据等价类/边界/异常路径覆盖。</p>
 * <p><b>覆盖场景</b>：{@code newBusinessException} 四种入参组合；{@code throwBusinessException} 抛出类型与 cause 传递、
 * 信息经 {@code Supplier} 延迟求值；{@code ignore} 正常/异常（{@code CRunnable} 与 {@code CSupplier}）；
 * {@code getMessageWithCause} null / 单层 / 多层 / 自引用 cause。</p>
 * <p><b>未覆盖</b>：SPI 自定义业务异常提供者的替换（默认提供者行为已由 {@code CBusinessExceptionProviderTests} 覆盖）。</p>
 *
 * <p><b>用例编号索引</b>：1 创建业务异常（1.1-1.4）；2 抛出业务异常（2.1-2.2）；3 忽略执行（3.1-3.4）；4 异常链信息（4.1-4.4）。
 * 各测试方法 javadoc 标注其编号与说明。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「创建业务异常 / 抛出业务异常 / 忽略执行 / 异常链信息」四个维度组织，每个维度覆盖正常、边界与异常路径。</li>
 *   <li>创建：覆盖四种入参组合（错误码、信息、错误码+信息、错误码+信息+原因），断言实际异常类型、{@code error}/{@code msgExtend}/{@code cause} 字段与消息格式。</li>
 *   <li>抛出：以 {@code assertThrowsExactly} 精确断言抛出类型（避免父类异常掩盖），并断言 {@code cause} 原样传递；消息支持 {@code Supplier} 延迟求值。</li>
 *   <li>忽略执行：{@code CRunnable} 与 {@code CSupplier} 两个入口各覆盖正常路径（无异常）与异常路径（吞异常），</li>
 *   <li>异常路径断言不向上抛且 {@code CSupplier} 返回 null。</li>
 *   <li>异常链：覆盖 null、单层、多层 cause，以及循环 cause 链（{@code a→b→a}）验证实现侧 {@code LinkedHashSet} 去重保护不进入死循环。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对默认提供者返回 {@code CBusinessException}、{@code ignore} 吞异常并返回 null、</li>
 *   <li>{@code getMessageWithCause} 拼接全链且对 null 返回 null 的约定。</li>
 *   <li>依据等价类/边界/异常路径覆盖：四种入参组合、null 与单层/多层/循环 cause、正常与异常执行。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：{@code newBusinessException} 四种入参组合（含 {@code error}/{@code msgExtend}/{@code cause} 字段断言）；</li>
 *   <li>{@code throwBusinessException} 抛出类型与 cause 传递、信息经 {@code Supplier} 延迟求值；</li>
 *   <li>{@code ignore} 正常/异常（{@code CRunnable} 与 {@code CSupplier} 各两条）；</li>
 *   <li>{@code getMessageWithCause} null / 单层 / 多层 / 循环 cause。</li>
 *   <li>未覆盖：SPI 自定义业务异常提供者的替换（默认提供者行为已由 {@code CBusinessExceptionProviderTests} 覆盖）。</li>
 * </ul>
 * <h2>创建业务异常</h2>
 * <ul>
 *   <li>1.1 仅信息：返回 CBusinessException（newBusinessException）</li>
 *   <li>1.2 仅错误码：异常携带 error、msgExtend 为 null、消息为 "[code] msg"（newBusinessException_errorOnly）</li>
 *   <li>1.3 错误码 + 信息：msgExtend 与消息同时体现附加信息（newBusinessException_errorAndMessage）</li>
 *   <li>1.4 错误码 + 信息 + 原因：cause 原样传递（newBusinessException_errorMessageAndCause）</li>
 * </ul>
 * <h2>抛出业务异常</h2>
 * <ul>
 *   <li>2.1 抛出 CBusinessException 且 cause 原样传递（throwBusinessException_carriesCause）</li>
 *   <li>2.2 信息经 Supplier 延迟求值（throwBusinessException_messageSupplier）</li>
 * </ul>
 * <h2>忽略执行</h2>
 * <ul>
 *   <li>3.1 CRunnable 正常执行（ignore_runnable_success）</li>
 *   <li>3.2 CRunnable 抛异常被吞掉、不向上抛（ignore_runnable_swallowsException）</li>
 *   <li>3.3 CSupplier 正常返回结果（ignore_supplier_success）</li>
 *   <li>3.4 CSupplier 抛异常被吞掉、返回 null（ignore_supplier_swallowsException）</li>
 * </ul>
 * <h2>异常链信息</h2>
 * <ul>
 *   <li>4.1 null 异常返回 null（getMessageWithCauseNull）</li>
 *   <li>4.2 单层异常仅返回自身消息（getMessageWithCause_singleLayer）</li>
 *   <li>4.3 含 cause：消息含 main 与 cause（getMessageWithCause）</li>
 *   <li>4.4 循环 cause 链不进入死循环（getMessageWithCause_cyclicCause）</li>
 * </ul>
 *
 * @since 2025/9/14
 * @version 1.0
 * @see CExceptionUtils
 */
@CustomLog
public class CExceptionUtilsTest {

    /**
     * 对应测试用例 1.1：仅信息：返回 CBusinessException
     * 1.1 仅信息：返回 CBusinessException（newBusinessException）
     */
    @Test
    public void newBusinessException() {

        val ex = CExceptionUtils.newBusinessException("test");
        Assertions.assertEquals(CBusinessException.class, ex.getClass());

    }

    /**
     * 对应测试用例 1.2：仅错误码：异常携带 error、msgExtend 为 null、消息为 "[code] msg"
     * 1.2 仅错误码：异常携带 error 且 message 含错误码与 msg（newBusinessException）
     */
    @Test
    public void newBusinessException_errorOnly() {

        val error = TestRes.of("100", "boom");
        val ex = CExceptionUtils.<CBusinessException>newBusinessException(error);

        Assertions.assertSame(error, ex.getError());
        Assertions.assertNull(ex.getMsgExtend());
        Assertions.assertEquals("[100] boom", ex.getMessage());

    }

    /**
     * 对应测试用例 1.3：错误码 + 信息：msgExtend 与消息同时体现附加信息
     * 1.3 错误码 + 信息：msgExtend 与 message 同时体现附加信息（newBusinessException）
     */
    @Test
    public void newBusinessException_errorAndMessage() {

        val error = TestRes.of("100", "boom");
        val ex = CExceptionUtils.<CBusinessException>newBusinessException(error, "detail");

        Assertions.assertSame(error, ex.getError());
        Assertions.assertEquals("detail", ex.getMsgExtend());
        Assertions.assertEquals("[100] boom: detail", ex.getMessage());

    }

    /**
     * 对应测试用例 1.4：错误码 + 信息 + 原因：cause 原样传递
     * 1.4 错误码 + 信息 + 原因：cause 原样传递（newBusinessException）
     */
    @Test
    public void newBusinessException_errorMessageAndCause() {

        val error = TestRes.of("100", "boom");
        val cause = new IllegalStateException("root");
        val ex = CExceptionUtils.<CBusinessException>newBusinessException(error, "detail", cause);

        Assertions.assertSame(cause, ex.getCause());
        Assertions.assertEquals("[100] boom: detail", ex.getMessage());

    }

    /**
     * 对应测试用例 2.1：抛出 CBusinessException 且 cause 原样传递
     * 2.1 抛出的异常类型为 CBusinessException 且原因原样传递（throwBusinessException）
     */
    @Test
    public void throwBusinessException_carriesCause() {

        val error = TestRes.of("400", "bad request");
        val cause = new IllegalArgumentException("root");

        val thrown = Assertions.assertThrowsExactly(
            CBusinessException.class,
            () -> CExceptionUtils.throwBusinessException(error, "detail", cause)
        );

        Assertions.assertSame(cause, thrown.getCause());
        Assertions.assertEquals("[400] bad request: detail", thrown.getMessage());

    }

    /**
     * 对应测试用例 2.2：信息经 Supplier 延迟求值
     * 2.2 信息经 Supplier 延迟求值（throwBusinessException）
     */
    @Test
    public void throwBusinessException_messageSupplier() {

        val error = TestRes.of("500", "server error");

        val thrown = Assertions.assertThrowsExactly(
            CBusinessException.class,
            () -> CExceptionUtils.throwBusinessException(error, () -> "lazy")
        );

        Assertions.assertEquals("[500] server error: lazy", thrown.getMessage());

    }

    /**
     * 对应测试用例 3.1：CRunnable 正常执行
     * 3.1 CRunnable 正常执行不抛异常（ignore）
     */
    @Test
    public void ignore_runnable_success() {

        val flag = new AtomicBoolean(false);
        CExceptionUtils.ignore(() -> flag.set(true));

        Assertions.assertTrue(flag.get());

    }

    /**
     * 对应测试用例 3.2：CRunnable 抛异常被吞掉、不向上抛
     * 3.2 CRunnable 抛异常被吞掉、不向上抛（ignore）
     */
    @Test
    public void ignore_runnable_swallowsException() {

        Assertions.assertDoesNotThrow(
            () -> CExceptionUtils.ignore(() -> {
                throw new IllegalStateException("boom");
            })
        );

    }

    /**
     * 对应测试用例 3.3：CSupplier 正常返回结果
     * 3.3 CSupplier 正常返回结果（ignore）
     */
    @Test
    public void ignore_supplier_success() {

        Assertions.assertEquals("ok", CExceptionUtils.ignore(() -> "ok"));

    }

    /**
     * 对应测试用例 3.4：CSupplier 抛异常被吞掉、返回 null
     * 3.4 CSupplier 抛异常被吞掉、返回 null（ignore）
     */
    @Test
    public void ignore_supplier_swallowsException() {

        Assertions.assertNull(CExceptionUtils.ignore(() -> {
            throw new IllegalStateException("boom");
        }));

    }

    /**
     * 对应测试用例 4.1：null 异常返回 null
     * 4.1 异常为 null 时返回 null（getMessageWithCause）
     */
    @Test
    public void getMessageWithCauseNull() {

        Assertions.assertNull(CExceptionUtils.getMessageWithCause(null));

    }

    /**
     * 对应测试用例 4.2：单层异常仅返回自身消息
     * 4.2 单层异常：仅返回自身 message（getMessageWithCause）
     */
    @Test
    public void getMessageWithCause_singleLayer() {

        Assertions.assertEquals("only", CExceptionUtils.getMessageWithCause(new IllegalStateException("only")));

    }

    /**
     * 对应测试用例 4.3：含 cause：消息含 main 与 cause
     * 4.3 含 cause：消息含 main 与 cause（getMessageWithCause）
     */
    @Test
    public void getMessageWithCause() {

        val cause = new IllegalArgumentException("cause");
        val ex = new IllegalStateException("main", cause);
        val message = CExceptionUtils.getMessageWithCause(ex);

        Assertions.assertTrue(message.contains("main"));
        Assertions.assertTrue(message.contains("cause"));

    }

    /**
     * 对应测试用例 4.4：循环 cause 链不进入死循环
     * 4.4 循环 cause 链（JDK 禁止 self-causation，故用 {@code CException} 的 cause 字段构造 a→b→a）：
     * 经 {@code LinkedHashSet} 去重后不进入死循环（getMessageWithCause）
     */
    @Test
    @SneakyThrows
    public void getMessageWithCause_cyclicCause() {

        val a = new CException("a");
        val b = new CException("b");
        // JDK initCause 禁止自引用，直接写 Throwable.cause 字段构造 a→b→a 循环链，验证实现侧的去重保护
        val causeField = Throwable.class.getDeclaredField("cause");
        val setter = CMethodHandleUtils.getSetterHandle(causeField);
        setter.invoke(a, b);
        setter.invoke(b, a);

        Assertions.assertTimeoutPreemptively(
            Duration.ofSeconds(5),
            () -> Assertions.assertEquals("a\ncause by b", CExceptionUtils.getMessageWithCause(a))
        );

    }

    /**
     * 测试用 ICRes 实现（仅承载错误码与消息，供本用例构造业务异常）
     */
    @Getter
    @RequiredArgsConstructor
    static class TestRes implements ICRes<Object> {

        private final String code;
        private final String msg;

        static TestRes of(String code, String msg) {
            return new TestRes(code, msg);
        }

    }

}
