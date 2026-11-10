package com.c332030.ctool4j.mybatisplus.spi;

/**
 * <p>
 * Description: IService（mybatis-plus &ge; 3.5.17 版本适配桥）
 * </p>
 *
 * <p>
 * 以中性包名 {@code com.c332030.ctool4j.mybatisplus.spi.IService} 暴露
 * {@code com.baomidou.mybatisplus.spring.service.IService}：本仓库面向业务侧的 service 契约
 * （{@code ICService} 一族）构建在自适应层 {@link CMybatisPlusSpi} 之上，本接口即
 * "MP 包名为 {@code spring.service}"一侧对外沿用的那个名字。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code MP &ge; 3.5.17 service 名}：所有成员从自适应层 {@link CMybatisPlusSpi} 继承，
 * 本接口不再新增声明。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>本接口只落在 {@code src/main/java}</b>：另一侧（MP &lt; 3.5.17，
 *   {@code extension.service} 包名）有同全限定名的一份，落在 {@code src/main/java-jdk8}，
 *   jdk8 档位由本模块 {@code build.gradle.kts} 整体替换主源集
 *   ——两个目录**永不共存**，故不会同时进 classpath。</li>
 *   <li><b>为何不直接把转发面命名为 IService</b>：{@link CMybatisPlusSpi} 是"自适应层"
 *   （承接 MP 上游坐标 + 空安全语义），每个方法都要写一遍 {@code default} 转发，属版本适配的实现细节；
 *   业务与仓库内部既有引用认的是 {@code spi/IService} 这个名，故保留它作为对外名。</li>
 *   <li><b>继承方向</b>：{@code IService → CMybatisPlusSpi → MP IService}
 *   （MP 的 {@code IService} 自身继承 {@code extension.repository.IRepository}）。
 *   全链都是接口继承接口，符合「接口只可继承接口」。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>无兜底：本接口只做名字承接，行为全部由 {@link CMybatisPlusSpi} 的 {@code default} 提供。</p>
 *
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.5.17 及以后（{@code spring.service} 包名侧）。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>接口；MP &lt; 3.5.17 的使用方由同模块 {@code java-jdk8} 目录的那一份生效。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与另一侧同名同包：两侧永不同时挂载，故不会同时出现在同一 classpath
 *   （配置期由模块的源码目录集合保证）。</li>
 * </ul>
 *
 * @since 2026/9/26
 * @version 2.1
 */
public interface IService<ENTITY> extends CMybatisPlusSpi<ENTITY> {

}
