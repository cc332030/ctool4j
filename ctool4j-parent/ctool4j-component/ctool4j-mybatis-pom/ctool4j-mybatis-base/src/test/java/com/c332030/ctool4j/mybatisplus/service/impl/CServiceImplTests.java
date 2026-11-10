package com.c332030.ctool4j.mybatisplus.service.impl;

import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Description: CServiceImplTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>不启动 Spring、不连库：{@link CServiceImpl} 的实体类型解析与三个消歧方法的空安全分支
 *   都可以在纯 POJO 实例上验证，从而让本用例在 jdk8（MP 3.5.16）与最新 LTS（MP 3.5.17）
 *   两档都能跑，也就同时覆盖了两侧适配桥。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据 {@code doc/design/mybatis/version-adapter.adoc}「测试设计」：服务基类用例覆盖
 *   {@code getEntityClass()} 与三个消歧方法。</li>
 *   <li>依据测试方法（等价类/边界）：泛型实参为具体类 / 原始类型、主键为 null / 集合为 null 与空。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：{@code getEntityClass()} 解析具体实体、结果按实例缓存、原始类型继承时报错；
 *   {@code getById(null)}、{@code getOptById(null)}、{@code listByIds(null|空)} 的空安全短路。</li>
 *   <li>未覆盖：真实 SQL 执行与 Mapper 代理（需数据源，属各版本模块的装配用例）。</li>
 * </ul>
 * <h2>实体类型解析</h2>
 * <ul>
 *   <li>1.1 解析具体实体（{@code getEntityClass}）</li>
 *   <li>1.2 结果按实例缓存（{@code getEntityClassCached}）</li>
 *   <li>1.3 中间层用类型变量承接后再落地（{@code getEntityClassThroughGenericMiddle}）</li>
 *   <li>1.4 继承链未给出具体实体时报错（{@code getEntityClassUnresolved}）</li>
 * </ul>
 * <h2>空安全</h2>
 * <ul>
 *   <li>2.1 空主键查单个（{@code getByIdNull}）</li>
 *   <li>2.2 空主键查 Optional（{@code getOptByIdNull}）</li>
 *   <li>2.3 空集合按主键批量查（{@code listByIdsNull}）</li>
 *   <li>2.4 空列表按主键批量查（{@code listByIdsEmpty}）</li>
 * </ul>
 *
 * @since 2026/11/10
 * @version 1.0
 * @see CServiceImpl
 */
public class CServiceImplTests {

    /** 测试用 Mapper（仅占位，实例化不触发代理生成） */
    interface CTestMapper extends CBaseMapper<CTestEntity> {

    }

    /** 测试用实体 */
    static class CTestEntity {

    }

    /** 直接以具体实体继承 */
    static class CTestService extends CServiceImpl<CTestMapper, CTestEntity> {

    }

    /** 中间层以类型变量承接，具体实体由末级子类给出 */
    abstract static class CTestMiddleService<M extends CBaseMapper<T>, T> extends CServiceImpl<M, T> {

    }

    /** 末级子类落地具体实体 */
    static class CTestMiddleServiceImpl extends CTestMiddleService<CTestMapper, CTestEntity> {

    }

    /** 测试用泛型 Mapper（供"未给出具体实体"的用例使用） */
    interface CTestGenericMapper<T> extends CBaseMapper<T> {

    }

    /**
     * 继承链上没有任何具体实参：末级子类仍以类型变量承接实体类型
     */
    static class CTestUnresolvedService<T> extends CServiceImpl<CTestGenericMapper<T>, T> {

    }

    /**
     * 对应测试用例 1.1：解析具体实体（{@code getEntityClass}）
     */
    @Test
    public void getEntityClass() {
        Assertions.assertEquals(CTestEntity.class, new CTestService().getEntityClass());
    }

    /**
     * 对应测试用例 1.2：结果按实例缓存（{@code getEntityClassCached}）
     */
    @Test
    public void getEntityClassCached() {
        CTestService service = new CTestService();
        Assertions.assertSame(service.getEntityClass(), service.getEntityClass());
    }

    /**
     * 对应测试用例 1.3：中间层用类型变量承接后再落地（{@code getEntityClassThroughGenericMiddle}）
     */
    @Test
    public void getEntityClassThroughGenericMiddle() {
        Assertions.assertEquals(CTestEntity.class, new CTestMiddleServiceImpl().getEntityClass());
    }

    /**
     * 对应测试用例 1.4：继承链未给出具体实体时报错（{@code getEntityClassUnresolved}）
     */
    @Test
    public void getEntityClassUnresolved() {
        Assertions.assertThrows(IllegalStateException.class, () -> new CTestUnresolvedService<Object>().getEntityClass());
    }

    /**
     * 对应测试用例 2.1：空主键查单个（{@code getByIdNull}）
     */
    @Test
    public void getByIdNull() {
        Serializable id = null;

        Assertions.assertNull(new CTestService().getById(id));
    }

    /**
     * 对应测试用例 2.2：空主键查 Optional（{@code getOptByIdNull}）
     */
    @Test
    public void getOptByIdNull() {
        Serializable id = null;

        Assertions.assertEquals(Optional.empty(), new CTestService().getOptById(id));
    }

    /**
     * 对应测试用例 2.3：空集合按主键批量查（{@code listByIdsNull}）
     */
    @Test
    public void listByIdsNull() {
        List<CTestEntity> list = new CTestService().listByIds(null);
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.isEmpty());
    }

    /**
     * 对应测试用例 2.4：空列表按主键批量查（{@code listByIdsEmpty}）
     */
    @Test
    public void listByIdsEmpty() {
        List<CTestEntity> list = new CTestService().listByIds(Collections.emptyList());
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.isEmpty());
    }

}
