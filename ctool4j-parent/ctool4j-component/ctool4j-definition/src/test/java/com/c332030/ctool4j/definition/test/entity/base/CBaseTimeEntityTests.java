package com.c332030.ctool4j.definition.test.entity.base;

import com.c332030.ctool4j.definition.entity.base.CBaseTimeEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * <p>
 * Description: CBaseTimeEntityTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「构造 / 设置取值 / toString / equals/hashCode / builder」多个维度组织，验证 Lombok @Data/@SuperBuilder 行为。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 @Data/@SuperBuilder 生成方法的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：无参构造；setter/getter；toString null 安全与有值；equals/hashCode；同引用相等；builder。</li>
 *   <li>未覆盖：无。</li>
 * </ul>
 * <h2>构造与取值</h2>
 * <ul>
 *   <li>1.1 无参构造：字段为 null（noArgsConstructor）</li>
 *   <li>1.2 setter/getter：设置与读取（setterAndGetter）</li>
 * </ul>
 * <h2>toString</h2>
 * <ul>
 *   <li>2.1 null 安全（toStringNullSafe）</li>
 *   <li>2.2 有值含字段（toStringWithValues）</li>
 * </ul>
 * <h2>equals/hashCode</h2>
 * <ul>
 *   <li>3.1 同值相等（equalsAndHashCode）</li>
 *   <li>3.2 同引用相等（equalsSameReference）</li>
 * </ul>
 * <h2>builder</h2>
 * <ul>
 *   <li>4.1 builder 构造（builder）</li>
 * </ul>
 *
 * @since 2026/8/16
 * @version 1.0
 */
public class CBaseTimeEntityTests {

    /**
     * 对应测试用例 1.1：无参构造：字段为 null
     */
    @Test
    public void noArgsConstructor() {

        CBaseTimeEntity<Long> entity = new CBaseTimeEntity<>();

        Assertions.assertNull(entity.getId());
        Assertions.assertNull(entity.getCreateTime());
        Assertions.assertNull(entity.getUpdateTime());

    }

    /**
     * 对应测试用例 1.2：setter/getter：设置与读取
     */
    @Test
    public void setterAndGetter() {

        CBaseTimeEntity<Long> entity = new CBaseTimeEntity<>();

        entity.setId(1L);
        Date createTime = new Date();
        Date updateTime = new Date();
        entity.setCreateTime(createTime);
        entity.setUpdateTime(updateTime);

        Assertions.assertEquals(Long.valueOf(1L), entity.getId());
        Assertions.assertEquals(createTime, entity.getCreateTime());
        Assertions.assertEquals(updateTime, entity.getUpdateTime());

    }

    /**
     * 对应测试用例 2.1：null 安全
     */
    @Test
    public void toStringNullSafe() {

        CBaseTimeEntity<Long> entity = new CBaseTimeEntity<>();

        String str = entity.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CBaseTimeEntity"));
        Assertions.assertTrue(str.contains("id=null"));
        Assertions.assertTrue(str.contains("createTime=null"));
        Assertions.assertTrue(str.contains("updateTime=null"));

    }

    /**
     * 对应测试用例 2.2：有值含字段
     */
    @Test
    public void toStringWithValues() {

        CBaseTimeEntity<Long> entity = new CBaseTimeEntity<>();
        entity.setId(1L);
        entity.setCreateTime(new Date(0));
        entity.setUpdateTime(new Date(0));

        String str = entity.toString();

        Assertions.assertTrue(str.contains("id=1"));
        Assertions.assertTrue(str.contains("createTime=Thu Jan 01 08:00:00 CST 1970"));
        Assertions.assertTrue(str.contains("updateTime=Thu Jan 01 08:00:00 CST 1970"));

    }

    /**
     * 对应测试用例 3.1：同值相等
     */
    @Test
    public void equalsAndHashCode() {

        CBaseTimeEntity<Long> a = new CBaseTimeEntity<>();
        a.setId(1L);
        a.setCreateTime(new Date(0));
        a.setUpdateTime(new Date(0));

        CBaseTimeEntity<Long> b = new CBaseTimeEntity<>();
        b.setId(1L);
        b.setCreateTime(new Date(0));
        b.setUpdateTime(new Date(0));

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CBaseTimeEntity<Long> c = new CBaseTimeEntity<>();
        c.setId(2L);

        Assertions.assertNotEquals(a, c);

    }

    /**
     * 对应测试用例 3.2：同引用相等
     */
    @Test
    public void equalsSameReference() {

        CBaseTimeEntity<Long> a = new CBaseTimeEntity<>();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1：builder 构造
     */
    @Test
    public void builder() {

        CBaseTimeEntity<Long> entity = CBaseTimeEntity.<Long>builder()
            .id(5L)
            .createTime(new Date(0))
            .updateTime(new Date(0))
            .build();

        Assertions.assertEquals(Long.valueOf(5L), entity.getId());
        Assertions.assertEquals(new Date(0), entity.getCreateTime());
        Assertions.assertEquals(new Date(0), entity.getUpdateTime());

    }

}
