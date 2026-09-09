package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CEnumUtils;
import com.c332030.ctool4j.definition.enums.CDbOperateEnum;
import com.c332030.ctool4j.definition.enums.business.CCountryCodeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: CEnumUtilsTests
 * </p>
 *
 * @since 2026/8/14
 */
public class CEnumUtilsTests {

    /**
     * 对应测试用例 1.1
     */
    @Test
    public void getNameMap() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getNameMap(CDbOperateEnum.class);

        Assertions.assertEquals(4, map.size());
        Assertions.assertEquals(CDbOperateEnum.INSERT, map.get("INSERT"));
        Assertions.assertEquals(CDbOperateEnum.DELETE, map.get("DELETE"));

    }

    /**
     * 对应测试用例 1.2
     */
    @Test
    public void getMapByFieldName() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getMap(CDbOperateEnum.class, "text");

        Assertions.assertEquals(4, map.size());
        Assertions.assertEquals(CDbOperateEnum.INSERT, map.get("插入"));
        Assertions.assertEquals(CDbOperateEnum.SELECT, map.get("查询"));

    }

    /**
     * 对应测试用例 1.3
     */
    @Test
    public void getMapByUnknownFieldThrows() {

        Assertions.assertThrowsExactly(NoSuchFieldException.class,
                () -> CEnumUtils.getMap(CDbOperateEnum.class, "notExist"));

    }

    /**
     * 对应测试用例 1.4
     */
    @Test
    public void getMapNotEnumThrows() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> CEnumUtils.getMap(String.class, "name"));

    }

    /**
     * 对应测试用例 1.5
     */
    @Test
    public void getMapByValueInterface() {

        Map<Integer, CCountryCodeEnum> map = CEnumUtils.getMap(CCountryCodeEnum.class);

        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(CCountryCodeEnum.CHN, map.get(86));

    }

    /**
     * 对应测试用例 1.6
     */
    @Test
    public void getMapNullValueField() {

        // B 的 text 字段值为 null，构建字段值 Map 时应被过滤，不建立反查项
        Map<String, NullValueEnum> map = CEnumUtils.getMap(NullValueEnum.class, "text");

        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(NullValueEnum.A, map.get("a"));
        Assertions.assertNull(map.get("b"));

    }

    /**
     * 对应测试用例 1.7
     */
    @Test
    public void getMapByFunc() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getMap(CDbOperateEnum.class, CDbOperateEnum::getText);

        Assertions.assertEquals(4, map.size());
        Assertions.assertEquals(CDbOperateEnum.INSERT, map.get("插入"));
        Assertions.assertEquals(CDbOperateEnum.SELECT, map.get("查询"));

    }

    /**
     * 对应测试用例 2.1
     */
    @Test
    public void valueOfByMap() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getNameMap(CDbOperateEnum.class);
        Assertions.assertEquals(CDbOperateEnum.UPDATE, CEnumUtils.valueOf(map, "UPDATE"));

    }

    /**
     * 对应测试用例 2.2
     */
    @Test
    public void valueOfByMapNotFoundThrows() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getNameMap(CDbOperateEnum.class);
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> CEnumUtils.valueOf(map, "UNKNOWN"));

    }

    /**
     * 对应测试用例 2.3
     */
    @Test
    public void nameOf() {

        Assertions.assertEquals(CDbOperateEnum.INSERT, CEnumUtils.nameOf(CDbOperateEnum.class, "INSERT"));

    }

    /**
     * 对应测试用例 2.4
     */
    @Test
    public void nameOfNotFoundThrows() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> CEnumUtils.nameOf(CDbOperateEnum.class, "UNKNOWN"));

    }

    /**
     * 对应测试用例 2.5
     */
    @Test
    public void valueOfByFieldName() {

        Assertions.assertEquals(CDbOperateEnum.DELETE,
                CEnumUtils.valueOf(CDbOperateEnum.class, "text", "删除"));

    }

    /**
     * 对应测试用例 2.6
     */
    @Test
    public void valueOfByFieldNameNotFoundThrows() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> CEnumUtils.valueOf(CDbOperateEnum.class, "text", "不存在"));

    }

    /**
     * 对应测试用例 2.7
     */
    @Test
    public void valueOfByValueInterface() {

        Assertions.assertEquals(CCountryCodeEnum.CHN,
                CEnumUtils.valueOf(CCountryCodeEnum.class, 86));

    }

    /**
     * 对应测试用例 2.8
     */
    @Test
    public void valueOfByFunc() {

        Assertions.assertEquals(CDbOperateEnum.DELETE,
                CEnumUtils.valueOf(CDbOperateEnum.class, CDbOperateEnum::getText, "删除"));

    }

    /**
     * 对应测试用例 3.1
     */
    @Test
    public void values() {

        List<CDbOperateEnum> values = CEnumUtils.values(CDbOperateEnum.class);
        Assertions.assertEquals(4, values.size());
        Assertions.assertEquals(CDbOperateEnum.INSERT, values.get(0));
        Assertions.assertEquals(CDbOperateEnum.DELETE, values.get(3));

    }

    /**
     * 仅用于测试的枚举：text 字段含 null 值，验证 getMap 字段反射时 null 值被过滤
     */
    enum NullValueEnum {

        A("a"),

        B(null),

        ;

        final String text;

        NullValueEnum(String text) {
            this.text = text;
        }

    }

}
