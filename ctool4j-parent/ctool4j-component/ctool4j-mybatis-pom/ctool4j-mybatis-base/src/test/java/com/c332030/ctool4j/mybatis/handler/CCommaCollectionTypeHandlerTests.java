package com.c332030.ctool4j.mybatis.handler;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * <p>
 * Description: CCommaCollectionTypeHandlerTests
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>覆盖 {@link CCommaCollectionTypeHandler} 的写读往返与边界：拼接、分割、空值、空白、null 元素、脏数据、元素类型解析。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>直接调用 {@code toText}/{@code toTextValue}/{@code fromText}（同包可见），不 mock JDBC——
 *   JDBC 端点接线由 {@link CBaseTypeHandler} 承担，此处只钉业务格式语义。</li>
 *   <li>重点钉住"换实现后易漂移"的语义：空集合写 {@code null}、空白读 {@code null}、
 *   null 元素不落成字面量、脏数据是否跳过——这几条是拼接/分割工具替换后最可能失真的部分。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>{@link CCommaCollectionTypeHandler} 及其具体化子类（{@code Integer}/{@code Long}/{@code String}）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>不起 Spring 上下文、不连库：只验格式转换，不验 MyBatis 装配（装配由使用方集成测试覆盖）。</li>
 * </ul>
 *
 * @see CCommaCollectionTypeHandler
 * @since 2026/9/24
 * @version 1.0
 */
public class CCommaCollectionTypeHandlerTests {

    /**
     * Integer 元素处理器
     */
    private final CCommaIntegerCollectionTypeHandler integerHandler = new CCommaIntegerCollectionTypeHandler();

    /**
     * String 元素处理器
     */
    private final CCommaStringCollectionTypeHandler stringHandler = new CCommaStringCollectionTypeHandler();

    // ---------- 写 ----------

    /**
     * 写库：按集合顺序拼成逗号分割字符串
     */
    @Test
    public void toText_joinInOrder() {
        Assertions.assertEquals("7,15,30", CCommaCollectionTypeHandler.toText(new ArrayList<>(Arrays.asList(7, 15, 30))));
    }

    /**
     * 写库：空集合与 null 入参都返回 null（不是空串）
     */
    @Test
    public void toText_emptyAsNull() {
        Assertions.assertNull(CCommaCollectionTypeHandler.toText(new ArrayList<>()));
        Assertions.assertNull(CCommaCollectionTypeHandler.toText(null));
    }

    /**
     * 写库：null 元素被跳过，不落成字面量 {@code "null"}
     */
    @Test
    public void toText_skipNullElement() {

        val values = new ArrayList<Integer>();
        values.add(1);
        values.add(null);
        values.add(2);

        Assertions.assertEquals("1,2", CCommaCollectionTypeHandler.toText(values));
    }

    /**
     * 写库：{@code toTextValue} 与静态 {@code toText} 同源
     */
    @Test
    public void toTextValue_sameAsToText() {
        Assertions.assertEquals("7,15", integerHandler.toTextValue(new ArrayList<>(Arrays.asList(7, 15))));
    }

    // ---------- 读 ----------

    /**
     * 读库：逗号串切成集合且保持原顺序
     */
    @Test
    public void fromText_inOrder() {
        Assertions.assertEquals(Arrays.asList(7, 15, 30), new ArrayList<>(integerHandler.fromText("7,15,30")));
    }

    /**
     * 读库：片段带空格时按 trim 语义处理
     */
    @Test
    public void fromText_withSpaces() {
        Assertions.assertEquals(Arrays.asList(7, 15, 30), new ArrayList<>(integerHandler.fromText("7, 15, 30")));
    }

    /**
     * 读库：空串、纯空白与 null 都返回 null（不是空集合）
     */
    @Test
    public void fromText_blankAsNull() {
        Assertions.assertNull(integerHandler.fromText(""));
        Assertions.assertNull(integerHandler.fromText("   "));
        Assertions.assertNull(integerHandler.fromText(null));
    }

    /**
     * 读库：脏数据（无法转换的元素）被跳过，不影响同一行的其余元素
     */
    @Test
    public void fromText_dirtyElement() {
        Assertions.assertEquals(Arrays.asList(7, 30), new ArrayList<>(integerHandler.fromText("7,abc,30")));
    }

    /**
     * 读库：String 元素原样保留
     */
    @Test
    public void fromText_stringElements() {
        Assertions.assertEquals(Arrays.asList("a", "b", "c"), new ArrayList<>(stringHandler.fromText("a,b,c")));
    }

    /**
     * 读写往返：写出的字符串能原样读回
     */
    @Test
    public void roundTrip() {

        Collection<Integer> source = Arrays.asList(7, 15, 30);

        Assertions.assertEquals(new ArrayList<>(source),
                new ArrayList<>(integerHandler.fromText(CCommaCollectionTypeHandler.toText(source))));
    }

    // ---------- 元素类型解析 ----------

    /**
     * 元素类型由子类声明的泛型实参解析
     */
    @Test
    public void resolveElementType_fromSubclass() {
        Assertions.assertEquals(Integer.class, integerHandler.elementType);
        Assertions.assertEquals(Long.class, new CCommaLongCollectionTypeHandler().elementType);
        Assertions.assertEquals(String.class, stringHandler.elementType);
    }

    /**
     * 泛型未具体化（仅透传）时构造即失败，避免运行期按错误类型转换
     */
    @Test
    public void resolveElementType_notConcrete() {
        Assertions.assertThrowsExactly(IllegalStateException.class, NotConcreteHandler::new);
    }

    /**
     * 泛型只透传、未具体化的子类（用于验证构造期快速失败）
     *
     * @param <T> 元素类型
     */
    static class NotConcreteHandler<T extends Serializable> extends CCommaCollectionTypeHandler<T> {
    }


    /**
     * 写库：元素全为 null 时无有效值，返回 null（不落成空串）
     */
    @Test
    public void toText_allNullElements() {

        val values = new ArrayList<Integer>();
        values.add(null);
        values.add(null);

        Assertions.assertNull(CCommaCollectionTypeHandler.toText(values));
    }
}
