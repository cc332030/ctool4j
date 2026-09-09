package com.c332030.ctool4j.redis.rate;

import com.c332030.ctool4j.core.exception.CException;
import lombok.experimental.StandardException;

/**
 * <p>
 * Description: CRateLimitException
 * </p>
 * <p>
 * 限流异常：{@code @CRateLimit} 在时间窗口内调用次数超过阈值时抛出，
 * 语义为「请求过于频繁，请稍后再试」。
 * </p>
 *
 * @see "doc/design/redis/CRateLimit.adoc"
 * @since 2026/9/8
 */
@StandardException
public class CRateLimitException extends CException {

    private static final long serialVersionUID = 1;

}
