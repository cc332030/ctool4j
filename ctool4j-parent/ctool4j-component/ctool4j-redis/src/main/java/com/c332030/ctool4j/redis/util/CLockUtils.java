package com.c332030.ctool4j.redis.util;

import com.c332030.ctool4j.definition.interfaces.IOperate;
import com.c332030.ctool4j.redis.service.impl.CLockService;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * <p>
 * Description: LockUtils
 * </p>
 *
 * @author c332030
 * @since 2024/3/20
 */
@UtilityClass
public class CLockUtils {

    public static final String LOCK_STR = "lock";

    @Setter
    CLockService lockService;

    /**
     * 获取锁 key
     * @param key 业务 key
     * @return 锁 key
     */
    public static String getLockKey(String key) {
        return key
                + CRedisUtils.KEY_SEPARATOR
                +  LOCK_STR;
    }

    /**
     * 获取锁 key
     * @param clazz 业务类
     * @param iOperate 操作
     * @param key 业务 key
     * @return 锁 key
     */
    public static String getLockKey(Class<?> clazz, IOperate iOperate, Object key) {
        return getLockKey(CRedisUtils.getKey(clazz, iOperate, key));
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitDuration 等待时长，单位：秒
     * @param valueSupplier 锁成功操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    public <T> T tryLockThenRun(String key, int waitDuration, Supplier<T> valueSupplier) {
        return tryLockThenRun(key, Duration.ofSeconds(waitDuration), valueSupplier);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitDuration 等待时长
     * @param valueSupplier 锁成功操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    public <T> T tryLockThenRun(String key, Duration waitDuration, Supplier<T> valueSupplier) {
        return lockService.tryLockThenRun(key, waitDuration, valueSupplier);
    }

}
