package com.c332030.ctool4j.redis.util;

import com.c332030.ctool4j.definition.function.CRunnable;
import com.c332030.ctool4j.definition.function.CSupplier;
import com.c332030.ctool4j.definition.interfaces.ICOperate;
import com.c332030.ctool4j.redis.service.impl.CLockService;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.time.Duration;

/**
 * <p>
 * Description: CLockUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLockUtils}（{@code @UtilityClass} + {@code @CAutowiredScan}）为分布式锁的静态访问入口，持有 {@code CLockService}（注入），提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>{@code lockService} 未注入</td>
 *     <td>{@code lock()}/{@code tryLockThenRun} 调用将 NPE（依赖 Spring 注入）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>非 Spring Bean 的静态代码需要加分布式锁时。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>依赖 {@code lockService} 注入；独立使用（无 Spring 上下文）时不适用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>静态注入依赖 Spring 容器初始化，静态方法调用前需确保注入完成。</li>
 *   <li>历史 {@code tryLockThenRun} 接口保留但废弃，避免破坏既有调用。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>通过 {@code @CAutowiredScan}/{@code @CAutowired} 注入 {@code CLockService}。</li>
 * </ul>
 *
 * @author c332030
 * @since 2024/3/20
 * @version 1.0
 */
@CustomLog
@UtilityClass
@CAutowiredScan
public class CLockUtils {

    /**
     * 锁 key 的后缀标识
     */
    public static final String LOCK_STR = "lock";

    @Setter
    @CAutowired
    CLockService lockService;

    /**
     * 获取锁 key
     *
     * @param key 业务 key
     * @return 锁 key
     */
    public String getLockKey(String key) {
        return key
                + CRedisUtils.KEY_SEPARATOR
                +  LOCK_STR;
    }

    /**
     * 获取锁 key
     * @param clazz 业务类
     * @param icOperate 操作
     * @param key 业务 key
     * @return 锁 key
     */
    public String getLockKey(Class<?> clazz, ICOperate icOperate, Object key) {
        return getLockKey(CRedisUtils.getKey(clazz, icOperate, key));
    }

    /**
     * 获取锁并做处理
     * <ul>
     *   <li>{@code tryLockThenRun(...)}（已废弃）：获取锁并执行操作。</li>
     * </ul>
     *
     * @param key lockKey
     * @param waitSeconds 等锁秒数
     * @param valueSupplier 锁成功操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    @Deprecated
    public <T> T tryLockThenRun(String key, int waitSeconds, CSupplier<T> valueSupplier) {
        return tryLockThenRun(key, Duration.ofSeconds(waitSeconds), valueSupplier);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param valueSupplier 锁成功操作
     * @param failureRunnable 锁失败操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    @Deprecated
    public <T> T tryLockThenRun(String key, CSupplier<T> valueSupplier, CRunnable failureRunnable) {
        return tryLockThenRun(key, null, valueSupplier, failureRunnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitSeconds 等锁秒数
     * @param valueSupplier 锁成功操作
     * @param failureRunnable 锁失败操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    @Deprecated
    public <T> T tryLockThenRun(String key, int waitSeconds, CSupplier<T> valueSupplier, CRunnable failureRunnable) {
        return tryLockThenRun(key, Duration.ofSeconds(waitSeconds), valueSupplier, failureRunnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitDuration 等待时长
     * @param valueSupplier 锁成功操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    @Deprecated
    public <T> T tryLockThenRun(String key, Duration waitDuration, CSupplier<T> valueSupplier) {
        return tryLockThenRun(key, waitDuration, valueSupplier, null);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitDuration 等待时长
     * @param valueSupplier 锁成功操作
     * @param failureRunnable 锁失败操作
     * @return 锁成功操作结果
     * @param <T> 返回结果类型
     */
    @Deprecated
    public <T> T tryLockThenRun(String key, Duration waitDuration, CSupplier<T> valueSupplier, CRunnable failureRunnable) {
        return lockService.tryLockThenRun(key, waitDuration, valueSupplier, failureRunnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitSeconds 等锁秒数
     * @param runnable 锁成功操作
     */
    @Deprecated
    public void tryLockThenRun(String key, int waitSeconds, CRunnable runnable) {
        tryLockThenRun(key, Duration.ofSeconds(waitSeconds), runnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param runnable 锁成功操作
     * @param failureRunnable 锁失败操作
     */
    @Deprecated
    public void tryLockThenRun(String key, CRunnable runnable, CRunnable failureRunnable) {
        tryLockThenRun(key, null, runnable, failureRunnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitSeconds 等锁秒数
     * @param runnable 锁成功操作
     * @param failureRunnable 锁失败操作
     */
    @Deprecated
    public void tryLockThenRun(String key, int waitSeconds, CRunnable runnable, CRunnable failureRunnable) {
        tryLockThenRun(key, Duration.ofSeconds(waitSeconds), runnable, failureRunnable);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitDuration 等待时长
     * @param runnable 锁成功操作
     */
    @Deprecated
    public void tryLockThenRun(String key, Duration waitDuration, CRunnable runnable) {
        tryLockThenRun(key, waitDuration, runnable, null);
    }

    /**
     * 获取锁并做处理
     * @param key lockKey
     * @param waitDuration 等待时长
     * @param runnable 锁成功操作
     * @param failureRunnable 锁失败操作
     */
    @Deprecated
    public void tryLockThenRun(String key, Duration waitDuration, CRunnable runnable, CRunnable failureRunnable) {
        lockService.tryLockThenRun(key, waitDuration, runnable, failureRunnable);
    }

    /**
     * 创建锁构建器
     * <ul>
     *   <li>{@code lock(...)}：创建锁构建器（支持格式化 key）。</li>
     *   <li>静态转发到 {@code CLockService}；{@code tryLockThenRun} 系列已废弃，推荐 {@code lock()} 构建器。</li>
     *   <li>{@code tryLockThenRun} 已废弃，新代码应使用 {@code lock()} 构建器。</li>
     * </ul>
     *
     * @param lockKey 锁 key
     * @return 锁构建器
     */
    public CLockService.CLockBuilder lock(String lockKey) {
        return lockService.lock(lockKey);
    }

    /**
     * 创建锁构建器，支持 StrUtil.format 格式化 lockKey
     *
     * @param format 格式串，如 "sign:report:{}:{}"
     * @param args   格式化参数
     * @return 锁构建器
     */
    public CLockService.CLockBuilder lock(String format, Object... args) {
        return lockService.lock(format, args);
    }

}
