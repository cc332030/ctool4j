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
 * @since 2025/11/4
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
            return null;
        }
        return getValue(key, value -> getValueObj(value, typeReference), defaultValue);
    }

    /**
     * 获取值及其剩余过期时间，反序列化为指定类型
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
