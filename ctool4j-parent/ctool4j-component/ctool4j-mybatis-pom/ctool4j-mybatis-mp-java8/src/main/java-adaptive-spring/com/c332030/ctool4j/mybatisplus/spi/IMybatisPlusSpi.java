package com.c332030.ctool4j.mybatisplus.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: IMybatisPlusSpi（mybatis-plus 3.5.x 版本适配桥）
 * </p>
 *
 * <p>
 * 以中性包名承接 {@code com.baomidou.mybatisplus.spring.service.IService}，
 * 并把元素级方法声明为 {@code default} 转发给上游——{@code ICCheckService} 的空安全覆写需要
 * {@code IService.super.x(...)}，而 Java 规定 {@code X.super.m()} 中的 {@code X}
 * 必须<em>自己</em>声明 {@code m} 为 {@code default}，故这三条必须在本接口重声明。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code MP 3.5.x service 全量面}（继承自上游 {@code spring.service.IService}）+
 * {@code 元素级 default 重声明}：{@link #getById(Serializable)}、
 * {@link #getOptById(Serializable)}、{@link #listByIds(Collection)}。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>本接口只落在 3.5.x 侧的桥目录 {@code java-mp-bridge}</b>：3.3.x / 3.4.x 侧有另一份
 *   同全限定名的 {@code spi/IMybatisPlusSpi}（承 {@code extension.service} 坐标），落在
 *   {@code java-mp-bridge-ext} 里、不会被本模块挂载。两个桥目录内容互不相交，满足仓库
 *   「一个模块下的多个源目录之间代码必须完全互斥」。</li>
 *   <li><b>与 MP 上游的关系</b>：本接口不额外引入父接口——{@code page} / {@code lambdaQuery} /
 *   {@code getBaseMapper} 等全部由上游 {@code spring.service.IService} 提供，
 *   故公共桶 {@code ICService} 的调用面在本侧完整可见，无需在公共桶里另立一套声明。</li>
 *   <li><b>继承方向</b>：{@code IService → 本接口 → MP 元素级 IService}，全链接口继承接口，
 *   符合「接口只可继承接口」。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>无兜底：三个元素级方法在 3.5.x 侧上游均存在，直接转发。</p>
 *
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.5.x（spring 包名侧），仅由 {@code ctool4j-mybatis} 模块挂载。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>接口，不直接实例化；不参与本仓库的公开契约（公开契约是 {@link IService}
 * 与业务侧的 {@code ICCheckService} 一族）。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与 3.3.x / 3.4.x 侧同名同包：两侧永不同时挂载，故不会同时出现在同一 classpath
 *   （配置期由模块的 {@code srcDir} 集合保证）。</li>
 *   <li>{@code getOptById} 在 3.5.x 上游已有默认实现，此处仍重声明并按"空主键即空结果"转发，
 *   使两侧语义在 {@code ICCheckService} 之下完全一致。</li>
 * </ul>
 *
 * @since 2026/9/27
 * @version 1.1
 */
public interface IMybatisPlusSpi<ENTITY>
        extends com.baomidou.mybatisplus.spring.service.IService<ENTITY> {

    @Override
    default ENTITY getById(Serializable id) {
        return com.baomidou.mybatisplus.spring.service.IService.super.getById(id);
    }

    /**
     * 按主键取单个元素（Optional 形态）。
     *
     * @param id 主键
     * @return 实体 Optional；主键为空或元素不存在时为 {@code Optional.empty()}
     */
    @Override
    default Optional<ENTITY> getOptById(Serializable id) {
        if (null == id) {
            return Optional.empty();
        }
        return Optional.ofNullable(getById(id));
    }

    @Override
    default List<ENTITY> listByIds(Collection<? extends Serializable> idList) {
        return com.baomidou.mybatisplus.spring.service.IService.super.listByIds(idList);
    }

}
