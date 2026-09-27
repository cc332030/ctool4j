package com.c332030.ctool4j.mybatisplus.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: IService（mybatis-plus 版本适配桥）
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code IService}：以中性包名暴露 {@code spring.service.IService}。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>仅继承并转发共用源码需要覆写的 default 方法（Java 规定 {@code X.super.m()} 中的 m 必须由 X 自己声明为 default）</li>
 *   <li><b>本类只落在 {@code ctool4j-mybatis}（3.5.x）模块</b>：3.3.x / 3.4.x 侧有另一份同全限定名的
 *   {@code spi/IService}（承 {@code extension.service} 坐标），落在只属于那一侧的桥目录里、不会被本模块挂载。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.5.x（spring 包名侧）</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口</p>
 *
 * @since 2026/9/26
 * @version 1.1
 */
public interface IService<ENTITY> extends com.baomidou.mybatisplus.spring.service.IService<ENTITY> {

    @Override
    default ENTITY getById(Serializable id) {
        return com.baomidou.mybatisplus.spring.service.IService.super.getById(id);
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
        return com.baomidou.mybatisplus.spring.service.IService.super.listByIds(idList);
    }

}
