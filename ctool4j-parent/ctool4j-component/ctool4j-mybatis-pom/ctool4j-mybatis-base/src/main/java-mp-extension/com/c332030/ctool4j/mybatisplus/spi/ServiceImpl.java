package com.c332030.ctool4j.mybatisplus.spi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Description: ServiceImpl（mybatis-plus 3.3.x / 3.4.x 版本适配桥）
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ServiceImpl}：以中性包名暴露 {@code extension.service.impl.ServiceImpl}。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>仅做继承桥接，实现全部由对应版本的 ServiceImpl 提供</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.3.x / 3.4.x（extension 包名侧）</p>
 * <h2>不适用与边界场景</h2>
 * <p>抽象类</p>
 * <h2>已知限制与取舍</h2>
 * <p>抽象类</p>
 *
 * @since 2026/9/26
 * @version 1.0
 */
public abstract class ServiceImpl<M extends BaseMapper<T>, T>
        extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<M, T> {

}
