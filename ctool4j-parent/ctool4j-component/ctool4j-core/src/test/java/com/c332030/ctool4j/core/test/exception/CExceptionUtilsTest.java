package com.c332030.ctool4j.core.test.exception;

import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.exception.CExceptionUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CExceptionUtilsTest
 * </p>
 * <p>`com.c332030.ctool4j.core.exception.CExceptionUtils`（CExceptionUtils）的测试用例</p>
 *
 * @since 2025/9/14
 */
@CustomLog
public class CExceptionUtilsTest {

    /**
     * 测试创建业务异常
     * 对应测试用例 1.1
     */
    @Test
    public void newBusinessException() {

        val ex = CExceptionUtils.newBusinessException("test");
        Assertions.assertEquals(CBusinessException.class, ex.getClass());

    }

    /**
     * 测试获取异常链信息：异常为 null 时返回 null（Q17）
     * 对应测试用例 2.1
     */
    @Test
    public void getMessageWithCauseNull() {

        Assertions.assertNull(CExceptionUtils.getMessageWithCause(null));

    }

    /**
     * 测试获取异常链信息：包含 cause 时拼接展示
     * 对应测试用例 2.2
     */
    @Test
    public void getMessageWithCause() {

        val cause = new IllegalArgumentException("cause");
        val ex = new IllegalStateException("main", cause);
        val message = CExceptionUtils.getMessageWithCause(ex);

        Assertions.assertTrue(message.contains("main"));
        Assertions.assertTrue(message.contains("cause"));

    }

}
