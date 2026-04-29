package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.function.CFunction;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * <p>
 * Description: CPatternUtils
 * </p>
 *
 * @since 2026/4/29
 */
@UtilityClass
public class CPatternUtils {

    /**
     * 正则表达式缓存
     */
    public Map<String, Pattern> REGEX_PATTERNS = new ConcurrentHashMap<>();

    /**
     * 获取正则表达式缓存
     * @param regex 正则表达式
     * @param toPattern 正则表达式转换函数
     * @return 正则表达式
     */
    public Pattern getCache(String regex, CFunction<String, Pattern> toPattern) {
        return REGEX_PATTERNS.computeIfAbsent(regex, toPattern);
    }

    /**
     * 获取正则表达式缓存
     * @param regex 正则表达式
     * @return 正则表达式
     */
    public Pattern getCache(String regex) {
        return getCache(regex, Pattern::compile);
    }

    /**
     * 获取url正则表达式缓存
     * @param regex 正则表达式
     * @return 正则表达式
     */
    public Pattern getUrlCache(String regex) {
        return getCache(regex + "-url", str -> {
            val strNew = str
                .replace(".", "\\.")          // 转义点号
                .replace("**/", "[\\s\\S]*/") // 匹配任意字符包括换行，后跟 /
                .replace("**", "[\\s\\S]*")   // 匹配任意字符包括换行
                .replace("*", "[^/]*");       // 匹配非 / 的任意字符
            return Pattern.compile(strNew);
        });
    }

}
