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
 * <h2>能力目录</h2>
 * <p>{@code CPatternUtils} 为正则工具类，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>同一 regex 重复获取</td>
 *     <td>返回缓存的同一 Pattern 实例（getCacheCached 验证）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>反复编译同一正则的场景（编译开销大，缓存提升性能）。</li>
 *   <li>URL 路径通配符匹配（如 {@code /api/**}、{@code /api/*}），将通配符表达式转为正则。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>getUrlCache 仅支持 {@code .}/{@code *}/{@code **} 三类通配符，URL 中其他正则特殊字符需自行转义。</li>
 *   <li>{@code **} 匹配含 {@code /}（{@code [\s\S]*}），{@code *} 不匹配 {@code /}（{@code [^/]*}），语义需按需选用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>缓存无失效机制：正则表达式静态不可变，风险低。</li>
 *   <li>占位符替换保证通配符语义互不污染，是核心实现取舍。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>普通正则缓存</b></p>
 * <ul>
 *   <li>{@code REGEX_PATTERNS}（ConcurrentHashMap）按 regex 键缓存，{@code computeIfAbsent} 保证单次编译。</li>
 * </ul>
 *
 * @since 2026/4/29
 * @version 1.0
 */
@UtilityClass
public class CPatternUtils {

    /**
     * 正则表达式缓存
     */
    private final Map<String, Pattern> REGEX_PATTERNS = new ConcurrentHashMap<>();

    /**
     * url 正则表达式缓存（独立于普通正则缓存，避免拼接键与用户 regex 冲突）
     */
    private final Map<String, Pattern> URL_REGEX_PATTERNS = new ConcurrentHashMap<>();

    /**
     * 获取正则表达式缓存
     *
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
     *
     * <h2>URL 通配符转换（getUrlCache）</h2>
     * <ul>
     *   <li>独立缓存 {@code URL_REGEX_PATTERNS}，避免拼接键与普通正则冲突。</li>
     *   <li>通配符规则：</li>
     *   <li>{@code .} → {@code \.}（转义点号）</li>
     *   <li>{@code **&#47;} → {@code [\s\S]*&#47;}（匹配任意字符含换行，后跟 /）</li>
     *   <li>{@code **} → {@code [\s\S]*}（匹配任意字符含换行）</li>
     *   <li>{@code *} → {@code [^/]*}（匹配非 / 的任意字符）</li>
     *   <li>采用<b>占位符替换</b>避免链式替换相互污染：先将各通配符替换为 URL 中不可能出现的控制字符</li>
     *   <li>（{@code \u0000S}/{@code \u0000D}/{@code \u0000A}），再统一替换为正则，避免 {@code **} 替换出的 {@code [\s\S]*} 里的 {@code *}</li>
     *   <li>被后续 {@code *} 规则误改。</li>
     * </ul>
     * <ul>
     *   <li>{@code getUrlCache(String regex)}：URL 通配符转正则并缓存（支持 {@code **}、{@code *}、{@code .} 通配符）</li>
     * </ul>
     *
     * @param regex 正则表达式
     * @return 正则表达式*/
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
