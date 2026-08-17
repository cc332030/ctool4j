package com.c332030.ctool4j.core.test.util;

import com.c332030.ctool4j.core.util.CEnumUtils;
import com.c332030.ctool4j.definition.enums.CDbOperateEnum;
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

    @Test
    public void getNameMap() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getNameMap(CDbOperateEnum.class);

        Assertions.assertEquals(4, map.size());
        Assertions.assertEquals(CDbOperateEnum.INSERT, map.get("INSERT"));
        Assertions.assertEquals(CDbOperateEnum.DELETE, map.get("DELETE"));

    }

    @Test
    public void getMapByFieldName() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getMap(CDbOperateEnum.class, "text");

        Assertions.assertEquals(4, map.size());
        Assertions.assertEquals(CDbOperateEnum.INSERT, map.get("插入"));
        Assertions.assertEquals(CDbOperateEnum.SELECT, map.get("查询"));

    }

    @Test
    public void getMapByUnknownFieldThrows() {

        Assertions.assertThrowsExactly(NoSuchFieldException.class,
                () -> CEnumUtils.getMap(CDbOperateEnum.class, "notExist"));

    }

    @Test
    public void getMapNotEnumThrows() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> CEnumUtils.getMap(String.class, "name"));

    }

    @Test
    public void valueOfByMap() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getNameMap(CDbOperateEnum.class);
        Assertions.assertEquals(CDbOperateEnum.UPDATE, CEnumUtils.valueOf(map, "UPDATE"));

    }

    @Test
    public void valueOfByMapNotFoundThrows() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getNameMap(CDbOperateEnum.class);
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> CEnumUtils.valueOf(map, "UNKNOWN"));

    }

    @Test
    public void nameOf() {

        Assertions.assertEquals(CDbOperateEnum.INSERT, CEnumUtils.nameOf(CDbOperateEnum.class, "INSERT"));

    }

    @Test
    public void nameOfNotFoundThrows() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> CEnumUtils.nameOf(CDbOperateEnum.class, "UNKNOWN"));

    }

    @Test
    public void valueOfByFieldName() {

        Assertions.assertEquals(CDbOperateEnum.DELETE,
                CEnumUtils.valueOf(CDbOperateEnum.class, "text", "删除"));

    }

    @Test
    public void valueOfByFieldNameNotFoundThrows() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> CEnumUtils.valueOf(CDbOperateEnum.class, "text", "不存在"));

    }

    @Test
    public void values() {

        List<CDbOperateEnum> values = CEnumUtils.values(CDbOperateEnum.class);
        Assertions.assertEquals(4, values.size());
        Assertions.assertEquals(CDbOperateEnum.INSERT, values.get(0));
        Assertions.assertEquals(CDbOperateEnum.DELETE, values.get(3));

    }

}
