package com.c332030.ctool4j.mybatisplus.spi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Description: CMybatisPlusServiceImpl（mybatis-plus &lt; 3.5.17 版本适配桥）
 * </p>
 *
 * <p>
 * 以中性包名承接 {@code com.baomidou.mybatisplus.extension.service.impl.ServiceImpl}：
 * "MP 包名为 {@code extension.service}"一侧的 service 实现基底，供 {@code CServiceImpl} 继承。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code MP &lt; 3.5.17 service 实现基底}：继承上游 {@code extension.service.impl.ServiceImpl}。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>仅做继承桥接，实现全部由对应版本的上游 {@code ServiceImpl} 提供</li>
 *   <li><b>本类只落在 {@code src/main/java-jdk8}</b>：另一侧（MP &ge; 3.5.17）有同全限定名的一份，
 *   落在 {@code src/main/java-latest}，两档位各追加自己那一侧——两个目录<b>永不共存</b>。</li>
 *   <li><b>为什么不在这里解析实体类型</b>：3.3.x 的 {@code ServiceImpl} 用 {@code protected entityClass}
 *   字段（3.5.16 起改为私有、并挪进 {@code AbstractRepository}），取值方式随版本漂移；
 *   而"走一遍父类泛型"的解析对四个版本都成立，故统一放在 {@code CServiceImpl#getEntityClass()}
 *   （单份、与版本无关），本桥保持零逻辑。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>无兜底：除空安全与计数由接口默认方法提供外，其余实现全部由上游提供。</p>
 *
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.3.x / 3.4.x / 3.5.16（{@code extension.service} 包名侧）。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>抽象类；MP &ge; 3.5.17 的使用方由同模块 {@code src/main/java-latest} 目录的那一份生效。</p>
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
public abstract class CMybatisPlusServiceImpl<M extends BaseMapper<T>, T>
        extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<M, T> {

}
