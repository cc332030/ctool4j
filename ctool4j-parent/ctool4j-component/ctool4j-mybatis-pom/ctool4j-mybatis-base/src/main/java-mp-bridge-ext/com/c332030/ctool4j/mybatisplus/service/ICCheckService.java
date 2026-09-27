package com.c332030.ctool4j.mybatisplus.service;

import cn.hutool.core.collection.CollUtil;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.mybatisplus.spi.IService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: ICCheckService
 * </p>
 *
 * <p>
 * 带空安全语义的 service 检查面：在版本侧 service 桥 {@link IService} 之上，把"空入参"折成
 * "空结果"，避免调用方先判空再查询。它是本仓库全部 service 契约（{@code ICService} 一族）
 * 的根。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code 空安全元素查询}：按主键取单个元素（{@link #getById(Serializable)}、
 * {@link #getOptById(Serializable)}）、按主键集合取元素（{@link #listByIds(Collection)}）；
 * 其余 MP 能力（分页、链式查询、{@code getBaseMapper} 等）由父接口 {@link IService} 提供。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么继承 {@link IService} 而不是 MP 原生接口</b>：MP 的 {@code IService} 在
 *   3.3.x / 3.4.x 与 3.5.x 下分属不同包名，本仓库全部 service 契约都以本接口为根，
 *   故"承哪个 MP 坐标"这件事必须在本文件处收敛到中性名 {@code spi.IService} 上。
 *   该中性名在两侧桥目录各有一份同全限定名、同成员集合的实现，**本文件因此两侧一字不差**——
 *   这正是仓库「多源目录之间的代码必须完全互斥」要的形状：本目录的源码只引用本目录里的
 *   {@code spi.IService}，不引用另一侧目录的任何类型。</li>
 *   <li><b>空洞语义短路在本层统一</b>：{@code getById(null)} / {@code getOptById(null)} /
 *   {@code listByIds(空集合)} 直接返回空结果，不下探到父接口。</li>
 *   <li><b>本文件在两侧桥目录各有一份</b>（{@code java-mp-bridge} / {@code java-mp-bridge-ext}）：
 *   两目录内容互不相交、从不同时挂载，故不会同时出现在同一 classpath。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>入参为空即返回空结果：{@code getById(null)} → {@code null}、
 * {@code getOptById(null)} → {@code Optional.empty()}、{@code listByIds(空集合)} → 空列表，
 * 全部短路、不触发底层查询。</p>
 *
 * <h2>适用范围</h2>
 * <p>查询服务：{@code ICBizIdService} 及本仓库全部 service 契约的父级。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>接口，不直接实例化；"元素是否存在"的语义不在此判断——空主键返回 {@code null} 与
 * 主键存在但记录不存在返回 {@code null} 是同一结果，需要区分时用 {@link #getOptById(Serializable)}。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>三个 {@code default} 方法都用 {@code IService.super.x(...)} 调用父接口，而 Java 规定
 *   {@code X.super.m()} 中的 {@code X} 必须<em>自己</em>声明 {@code m} 为 {@code default}——
 *   故两侧桥接口都把这三个方法声明为 {@code default} 并转发给 MP 对应成员。</li>
 *   <li>空安全覆写与 MP 上游 default 语义不同（上游 {@code getById} 对 null 主键可能走一次查询
 *   或抛异常）；本仓库以"空主键即空结果"为准。</li>
 * </ul>
 *
 * @since 2026/5/20
 * @version 1.2
 */
public interface ICCheckService<ENTITY> extends IService<ENTITY> {

    /**
     * 根据 ID 查询（ID 为空时返回 null）
     *
     * @param id 主键
     * @return 实体
     */
    @Override
    default ENTITY getById(Serializable id) {
        if (null == id) {
            return null;
        }
        return IService.super.getById(id);
    }

    /**
     * 根据 ID 查询（ID 为空时返回 Optional.empty）
     *
     * @param id 主键
     * @return 实体 Optional
     */
    @Override
    default Optional<ENTITY> getOptById(Serializable id) {
        if (null == id) {
            return Optional.empty();
        }
        return IService.super.getOptById(id);
    }

    /**
     * 根据 ID 列表查询（列表为空时返回空列表）
     *
     * @param idList 主键列表
     * @return 实体列表
     */
    @Override
    default List<ENTITY> listByIds(Collection<? extends Serializable> idList) {
        if (CollUtil.isEmpty(idList)) {
            return CList.of();
        }
        return IService.super.listByIds(idList);
    }

}
