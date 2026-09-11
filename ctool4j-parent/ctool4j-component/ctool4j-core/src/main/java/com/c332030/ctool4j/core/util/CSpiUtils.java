package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * <p>
 * Description: CSpiUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code getImpls(Class)}：获取接口所有实现（按 ServiceLoader 顺序）</li>
 *   <li>{@code getFirstImpl(Class)}：获取第一个实现（无实现抛 IllegalStateException）</li>
 *   <li>{@code getFirstCustomImplOrDefault(Class, Class defaultImpl)}：取第一个非默认实现，否则返回默认实现</li>
 *   <li>{@code getImplsSorted(Class, Comparator)} / {@code getImplsSorted(Class)}：获取实现并排序</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>getFirstImpl 无实现</td>
 *     <td>抛 IllegalStateException</td>
 *   </tr>
 *   <tr>
 *     <td>getFirstCustomImplOrDefault 无自定义实现</td>
 *     <td>返回默认实现</td>
 *   </tr>
 *   <tr>
 *     <td>getFirstCustomImplOrDefault 无任何实现</td>
 *     <td>返回 null（defaultProvider 也为 null）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>通过 SPI 机制加载可插拔实现，支持默认实现回退与自定义优先。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>依赖 {@code META-INF/services} 配置；无配置时 getImpls 返回空。</li>
 *   <li>getFirstCustomImplOrDefault 无任何实现时返回 null，调用方需注意判空。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>getFirstImpl 无实现抛异常（而非返回 null），保证调用方明确感知加载失败。</li>
 *   <li>自定义优先策略简单：第一个非默认实现即命中，多自定义实现时取顺序首个。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>加载与空处理</b></p>
 * <ul>
 *   <li>{@code getImpls} 基于 {@code ServiceLoader.load(clazz)} 遍历收集为 List。</li>
 *   <li>{@code getFirstImpl}：实现为空时抛 {@code IllegalStateException("No impl for ...")}，避免返回 null。</li>
 * </ul>
 * <p><b>自定义实现优先</b></p>
 * <ul>
 *   <li>{@code getFirstCustomImplOrDefault}：遍历实现，{@code defaultImpl.isInstance(provider)} 时记为默认实现，</li>
 *   <li>遇到第一个非默认实现记为自定义实现并中断；最后 {@code ObjUtil.defaultIfNull(custom, default)}——</li>
 *   <li>有自定义返回自定义，否则返回默认实现。</li>
 * </ul>
 * <p><b>排序</b></p>
 * <ul>
 *   <li>{@code getImplsSorted(clazz, comparator)} 在 {@code getImpls} 结果上 {@code sort(comparator)}。</li>
 *   <li>{@code getImplsSorted(clazz)}（元素 Comparable）按 {@code Comparable::compareTo} 自然序排序。</li>
 * </ul>
 *
 * @since 2025/9/14
 * @version 1.0
 */
@UtilityClass
public class CSpiUtils {

    /**
     * 获取第一个实现
     * @param clazz spi 基类
     * @param <T> 泛型
     * @return 默认实现
     */
    public <T> T getFirstImpl(Class<T> clazz) {

        val services = getImpls(clazz);
        if(CollUtil.isEmpty(services)) {
            throw new IllegalStateException("No impl for " + clazz);
        }

        return services.get(0);
    }

    /**
     * 获取第一个自定义实现，如果没有则返回默认实现
     * @param clazz spi 基类
     * @param defaultImpl 默认实现
     * @param <T> 泛型
     * @return 默认实现或者自定义实现
     */
    public <T> T getFirstCustomImplOrDefault(Class<T> clazz, Class<? extends T> defaultImpl) {

        val providers = getImpls(clazz);

        T defaultProvider = null;
        T customProvider = null;
        for (val provider : providers) {

            if(defaultImpl.isInstance(provider)) {
                defaultProvider = provider;
            } else {
                customProvider = provider;
                break;
            }
        }

        return ObjUtil.defaultIfNull(customProvider, defaultProvider);
    }

    /**
     * 获取所有实现
     * @param clazz spi 基类
     * @param <T> 泛型
     * @return 默认顺序的值
     */
    public <T> List<T> getImpls(Class<T> clazz) {

        val services = ServiceLoader.load(clazz);

        val list = new ArrayList<T>();
        for (val service : services) {
            list.add(service);
        }

        return list;
    }

    /**
     * 获取所有实现-自定义排序
     * @param clazz spi 基类
     * @param comparator 比较器
     * @param <T> 泛型
     * @return 排序后的值
     */
    public <T> List<T> getImplsSorted(Class<T> clazz, Comparator<T> comparator) {

        val impls = getImpls(clazz);
        impls.sort(comparator);
        return impls;
    }

    /**
     * 获取所有实现-类定义的排序
     * @param clazz spi 基类
     * @return 排序后的值
     * @param <T> 泛型
     */
    public <T extends Comparable<T>> List<T> getImplsSorted(Class<T> clazz) {
        return getImplsSorted(clazz, Comparable::compareTo);
    }

}
