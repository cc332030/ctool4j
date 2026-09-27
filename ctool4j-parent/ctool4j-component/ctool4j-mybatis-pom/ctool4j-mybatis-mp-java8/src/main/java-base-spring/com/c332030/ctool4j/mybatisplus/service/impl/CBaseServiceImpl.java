package com.c332030.ctool4j.mybatisplus.service.impl;

import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;
import com.c332030.ctool4j.mybatisplus.service.ICService;
import com.c332030.ctool4j.mybatisplus.spi.CMybatisPlusServiceImpl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: CBaseServiceImpl
 * </p>
 *
 * <p>
 * service 实现基底：继承版本侧的实现桥 {@link CMybatisPlusServiceImpl}、实现业务侧契约
 * {@link ICService}，并把两侧同名的元素级方法显式消歧到业务侧契约。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code 分页/查询/业务 ID 公共实现}（由 {@link ICService} 提供）+ {@code 元素级消歧}
 * （{@link #getById(Serializable)}、{@link #getOptById(Serializable)}、{@link #listByIds(Collection)}）。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>实现 {@link ICService}，提供分页、查询、业务 ID 等公共实现</li>
 *   <li><b>为什么必须在这里消歧</b>：{@link CMybatisPlusServiceImpl}（经版本侧桥接口）与
 *   {@link ICService}（经 {@code ICCheckService}）各自带一份同名 {@code default}，
 *   两边血统不同、Java 无法自动选择，必须由本类显式转交一侧，否则编译期报
 *   "inherits unrelated defaults"。转交 {@link ICService} 侧即取得本仓库统一的空安全语义。</li>
 *   <li><b>本文件在两侧桥目录各有一份</b>（{@code java-mp-bridge} / {@code java-mp-bridge-ext}）：
 *   它承的是"版本侧 {@code CMybatisPlusServiceImpl} + 公共 {@code ICService}"的交叉点，
 *   两侧的实现类不是同一个对象，故无法只写一份。两目录内容互不相交、从不同时挂载。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>无兜底：全部分支转交 {@link ICService} 的空安全实现。</p>
 *
 * <h2>适用范围</h2>
 * <p>服务基类：本仓库全部 service 实现类的父级。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>抽象类，需子类提供具体 Mapper 与实体类型。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>三个消歧方法必须手写、不能省略——它们使"实现类继承实现类或实现接口、
 *   接口只继承接口"的继承方向得以保持（本类为 {@code C}，其父 {@code CMybatisPlusServiceImpl}
 *   为 {@code C}，实现的 {@code ICService} 为 {@code IC}）。</li>
 * </ul>
 *
 * @since 2025/11/27
 * @version 1.1
 */
public abstract class CBaseServiceImpl<M extends CBaseMapper<T>, T>
        extends CMybatisPlusServiceImpl<M, T>
        implements ICService<T> {

    @Override
    public T getById(Serializable id) {
        return ICService.super.getById(id);
    }

    @Override
    public Optional<T> getOptById(Serializable id) {
        return ICService.super.getOptById(id);
    }

    @Override
    public List<T> listByIds(Collection<? extends Serializable> idList) {
        return ICService.super.listByIds(idList);
    }

}
