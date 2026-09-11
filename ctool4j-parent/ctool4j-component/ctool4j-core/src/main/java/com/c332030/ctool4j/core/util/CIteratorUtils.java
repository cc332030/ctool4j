package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.function.CConsumer;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Iterator;

/**
 * <p>
 * Description: CIteratorUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CIteratorUtils} 为迭代遍历工具类，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>iterable/iterator 为 null</td>
 *     <td>直接返回，无操作</td>
 *   </tr>
 *   <tr>
 *     <td>元素为 null</td>
 *     <td>跳过，不调用 consumer</td>
 *   </tr>
 *   <tr>
 *     <td>consumer 抛异常</td>
 *     <td>记录 debug 日志，继续处理后续元素</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>批量处理元素时希望单元素失败不影响整体（如日志记录、非关键数据同步）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要严格失败语义（任一元素失败即中断）时不适用，应捕获异常自行处理。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>忽略异常换取遍历不中断，牺牲单元素失败的可见性（仅 debug 日志）。</li>
 *   <li>null 元素被跳过，与"容错遍历"语义一致。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>忽略异常语义</b></p>
 * <ul>
 *   <li>元素消费过程中抛出任意 {@code Throwable} 都被捕获并记录 debug 日志，<b>不中断</b>后续元素处理。</li>
 * </ul>
 * <p><b>null 语义</b></p>
 * <ul>
 *   <li>iterable / iterator 为 null 时直接返回（无操作）。</li>
 *   <li>遍历过程中元素为 null 时跳过（不调用 consumer），避免 consumer 对 null 处理异常。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>Iterable 重载委托 Iterator 重载（{@code iterable.iterator()}）。</li>
 *   <li>Iterator 重载：{@code while(iterator.hasNext())} 内 try-catch(Throwable) 包裹取值与消费，null 元素 continue。</li>
 * </ul>
 *
 * @since 2025/9/28
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CIteratorUtils {

    /**
     * 遍历集合并忽略异常执行消费
     *
     * @param iterable 集合
     * @param consumer 消费函数
     * @param <T>      元素类型
     */
    public <T> void forEachIgnoreException(Iterable<T> iterable, CConsumer<T> consumer) {

        if(iterable == null) {
            return;
        }

        forEachIgnoreException(iterable.iterator(), consumer);

    }

    /**
     * 遍历迭代器并忽略异常执行消费（跳过 null 元素）
     *
     * @param iterator 迭代器
     * @param consumer 消费函数
     * @param <T>      元素类型
     */
    public <T> void forEachIgnoreException(Iterator<T> iterator, CConsumer<T> consumer) {

        if(iterator == null) {
            return;
        }

        while (iterator.hasNext()) {

            try {

                val value = iterator.next();
                if(value == null) {
                    continue;
                }

                consumer.accept(value);
            } catch (Throwable e) {
                log.debug("happen ignore Exception", e);
            }
        }
    }

}
