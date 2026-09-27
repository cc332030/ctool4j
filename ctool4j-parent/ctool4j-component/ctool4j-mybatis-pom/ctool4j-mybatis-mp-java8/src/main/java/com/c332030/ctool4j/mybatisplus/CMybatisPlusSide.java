package com.c332030.ctool4j.mybatisplus;

/**
 * <p>
 * Description: CMybatisPlusSide
 * </p>
 *
 * <p>
 * mybatis-plus 版本适配桥的<b>侧数常量</b>：记录"同一全限定名必须按版本侧各写一份"的符号个数，
 * 供复核用。mybatis-plus 3.5.17 起把 {@code IService} / {@code ServiceImpl} 从
 * {@code com.baomidou.mybatisplus.extension.service} 迁到
 * {@code com.baomidou.mybatisplus.spring.service}，两个坐标里只能有一个存在，
 * 故元素级转发面必须按版本侧各写一份。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>本类只有常量，无行为。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么本类在公共桶 {@code java-mp} 而不在某个版本侧</b>：本类只描述"有几个符号需要
 *   两侧各一份"，与"哪一侧"无关。放在任一版本侧目录里都会让它变成"只有那一侧挂载时才存在"，
 *   而它显然要被两侧同时看到——这正是仓库「多源目录之间的代码必须完全互斥」的正面应用：
 *   版本无关的内容进公共桶，版本相关的才进版本侧目录。</li>
 *   <li><b>两侧的目录布局是镜像的</b>：{@code java-mp-bridge}（3.5.x 侧）与
 *   {@code java-mp-bridge-ext}（3.3.x / 3.4.x 侧）承载完全相同的<b>相对路径集合</b>——
 *   {@code mybatisplus/spi/IService}、{@code spi/IMybatisPlusSpi}、
 *   {@code spi/CMybatisPlusServiceImpl}、{@code service/ICCheckService}、
 *   {@code service/impl/CBaseServiceImpl}。两目录内容互不相交，任一版本模块只挂其中一侧。</li>
 *   <li><b>为什么可以镜像</b>：两侧的差异只体现在"承哪个 MP 坐标"上，文件名、包名、公共面继承
 *   关系完全一致——于是 {@code java-mp} 里的公共源码（{@code ICService} 一族）对两侧一字不改。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>无兜底：本类只提供常量。</p>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>mybatis-plus 版本适配桥模块（{@code ctool4j-mybatis} / {@code -33} / {@code -34}）的公共源码桶。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不直接面向业务使用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>两侧新增一对"版本侧各一份"的桥接类时，须同步改 {@link #SIDE_COUNT}：这属于约定落地，
 *   不是可省略的仪式。类名与常量名沿用既有（{@code Side}），未随本次改造改名，避免无谓的调用面变动。</li>
 * </ul>
 *
 * @since 2026/9/26
 * @version 1.1
 */
public final class CMybatisPlusSide {

    /**
     * 需要在两个版本侧各写一份的适配桥符号数：{@code spi/IService} 与
     * {@code spi/CMybatisPlusServiceImpl} 各一份，故为 2。
     */
    public static final int SIDE_COUNT = 2;

    private CMybatisPlusSide() {
    }

}
