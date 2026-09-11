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
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「Map 构建 / 反查 / 异常路径 / 值列表」四个维度组织。</li>
 *   <li>Map 构建覆盖 getNameMap、getMap（按字段名/函数引用/ICValue）、getMap（按 ICValue.VALUE），</li>
 *   <li>并验证 Map 大小与内容。</li>
 *   <li>反查覆盖 nameOf / valueOf（map、字段名、函数引用、ICValue），以及各入口的不存在异常路径。</li>
 *   <li>异常路径用 {@code assertThrowsExactly} 精确匹配异常类型：字段不存在抛 {@code NoSuchFieldException}、</li>
 *   <li>非枚举抛 {@code IllegalArgumentException}、反查不存在抛 {@code IllegalArgumentException}。</li>
 *   <li>测试数据用 {@code CDbOperateEnum}（含 name 与 text 字段）、{@code CCountryCodeEnum}（实现 ICValue&lt;Integer&gt;）</li>
 *   <li>及测试专用 {@code NullValueEnum}（含 null 值字段），贴近真实枚举使用场景。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 Map 构建规则（name 用 {@code Enum.name()}、字段值用反射取值且 null 过滤）与反查抛异常约定。</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖/异常路径）：正例反查、不存在反查、非枚举、字段不存在、</li>
 *   <li>字段值为 null 的枚举不建反查项。</li>
 *   <li>{@code assertThrowsExactly} 精确匹配异常类型（依据测试规范，不用 {@code assertThrows} 掩盖子类语义）。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：getNameMap（按 name）、getMap 按字段名（text）、getMap 按函数引用（{@code CDbOperateEnum::getText}）、</li>
 *   <li>getMap 按 ICValue.VALUE（{@code getMap(CCountryCodeEnum.class)}）、getMap 字段值为 null 的枚举被过滤</li>
 *   <li>（{@code NullValueEnum}，强化字段反射 null 过滤分支）、字段不存在抛 NoSuchFieldException、非枚举抛</li>
 *   <li>IllegalArgumentException、valueOf(map)/nameOf/valueOf(字段名/func/ICValue) 正例与不存在抛异常、</li>
 *   <li>values 值列表。</li>
 *   <li>未覆盖：{@code valueOf(Class, Func1, value)} 不存在值抛异常路径（正例已覆盖，异常抛 IllegalArgumentException</li>
 *   <li>与其余反查入口一致）。</li>
 * </ul>
 * <h2>Map 构建</h2>
 * <ul>
 *   <li>1.1 getNameMap：按枚举名构建，含全部枚举（getNameMap）</li>
 *   <li>1.2 getMap 按字段名：按 text 字段构建，值映射正确（getMapByFieldName）</li>
 *   <li>1.3 异常：字段不存在抛 NoSuchFieldException（getMapByUnknownFieldThrows）</li>
 *   <li>1.4 异常：非枚举类抛 IllegalArgumentException（getMapNotEnumThrows）</li>
 *   <li>1.5 getMap 按 ICValue：无参重载按 ICValue.VALUE 构建，值映射正确（getMapByValueInterface）</li>
 *   <li>1.6 getMap 字段值为 null 过滤：字段值为 null 的枚举不建立反查项（getMapNullValueField）</li>
 *   <li>1.7 getMap 按函数引用：按字段函数引用构建，值映射正确（getMapByFunc）</li>
 * </ul>
 * <h2>反查</h2>
 * <ul>
 *   <li>2.1 valueOf(map, value)：从 Map 反查命中（valueOfByMap）</li>
 *   <li>2.2 异常：valueOf(map, value) 值不存在抛 IllegalArgumentException（valueOfByMapNotFoundThrows）</li>
 *   <li>2.3 nameOf：按枚举名反查命中（nameOf）</li>
 *   <li>2.4 异常：nameOf 枚举名不存在抛 IllegalArgumentException（nameOfNotFoundThrows）</li>
 *   <li>2.5 valueOf(Class, fieldName, value)：按字段值反查命中（valueOfByFieldName）</li>
 *   <li>2.6 异常：valueOf(Class, fieldName, value) 值不存在抛 IllegalArgumentException（valueOfByFieldNameNotFoundThrows）</li>
 *   <li>2.7 valueOf(Class, ICValue)：按 ICValue.VALUE 值反查命中（valueOfByValueInterface）</li>
 *   <li>2.8 valueOf(Class, Func1, value)：按字段函数引用值反查命中（valueOfByFunc）</li>
 * </ul>
 * <h2>值列表（values）</h2>
 * <ul>
 *   <li>3.1 values：返回全部枚举值列表且顺序正确（values）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CEnumUtilsTests {

    /**
     * 对应测试用例 1.1：按枚举名构建，含全部枚举
     */
    @Test
    public void getNameMap() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getNameMap(CDbOperateEnum.class);

        Assertions.assertEquals(4, map.size());
        Assertions.assertEquals(CDbOperateEnum.INSERT, map.get("INSERT"));
        Assertions.assertEquals(CDbOperateEnum.DELETE, map.get("DELETE"));

    }

    /**
     * 对应测试用例 1.2：getMap 按字段名：按 text 字段构建，值映射正确
     */
    @Test
    public void getMapByFieldName() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getMap(CDbOperateEnum.class, "text");

        Assertions.assertEquals(4, map.size());
        Assertions.assertEquals(CDbOperateEnum.INSERT, map.get("插入"));
        Assertions.assertEquals(CDbOperateEnum.SELECT, map.get("查询"));

    }

    /**
     * 对应测试用例 1.3：异常：字段不存在抛 NoSuchFieldException
     */
    @Test
    public void getMapByUnknownFieldThrows() {

        Assertions.assertThrowsExactly(NoSuchFieldException.class,
                () -> CEnumUtils.getMap(CDbOperateEnum.class, "notExist"));

    }

    /**
     * 对应测试用例 1.4：异常：非枚举类抛 IllegalArgumentException
     */
    @Test
    public void getMapNotEnumThrows() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> CEnumUtils.getMap(String.class, "name"));

    }

    /**
     * 对应测试用例 1.5：getMap 按 ICValue：无参重载按 ICValue.VALUE 构建，值映射正确
     */
    @Test
    public void getMapByValueInterface() {

        Map<Integer, CCountryCodeEnum> map = CEnumUtils.getMap(CCountryCodeEnum.class);

        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(CCountryCodeEnum.CHN, map.get(86));

    }

    /**
     * 对应测试用例 1.6：getMap 字段值为 null 过滤：字段值为 null 的枚举不建立反查项
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
     * 对应测试用例 1.7：getMap 按函数引用：按字段函数引用构建，值映射正确
     */
    @Test
    public void getMapByFunc() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getMap(CDbOperateEnum.class, CDbOperateEnum::getText);

        Assertions.assertEquals(4, map.size());
        Assertions.assertEquals(CDbOperateEnum.INSERT, map.get("插入"));
        Assertions.assertEquals(CDbOperateEnum.SELECT, map.get("查询"));

    }

    /**
     * 对应测试用例 2.1：valueOf(map, value)：从 Map 反查命中
     */
    @Test
    public void valueOfByMap() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getNameMap(CDbOperateEnum.class);
        Assertions.assertEquals(CDbOperateEnum.UPDATE, CEnumUtils.valueOf(map, "UPDATE"));

    }

    /**
     * 对应测试用例 2.2：异常：valueOf(map, value) 值不存在抛 IllegalArgumentException
     */
    @Test
    public void valueOfByMapNotFoundThrows() {

        Map<String, CDbOperateEnum> map = CEnumUtils.getNameMap(CDbOperateEnum.class);
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> CEnumUtils.valueOf(map, "UNKNOWN"));

    }

    /**
     * 对应测试用例 2.3：按枚举名反查命中
     */
    @Test
    public void nameOf() {

        Assertions.assertEquals(CDbOperateEnum.INSERT, CEnumUtils.nameOf(CDbOperateEnum.class, "INSERT"));

    }

    /**
     * 对应测试用例 2.4：异常：nameOf 枚举名不存在抛 IllegalArgumentException
     */
    @Test
    public void nameOfNotFoundThrows() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> CEnumUtils.nameOf(CDbOperateEnum.class, "UNKNOWN"));

    }

    /**
     * 对应测试用例 2.5：valueOf(Class, fieldName, value)：按字段值反查命中
     */
    @Test
    public void valueOfByFieldName() {

        Assertions.assertEquals(CDbOperateEnum.DELETE,
                CEnumUtils.valueOf(CDbOperateEnum.class, "text", "删除"));

    }

    /**
     * 对应测试用例 2.6：异常：valueOf(Class, fieldName, value) 值不存在抛 IllegalArgumentException
     */
    @Test
    public void valueOfByFieldNameNotFoundThrows() {

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> CEnumUtils.valueOf(CDbOperateEnum.class, "text", "不存在"));

    }

    /**
     * 对应测试用例 2.7：valueOf(Class, ICValue)：按 ICValue.VALUE 值反查命中
     */
    @Test
    public void valueOfByValueInterface() {

        Assertions.assertEquals(CCountryCodeEnum.CHN,
                CEnumUtils.valueOf(CCountryCodeEnum.class, 86));

    }

    /**
     * 对应测试用例 2.8：valueOf(Class, Func1, value)：按字段函数引用值反查命中
     */
    @Test
    public void valueOfByFunc() {

        Assertions.assertEquals(CDbOperateEnum.DELETE,
                CEnumUtils.valueOf(CDbOperateEnum.class, CDbOperateEnum::getText, "删除"));

    }

    /**
     * 对应测试用例 3.1：返回全部枚举值列表且顺序正确
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
