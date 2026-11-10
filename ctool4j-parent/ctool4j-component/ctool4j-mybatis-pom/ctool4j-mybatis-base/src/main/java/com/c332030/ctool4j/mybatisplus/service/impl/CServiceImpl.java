package com.c332030.ctool4j.mybatisplus.service.impl;

import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;
import com.c332030.ctool4j.mybatisplus.service.ICService;
import com.c332030.ctool4j.mybatisplus.spi.CMybatisPlusServiceImpl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: CServiceImpl
 * </p>
 *
 * <p>
 * service 实现基类：继承版本侧的实现桥 {@link CMybatisPlusServiceImpl}、实现业务侧契约
 * {@link ICService}，并把两侧同名的元素级方法显式消歧到业务侧契约，同时以**版本无关**的方式
 * 解析实体类型。
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code 分页/查询/业务 ID 公共实现}：由 {@link ICService} 的 {@code default} 提供</li>
 *   <li>{@code 元素级消歧}：{@link #getById(Serializable)}、{@link #getOptById(Serializable)}、
 *   {@link #listByIds(Collection)}</li>
 *   <li>{@code 实体类型解析}：{@link #getEntityClass()}</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>为什么必须在这里消歧</b>：{@link CMybatisPlusServiceImpl}（经 MP 自己的 {@code IService}）
 *   与 {@link ICService}（经 {@code ICCheckService} → {@code spi/CMybatisPlusSpi}）各自带一份同名
 *   {@code default}，两边血统不同、Java 无法自动选择，不显式转交就编译不过
 *   （"inherits unrelated defaults"）。转交 {@link ICService} 侧即取得本仓库统一的空安全语义。</li>
 *   <li><b>为什么 {@link #getEntityClass()} 自己走泛型、不读上游字段</b>：{@code entityClass}
 *   的可见性与位置随版本漂移——3.3.x / 3.4.x 是 {@code ServiceImpl} 的 {@code protected} 字段、
 *   3.5.16 起改为私有并移入 {@code AbstractRepository}，3.3.x 更没有 {@code getEntityClass()}、
 *   3.4.x 只有 {@code currentModelClass()}（3.5.16 起已删）。"沿父类找第二个泛型实参"对四个版本都成立，
 *   故只写一份。</li>
 *   <li><b>本类全仓只有一份、位于 base 的单源目录</b>：它承的是"版本侧 {@code CMybatisPlusServiceImpl}"
 *   ＋"公共 {@code ICService}"的交叉点，而版本差异已在 base 的 {@code spi} 档位源目录
 *   （原独立适配模块 {@code ctool4j-mybatis-adapter}，已并回）收敛为同全限定名的两份、按 JDK 档位二选一，
 *   故此处不再需要按版本复制。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li><b>泛型解析失败即报错</b>：子类若以类型变量继承（如 {@code abstract class X<M, T> extends
 *   CServiceImpl<M, T>}）且未给出具体实体类型，解析不到时抛 {@link IllegalStateException}，
 *   与 MP 自身解析失败的行为一致——不静默返回 {@code null}。</li>
 *   <li><b>三个消歧方法全部转交 {@link ICService}</b>：空主键即 {@code null} /
 *   {@code Optional.empty()}、空集合即空列表，全部短路。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <p>服务基类：本仓库全部 service 实现类的父级（{@code extends CServiceImpl<XxxMapper, Xxx>}）。</p>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>抽象类，需子类提供具体 Mapper 与实体类型。</li>
 *   <li>不支持"中间层再用类型变量承接、到具体子类才落地"的多跳解析（与 MP 自身限制相同）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>三个消歧方法必须手写、不能省略——它们使"实现类继承实现类或实现接口、接口只继承接口"的
 *   继承方向得以保持（本类为 {@code C}，其父 {@link CMybatisPlusServiceImpl} 为 {@code C}，
 *   实现的 {@link ICService} 为 {@code IC}）。</li>
 *   <li>{@link #getEntityClass()} 结果按实例缓存（字段 {@code cEntityClass}）：并发首次调用可能各算一次，
 *   结果相同，不做同步。</li>
 * </ul>
 *
 * @since 2025/11/27
 * @version 2.0
 */
public abstract class CServiceImpl<M extends CBaseMapper<T>, T>
        extends CMybatisPlusServiceImpl<M, T>
        implements ICService<T> {

    /**
     * 实体类型缓存（不叫 {@code entityClass}：3.3.x / 3.4.x 的上游 {@code ServiceImpl} 有同名
     * {@code protected} 字段，同名会把它遮蔽掉，读代码时极易误解）
     */
    private Class<T> cEntityClass;

    /**
     * 获取实体类类型
     *
     * @return 实体类
     */
    @Override
    public Class<T> getEntityClass() {

        if(null == cEntityClass) {
            cEntityClass = resolveEntityClass(getClass());
        }
        return cEntityClass;
    }

    @Override
    public T getById(Serializable id) {
        return ICService.super.getById(id);
    }

    @Override
    public Optional<T> getOptById(Serializable id) {
        return ICService.super.getOptById(id);
    }

    @Override
    public List<T> listByIds(Collection<? extends Serializable> idList) {
        return ICService.super.listByIds(idList);
    }

    /**
     * 沿父类链解析第二个泛型实参（{@code CServiceImpl<M, T>} 的 {@code T}）
     *
     * @param clazz 具体实现类
     * @param <T>   实体类型
     * @return 实体类型
     * @throws IllegalStateException 父类链上找不到具体实体类型时
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> resolveEntityClass(Class<?> clazz) {

        Class<?> current = clazz;
        while(null != current && Object.class != current) {

            Type genericSuperclass = current.getGenericSuperclass();
            if(genericSuperclass instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
                if(actualTypeArguments.length > 1 && actualTypeArguments[1] instanceof Class) {
                    return (Class<T>) actualTypeArguments[1];
                }
            }
            current = current.getSuperclass();
        }

        throw new IllegalStateException("cannot resolve entity class of " + clazz.getName()
                + ": subclass must extend CServiceImpl with a concrete entity type");
    }

}
