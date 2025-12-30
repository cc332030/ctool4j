package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.annotation.CIdPrefix;
import com.c332030.ctool4j.definition.function.StringFunction;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * <p>
 * Description: CIdUtils
 * </p>
 *
 * @since 2025/11/27
 */
@UtilityClass
public class CIdUtils {

    public String stringUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 没有 '-' 的uuid
     */
    public String stringUUIDNoHyphen() {
        return stringUUID().replaceAll("-", "");
    }

    public Long nextId() {
        return IdUtil.getSnowflakeNextId();
    }

    private final CClassValue<String> CLASS_PREFIX = CClassValue.of(type -> {

        val idPrefixAnno = type.getAnnotation(CIdPrefix.class);
        val idPrefix = CObjUtils.convert(idPrefixAnno, CIdPrefix::value);
        if(StrUtil.isNotBlank(idPrefix)) {
            return idPrefix;
        }

        return type.getSimpleName()
                .replaceAll("[^A-Z]|DO", "");
    });

    public String getPrefix(Class<?> clazz) {
        return CLASS_PREFIX.get(clazz);
    }

    public String getPrefix(Class<?> clazz, int length) {

        val prefix = getPrefix(clazz);
        return prefix.substring(0, Math.min(length, prefix.length()));
    }

    public String nextIdWithPrefix(String prefix) {
        return prefix + nextId();
    }

    public String nextIdWithPrefix(Class<?> clazz) {
        return nextIdWithPrefix(getPrefix(clazz));
    }

    public String nextIdWithPrefix(Class<?> clazz, int length) {
        return nextIdWithPrefix(getPrefix(clazz, length));
    }

    public <T> T getPrefixFromId(String id, StringFunction<T> convert) {

        val prefix = getPrefixFromId(id);
        if(StringUtils.isEmpty(prefix)){
            return null;
        }

        return convert.apply(prefix);
    }

    public String getPrefixFromId(String id) {

        if(StringUtils.isEmpty(id)) {
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
