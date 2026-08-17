package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.definition.entity.base.CBaseCreateTimeEntity;
import com.c332030.ctool4j.definition.entity.base.CBaseEntity;
import com.c332030.ctool4j.definition.entity.base.CBaseTimeEntity;
import com.c332030.ctool4j.definition.entity.base.CId;
import com.c332030.ctool4j.definition.entity.base.ICCreateUpdateBy;
import com.c332030.ctool4j.definition.entity.base.ICCreateUpdateByAndTime;
import com.c332030.ctool4j.definition.entity.base.ICCreateUpdateTime;
import com.c332030.ctool4j.definition.entity.base.ICCreateTime;
import com.c332030.ctool4j.definition.entity.base.ICId;
import com.oracle.net.Sdp;
import com.sun.beans.TypeResolver;
import jdk.Exported;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CClassUtilsTests
 * </p>
 * <p>刻意使用 JDK 内部类（sun.misc.Unsafe、com.sun.beans.TypeResolver 等）验证包名判断逻辑，
 * 内部专用 API 警告（sun.proprietary，javac 无法用 @SuppressWarnings 抑制）已知且接受</p>
 *
 * @since 2025/12/12
 */
public class CClassUtilsTests {

    /**
     * 测试类字段对比
     */
    @Test
    public void compareField(){

        CClassUtils.compareField(CId.class, CBaseTimeEntity.class, CBaseEntity.class);

    }

    /**
     * 测试获取类所在包的首段名称
     */
    @Test
    public void getFirstPackage() {

        Assertions.assertEquals("java", CClassUtils.getFirstPackage(String.class));
        Assertions.assertEquals("javax", CClassUtils.getFirstPackage(DataSource.class));
        Assertions.assertEquals("jdk", CClassUtils.getFirstPackage(Exported.class));
        Assertions.assertEquals("sun", CClassUtils.getFirstPackage(Unsafe.class));

    }

    /**
     * 测试是否为 JDK 类
     */
    @Test
    public void isJdkClass() {

        Assertions.assertTrue(CClassUtils.isJdkClass(String.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(DataSource.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(Exported.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(Unsafe.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(TypeResolver.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(Sdp.class));

    }

    /**
     * 测试获取类及其所有父类（不含 Object）
     * <p>顺序约定：类本身在前，沿继承链由子至父，直至顶层父类（Object 除外）</p>
     */
    @Test
    public void getSuperClasses() {

        // 多层继承链：CBaseEntity -> CBaseTimeEntity -> CBaseCreateTimeEntity -> CId，顺序由子至父，不含 Object
        Assertions.assertEquals(
            Arrays.asList(CBaseEntity.class, CBaseTimeEntity.class, CBaseCreateTimeEntity.class, CId.class),
            CClassUtils.getSuperClasses(CBaseEntity.class)
        );

        // 中间层级继承链
        Assertions.assertEquals(
            Arrays.asList(CBaseTimeEntity.class, CBaseCreateTimeEntity.class, CId.class),
            CClassUtils.getSuperClasses(CBaseTimeEntity.class)
        );

        // 父类为 Object 时仅返回类本身
        Assertions.assertEquals(
            Collections.singletonList(CId.class),
            CClassUtils.getSuperClasses(CId.class)
        );

        // 边界：Object 自身（do-while 至少执行一次，返回仅含自身）
        Assertions.assertEquals(
            Collections.singletonList(Object.class),
            CClassUtils.getSuperClasses(Object.class)
        );

        // 接口：无父类（getSuperclass 为 null），仅返回接口自身
        Assertions.assertEquals(
            Collections.singletonList(ICId.class),
            CClassUtils.getSuperClasses(ICId.class)
        );

    }

    /**
     * 测试获取类及其父类实现的所有接口
     * <p>顺序约定：按父类链由子至父遍历，每层按 getInterfaces 声明顺序，
     * LinkedHashSet 去重保序；只取各类直接实现的接口，不递归接口继承
     * （如 ICCreateUpdateBy 是 ICCreateUpdateByAndTime 的父接口，不会出现）</p>
     */
    @Test
    public void getInterfaces() {

        // 各层直接接口按父类链由子至父顺序：CBaseEntity->ICCreateUpdateByAndTime、
        // CBaseTimeEntity->ICCreateUpdateTime、CBaseCreateTimeEntity->ICCreateTime、CId->ICId
        val interfaces = CClassUtils.getInterfaces(CBaseEntity.class);
        Assertions.assertEquals(
            Arrays.asList(
                ICCreateUpdateByAndTime.class,
                ICCreateUpdateTime.class,
                ICCreateTime.class,
                ICId.class
            ),
            interfaces.stream().collect(Collectors.toList())
        );

        // 接口继承不递归：ICCreateUpdateBy 仅作为 ICCreateUpdateByAndTime 的父接口存在，不应被收集
        Assertions.assertFalse(interfaces.contains(ICCreateUpdateBy.class));

        // 单层实现
        Assertions.assertEquals(
            Collections.singleton(ICId.class),
            CClassUtils.getInterfaces(CId.class)
        );

        // 无接口：Object 自身、接口本身均返回空集合
        Assertions.assertTrue(CClassUtils.getInterfaces(Object.class).isEmpty());
        Assertions.assertTrue(CClassUtils.getInterfaces(ICId.class).isEmpty());

    }

}
