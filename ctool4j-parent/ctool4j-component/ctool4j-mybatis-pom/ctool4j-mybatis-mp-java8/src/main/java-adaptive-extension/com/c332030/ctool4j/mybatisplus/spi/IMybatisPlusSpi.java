package com.c332030.ctool4j.mybatisplus.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: IMybatisPlusSpi（mybatis-plus 3.3.x / 3.4.x 版本适配桥）
 * </p>
 *
 * <p>
 * 以中性包名承接 {@code com.baomidou.mybatisplus.extension.service.IService}，
 * 并把元素级方法声明为 {@code default} 转发给上游——{@code ICCheckService} 的空安全覆写需要
 * {@code IService.super.x(...)}，而 Java 规定 {@code X.super.m()} 中的 {@code X}
 * 必须<em>自己</em>声明 {@code m} 为 {@code default}，故这三条必须在本接口重声明。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code MP 3.3.x/3.4.x service 全量面}（继承自上游 {@code extension.service.IService}）+
 * {@code 元素级 default 重声明}：{@link #getById(Serializable)}、
 * {@link #getOptById(Serializable)}、{@link #listByIds(Collection)}。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>本接口只落在 3.3.x / 3.4.x 侧的桥目录 {@code java-mp-bridge-ext}</b>：3.5.x 侧有另一份
 *   同全限定名的 {@code spi/IMybatisPlusSpi}（承 {@code spring.service} 坐标），落在
 *   {@code java-mp-bridge} 里、不会被 {@code -33} / {@code -34} 挂载。两个桥目录内容互不相交，
 *   满足仓库「一个模块下的多个源目录之间代码必须完全互斥」。</li>
 *   <li><b>两个版本侧共用这一份</b>：3.3.x 与 3.4.x 的 {@code extension.service} 坐标相同，故不必
 *   按 3.3 / 3.4 再各分一份。</li>
 *   <li><b>与 MP 上游的关系</b>：本接口不额外引入父接口——{@code page} / {@code lambdaQuery} /
 *   {@code getBaseMapper} 等全部由上游 {@code extension.service.IService} 提供，
 *   故公共桶 {@code ICService} 的调用面在本侧完整可见，无需在公共桶里另立一套声明。</li>
 *   <li><b>继承方向</b>：{@code IService → 本接口 → MP 元素级 IService}，全链接口继承接口，
 *   符合「接口只可继承接口」。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>{@code getOptById} 由本接口的 {@code default} 兜底（基于 {@code getById} 包一层
 * {@code Optional.ofNullable}），补上 3.3.x / 3.4.x 上游缺失的能力。</p>
 *
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.3.x / 3.4.x（extension 包名侧），由 {@code ctool4j-mybatis-33} / {@code -34} 挂载。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>接口，不直接实例化；不参与本仓库的公开契约（公开契约是 {@link IService}
 * 与业务侧的 {@code ICCheckService} 一族）。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与 3.5.x 侧同名同包：两侧永不同时挂载，故不会同时出现在同一 classpath
 *   （配置期由模块的 {@code srcDir} 集合保证）。</li>
 *   <li>{@code getOptById} 在 3.3.x / 3.4.x 上游不存在、由本桥按 3.5.x 语义补齐；
 *   {@code Optional.ofNullable(getById(id))} 会先执行一次查询，语义与 3.5.x 上游一致
 *   （主键为空时返回 {@code Optional.empty()} 而不抛异常）。</li>
 * </ul>
 *
 * @since 2026/9/27
 * @version 1.1
 */
public interface IMybatisPlusSpi<ENTITY>
        extends com.baomidou.mybatisplus.extension.service.IService<ENTITY> {

    @Override
    default ENTITY getById(Serializable id) {
        return com.baomidou.mybatisplus.extension.service.IService.super.getById(id);
    }

    /**
     * 按 3.5.x 语义补齐的 Optional 版本（3.3.x / 3.4.x 的上游无此方法）
     *
     * @param id 主键
     * @return 实体 Optional；主键为空或元素不存在时为 {@code Optional.empty()}
     */
    default Optional<ENTITY> getOptById(Serializable id) {
        if (null == id) {
            return Optional.empty();
        }
        return Optional.ofNullable(getById(id));
    }

    @Override
    default List<ENTITY> listByIds(Collection<? extends Serializable> idList) {
        return com.baomidou.mybatisplus.extension.service.IService.super.listByIds(idList);
    }

}
