package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.definition.entity.base.CBaseEntity;
import com.c332030.ctool4j.definition.entity.base.CBaseTimeEntity;
import com.c332030.ctool4j.definition.entity.base.CId;
import com.oracle.net.Sdp;
import com.sun.beans.TypeResolver;
import jdk.Exported;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import javax.sql.DataSource;

/**
 * <p>
 * Description: CClassUtilsTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CClassUtilsTests {

    @Test
    public void compareField(){

        CClassUtils.compareField(CId.class, CBaseTimeEntity.class, CBaseEntity.class);

    }

    @Test
    public void getFirstPackage() {

        Assertions.assertEquals("java", CClassUtils.getFirstPackage(String.class));
        Assertions.assertEquals("javax", CClassUtils.getFirstPackage(DataSource.class));
        Assertions.assertEquals("jdk", CClassUtils.getFirstPackage(Exported.class));
        Assertions.assertEquals("sun", CClassUtils.getFirstPackage(Unsafe.class));

    }

    @Test
    public void isJdkClass() {

        Assertions.assertTrue(CClassUtils.isJdkClass(String.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(DataSource.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(Exported.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(Unsafe.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(TypeResolver.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(Sdp.class));

    }

}
