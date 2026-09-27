package com.c332030.ctool4j.mybatisplus.service;

import com.c332030.ctool4j.mybatisplus.spi.CMybatisPlusSpi;

/**
 * <p>
 * Description: ICCheckService
 * </p>
 *
 * <p>
 * 带空安全语义的 service 检查面：本仓库全部 service 契约（{@code ICService} 一族）的根，
 * 以中性名 {@link CMybatisPlusSpi} 承接版本侧 service 桥。
 * <b>空安全语义（空入参折成空结果）由自适应层 {@link CMybatisPlusSpi} 实现</b>，
 * 故本接口<em>不引用任何版本侧类型</em>，可以落在中间模块的公共面里、随 jar 发布一次。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code 空安全元素查询}：按主键取单个元素（{@code getById}、{@code getOptById}）、
 * 按主键集合取元素（{@code listByIds}），三者均继承自 {@link CMybatisPlusSpi}；
 * 其余 MP 能力（分页、链式查询、{@code getBaseMapper} 等）由同一父接口提供，本接口不新增声明。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么继承的是中性名而不是 MP 原生接口</b>：MP 的 {@code IService} 在
 *   3.3.x / 3.4.x（{@code extension.service}）与 3.5.x（{@code spring.service}）下分属不同包名，
 *   而本仓库全部 service 契约都以本接口为根。中间模块把"承哪个 MP 坐标"收敛到同全限定名的
 *   {@link CMybatisPlusSpi}（两侧各一份、自适应）上，本接口因此版本无关。</li>
 *   <li><b>本接口是本仓库"继承链的公共段"</b>：{@code MP IService → spi/CMybatisPlusSpi → ICCheckService
 *   → ICBizIdService → ICService}。前三段（含本接口）都版本无关，落在中间模块；
 *   {@code ICService} 一族落在 base 的公共源码目录，两者共同构成"任何版本模块都能直接编译"的公共面。</li>
 *   <li><b>为什么本接口不放空安全实现</b>：空安全实现必须调用 {@code 上游.super.x(...)}，
 *   而 Java 规定 {@code X.super.m()} 中的 {@code X} 必须<em>自己</em>声明 {@code m} 为 {@code default}——
 *   那会让本接口编译期就必须解析版本侧类型，公共面随即失效。故实现下沉到 {@link CMybatisPlusSpi}。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>无兜底：空安全语义由 {@link CMybatisPlusSpi} 提供（空主键 → {@code null} /
 * {@code Optional.empty()}，空集合 → 空列表，全部短路）。</p>
 *
 * <h2>适用范围</h2>
 * <p>查询服务：{@code ICBizIdService} 及本仓库全部 service 契约的父级。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>接口，不直接实例化；"元素是否存在"的语义不在此判断——空主键返回 {@code null} 与
 * 主键存在但记录不存在返回 {@code null} 是同一结果，需要区分时用 {@code getOptById}。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>本接口不声明任何成员：语义（空安全）在自适应层，名字承接在 {@code spi/IService}，
 *   职责单一，改动时不必两侧同步。</li>
 *   <li>空安全覆写与 MP 上游 default 语义不同（上游 {@code getById} 对 null 主键可能走一次查询
 *   或抛异常）；本仓库以"空主键即空结果"为准。</li>
 * </ul>
 *
 * @since 2026/5/20
 * @version 2.0
 */
public interface ICCheckService<ENTITY> extends CMybatisPlusSpi<ENTITY> {

}
