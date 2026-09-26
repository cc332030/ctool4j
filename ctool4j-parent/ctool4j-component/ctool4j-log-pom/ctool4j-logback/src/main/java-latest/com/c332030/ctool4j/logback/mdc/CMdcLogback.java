package com.c332030.ctool4j.logback.mdc;

import com.c332030.ctool4j.log.mdc.CMdc;
import lombok.val;
import org.slf4j.spi.MDCAdapter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * Description: CMdcLogback
 * </p>
 *
 * <p>
 * logback 后端的 MDC 适配器，实现 slf4j {@link MDCAdapter}；
 * 存储与基础读写逻辑见父类 {@link CMdc}，此处仅补充 slf4j 特有语义。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMdcLogback} 实现 SLF4J {@code MDCAdapter}，继承公共存储基类 {@code CMdc}，实现日志上下文在线程间传递。</p>
 * <ul>
 *   <li>基础读写（{@code put/get/remove/clear}、{@code containsKey/isEmpty}）：继承自 {@code CMdc}。</li>
 *   <li>{@code getCopyOfContextMap}：返回上下文副本（{@code LinkedHashMap}）。</li>
 *   <li>deque 语义（{@code pushByKey/popByKey/getCopyOfDequeByKey/clearDequeByKey}）：SLF4J 2.0 起 {@code MDCAdapter} 新增，
 *   本份承接最新 LTS 档位时一并实现（jdk8 档位的 SLF4J 1.7 接口无这四个方法）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>setContextMap(null)</td>
 *     <td>清空上下文</td>
 *   </tr>
 *   <tr>
 *     <td>setContextMap(普通 Map)</td>
 *     <td>清空后 putAll 拷贝</td>
 *   </tr>
 *   <tr>
 *     <td>getCopyOfContextMap</td>
 *     <td>返回新 LinkedHashMap 副本，修改不影响原 MDC</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要跨线程传递 MDC 上下文（异步任务、线程池）的 logback 日志场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>{@code ConcurrentHashMap} 不允许 null key/value，put(null, val) 等会抛 NPE。</li>
 *   <li>与 log4j2 版 {@code CMdcLog4j}（{@code com.c332030.ctool4j.log4j.mdc}）互斥使用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>本份承接最新 LTS 档位：SLF4J 由 1.7 升至 2.0，{@code MDCAdapter} 新增
 *   {@code pushByKey/popByKey/getCopyOfDequeByKey/clearDequeByKey} 四个抽象方法，故本份补上对应实现；
 *   其余方法与 jdk8 档位逐字节一致。deque 值独立于单值上下文存放（键相同互不影响），
 *   与 SLF4J 的「单值 MAP + 每键 deque」两套存储语义保持一致。</li>
 *   <li>基于 TransmittableThreadLocal，需配合 TTL 依赖与跨线程包装使用。</li>
 *   <li>依赖 {@code ConcurrentHashMap} 的 null 限制。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>存储结构</b></p>
 * <ul>
 *   <li>存储与基础读写下沉至父类 {@code CMdc}（可变实例 + 不可变视图），本类只补充 slf4j 特有语义。</li>
 * </ul>
 *
 * @since 2025/9/26
 * @version 1.0
 */
public class CMdcLogback extends CMdc implements MDCAdapter {

    /**
     * 获取上下文副本
     *
     * @return 上下文副本
     */
    @Override
    public Map<String, String> getCopyOfContextMap() {
        return new LinkedHashMap<>(getMdcMap());
    }

    /**
     * 设置上下文，null 时清空
     *
     * <p><b>setContextMap</b></p>
     * <ul>
     *   <li>清空可变实例后 {@code putAll} 拷贝传入内容（null 时仅清空）；符合 slf4j「拷贝传入 map」语义。</li>
     * </ul>
     * <ul>
     *   <li>{@code setContextMap(Map)}：设置整个上下文，null 时清空。</li>
     * </ul>
     *
     * @param contextMap 上下文*/
    @Override
    public void setContextMap(Map<String, String> contextMap) {

        val map = getMdcMap();
        map.clear();

        if(contextMap != null) {
            map.putAll(contextMap);
        }
    }

    /**
     * 每键 deque 存储：SLF4J 2.0 的 {@code MDCAdapter} 新增 deque 语义，
     * 与单值上下文相互独立（同一 key 的单值与 deque 互不影响）。
     */
    private static final ThreadLocal<ConcurrentMap<String, Deque<String>>> DEQUE_THREAD_LOCAL =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    /**
     * 取当前线程的 deque 存储
     *
     * @return 当前线程的 deque 存储
     */
    private ConcurrentMap<String, Deque<String>> getDequeMap() {
        return DEQUE_THREAD_LOCAL.get();
    }

    /**
     * 取指定键的 deque（不存在时创建）
     *
     * @param key 键
     * @return deque
     */
    private Deque<String> getOrCreateDeque(String key) {
        return getDequeMap().computeIfAbsent(key, k -> new ArrayDeque<>());
    }

    /**
     * 将值压入指定键的 deque 栈顶
     *
     * @param key 键
     * @param val 值
     */
    @Override
    public void pushByKey(String key, String val) {
        getOrCreateDeque(key).push(val);
    }

    /**
     * 弹出指定键 deque 的栈顶值
     *
     * @param key 键
     * @return 栈顶值；deque 为空时返回 {@code null}
     */
    @Override
    public String popByKey(String key) {

        val deque = getDequeMap().get(key);
        return (null == deque || deque.isEmpty()) ? null : deque.pop();
    }

    /**
     * 取指定键 deque 的副本
     *
     * @param key 键
     * @return deque 副本（栈顶在前）；不存在时返回空 deque
     */
    @Override
    public Deque<String> getCopyOfDequeByKey(String key) {

        val deque = getDequeMap().get(key);
        return (null == deque) ? new ArrayDeque<>() : new ArrayDeque<>(deque);
    }

    /**
     * 清空指定键的 deque
     *
     * @param key 键
     */
    @Override
    public void clearDequeByKey(String key) {
        getDequeMap().remove(key);
    }

}
