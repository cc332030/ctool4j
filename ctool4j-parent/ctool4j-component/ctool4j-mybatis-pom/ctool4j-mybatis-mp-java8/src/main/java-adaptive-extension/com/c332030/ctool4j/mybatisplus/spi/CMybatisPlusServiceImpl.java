package com.c332030.ctool4j.mybatisplus.spi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Description: CMybatisPlusServiceImpl（mybatis-plus 3.3.x / 3.4.x 版本适配桥）
 * </p>
 *
 * <p>
 * 以中性包名承接 {@code com.baomidou.mybatisplus.extension.service.impl.ServiceImpl}：
 * 3.3.x / 3.4.x 侧的 service 实现基底，供 {@code CBaseServiceImpl} 继承。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code 3.3.x/3.4.x 侧 service 实现基底}：继承上游 {@code extension.service.impl.ServiceImpl}。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>仅做继承桥接，实现全部由对应版本的上游 {@code ServiceImpl} 提供</li>
 *   <li><b>本类只落在 3.3.x / 3.4.x 侧的桥目录 {@code java-mp-bridge-ext}</b>：3.5.x 侧有另一份
 *   同全限定名的 {@code spi/CMybatisPlusServiceImpl}（承 {@code spring.service.impl} 坐标），
 *   落在 {@code java-mp-bridge} 里、不会被 {@code -33} / {@code -34} 挂载——两个桥目录内容互不相交，
 *   满足仓库「一个模块下的多个源目录之间代码必须完全互斥」。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>无兜底：除 {@code getOptById} 由接口 {@code default} 提供外，其余实现全部由上游提供。</p>
 *
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.3.x / 3.4.x（extension 包名侧），由 {@code ctool4j-mybatis-33} / {@code -34} 挂载。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>抽象类；3.5.x 使用方走 {@code ctool4j-mybatis}，不由本份生效。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与 3.5.x 侧同名同包：两侧永不同时挂载，故不会同时出现在同一 classpath
 *   （配置期由模块的 {@code srcDir} 集合保证）。</li>
 * </ul>
 *
 * @since 2026/9/26
 * @version 1.3
 */
public abstract class CMybatisPlusServiceImpl<M extends BaseMapper<T>, T>
        extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<M, T> {

}
