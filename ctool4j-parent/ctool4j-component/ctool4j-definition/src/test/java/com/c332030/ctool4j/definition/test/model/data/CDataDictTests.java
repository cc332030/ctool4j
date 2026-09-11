package com.c332030.ctool4j.definition.test.model.data;

import com.c332030.ctool4j.definition.model.data.CDataDict;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDataDictTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「构造 / 设置取值 / toString / equals/hashCode / builder」多个维度组织，验证 Lombok @Data 行为。</li>
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
 *   <li>1.1 无参构造（noArgsConstructor）</li>
 *   <li>1.2 setter/getter（setterAndGetter）</li>
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
public class CDataDictTests {

    /**
     * 对应测试用例 1.1：无参构造
     */
    @Test
    public void noArgsConstructor() {

        CDataDict<String> dataDict = new CDataDict<>();

        Assertions.assertNull(dataDict.getValue());
        Assertions.assertNull(dataDict.getText());

    }

    /**
     * 对应测试用例 1.2：setter/getter
     */
    @Test
    public void setterAndGetter() {

        CDataDict<String> dataDict = new CDataDict<>();

        dataDict.setValue("1");
        dataDict.setText("是");

        Assertions.assertEquals("1", dataDict.getValue());
        Assertions.assertEquals("是", dataDict.getText());

    }

    /**
     * 对应测试用例 2.1：null 安全
     */
    @Test
    public void toStringNullSafe() {

        CDataDict<String> dataDict = new CDataDict<>();

        String str = dataDict.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CDataDict"));
        Assertions.assertTrue(str.contains("value=null"));
        Assertions.assertTrue(str.contains("text=null"));

    }

    /**
     * 对应测试用例 2.2：有值含字段
     */
    @Test
    public void toStringWithValues() {

        CDataDict<String> dataDict = new CDataDict<>();
        dataDict.setValue("1");
        dataDict.setText("是");

        String str = dataDict.toString();

        Assertions.assertTrue(str.contains("value=1"));
        Assertions.assertTrue(str.contains("text=是"));

    }

    /**
     * 对应测试用例 3.1：同值相等
     */
    @Test
    public void equalsAndHashCode() {

        CDataDict<String> a = new CDataDict<>();
        a.setValue("1");
        a.setText("是");

        CDataDict<String> b = new CDataDict<>();
        b.setValue("1");
        b.setText("是");

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());

        CDataDict<String> c = new CDataDict<>();
        c.setValue("2");

        Assertions.assertNotEquals(a, c);

    }

    /**
     * 对应测试用例 3.2：同引用相等
     */
    @Test
    public void equalsSameReference() {

        CDataDict<String> a = new CDataDict<>();
        Assertions.assertEquals(a, a);

    }

    /**
     * 对应测试用例 4.1：builder 构造
     */
    @Test
    public void builder() {

        CDataDict<String> dataDict = CDataDict.<String>builder()
            .value("1")
            .text("是")
            .build();

        Assertions.assertEquals("1", dataDict.getValue());
        Assertions.assertEquals("是", dataDict.getText());

    }

}
