package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CClassUtils;
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
    public void getFirstPackage() {

        Assertions.assertEquals("java", CClassUtils.getFirstPackage(String.class));
        Assertions.assertEquals("javax", CClassUtils.getFirstPackage(DataSource.class));
        Assertions.assertEquals("jdk", CClassUtils.getFirstPackage(Exported.class));
        Assertions.assertEquals("sun", CClassUtils.getFirstPackage(Unsafe.class));

    }

    @Test
    public void isBasicClass() {

        Assertions.assertTrue(CClassUtils.isBasicClass(String.class));
        Assertions.assertTrue(CClassUtils.isBasicClass(DataSource.class));
        Assertions.assertTrue(CClassUtils.isBasicClass(Exported.class));
        Assertions.assertTrue(CClassUtils.isBasicClass(Unsafe.class));
        Assertions.assertTrue(CClassUtils.isBasicClass(TypeResolver.class));
        Assertions.assertTrue(CClassUtils.isBasicClass(Sdp.class));

    }

}
