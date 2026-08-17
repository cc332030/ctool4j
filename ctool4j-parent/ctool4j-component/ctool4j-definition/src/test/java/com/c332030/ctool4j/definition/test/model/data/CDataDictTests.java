package com.c332030.ctool4j.definition.test.model.data;

import com.c332030.ctool4j.definition.model.data.CDataDict;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CDataDictTests
 * </p>
 *
 * @since 2026/8/16
 */
public class CDataDictTests {

    @Test
    public void noArgsConstructor() {

        CDataDict<String> dataDict = new CDataDict<>();

        Assertions.assertNull(dataDict.getValue());
        Assertions.assertNull(dataDict.getText());

    }

    @Test
    public void setterAndGetter() {

        CDataDict<String> dataDict = new CDataDict<>();

        dataDict.setValue("1");
        dataDict.setText("是");

        Assertions.assertEquals("1", dataDict.getValue());
        Assertions.assertEquals("是", dataDict.getText());

    }

    @Test
    public void toStringNullSafe() {

        CDataDict<String> dataDict = new CDataDict<>();

        String str = dataDict.toString();

        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains("CDataDict"));
        Assertions.assertTrue(str.contains("value=null"));
        Assertions.assertTrue(str.contains("text=null"));

    }

    @Test
    public void toStringWithValues() {

        CDataDict<String> dataDict = new CDataDict<>();
        dataDict.setValue("1");
        dataDict.setText("是");

        String str = dataDict.toString();

        Assertions.assertTrue(str.contains("value=1"));
        Assertions.assertTrue(str.contains("text=是"));

    }

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

    @Test
    public void equalsSameReference() {

        CDataDict<String> a = new CDataDict<>();
        Assertions.assertEquals(a, a);

    }

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
