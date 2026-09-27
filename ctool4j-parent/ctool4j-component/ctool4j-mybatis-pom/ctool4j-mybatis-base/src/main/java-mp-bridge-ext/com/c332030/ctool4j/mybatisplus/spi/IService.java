package com.c332030.ctool4j.mybatisplus.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: IService（mybatis-plus 3.3.x / 3.4.x 版本适配桥）
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code IService}：以中性包名暴露 {@code extension.service.IService}。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>仅继承并转发共用源码需要覆写的 default 方法（Java 规定 {@code X.super.m()} 中的 m 必须由 X 自己声明为 default）</li>
 *   <li><b>本类只落在 3.3.x / 3.4.x 侧</b>：两个版本侧共用这一份（两侧的
 *   {@code extension.service} 坐标相同），故目录名不带档位后缀——它由版本模块显式挂载，
 *   与档位（jdk8 / 最新 LTS）无关。</li>
 *   <li>3.3.x / 3.4.x 无 {@code getOptById}，由本桥按 3.5.x 语义补齐</li>
 *   <li><b>为什么只在 3.3.x/3.4.x 侧的桥目录里</b>：mybatis-plus 3.5.17 起把 {@code IService} 由
 *   {@code extension.service} 迁到 {@code spring.service}，两坐标不能共存——本文件只对 3.3.x/3.4.x 成立，
 *   3.5.x 侧落在公共桶 {@code java-mp-bridge} 的同名文件里。两处不会同时挂载，不需要"同一全限定名两份副本"。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.3.x / 3.4.x（extension 包名侧）</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口</p>
 *
 * @since 2026/9/26
 * @version 1.2
 */
public interface IService<ENTITY> extends com.baomidou.mybatisplus.extension.service.IService<ENTITY> {

    @Override
    default ENTITY getById(Serializable id) {
        return com.baomidou.mybatisplus.extension.service.IService.super.getById(id);
    }

    /**
     * 3.3.x / 3.4.x 的 IService 无此方法，按 3.5.x 语义补齐（为空返回 Optional.empty）
     *
     * @param id 主键
     * @return 实体 Optional
     */
    default Optional<ENTITY> getOptById(Serializable id) {
        return Optional.ofNullable(getById(id));
    }

    @Override
    default List<ENTITY> listByIds(Collection<? extends Serializable> idList) {
        return com.baomidou.mybatisplus.extension.service.IService.super.listByIds(idList);
    }

}
