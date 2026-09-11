package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * <p>
 * Description: CStreamUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code stream.filter(...)}。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>元素 key 为 null</td>
 *     <td>谓词返回 false，该元素被过滤</td>
 *   </tr>
 * </table>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>key 为 null 的元素会被过滤而非保留，与"去重"语义不同，需注意。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>使用 ConcurrentHashMap 保证并发安全，牺牲少量内存。</li>
 *   <li>null key 元素被过滤，避免无法判断重复的歧义。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>去重实现</b></p>
 * <ul>
 *   <li>内部用 {@code ConcurrentHashMap&lt;Object, Boolean&gt;} 作为已见集合，谓词经</li>
 *   <li>{@code seen.putIfAbsent(key, TRUE) == null} 判断是否首次出现（首次返回 true，重复返回 false）。</li>
 *   <li>并发安全（ConcurrentHashMap），可安全用于并行流。</li>
 * </ul>
 * <p><b>null key 语义</b></p>
 * <ul>
 *   <li>key 经 {@code CObjUtils.convert(t, keyExtractor)} 提取，key 为 null 的元素返回 false（被过滤），</li>
 *   <li>不参与去重集合——null key 无法稳定判断是否重复，故统一过滤。</li>
 * </ul>
 *
 * @since 2025/9/10
 * @version 1.0
 */
@UtilityClass
public class CStreamUtils {

    /**
     * 按 key 去重谓词（并发安全，key 为 null 的元素会被过滤）
     * <ul>
     *   <li>{@code distinctByKey(CFunction&lt;T, ?&gt; keyExtractor)}：返回按 key 去重的谓词，可直接用于</li>
     *   <li>对 Stream 元素按某字段/转换结果去重（如 {@code filter(distinctByKey(User::getId))}）。</li>
     * </ul>
     *
     * @param keyExtractor key 提取函数
     * @param <T>          元素类型
     * @return 去重谓词
     */
    public <T> Predicate<T> distinctByKey(CFunction<? super T, ?> keyExtractor) {

        val seen = new ConcurrentHashMap<Object, Boolean>();
        return t -> {

            val key = CObjUtils.convert(t, keyExtractor);
            if(key == null) {
                return false;
            }

            return seen.putIfAbsent(key, Boolean.TRUE) == null;
        };
    }

}
