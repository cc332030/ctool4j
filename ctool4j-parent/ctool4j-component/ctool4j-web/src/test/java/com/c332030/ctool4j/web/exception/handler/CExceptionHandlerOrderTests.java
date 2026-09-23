package com.c332030.ctool4j.web.exception.handler;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Description: CExceptionHandlerOrderTests
 * </p>
 *
 * <h2>测试用例目录</h2>
 * <ul>
 *   <li>1.1 档位递增：具体类型档 &lt; CException 兜底档 &lt; Exception 兜底档 &lt; Throwable 兜底档</li>
 *   <li>1.2 所有内置处理器均显式声明 {@code @Order}，且取值仅来自 CExceptionHandlerOrder 的各档</li>
 *   <li>1.3 具体类型处理器归属 CONCRETE；CCExceptionHandler、CExceptionHandler、CThrowableHandler 归属各自兜底档</li>
 *   <li>1.4 兜底不激进：具体类型档位不使用最高优先级、兜底档不越出最低优先级（业务方可显式 {@code @Order} 抢先）</li>
 * </ul>
 *
 * <h2>测试设计</h2>
 * <ul>
 *   <li>依据测试方法（正例/边界/分支覆盖）：各档正例 + 越界边界；顺序属常量约定，用反射读取 {@code @Order} 逐类核对。</li>
 *   <li>覆盖场景：见上方编号索引；新增内置处理器需同步加入用例中的处理器清单。</li>
 *   <li>未覆盖：业务方自定义 advice 的实际抢先效果（由各处理器用例的命中归属间接固化，如 401 未被兜底成 500）。</li>
 *   <li>依据：Spring {@code ExceptionHandlerExceptionResolver} 按 advice 顺序取首个能匹配的处理器、不跨 advice 比较异常类型精确度。</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.2
 * @see CExceptionHandlerOrder
 */
public class CExceptionHandlerOrderTests {

    /**
     * 具体异常类型处理器（应归属 CONCRETE 档位）
     */
    private static final List<Class<?>> CONCRETE_HANDLERS = Arrays.asList(
        CCBusinessExceptionHandler.class,
        CClientAbortExceptionHandler.class,
        CFileUploadExceptionHandler.class,
        CHttpMessageNotReadableExceptionHandler.class,
        CHttpMessageNotWritableExceptionHandler.class,
        CHttpRequestMethodNotSupportedExceptionHandler.class,
        CIllegalArgumentExceptionHandler.class,
        CIllegalStateExceptionHandler.class,
        CMethodArgumentNotValidExceptionHandler.class,
        CMethodArgumentTypeMismatchExceptionHandler.class,
        CMissingServletRequestParameterExceptionHandler.class,
        CUnauthorizedExceptionHandler.class
    );

    /**
     * 测试档位严格递增：具体类型档在最前，CException 兜底次之，Exception 兜底再次，Throwable 兜底最后
     * <p>对应测试用例 1.1：兜底档一旦排在具体类型档之前，会截走子类异常（如未授权 401 被兜底成 500）；
     * 四个档位两两不得并列（并列则归属取决于装配顺序）</p>
     */
    @Test
    public void orderTier_ascending() {

        Assertions.assertTrue(
            CExceptionHandlerOrder.CONCRETE < CExceptionHandlerOrder.C_EXCEPTION_FALLBACK,
            "具体类型档必须小于 CException 兜底档"
        );
        Assertions.assertTrue(
            CExceptionHandlerOrder.C_EXCEPTION_FALLBACK < CExceptionHandlerOrder.EXCEPTION_FALLBACK,
            "CException 兜底档必须小于 Exception 兜底档"
        );
        Assertions.assertTrue(
            CExceptionHandlerOrder.EXCEPTION_FALLBACK < CExceptionHandlerOrder.THROWABLE_FALLBACK,
            "Exception 兜底档必须小于 Throwable 兜底档"
        );
    }

    /**
     * 测试所有内置处理器均显式声明 {@code @Order} 且取值仅来自三档
     * <p>对应测试用例 1.2：漏声明会退回默认序（与业务方 advice 同级，归属不确定）；四档清单与实际兜底处理器逐一对齐</p>
     */
    @Test
    public void allHandlers_orderDeclaredFromTiers() {

        val tiers = Arrays.asList(
            CExceptionHandlerOrder.CONCRETE,
            CExceptionHandlerOrder.C_EXCEPTION_FALLBACK,
            CExceptionHandlerOrder.EXCEPTION_FALLBACK,
            CExceptionHandlerOrder.THROWABLE_FALLBACK
        );

        val handlers = new ArrayList<Class<?>>(CONCRETE_HANDLERS);
        handlers.add(CCExceptionHandler.class);
        handlers.add(CExceptionHandler.class);
        handlers.add(CThrowableHandler.class);

        for (val handler : handlers) {

            val order = handler.getAnnotation(Order.class);
            Assertions.assertNotNull(order, handler.getSimpleName() + " 需显式声明 @Order");
            Assertions.assertTrue(
                tiers.contains(order.value()),
                handler.getSimpleName() + " 的 @Order 取值须来自 CExceptionHandlerOrder 档位"
            );
        }
    }

    /**
     * 测试各处理器的档位归属
     * <p>对应测试用例 1.3：具体类型处理器同为 CONCRETE；三个兜底处理器各自独立档位、不并列（并列则归属取决于装配顺序）</p>
     */
    @Test
    public void handlers_useExpectedTier() {

        for (val handler : CONCRETE_HANDLERS) {
            Assertions.assertEquals(
                CExceptionHandlerOrder.CONCRETE,
                handler.getAnnotation(Order.class).value(),
                handler.getSimpleName()
            );
        }

        Assertions.assertEquals(
            CExceptionHandlerOrder.C_EXCEPTION_FALLBACK,
            CCExceptionHandler.class.getAnnotation(Order.class).value()
        );
        Assertions.assertEquals(
            CExceptionHandlerOrder.EXCEPTION_FALLBACK,
            CExceptionHandler.class.getAnnotation(Order.class).value()
        );
        Assertions.assertEquals(
            CExceptionHandlerOrder.THROWABLE_FALLBACK,
            CThrowableHandler.class.getAnnotation(Order.class).value()
        );
    }

    /**
     * 测试兜底不激进：档位均落在兜底区，业务方可用显式 {@code @Order} 抢先
     * <p>对应测试用例 1.4：回归断言——内置处理器不得使用 {@code Ordered.HIGHEST_PRECEDENCE}（否则业务方无法覆盖，
     * 只剩精确类型的条件装配一条路径），且不越出 {@code Ordered.LOWEST_PRECEDENCE}</p>
     */
    @Test
    public void fallback_notAggressive() {

        Assertions.assertTrue(
            CExceptionHandlerOrder.CONCRETE > Ordered.HIGHEST_PRECEDENCE,
            "内置处理器不得抢占最高优先级，须留给业务方显式 @Order 抢先"
        );
        Assertions.assertTrue(
            CExceptionHandlerOrder.THROWABLE_FALLBACK <= Ordered.LOWEST_PRECEDENCE,
            "兜底档不得越出最低优先级"
        );
    }

}
