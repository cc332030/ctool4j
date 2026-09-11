package com.c332030.ctool4j.mybatisplus.service;

import com.c332030.ctool4j.definition.enums.CDbOperateEnum;
import com.c332030.ctool4j.definition.function.CRunnable;
import com.c332030.ctool4j.definition.function.CSupplier;
import com.c332030.ctool4j.redis.util.CLockUtils;

/**
 * <p>
 * Description: ICMpLockService
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICMpLockService}：锁服务接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>扩展 ICService，提供插入/更新/删除锁 key</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>锁服务</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口</p>
 *
 * @since 2025/12/8
 * @version 1.0
 */
public interface ICMpLockService<ENTITY> extends ICService<ENTITY> {

    /**
     * 获取插入锁 key
     * @param key 业务 key
     * @return 锁 key
     */
    default String getInsertLockKey(Object key) {
        return CLockUtils.getLockKey(getEntityClass(), CDbOperateEnum.INSERT, key);
    }

    /**
     * 获取更新锁 key
     * @param key 业务 key
     * @return 锁 key
     */
    default String getUpdateLockKey(Object key) {
        return CLockUtils.getLockKey(getEntityClass(), CDbOperateEnum.UPDATE, key);
    }

    /**
     * 获取删除锁 key
     * @param key 业务 key
     * @return 锁 key
     */
    default String getDeleteLockKey(Object key) {
        return CLockUtils.getLockKey(getEntityClass(), CDbOperateEnum.DELETE, key);
    }

    /**
     * 尝试获取插入锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param valueSupplier 处理逻辑
     * @return 处理结果
     * @param <T> 处理结果泛型
     */
    default <T> T tryLockInsertThenRun(Object key, int waitSeconds, CSupplier<T> valueSupplier) {
        return CLockUtils.tryLockThenRun(getInsertLockKey(key), waitSeconds, valueSupplier);
    }

    /**
     * 尝试获取更新锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param valueSupplier 处理逻辑
     * @return 处理结果
     * @param <T> 处理结果泛型
     */
    default <T> T tryLockUpdateThenRun(Object key, int waitSeconds, CSupplier<T> valueSupplier) {
        return CLockUtils.tryLockThenRun(getUpdateLockKey(key), waitSeconds, valueSupplier);
    }

    /**
     * 尝试获取删除锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param valueSupplier 处理逻辑
     * @return 处理结果
     * @param <T> 处理结果泛型
     */
    default <T> T tryLockDeleteThenRun(Object key, int waitSeconds, CSupplier<T> valueSupplier) {
        return CLockUtils.tryLockThenRun(getDeleteLockKey(key), waitSeconds, valueSupplier);
    }

    /**
     * 尝试获取插入锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param valueSupplier 处理逻辑
     * @param failureRunnable 锁失败操作
     * @return 处理结果
     * @param <T> 处理结果泛型
     */
    default <T> T tryLockInsertThenRun(Object key, int waitSeconds, CSupplier<T> valueSupplier, CRunnable failureRunnable) {
        return CLockUtils.tryLockThenRun(getInsertLockKey(key), waitSeconds, valueSupplier, failureRunnable);
    }

    /**
     * 尝试获取更新锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param valueSupplier 处理逻辑
     * @param failureRunnable 锁失败操作
     * @return 处理结果
     * @param <T> 处理结果泛型
     */
    default <T> T tryLockUpdateThenRun(Object key, int waitSeconds, CSupplier<T> valueSupplier, CRunnable failureRunnable) {
        return CLockUtils.tryLockThenRun(getUpdateLockKey(key), waitSeconds, valueSupplier, failureRunnable);
    }

    /**
     * 尝试获取删除锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param valueSupplier 处理逻辑
     * @param failureRunnable 锁失败操作
     * @return 处理结果
     * @param <T> 处理结果泛型
     */
    default <T> T tryLockDeleteThenRun(Object key, int waitSeconds, CSupplier<T> valueSupplier, CRunnable failureRunnable) {
        return CLockUtils.tryLockThenRun(getDeleteLockKey(key), waitSeconds, valueSupplier, failureRunnable);
    }

    /**
     * 尝试获取插入锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param runnable 处理逻辑
     */
    default void tryLockInsertThenRun(Object key, int waitSeconds, CRunnable runnable) {
        CLockUtils.tryLockThenRun(getInsertLockKey(key), waitSeconds, runnable);
    }

    /**
     * 尝试获取更新锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param runnable 处理逻辑
     */
    default void tryLockUpdateThenRun(Object key, int waitSeconds, CRunnable runnable) {
        CLockUtils.tryLockThenRun(getUpdateLockKey(key), waitSeconds, runnable);
    }

    /**
     * 尝试获取删除锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param runnable 处理逻辑
     */
    default void tryLockDeleteThenRun(Object key, int waitSeconds, CRunnable runnable) {
        CLockUtils.tryLockThenRun(getDeleteLockKey(key), waitSeconds, runnable);
    }

    /**
     * 尝试获取插入锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param runnable 处理逻辑
     * @param failureRunnable 锁失败操作
     */
    default void tryLockInsertThenRun(Object key, int waitSeconds, CRunnable runnable, CRunnable failureRunnable) {
        CLockUtils.tryLockThenRun(getInsertLockKey(key), waitSeconds, runnable, failureRunnable);
    }

    /**
     * 尝试获取更新锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param runnable 处理逻辑
     * @param failureRunnable 锁失败操作
     */
    default void tryLockUpdateThenRun(Object key, int waitSeconds, CRunnable runnable, CRunnable failureRunnable) {
        CLockUtils.tryLockThenRun(getUpdateLockKey(key), waitSeconds, runnable, failureRunnable);
    }

    /**
     * 尝试获取删除锁并做处理
     * @param key 业务 key
     * @param waitSeconds 等锁秒数
     * @param runnable 处理逻辑
     * @param failureRunnable 锁失败操作
     */
    default void tryLockDeleteThenRun(Object key, int waitSeconds, CRunnable runnable, CRunnable failureRunnable) {
        CLockUtils.tryLockThenRun(getDeleteLockKey(key), waitSeconds, runnable, failureRunnable);
    }

}
