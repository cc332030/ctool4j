package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.annotation.CBizId;
import com.c332030.ctool4j.definition.function.StringFunction;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import java.util.UUID;

/**
 * <p>
 * Description: CIdUtils
 * </p>
 *
 * @since 2025/11/27
 * @see doc/design/core/CIdUtils.adoc
 * @see doc/design/core/CIdUtilsTests.adoc
 */
@UtilityClass
public class CIdUtils {

    /**
     * 生成 UUID 字符串
     *
     * @return UUID 字符串
     */
    public String stringUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 没有 '-' 的uuid
     *
     * @return 没有 '-' 的 UUID 字符串
     */
    public String stringUUIDNoHyphen() {
        return stringUUID().replace("-", "");
    }

    /**
     * 生成雪花 ID
     *
     * @return 雪花 ID
     */
    public Long nextId() {
        return IdUtil.getSnowflakeNextId();
    }

    private final CClassValue<String> CLASS_PREFIX = CClassValue.of(type -> {

        val idPrefixAnno = type.getAnnotation(CBizId.class);
        val idPrefix = CObjUtils.convert(idPrefixAnno, CBizId::value);
        if(StrUtil.isNotBlank(idPrefix)) {
            return idPrefix;
        }

        return type.getSimpleName()
                .replaceAll("[^A-Z]|DO", "");
    });

    /**
     * 获取类 ID 前缀（优先注解值，否则取类名首字母）
     *
     * @param clazz 类
     * @return ID 前缀
     */
    public String getPrefix(Class<?> clazz) {
        return CLASS_PREFIX.get(clazz);
    }

    /**
     * 获取类 ID 前缀（截取指定长度）
     *
     * @param clazz  类
     * @param length 长度
     * @return ID 前缀
     */
    public String getPrefix(Class<?> clazz, int length) {

        val prefix = getPrefix(clazz);
        return prefix.substring(0, Math.min(length, prefix.length()));
    }

    /**
     * 生成带前缀的雪花 ID
     *
     * @param prefix 前缀
     * @return 带前缀的雪花 ID
     */
    public String nextIdWithPrefix(String prefix) {
        return prefix + nextId();
    }

    /**
     * 生成带类 ID 前缀的雪花 ID
     *
     * @param clazz 类
     * @return 带前缀的雪花 ID
     */
    public String nextIdWithPrefix(Class<?> clazz) {
        return nextIdWithPrefix(getPrefix(clazz));
    }

    /**
     * 生成带类 ID 前缀的雪花 ID（前缀截取指定长度）
     *
     * @param clazz  类
     * @param length 前缀长度
     * @return 带前缀的雪花 ID
     */
    public String nextIdWithPrefix(Class<?> clazz, int length) {
        return nextIdWithPrefix(getPrefix(clazz, length));
    }

    /**
     * 从 ID 中解析前缀并转换
     *
     * @param id      ID
     * @param convert 转换函数
     * @param <T>     转换结果类型
     * @return 转换后的前缀，无前缀时返回 null
     */
    public <T> T getPrefixFromId(String id, StringFunction<T> convert) {

        val prefix = getPrefixFromId(id);
        if(StrUtil.isEmpty(prefix)){
            return null;
        }

        return convert.apply(prefix);
    }

    /**
     * 从 ID 中解析前缀（数字前的字符）
     *
     * @param id ID
     * @return 前缀，无前缀时返回 null
     */
    public String getPrefixFromId(String id) {

        if(StrUtil.isEmpty(id)) {
            return null;
        }

        var index = 0;
        while (index < id.length() && !CharUtil.isNumber(id.charAt(index))) {
            index++;
        }

        if(index == 0) {
            return null;
        }

        return id.substring(0, index);
    }

}
