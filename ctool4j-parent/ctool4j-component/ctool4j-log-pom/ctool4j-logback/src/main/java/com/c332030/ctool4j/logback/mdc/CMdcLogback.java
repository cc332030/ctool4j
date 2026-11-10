package com.c332030.ctool4j.logback.mdc;

import com.c332030.ctool4j.log.mdc.CMdc;
import com.c332030.ctool4j.log.mdc.CMdcDequeAble;
import lombok.val;
import org.slf4j.spi.MDCAdapter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Description: CMdcLogback
 * </p>
 *
 * <p>
 * 本类是本档位（SLF4J 2.0）下 {@code CMdcLogback} 的实现：与另一档位的同名文件是同一全限定名的
 * 两份实现、按档位二选一编译；本份在另一档位那份的基础上<b>纯增量</b>补 SLF4J 2.0 新增的 deque 语义。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>继承 {@code CMdcDequeAble}（→ {@code CMdc}）的全部能力：{@code getCopyOfContextMap} /
 * {@code setContextMap} 与基础读写，另加：</p>
 * <ul>
 *   <li>{@code pushByKey/popByKey/getCopyOfDequeByKey/clearDequeByKey}：SLF4J 2.0 起 {@code MDCAdapter} 新增的
 *   每键 deque 语义，本类实现。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>popByKey(不存在的键 / 空 deque)</td>
 *     <td>返回 {@code null}</td>
 *   </tr>
 *   <tr>
 *     <td>getCopyOfDequeByKey(不存在的键)</td>
 *     <td>返回空 deque（不留 null）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要跨线程传递 MDC 上下文（异步任务、线程池）的 logback 日志场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>与 log4j2 版 {@code CMdcLog4j}（{@code com.c332030.ctool4j.log4j.mdc}）互斥使用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>本份承接最新 LTS 档位：SLF4J 由 1.7 升至 2.0，{@code MDCAdapter} 新增
 *   {@code pushByKey/popByKey/getCopyOfDequeByKey/clearDequeByKey} 四个抽象方法，故本份补上对应实现；
 *   其余方法与另一档位逐字节一致。deque 值独立于单值上下文存放（键相同互不影响），
 *   与 SLF4J 的「单值 MAP + 每键 deque」两套存储语义保持一致。</li>
 *   <li>与另一档位的同名类构成"纯增量"链——本类只增不减，另一档位缺的方法不在此凭空补（编不过即暴露）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>类链形态</b></p>
 * <ul>
 *   <li><b>无档位差异</b>的 deque 存储与算法下沉到公共基类 {@code com.c332030.ctool4j.log.mdc.CMdcDequeAble}
 *   （ctool4j-log-base）；档位差异只有"接口有没有这四个方法"一处，故公共层不实现、由本档位的实现覆写。</li>
 * </ul>
 *
 * @since 2025/9/26
 * @version 1.1
 */
public class CMdcLogback extends CMdcDequeAble implements MDCAdapter {

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
