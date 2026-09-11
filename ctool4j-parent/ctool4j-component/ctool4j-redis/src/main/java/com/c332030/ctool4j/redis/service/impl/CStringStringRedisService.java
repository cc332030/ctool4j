package com.c332030.ctool4j.redis.service.impl;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.redis.model.CValueWithTtl;
import com.c332030.ctool4j.redis.service.CAbstractRedisService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Description: CStringStringRedisService
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>key 无效按空白（{@code StrUtil.isBlank}）判断。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>key 空白/无效</td>
 *     <td>getValue 返回 defaultValue，setValue 不写入</td>
 *   </tr>
 *   <tr>
 *     <td>value null</td>
 *     <td>setValue 不写入</td>
 *   </tr>
 *   <tr>
 *     <td>timeout&lt;=0</td>
 *     <td>setValue(带超时) 不写入</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要存储对象（JSON）到 Redis、且 key 为字符串的场景，如缓存服务、业务 id 自增。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>key/value 需非 String 类型时使用 {@code CObjectValueRedisService}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 Jackson（{@code CJsonUtils}）JSON 序列化，反序列化类型需匹配存储时的实际结构。</li>
 *   <li>空白值按无效处理，无法缓存空白字符串（由 toJson 后 JSON 串通常非空白，实际影响有限）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>key/value 空白视为无效（重写 {@code isInvalidKey}/{@code isInvalidValue}）。</li>
 *   <li>对象值统一经 {@code CJsonUtils.toJson} 序列化存储，读取时按目标类型反序列化。</li>
 *   <li>无效 key/value 或 timeout&lt;=0 时静默返回，不写不抛。</li>
 * </ul>
 *
 * @since 2025/11/4
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class CStringStringRedisService extends CAbstractRedisService<String, String> {

    /**
     * 判断 key 是否无效，重写 String 类型的 key
     * @param key key
     * @return 有效性
     */
    @Override
    public boolean isInvalidKey(String key) {
        return StrUtil.isBlank(key);
    }

    private String toValueStr(Object value) {
        return CJsonUtils.toJson(value);
    }

    /**
     * 获取值，空白值返回空 Opt
     *
     * @param key key
     * @return 值的 Opt
     */
    public Opt<String> getValueOpt(String key){
        return Opt.ofBlankAble(getValue(key));
    }

    private <T> T getValueObj(String value, Class<T> valueClass) {
        return CJsonUtils.fromJson(value, valueClass);
    }

    private <T> T getValueObj(String value, TypeReference<T> typeReference) {
        return CJsonUtils.fromJson(value, typeReference);
    }

    /**
     * 判断 value 是否无效，重写 String 值的判断
     * @param value 值
     * @return 有效性
     */
    @Override
    public boolean isInvalidValue(String value) {
        return StrUtil.isBlank(value);
    }

    /**
     * 设置值，对象序列化为 JSON 存储
     * <ul>
     *   <li>{@code setValue(key, Object, ...)}：对象序列化为 JSON 字符串存储。</li>
     * </ul>
     *
     * @param key   key
     * @param value 值
     */
    public void setValue(String key, Object value) {
        if(isInvalidKey(key)
            || Objects.isNull(value)
        ) {
            return;
        }
        setValue(key, toValueStr(value));
    }

    /**
     * 设置值并指定过期时长，对象序列化为 JSON 存储
     *
     * @param key      key
     * @param value    值
     * @param duration 过期时长
     */
    public void setValue(String key, Object value, Duration duration) {
        if(isInvalidKey(key)
            || Objects.isNull(value)
        ) {
            return;
        }
        setValue(key, toValueStr(value), duration);
    }

    /**
     * 设置值
     * @param key key
     * @param value 值
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    public void setValue(String key, Object value, long timeout, TimeUnit unit) {

        if(isInvalidKey(key)
            || Objects.isNull(value)
            || timeout <= 0
        ) {
            return;
        }

        opsForValue().set(key, toValueStr(value), timeout, unit);
    }

    /**
     * 获取值并反序列化为指定类型
     * <ul>
     *   <li>{@code getValue(key, Class/TypeReference, ...)}：反序列化指定类型。</li>
     *   <li>基于 {@code ICRedisService} 默认方法 + JSON 序列化；{@code getValue(key, valueClass, default)} 复用父接口默认方法做 convert。</li>
     * </ul>
     *
     * @param key        key
     * @param valueClass 目标类型
     * @param <T>        目标类型
     * @return 反序列化后的值
     */
    public <T> T getValue(String key, Class<T> valueClass) {
        return getValue(key, valueClass, null);
    }

    /**
     * 获取值并反序列化为指定类型，无值时返回默认值
     *
     * @param key          key
     * @param valueClass   目标类型
     * @param defaultValue 默认值
     * @param <T>          目标类型
     * @return 反序列化后的值；key 无效时返回默认值
     */
    public <T> T getValue(String key, Class<T> valueClass, T defaultValue) {
        if(isInvalidKey(key)) {
            return defaultValue;
        }
        return getValue(key, value -> getValueObj(value, valueClass), defaultValue);
    }

    /**
     * 获取值并反序列化为指定泛型类型
     *
     * @param key           key
     * @param typeReference 泛型类型引用
     * @param <T>           目标类型
     * @return 反序列化后的值
     */
    public <T> T getValue(String key, TypeReference<T> typeReference) {
        return getValue(key, typeReference, null);
    }

    /**
     * 获取值并反序列化为指定泛型类型
     *
     * @param key           key
     * @param typeReference 泛型类型引用
     * @param defaultValue  默认值
     * @param <T>           目标类型
     * @return 反序列化后的值
     */
    public <T> T getValue(String key, TypeReference<T> typeReference, T defaultValue) {
        if(isInvalidKey(key)) {
            return defaultValue;
        }
        return getValue(key, value -> getValueObj(value, typeReference), defaultValue);
    }

    /**
     * 获取值及其剩余过期时间，反序列化为指定类型
     * <ul>
     *   <li>{@code getValueWithTtl(key, Class/TypeReference)}：值 + 剩余 TTL，反序列化指定类型。</li>
     * </ul>
     *
     * @param key        key
     * @param valueClass 目标类型
     * @param <T>        目标类型
     * @return 值及剩余过期时间
     */
    public <T> CValueWithTtl<T> getValueWithTtl(String key, Class<T> valueClass) {
        return getValueWithTtl(key, value -> getValueObj(value, valueClass));
    }

    /**
     * 获取值及其剩余过期时间，反序列化为指定泛型类型
     *
     * @param key           key
     * @param typeReference 泛型类型引用
     * @param <T>           目标类型
     * @return 值及剩余过期时间
     */
    public <T> CValueWithTtl<T> getValueWithTtl(String key, TypeReference<T> typeReference) {
        return getValueWithTtl(key, value -> getValueObj(value, typeReference));
    }

}
