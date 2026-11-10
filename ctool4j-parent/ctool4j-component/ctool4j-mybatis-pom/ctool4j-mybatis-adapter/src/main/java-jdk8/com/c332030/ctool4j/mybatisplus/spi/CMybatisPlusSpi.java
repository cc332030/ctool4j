package com.c332030.ctool4j.mybatisplus.spi;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.c332030.ctool4j.core.classes.CMethodHandleUtils;
import com.c332030.ctool4j.core.util.CList;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: CMybatisPlusSpi（mybatis-plus &lt; 3.5.17 侧自适应层）
 * </p>
 *
 * <p>
 * <b>自适应中间层</b>：以中性全限定名承接"MP 包名为 {@code extension.service}"一侧的 service 坐标
 * {@code com.baomidou.mybatisplus.extension.service.IService}，
 * 并把本仓库统一要求的<b>空安全语义</b>（空主键 / 空集合入参折成空结果）与
 * <b>跨版本返回值差异</b>（{@link #cCount(LambdaQueryChainWrapper)}）收敛在本层。
 * 上层公共契约（{@code ICCheckService} → {@code ICService} 一族）因此可以
 * <b>完全不引用任何版本侧类型</b>。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code MP &lt; 3.5.17 service 全量面}（继承自上游 {@code extension.service.IService}）+
 * {@code 空安全元素级实现}：{@link #getById(Serializable)}、{@link #getOptById(Serializable)}、
 * {@link #listByIds(Collection)} + {@code 跨版本计数}：{@link #cCount(LambdaQueryChainWrapper)}。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>本接口是"自适应层"的实体</b>：{@code IService → CMybatisPlusSpi → MP IService}。
 *   上游包名换成 {@code spring.service} 时（MP &ge; 3.5.17），只需换掉本层的继承目标
 *   与 {@code super} 限定名，上层一字不改。</li>
 *   <li><b>为什么空安全语义放在本层而不是上层</b>：上层公共面要"版本无关"就不能出现
 *   {@code X.super.m()}（{@code X} 是版本侧类型）；而空安全实现必须调用上游 {@code super}，
 *   故它天然属于本层。</li>
 *   <li><b>为什么 {@link #cCount(LambdaQueryChainWrapper)} 走方法句柄而不是直接调
 *   {@code ChainQuery#count()}</b>：该方法 3.3.x 返回 {@code Integer}、3.4.x 起返回 {@code Long}，
 *   <b>描述符不兼容</b>。本模块的 jdk8 档产物是在 3.5.16 上编译的（描述符为 {@code ()Long}），
 *   若直接调用，在 3.3.x 上运行会抛 {@code NoSuchMethodError}；而一份产物要同时服务
 *   3.3.x / 3.4.x / 3.5.16。方法句柄在运行期按实际返回类型取值、再统一按 {@link Number} 收口，
 *   因此对三个版本都成立。</li>
 *   <li><b>本接口只落在 {@code src/main/java-jdk8}</b>：另一侧（MP &ge; 3.5.17）有同全限定名的一份，
 *   落在 {@code src/main/java}，jdk8 档位由本模块 {@code build.gradle.kts} 整体替换主源集
 *   ——两个目录**永不共存**。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <p>空主键 / 空集合即空结果，全部短路、不触发底层查询：
 * {@code getById(null)} → {@code null}、{@code getOptById(null)} → {@code Optional.empty()}、
 * {@code listByIds(空集合)} → 空列表；{@code cCount(null)} → {@code 0}。</p>
 *
 * <h2>适用范围</h2>
 * <p>mybatis-plus 3.3.x / 3.4.x / 3.5.16（{@code extension.service} 包名侧）。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <p>接口；MP &ge; 3.5.17 的使用方由同模块 {@code src/main/java} 目录的那一份生效。</p>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>与另一侧同名同包：两侧永不同时挂载，故不会同时出现在同一 classpath。</li>
 *   <li>空安全覆写与 MP 上游 default 语义不同（上游 {@code getById} 对 null 主键可能走一次查询
 *   或抛异常）；本仓库以"空主键即空结果"为准。</li>
 *   <li>{@code cCount} 用方法句柄取 {@code count()}，比直接调用多一次查表（句柄本身由
 *   {@code CMethodHandleUtils} 按 {@code Method} 缓存）；与一次 SQL 查询相比可忽略。</li>
 * </ul>
 *
 * @since 2026/9/27
 * @version 2.1
 */
public interface CMybatisPlusSpi<ENTITY>
        extends com.baomidou.mybatisplus.extension.service.IService<ENTITY> {

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
        return com.baomidou.mybatisplus.extension.service.IService.super.getById(id);
    }

    /**
     * 根据 ID 查询（ID 为空时返回 Optional.empty）
     *
     * <p>3.3.x / 3.4.x 的上游无此方法，由本自适应层按 3.5.x 语义补齐——
     * 上层公共契约（{@code ICCheckService}）因此两侧 API 面一致。</p>
     *
     * @param id 主键
     * @return 实体 Optional
     */
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
        return com.baomidou.mybatisplus.extension.service.IService.super.listByIds(idList);
    }

    /**
     * 统计链式查询的命中行数（链式查询为空时返回 0）
     *
     * <p>吸收 {@code ChainQuery#count()} 的返回值漂移：3.3.x 返回 {@code Integer}、3.4.x 起返回
     * {@code Long}，两者描述符不兼容。这里用方法句柄取运行期的实际返回值、再按 {@link Number}
     * 统一转 {@code long}，故同一份产物对三个版本都成立。</p>
     *
     * @param chainQuery 链式查询
     * @return 命中行数
     */
    default long cCount(LambdaQueryChainWrapper<ENTITY> chainQuery) {

        if (null == chainQuery) {
            return 0L;
        }

        try {
            return ((Number) CMethodHandleUtils.getHandle(chainQuery.getClass().getMethod("count"))
                    .invoke(chainQuery))
                    .longValue();
        } catch(Throwable e) {
            throw new IllegalStateException("count chain query error", e);
        }
    }

}
