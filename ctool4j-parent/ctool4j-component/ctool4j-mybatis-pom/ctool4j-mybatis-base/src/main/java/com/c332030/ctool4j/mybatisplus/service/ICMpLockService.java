package com.c332030.ctool4j.mybatisplus.service;

import com.c332030.ctool4j.definition.enums.CDbOperateEnum;
import com.c332030.ctool4j.redis.util.CLockUtils;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * <p>
 * Description: ICMpLockService
 * </p>
 *
 * @since 2025/12/8
 */
public interface ICMpLockService<ENTITY> extends ICBaseService<ENTITY> {

    /**
     * 获取插入锁 key
     * @param key 业务 key
     * @return 锁 key
     */
    default String getInsertLockKey(String key) {
        return CLockUtils.getLockKey(getEntityClass(), CDbOperateEnum.INSERT, key);
    }

    /**
     * 获取更新锁 key
     * @param key 业务 key
     * @return 锁 key
     */
    default String getUpdateLockKey(String key) {
        return CLockUtils.getLockKey(getEntityClass(), CDbOperateEnum.UPDATE, key);
    }

    /**
     * 获取删除锁 key
     * @param key 业务 key
     * @return 锁 key
     */
    default String getDeleteLockKey(String key) {
        return CLockUtils.getLockKey(getEntityClass(), CDbOperateEnum.DELETE, key);
    }

    /**
     * 尝试获取插入锁并做处理
     * @param key 业务 key
     * @param waitDuration 等待时长
     * @param valueSupplier 处理逻辑
     * @return 处理结果
     * @param <T> 处理结果泛型
     */
    default <T> T tryLockInsertThenRun(String key, Duration waitDuration, Supplier<T> valueSupplier) {
        return CLockUtils.tryLockThenRun(getInsertLockKey(key), waitDuration, valueSupplier);
    }

    /**
     * 尝试获取更新锁并做处理
     * @param key 业务 key
     * @param waitDuration 等待时长
     * @param valueSupplier 处理逻辑
     * @return 处理结果
     * @param <T> 处理结果泛型
     */
    default <T> T tryLockUpdateThenRun(String key, Duration waitDuration, Supplier<T> valueSupplier) {
        return CLockUtils.tryLockThenRun(getUpdateLockKey(key), waitDuration, valueSupplier);
    }

    /**
     * 尝试获取删除锁并做处理
     * @param key 业务 key
     * @param waitDuration 等待时长
     * @param valueSupplier 处理逻辑
     * @return 处理结果
     * @param <T> 处理结果泛型
     */
    default <T> T tryLockDeleteThenRun(String key, Duration waitDuration, Supplier<T> valueSupplier) {
        return CLockUtils.tryLockThenRun(getDeleteLockKey(key), waitDuration, valueSupplier);
    }

}
