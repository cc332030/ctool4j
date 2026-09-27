package com.c332030.ctool4j.mybatisplus;

/**
 * <p>
 * Description: CMybatisPlusSide
 * </p>
 *
 * <p>
 * mybatis-plus 版本适配桥两侧共用的<b>符号占位</b>：mybatis-plus 3.5.17 起把
 * {@code IService} / {@code ServiceImpl} 从 {@code com.baomidou.mybatisplus.extension.service} 迁到
 * {@code com.baomidou.mybatisplus.spring.service}，两个坐标里只能有一个存在，故适配桥
 * （{@code com.c332030.ctool4j.mybatisplus.spi.IService} / {@code ServiceImpl}）必须按版本各写一份、
 * 分别继承对应坐标——同一全限定名因此无法只写一份。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>本类只有常量，无行为。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么需要本类</b>：仓库约定「一个模块下的多个档位专属源目录必须完全互斥」
 *   （同一全限定名只能出现一次，见根 {@code build.gradle.kts} 的配置期互斥校验）。
 *   适配桥的两份同名副本必须分放两侧源目录（否则有一档位缺类），恰好与该约定冲突；
 *   又不能把两侧共用的 {@code spi} 目录直接挂进各版本模块——会让"公共代码"在多个模块里各编一份。</li>
 *   <li><b>解法</b>：把两侧源目录改成"各自持有一份唯一文件"——档位专属目录只放
 *   <b>只在这一侧存在</b>的桥接类（{@code CMybatisPlusSide}），
 *   真正两份同名的适配桥则落在<b>单一公共目录</b> {@code java-mp-bridge}，
 *   由各版本模块按同一个虚拟目录路径挂载到同一个包 {@code ...mybatisplus.spi}。</li>
 *   <li><b>mvc 结构</b>：公共目录按"每一侧各有一份文件"填充——
 *   {@code CMybatisPlusSide}（本类，占位常量，与包同名）与 {@code spi} 子包（适配桥）。
 *   新增桥接类时若需要两侧各一份，放进 {@code java-mp-bridge/.../spi/} 并把 {@link #SIDE_COUNT} 加一。</li>
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
 *   <li>每新增一份"两侧各一份"的桥接类，都要同步改 {@link #SIDE_COUNT}：这属于约定落地，不是可省略的仪式。</li>
 * </ul>
 *
 * @since 2026/9/26
 * @version 1.0
 */
public final class CMybatisPlusSide {

    /** 本包适配桥的"侧"数：{@code IService} 与 {@code ServiceImpl} 各一份，故为 2 */
    public static final int SIDE_COUNT = 2;

    private CMybatisPlusSide() {
    }

}
