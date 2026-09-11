package com.c332030.ctool4j.redis.idempotent;

import com.c332030.ctool4j.core.exception.CException;
import lombok.experimental.StandardException;

/**
 * <p>
 * Description: CIdempotentException
 * </p>
 * <p>
 * 幂等异常：{@code @CIdempotent} 在获取同一业务 key 的执行权失败（已有调用正在进行，
 * 即发生重复提交或并发穿透）时抛出，语义为「重复请求，请勿重复提交」。
 * </p>
 *
 * @since 2026/9/9
 * @version 1.0
 */
@StandardException
public class CIdempotentException extends CException {

    private static final long serialVersionUID = 1;

}
