package com.c332030.ctool4j.spring.test.util;

import com.c332030.ctool4j.spring.util.CAspectUtils;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import static org.mockito.Mockito.*;

/**
 * <p>
 * Description: CAspectUtilsTests
 * </p>
 *
 * <p>覆盖 CAspectUtils 的 getMethod/process，通过 Mockito mock 切点，
 * 验证切点方法获取与异常解包逻辑</p>
 *
 * @since 2026/8/16
 */
public class CAspectUtilsTests {

    // ---------- getMethod ----------

        /**
     * 对应测试用例 1.1
     */
    @Test
    public void getMethod() throws NoSuchMethodException {
        // 正例：返回切点签名对应的 Method
        Method expected = String.class.getMethod("length");

        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(expected);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);

        Method method = CAspectUtils.getMethod(joinPoint);

        Assertions.assertSame(expected, method);
    }

    // ---------- process ----------

        /**
     * 对应测试用例 1.2
     */
    @Test
    public void process() throws Throwable {
        // 正例：返回切点方法执行结果
        Object[] args = new Object[]{"a", "b"};
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed(args)).thenReturn("result");

        val result = CAspectUtils.process(joinPoint);

        Assertions.assertEquals("result", result);
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    public void process_whenUndeclaredThrowableException() throws Throwable {
        // 异常：切点抛出未声明异常时解包并抛出原异常
        IOException io = new IOException("boom");
        UndeclaredThrowableException ute = new UndeclaredThrowableException(io);

        Object[] args = new Object[]{"a"};
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(args);
        doThrow(ute).when(joinPoint).proceed(args);

        Assertions.assertThrowsExactly(IOException.class, () -> CAspectUtils.process(joinPoint));
    }

        /**
     * 对应测试用例 1.4
     */
    @Test
    public void process_whenRuntimeException() throws Throwable {
        // 反例：普通运行时异常直接透传
        IllegalArgumentException iae = new IllegalArgumentException("bad");

        Object[] args = new Object[]{"a"};
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(args);
        doThrow(iae).when(joinPoint).proceed(args);

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> CAspectUtils.process(joinPoint));
    }

}
