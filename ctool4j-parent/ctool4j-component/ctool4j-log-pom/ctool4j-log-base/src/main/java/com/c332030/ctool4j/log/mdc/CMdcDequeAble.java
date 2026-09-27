package com.c332030.ctool4j.log.mdc;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * Description: CMdcDequeAble
 * </p>
 *
 * <p>
 * MDC 的<b>每键 deque 存储</b>基类：在 {@link CMdc}（单值上下文）之外，另存一套「键 → 双端队列」的存储。
 * 两套存储互不影响——同一个 key 的单值与 deque 各自独立，与 SLF4J 的语义一致。
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code getDequeMap}：取当前线程的 deque 存储（受保护，供子类读取）。</li>
 *   <li>{@code getOrCreateDeque}：取指定键的 deque，不存在时创建（受保护，供子类写入）。</li>
 * </ul>
 * <p>本类<b>不实现</b> SLF4J 的 {@code pushByKey/popByKey/getCopyOfDequeByKey/clearDequeByKey}：
 * 这四个方法自 SLF4J 2.0 才进入 {@code MDCAdapter}，jdk8 档位的 SLF4J 1.7 接口没有它们，
 * 在公共层实现会让 jdk8 档位出现"多余的公开 API"。故存储与算法留在此处，由各档位的最终实现类按需覆写。</p>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>取不存在的键的 deque</td>
 *     <td>{@code getDequeMap} 返回空存储，{@code getOrCreateDeque} 创建新 deque（不返回 null）</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>MDC 适配器需要 SLF4J 2.0 deque 语义时的公共存储父类。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不直接面向业务使用，需经各日志后端的 MDC 适配器子类（{@code CMdcLogback} 等）间接使用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>存储为每线程独立（{@link ThreadLocal}），<b>不随父类 {@code CMdc} 的 TTL 跨线程透传</b>——
 *   deque 语义在 slf4j 中本就是线程内的栈式快照，跨线程透传无意义。</li>
 *   <li>{@code ConcurrentHashMap} 不允许 null key，deque 相关操作传 null key 会抛 NPE。</li>
 * </ul>
 *
 * @since 2026/9/26
 * @version 1.0
 */
public class CMdcDequeAble extends CMdc {

    /** 每线程的 deque 存储：键 → 双端队列 */
    private static final ThreadLocal<ConcurrentMap<String, Deque<String>>> DEQUE_THREAD_LOCAL =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    /**
     * 取当前线程的 deque 存储
     *
     * @return 当前线程的 deque 存储
     */
    protected ConcurrentMap<String, Deque<String>> getDequeMap() {
        return DEQUE_THREAD_LOCAL.get();
    }

    /**
     * 取指定键的 deque（不存在时创建）
     *
     * @param key 键
     * @return deque
     */
    protected Deque<String> getOrCreateDeque(String key) {
        return getDequeMap().computeIfAbsent(key, k -> new ArrayDeque<>());
    }

}
