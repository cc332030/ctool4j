package com.c332030.ctool4j.cache.aop;

import cn.hutool.core.util.StrUtil;

/**
 * <p>
 * Description: CDefaultCacheIdConverter
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code applyThrowable(Object key, Object object)} 的生成规则：</p>
 * <ul>
 *   <li>key 与 object 都为 null → 返回 null</li>
 *   <li>key 为 null → 返回 {@code object} 的字符串形式（{@code StrUtil.toStringOrNull}）</li>
 *   <li>key 非 null → 返回 {@code key} 的字符串形式（{@code StrUtil.toStringOrNull}）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>key 与 object 均为 null</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>key 为 null</td>
 *     <td>返回 object 字符串形式</td>
 *   </tr>
 *   <tr>
 *     <td>object 为 null（key 非空）</td>
 *     <td>返回 key 字符串形式</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>缓存入参为简单类型（key 直接可用）或 {@code @CCacheId} 字段值已被切面提取为 cacheId 的默认场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要复合/带前缀缓存 ID 时需自定义 idConverter。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅取 key 或 object 的字符串形式，不做拼接；复合 key 不适用。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>优先级：key 优先于 object；key 为 null 时才退到 object。</li>
 *   <li>均为 null 返回 null，由调用方保证不写入缓存（避免缓存空 key）。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>用 hutool {@code StrUtil.toStringOrNull} 转字符串，null 安全。</li>
 * </ul>
 *
 * @since 2025/9/27
 * @version 1.0
 */
public class CDefaultCacheIdConverter implements ICCacheIdConverter<Object, Object> {

    /**
     * 生成缓存 id：优先取 key，key 为空时取 object 的字符串形式，都为空返回 null
     *
     * @param key    缓存 key
     * @param object 缓存对象
     * @return 缓存 id
     */
    @Override
    public String applyThrowable(Object key, Object object) throws Throwable {

        if(null == key
            && null == object
        ) {
            return null;
        }

        if(null == key) {
            return StrUtil.toStringOrNull(object);
        }

        return StrUtil.toStringOrNull(key);
    }

}
