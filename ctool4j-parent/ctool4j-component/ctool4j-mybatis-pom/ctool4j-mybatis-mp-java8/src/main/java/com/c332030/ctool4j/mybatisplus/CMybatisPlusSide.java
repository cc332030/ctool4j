package com.c332030.ctool4j.mybatisplus;

/**
 * <p>
 * Description: CMybatisPlusSide
 * </p>
 *
 * <p>
 * mybatis-plus 版本侧适配的<b>侧数常量</b>：记录"同一全限定名必须按版本侧各写一份、且落在
 * 侧专属目录里"的符号个数，供复核用。mybatis-plus 3.5.17 起把 {@code IService} / {@code ServiceImpl}
 * 从 {@code com.baomidou.mybatisplus.extension.service} 迁到
 * {@code com.baomidou.mybatisplus.spring.service}，两个坐标里只能有一个存在，
 * 故自适应层与其实现基底必须按版本侧各写一份。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>本类只有常量，无行为。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么本类在中间模块的公共面 {@code src/main/java} 而不在某个侧目录</b>：本类只描述
 *   "有几个符号需要两侧各一份"，与"哪一侧"无关。放在任一版本侧目录里都会让它变成
 *   "只有那一侧挂载时才存在"，而它显然要被两侧同时看到——这正是仓库「多源目录之间的代码必须
 *   完全互斥」的正面应用：版本无关的内容进公共面，版本相关的才进版本侧目录。</li>
 *   <li><b>两侧目录是镜像的</b>：{@code java-adaptive-spring} / {@code java-adaptive-extension}
 *   承载自适应层（{@code spi/IMybatisPlusSpi}、{@code spi/IService}、
 *   {@code spi/CMybatisPlusServiceImpl}），{@code java-base-spring} / {@code java-base-extension}
 *   承载实现基底（{@code service/impl/CBaseServiceImpl}）——
 *   两组目录的相对路径集合完全一致，两侧内容互不相交，任一消费模块只挂其中一侧。</li>
 *   <li><b>为什么可以镜像</b>：两侧的差异只体现在"承哪个 MP 坐标"上，文件名、包名、
 *   公共面继承关系完全一致——于是公共面（{@code ICCheckService} 与 base 的 {@code ICService} 一族）
 *   对两侧一字不改，这正是"自适应中间层"要达成的形状。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>无兜底：本类只提供常量。</p>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>mybatis-plus 版本侧适配模块（{@code ctool4j-mybatis-mp-java8}）的公共面。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不直接面向业务使用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>两侧新增一对"版本侧各一份"的符号时，须同步改 {@link #SIDE_COUNT}：这属于约定落地，
 *   不是可省略的仪式。类名与常量名沿用既有（{@code Side}），未随本次改造改名，
 *   避免无谓的调用面变动。</li>
 * </ul>
 *
 * @since 2026/9/26
 * @version 2.0
 */
public final class CMybatisPlusSide {

    /**
     * 需要在两个版本侧各写一份的适配符号数：{@code spi/IService}、{@code spi/IMybatisPlusSpi}、
     * {@code spi/CMybatisPlusServiceImpl} 与 {@code service/impl/CBaseServiceImpl} 各一份，故为 4。
     */
    public static final int SIDE_COUNT = 4;

    private CMybatisPlusSide() {
    }

}
