package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.util.CEntityUtils;
import com.c332030.ctool4j.definition.entity.base.*;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CEntityUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「各类实体清空 / 继承查找」两个维度组织，重点验证按继承距离查找"最近"清除方法的语义。</li>
 *   <li>各类实体：CBaseEntity、CBaseTimeEntity、CLongId 的公共字段清空，含 Object 重载与类型化重载两入口。</li>
 *   <li>无公共字段对象：clear(new Object()) 不抛异常。</li>
 *   <li>继承查找：XBaseTimeEntity 走父类链；XChild 验证接口优先（子类接口 ICCreateUpdateBy 优先于</li>
 *   <li>父类接口 ICCreateUpdateTime）；XByAndTime 验证深层接口级联清空。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对按继承距离查找"最近" clear、级联清空的约定。</li>
 *   <li>依据测试方法（分支覆盖/继承层次覆盖）：父类链、接口优先级、深层接口级联、无匹配空函数。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：CBaseEntity/CBaseTimeEntity/CLongId 各重载清空（Object 与类型化两入口）；无公共字段对象</li>
 *   <li>不抛异常；继承子类命中父类清除；子类接口优先于父类接口；深层接口级联清空。</li>
 *   <li>未覆盖：{@code CCreateUpdateByAndTime} 以外其余单一接口重载（ICCreateTime 等）的独立调用（被级联路径</li>
 *   <li>覆盖）；具体断言字段较多但均已覆盖。</li>
 * </ul>
 * <h2>各类实体清空</h2>
 * <ul>
 *   <li>1.1 CBaseEntity：全公共字段清空（id/by/time），Object 与类型化两入口（clearCBaseEntity）</li>
 *   <li>1.2 CBaseTimeEntity：id/createTime/updateTime 清空，两入口（clearBaseTimeEntity）</li>
 *   <li>1.3 CLongId：id 清空，两入口（clearLongId）</li>
 *   <li>1.4 无公共字段对象：clear(new Object()) 不抛异常（clearNone）</li>
 * </ul>
 * <h2>继承距离查找</h2>
 * <ul>
 *   <li>2.1 继承子类：XBaseTimeEntity 命中父类 CBaseTimeEntity 清除（clearSubClass）</li>
 *   <li>2.2 接口优先：XChild 命中子类接口 ICCreateUpdateBy（清 by）、未命中父类接口 ICCreateUpdateTime</li>
 *   <li>（time 保留）（clearChildInterfaceFirst）</li>
 *   <li>2.3 深层接口级联：XByAndTime 命中 ICCreateUpdateByAndTime，by 与 time 全清（clearByAndTimeInterface）</li>
 * </ul>
 *
 * @author c332030
 * @since 2025/12/20
 * @version 1.0
 */
public class CEntityUtilsTests {

    /**
     * 测试清空 CBaseEntity 的公共字段
     * 对应测试用例 1.1：全公共字段清空（id/by/time），Object 与类型化两入口
     */
    @Test
    public void clearCBaseEntity() {

        val entity = CBaseEntity.builder()
                .id(1L)
                .createById(33L)
                .createBy("c332030")
                .createTime(new Date())
                .updateById(44L)
                .updateBy("cc332030")
                .updateTime(new Date())
                .build();
        val entity2 = CBeanUtils.copy(entity, CBaseEntity.class);

        CEntityUtils.clear((Object) entity);
        Assertions.assertNull(entity.getId());
        Assertions.assertNull(entity.getCreateById());
        Assertions.assertNull(entity.getCreateBy());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateById());
        Assertions.assertNull(entity.getUpdateBy());
        Assertions.assertNull(entity.getUpdateTime());

        CEntityUtils.clear(entity2);
        Assertions.assertNull(entity2.getId());
        Assertions.assertNull(entity2.getCreateById());
        Assertions.assertNull(entity2.getCreateBy());
        Assertions.assertNull(entity2.getCreateTime());
        Assertions.assertNull(entity2.getUpdateById());
        Assertions.assertNull(entity2.getUpdateBy());
        Assertions.assertNull(entity2.getUpdateTime());

    }

    /**
     * 测试清空 CBaseTimeEntity 的公共字段
     * 对应测试用例 1.2：id/createTime/updateTime 清空，两入口
     */
    @Test
    public void clearBaseTimeEntity() {

        val entity = CBaseTimeEntity.builder()
                .id(1L)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        val entity2 = CBeanUtils.copy(entity, CBaseTimeEntity.class);

        CEntityUtils.clear((Object) entity);
        Assertions.assertNull(entity.getId());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateTime());

        CEntityUtils.clear(entity2);
        Assertions.assertNull(entity2.getId());
        Assertions.assertNull(entity2.getCreateTime());
        Assertions.assertNull(entity2.getUpdateTime());

    }

    /**
     * 测试清空 CLongId 的 id 字段
     * 对应测试用例 1.3：id 清空，两入口
     */
    @Test
    public void clearLongId() {

        val entity = CLongId.builder()
                .id(1L)
                .build();
        val entity2 = CBeanUtils.copy(entity, CLongId.class);

        CEntityUtils.clear((Object) entity);
        Assertions.assertNull(entity.getId());

        CEntityUtils.clear(entity2);
        Assertions.assertNull(entity2.getId());

    }

    /**
     * 测试清空无公共字段的普通对象
     * 对应测试用例 1.4：无公共字段对象：clear(new Object()) 不抛异常
     */
    @Test
    public void clearNone() {

        CEntityUtils.clear(new Object());

    }

    /**
     * 测试无自身 clear 的子类，通过遍历父类/父接口链命中最近的清除方法
     * <p>验证按继承距离查找"最近" clear，而非依赖方法声明顺序</p>
     * 对应测试用例 2.1：继承子类：XBaseTimeEntity 命中父类 CBaseTimeEntity 清除
     */
    @Test
    public void clearSubClass() {

        val entity = new XBaseTimeEntity();
        entity.setId(1L);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());

        // 走 Object 重载，运行时动态查找最近的 clear
        CEntityUtils.clear((Object) entity);

        Assertions.assertNull(entity.getId());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateTime());

    }

    /**
     * 测试接口按继承顺序查找"最近" clear：子类实现接口优先于父类实现接口
     * <p>XChild 类链（XChild、XParent）均无类级 clear，只能走接口；getInterfaces 按类继承
     * 由近及远取直接接口（子类接口在前），应命中子类接口 {@code ICCreateUpdateBy}（只清 by），
     * 而非父类接口 {@code ICCreateUpdateTime}（清 time）</p>
     * 对应测试用例 2.2：接口优先：XChild 命中子类接口 ICCreateUpdateBy（清 by）、未命中父类接口 ICCreateUpdateTime
     */
    @Test
    public void clearChildInterfaceFirst() {

        val entity = new XChild();
        entity.setCreateById(1L);
        entity.setCreateBy("c332030");
        entity.setUpdateById(2L);
        entity.setUpdateBy("cc332030");
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());

        CEntityUtils.clear((Object) entity);

        // 命中子类接口 ICCreateUpdateBy：by 字段全清
        Assertions.assertNull(entity.getCreateById());
        Assertions.assertNull(entity.getCreateBy());
        Assertions.assertNull(entity.getUpdateById());
        Assertions.assertNull(entity.getUpdateBy());
        // 未命中父类接口 ICCreateUpdateTime：time 字段保留
        Assertions.assertNotNull(entity.getCreateTime());
        Assertions.assertNotNull(entity.getUpdateTime());

    }

    /**
     * 测试直接实现深层接口的类命中该接口的 clear，清空其全部字段
     * <p>XByAndTime 类链仅自身、无类级 clear，走接口命中 {@code ICCreateUpdateByAndTime}，
     * 该 clear 级联清 by 与 time 全字段</p>
     * 对应测试用例 2.3：深层接口级联：XByAndTime 命中 ICCreateUpdateByAndTime，by 与 time 全清
     */
    @Test
    public void clearByAndTimeInterface() {

        val entity = new XByAndTime();
        entity.setCreateById(1L);
        entity.setCreateBy("c332030");
        entity.setUpdateById(2L);
        entity.setUpdateBy("cc332030");
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());

        CEntityUtils.clear((Object) entity);

        Assertions.assertNull(entity.getCreateById());
        Assertions.assertNull(entity.getCreateBy());
        Assertions.assertNull(entity.getUpdateById());
        Assertions.assertNull(entity.getUpdateBy());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateTime());

    }

    /**
     * 仅用于测试的继承子类（无自身 clear 方法）
     */
    private static class XBaseTimeEntity extends CBaseTimeEntity<Long> {
    }

    /**
     * 仅用于测试的父类：实现 ICCreateUpdateTime（有 clear，清 time）
     */
    @Data
    private static class XParent implements ICCreateUpdateTime {

        private Date createTime;
        private Date updateTime;

    }

    /**
     * 仅用于测试的子类：继承 XParent 并实现 ICCreateUpdateBy（有 clear，清 by）
     */
    @Data
    private static class XChild extends XParent implements ICCreateUpdateBy {

        private Long createById;
        private String createBy;
        private Long updateById;
        private String updateBy;

    }

    /**
     * 仅用于测试的类：直接实现 ICCreateUpdateByAndTime（有 clear，清 by 与 time）
     */
    @Data
    private static class XByAndTime implements ICCreateUpdateByAndTime {

        private Long createById;
        private String createBy;
        private Long updateById;
        private String updateBy;
        private Date createTime;
        private Date updateTime;

    }

}
