package com.c332030.ctool4j.mybatisplus.spi;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.c332030.ctool4j.core.util.CList;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: CMybatisPlusSpi（mybatis-plus &ge; 3.5.17 侧自适应层）
 * </p>
 *
 * <p>
 * <b>自适应中间层</b>：以中性全限定名承接"MP 包名为 {@code spring.service}"一侧的 service 坐标
 * {@code com.baomidou.mybatisplus.spring.service.IService}，
 * 并把本仓库统一要求的<b>空安全语义</b>（空主键 / 空集合入参折成空结果）与
 * <b>计数返回值收口</b>（{@link #cCount(LambdaQueryChainWrapper)}）收敛在本层。
 * 上层公共契约（{@code ICCheckService} → {@code ICService} 一族）因此可以
 * <b>完全不引用任何版本侧类型</b>。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code MP &ge; 3.5.17 service 全量面}（继承自上游 {@code spring.service.IService}）+
 * {@code 空安全元素级实现}：{@link #getById(Serializable)}、{@link #getOptById(Serializable)}、
 * {@link #listByIds(Collection)} + {@code 计数}：{@link #cCount(LambdaQueryChainWrapper)}。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>本接口是"自适应层"的实体</b>：{@code IService → CMybatisPlusSpi → MP IService}。
 *   上游包名换成 {@code extension.service} 时（MP &lt; 3.5.17），只需换掉本层的继承目标
 *   与 {@code super} 限定名，上层一字不改。</li>
 *   <li><b>为什么空安全语义放在本层而不是上层</b>：上层公共面要"版本无关"就不能出现
 *   {@code X.super.m()}（{@code X} 是版本侧类型）；而空安全实现必须调用上游 {@code super}，
 *   故它天然属于本层。</li>
 *   <li><b>为什么 {@link #cCount(LambdaQueryChainWrapper)} 在本侧可以直接调
 *   {@code ChainQuery#count()}</b>：该方法自 3.4.x 起返回 {@code Long}，本侧产物在 3.5.17 上编译、
 *   也只在 3.5.17 及以后运行，描述符一致。与之相对，{@code java-jdk8} 侧要同时服务
 *   3.3.x（{@code Integer}）/ 3.4.x / 3.5.16（{@code Long}），故那一侧走方法句柄。</li>
 *   <li><b>本接口只落在 {@code src/main/java-latest}</b>：另一侧（MP &lt; 3.5.17）有同全限定名的一份，
 *   落在 {@code src/main/java-jdk8}——标准源目录互不相交、追加语义按档位各取其一，
 *   两个目录<b>永不共存</b>。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>空主键 / 空集合即空结果，全部短路、不触发底层查询：
 * {@code getById(null)} → {@code null}、{@code getOptById(null)} → {@code Optional.empty()}、
 * {@code listByIds(空集合)} → 空列表；{@code cCount(null)} → {@code 0}。</p>
 *
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.5.17 及以后（{@code spring.service} 包名侧）。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>接口；MP &lt; 3.5.17 的使用方由同模块 {@code java-jdk8} 目录的那一份生效。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与另一侧同名同包：两侧永不同时挂载，故不会同时出现在同一 classpath。</li>
 *   <li>空安全覆写与 MP 上游 default 语义不同（上游 {@code getById} 对 null 主键可能走一次查询
 *   或抛异常）；本仓库以"空主键即空结果"为准。</li>
 * </ul>
 *
 * @since 2026/9/27
 * @version 2.1
 */
public interface CMybatisPlusSpi<ENTITY>
        extends com.baomidou.mybatisplus.spring.service.IService<ENTITY> {

    /**
     * 根据 ID 查询（ID 为空时返回 null）
     *
     * @param id 主键
     * @return 实体
     */
    @Override
    default ENTITY getById(Serializable id) {
        if (null == id) {
            return null;
        }
        return com.baomidou.mybatisplus.spring.service.IService.super.getById(id);
    }

    /**
     * 根据 ID 查询（ID 为空时返回 Optional.empty）
     *
     * @param id 主键
     * @return 实体 Optional
     */
    @Override
    default Optional<ENTITY> getOptById(Serializable id) {
        if (null == id) {
            return Optional.empty();
        }
        return Optional.ofNullable(getById(id));
    }

    /**
     * 根据 ID 列表查询（列表为空时返回空列表）
     *
     * @param idList 主键列表
     * @return 实体列表
     */
    @Override
    default List<ENTITY> listByIds(Collection<? extends Serializable> idList) {
        if (CollUtil.isEmpty(idList)) {
            return CList.of();
        }
        return com.baomidou.mybatisplus.spring.service.IService.super.listByIds(idList);
    }

    /**
     * 统计链式查询的命中行数（链式查询为空时返回 0）
     *
     * @param chainQuery 链式查询
     * @return 命中行数
     */
    default long cCount(LambdaQueryChainWrapper<ENTITY> chainQuery) {
        if (null == chainQuery) {
            return 0L;
        }
        return chainQuery.count();
    }

}
