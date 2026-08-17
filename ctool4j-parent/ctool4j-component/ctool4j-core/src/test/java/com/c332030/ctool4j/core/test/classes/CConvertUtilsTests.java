package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CConvertUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: CConvertUtilsTests
 * </p>
 *
 * @since 2025/12/12
 */
public class CConvertUtilsTests {

    @Test
    public void convert_toInt() {

        Assertions.assertEquals(123, CConvertUtils.convert("123", Integer.class));

    }

    @Test
    public void convert_toLong() {

        Assertions.assertEquals(123L, CConvertUtils.convert("123", Long.class));

    }

    @Test
    public void convert_toDouble() {

        Assertions.assertEquals(1.5d, CConvertUtils.convert("1.5", Double.class));

    }

    @Test
    public void convert_toString() {

        Assertions.assertEquals("123", CConvertUtils.convert(123, String.class));

    }

    @Test
    public void convert_sameType() {

        Assertions.assertEquals("abc", CConvertUtils.convert("abc", String.class));

    }

    @Test
    public void convert_null() {

        Assertions.assertNull(CConvertUtils.convert(null, String.class));

    }

    @Test
    public void convert_collectionFrom() {

        List<String> list = new ArrayList<>();
        list.add("a");
        Assertions.assertNull(CConvertUtils.convert(list, String.class));

    }

    @Test
    public void convert_mapFrom() {

        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        Assertions.assertNull(CConvertUtils.convert(map, String.class));

    }

    @Test
    public void convert_arrayFrom() {

        String[] array = new String[] {"a"};
        Assertions.assertNull(CConvertUtils.convert(array, String.class));

    }

    @Test
    public void convert_enum() {

        Assertions.assertEquals("INSERT", CConvertUtils.convert(EnumBean.INSERT, String.class));

    }

    @Test
    public void convertOpt() {

        Assertions.assertTrue(CConvertUtils.convertOpt("123", Integer.class).isPresent());
        Assertions.assertFalse(CConvertUtils.convertOpt(new ArrayList<>(), String.class).isPresent());
        Assertions.assertFalse(CConvertUtils.convertOpt(null, String.class).isPresent());

    }

    /**
     * 测试注册无参方法为转换器：不抛异常并跳过注册（Q16）
     */
    @Test
    public void addConverterNoArgMethod() throws NoSuchMethodException {

        val method = NoArgConverterBean.class.getDeclaredMethod("noArg");
        Assertions.assertDoesNotThrow(() -> CConvertUtils.addConverter(method));

    }

    /**
     * 测试用枚举，实现 toString 语义（此处仅用于验证 Collection 之外的转换回退行为）
     */
    enum EnumBean {
        INSERT
    }

    /**
     * 无参方法 Bean（验证 addConverter 跳过注册）
     */
    static class NoArgConverterBean {

        public String noArg() {
            return "no-arg";
        }

    }

}
