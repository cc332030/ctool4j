package com.c332030.ctool4j.core.util;

/**
 * <p>
 * Description: ICView，同时提供「可变实例」与「不可变视图」的容器契约。
 * </p>
 *
 * <p>
 * 用于需要频繁读不可变副本、又需内部可变的场景：写方直接改可变实例，
 * 读方直接返回不可变视图（实时视图，零构建）。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICView} 为「可变实例 + 不可变视图」容器的公共契约，定义：</p>
 * <ul>
 *   <li>{@code getMutable}：获取可变实例，供写操作。</li>
 *   <li>{@code getImmutable}：获取不可变视图，供读操作，零构建。</li>
 * </ul>
 * <p>各实现类（{@code CMapView} / {@code CListView} / {@code CSetView}）成对创建可变实例与不可变视图， 视图为可变实例的实时视图（live view），底层修改实时反映。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无参构造</td>
 *     <td>可变实例默认为对应集合的普通实现（HashMap/ArrayList/HashSet）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要「内部可变 + 外部只读视图」的集合持有场景（如 MDC 上下文、配置缓存）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不可变视图为实时视图（非快照），若需「固定快照」应另用副本构造。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>不可变视图为只读视图（非快照），底层可变实例被修改后视图内容随之变化。</li>
 *   <li>默认非线程安全；线程安全由调用方按需传入具体实现。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>契约语义</b></p>
 * <ul>
 *   <li>可变实例与不可变视图成对提供；写方改可变实例，读方读不可变视图。</li>
 *   <li>视图为实时视图（非快照），底层修改实时反映，无需每次重建。</li>
 * </ul>
 * <p><b>实现类</b></p>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>实现类</th>
 *     <th>可变实例默认</th>
 *     <th>不可变视图</th>
 *   </tr>
 *   <tr>
 *     <td>{@code CMapView}</td>
 *     <td>{@code HashMap}</td>
 *     <td>{@code unmodifiableMap}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code CListView}</td>
 *     <td>{@code ArrayList}</td>
 *     <td>{@code unmodifiableList}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code CSetView}</td>
 *     <td>{@code HashSet}</td>
 *     <td>{@code unmodifiableSet}</td>
 *   </tr>
 * </table>
 * <p>默认均非线程安全；线程安全场景由调用方显式传入线程安全的集合实现。</p>
 *
 * @param <T> 实例类型
 * @since 2026/8/31
 * @version 1.0
 */
public interface ICView<T> {

    /**
     * 获取可变实例
     *
     * @return 可变实例
     */
    T getMutable();

    /**
     * 获取不可变视图
     *
     * @return 不可变视图
     */
    T getImmutable();

}
