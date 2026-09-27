package com.c332030.ctool4j.mybatisplus.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.collection.CollUtil;
import com.c332030.ctool4j.core.util.CList;

/**
 * <p>
 * Description: CMybatisPlusSpi（mybatis-plus 3.5.x 侧自适应层）
 * </p>
 *
 * <p>
 * <b>自适应中间层</b>：以中性全限定名承接 3.5.x 侧的 service 坐标
 * {@code com.baomidou.mybatisplus.spring.service.IService}，
 * 并把本仓库统一要求的<b>空安全语义</b>（空主键 / 空集合入参折成空结果）收敛在本层。
 * 上层公共契约（{@code ICCheckService} → {@code ICService} 一族）因此可以
 * <b>完全不引用任何版本侧类型</b>——这正是"中间模块"要解决的问题：
 * 版本侧差异与本仓库的语义约定都收在本层，公共面得以版本无关。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code MP 3.5.x service 全量面}（继承自上游 {@code spring.service.IService}）+
 * {@code 空安全元素级实现}：{@link #getById(Serializable)}、{@link #getOptById(Serializable)}、
 * {@link #listByIds(Collection)}。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>本接口是"自适应层"的实体</b>：{@code IService → CMybatisPlusSpi → MP IService}。
 *   上游坐标换成 {@code extension.service} 时（3.3.x / 3.4.x 侧），只需换掉本层的继承目标
 *   与 {@code super} 限定名，上层一字不改——这就是"自适应"的含义，
 *   与仓库里 {@code java-javax} ↔ {@code java-jakarta} 成对适配同构。</li>
 *   <li><b>为什么空安全语义放在本层而不是上层</b>：上层公共面要"版本无关"就不能出现
 *   {@code X.super.m()}（{@code X} 是版本侧类型）；而空安全实现必须调用上游 {@code super}，
 *   故它天然属于本层。放这里也顺带让 3.3.x / 3.4.x 侧补齐与 3.5.x 一致的 API 面。</li>
 *   <li><b>本接口只落在 3.5.x 侧的源目录 {@code java-spring}</b>：3.3.x / 3.4.x 侧有另一份
 *   同全限定名的 {@code spi/CMybatisPlusSpi}（承 {@code extension.service} 坐标），
 *   落在 {@code java-extension} 里、不会被 {@code ctool4j-mybatis} 挂载——两个侧目录内容互不相交，
 *   满足仓库「一个模块下的多个源目录之间代码必须完全互斥」。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>空主键 / 空集合即空结果，全部短路、不触发底层查询：
 * {@code getById(null)} → {@code null}、{@code getOptById(null)} → {@code Optional.empty()}、
 * {@code listByIds(空集合)} → 空列表。</p>
 *
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.5.x（spring 包名侧），仅由 {@code ctool4j-mybatis} 模块挂载。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>接口；3.3.x / 3.4.x 使用方走 {@code ctool4j-mybatis-33} / {@code -34}，不由本份生效。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与 3.3.x / 3.4.x 侧同名同包：两侧永不同时挂载，故不会同时出现在同一 classpath
 *   （配置期由消费模块的 {@code srcDir} 集合保证）。</li>
 *   <li>空安全覆写与 MP 上游 default 语义不同（上游 {@code getById} 对 null 主键可能走一次查询
 *   或抛异常）；本仓库以"空主键即空结果"为准。</li>
 * </ul>
 *
 * @since 2026/9/27
 * @version 2.0
 */
public interface CMybatisPlusSpi<ENTITY>
        extends com.baomidou.mybatisplus.spring.service.IService<ENTITY> {

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
        return com.baomidou.mybatisplus.spring.service.IService.super.getById(id);
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
        return Optional.ofNullable(getById(id));
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
        return com.baomidou.mybatisplus.spring.service.IService.super.listByIds(idList);
    }

}
