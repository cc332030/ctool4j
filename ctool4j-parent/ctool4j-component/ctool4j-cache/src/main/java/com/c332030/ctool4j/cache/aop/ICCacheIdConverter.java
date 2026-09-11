package com.c332030.ctool4j.cache.aop;

import com.c332030.ctool4j.definition.function.CBiFunction;

/**
 * <p>
 * Description: ICCacheIdConverter
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICCacheIdConverter&lt;KEY, CLASS&gt;} 为缓存 ID 生成策略接口，继承 {@code CBiFunction&lt;KEY, CLASS, String&gt;}， 约定根据缓存 key 与缓存对象生成缓存 ID 字符串。</p>
 * <p>核心方法：</p>
 * <ul>
 *   <li>{@code applyThrowable(KEY key, CLASS object)}：生成缓存 ID，可抛出受检异常（{@code throws Throwable}）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未指定实现类</td>
 *     <td>使用默认 {@code CDefaultCacheIdConverter}</td>
 *   </tr>
 *   <tr>
 *     <td>key 与 object 均为 null</td>
 *     <td>默认实现返回 null（不写缓存）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>自定义缓存 ID 生成规则（如拼接业务前缀、组装复合 key）时实现本接口。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>单 key 单对象双参输入；无此语义的场景不适合。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>实现需自行处理 key/object 的 null 与类型转换，接口不强制。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>泛型 {@code KEY}（缓存键类型）、{@code CLASS}（缓存对象类型）、返回 {@code String}（缓存 ID）。</li>
 *   <li>继承 {@code CBiFunction} 的 {@code applyThrowable}（允许受检异常），实现方重写该方法生成 ID。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>接口继承 {@code CBiFunction}，复用其双参函数语义；由 {@code CCacheAspect} 通过 {@code CLASS_ID_CONVERTER}</li>
 *   <li>按 {@code @CCacheable.idConverter()} 指定的实现类实例化并调用。</li>
 * </ul>
 *
 * @since 2025/9/27
 * @version 1.0
 */
public interface ICCacheIdConverter<KEY, CLASS> extends CBiFunction<KEY, CLASS, String> {

    /**
     * 生成缓存ID，可抛出受检异常
     * @param key 缓存键
     * @param object 缓存对象
     * @return 缓存ID
     * @throws Throwable 生成过程中可能抛出的异常
     */
    @Override
    String applyThrowable(KEY key, CLASS object) throws Throwable;

}
