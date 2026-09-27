package com.c332030.ctool4j.mybatisplus.spi;

/**
 * <p>
 * Description: IService（mybatis-plus 3.3.x / 3.4.x 版本适配桥）
 * </p>
 *
 * <p>
 * 以中性包名 {@code com.c332030.ctool4j.mybatisplus.spi.IService} 暴露
 * {@code com.baomidou.mybatisplus.extension.service.IService}：本仓库面向业务侧的 service 契约（{@code ICCheckService} → {@code ICService} 一族）构建在
 * 自适应层 {@link IMybatisPlusSpi} 之上，
 * 本接口即 3.3.x / 3.4.x 侧对外沿用的那个名字（3.5.x 侧的同名接口见 {@code java-spring}）。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code 3.3.x/3.4.x 侧 service 名}：所有成员从自适应层 {@link IMybatisPlusSpi} 继承，
 * 本接口不再新增声明。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>本接口只落在 3.3.x / 3.4.x 侧的桥目录 {@code java-extension}</b>：3.5.x 侧有另一份
 *   同全限定名的 {@code spi/IService}（承 {@code spring.service} 坐标），落在
 *   {@code java-spring} 里、不会被 {@code -33} / {@code -34} 挂载——两个桥目录内容互不相交，
 *   满足仓库「一个模块下的多个源目录之间代码必须完全互斥」。</li>
 *   <li><b>为何不直接把转发面命名为 IService</b>：{@link IMybatisPlusSpi} 是"自适应层"（承接 MP 上游坐标 + 空安全语义），
 *   每个方法都要写一遍 {@code default} 转发，属版本适配的实现细节；业务与仓库内部既有引用认的是
 *   {@code spi/IService} 这个名，故保留它作为对外名，转发面放在其父接口上。</li>
 *   <li><b>继承方向</b>：{@code IService → IMybatisPlusSpi → MP 元素级 IService}。
 *   全链都是接口继承接口，符合「接口只可继承接口」。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>无兜底：本接口只做名字承接，行为全部由 {@link IMybatisPlusSpi} 的 {@code default} 提供。</p>
 *
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.3.x / 3.4.x（extension 包名侧），由 {@code ctool4j-mybatis-33} / {@code -34} 挂载。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>接口；3.5.x 使用方走 {@code ctool4j-mybatis}，不由本份生效。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与 3.5.x 侧同名同包：两侧永不同时挂载，故不会同时出现在同一 classpath
 *   （配置期由模块的 {@code srcDir} 集合保证）。</li>
 *   <li>{@code getOptById} 由 {@link IMybatisPlusSpi} 补齐，本仓库的 3.3.x / 3.4.x 使用方
 *   因此获得与 3.5.x 一致的 API 面。</li>
 * </ul>
 *
 * @since 2026/9/26
 * @version 1.3
 */
public interface IService<ENTITY> extends IMybatisPlusSpi<ENTITY> {

}
