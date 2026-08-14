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
     * url 正则表达式缓存（独立于普通正则缓存，避免拼接键与用户 regex 冲突）
     */
    public Map<String, Pattern> URL_REGEX_PATTERNS = new ConcurrentHashMap<>();

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
        return URL_REGEX_PATTERNS.computeIfAbsent(regex, str -> {
            // 通配符先替换为占位符（URL 中不可能出现的控制字符）再统一替换为正则：
            // 避免链式替换相互污染，如 "a**b" 中 "**" 替换出的 [\\s\\S]* 里的 * 被后续 * 替换误改
            val strNew = str
                .replace(".", "\\.")          // 转义点号
                .replace("**/", "\u0000S")    // 匹配任意字符包括换行，后跟 /
                .replace("**", "\u0000D")     // 匹配任意字符包括换行
                .replace("*", "\u0000A")      // 匹配非 / 的任意字符
                .replace("\u0000S", "[\\s\\S]*/")
                .replace("\u0000D", "[\\s\\S]*")
                .replace("\u0000A", "[^/]*");
            return Pattern.compile(strNew);
        });
    }

}
