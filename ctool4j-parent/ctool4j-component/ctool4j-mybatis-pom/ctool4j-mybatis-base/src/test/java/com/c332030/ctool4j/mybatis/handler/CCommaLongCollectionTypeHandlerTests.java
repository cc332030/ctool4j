package com.c332030.ctool4j.mybatis.handler;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * <p>
 * Description: CCommaLongCollectionTypeHandlerTests
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>覆盖 {@link CCommaLongCollectionTypeHandler} 的<b>类型具体化</b>：泛型实参解析与读写往返。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>本类是"只声明类型"的空体子类，故只验两件事：泛型实参被解析为 {@code Long}、往返不丢值。</li>
 *   <li>通用格式语义（空集合/空白/脏数据/null 元素）已在 {@link CCommaCollectionTypeHandlerTests} 覆盖，本类不重复。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>{@code @TableField(typeHandler = CCommaLongCollectionTypeHandler.class)} 标注的 {@code List<Long>} 字段。</li>
 * </ul>
 *
 * @see CCommaLongCollectionTypeHandler
 * @since 2026/9/24
 * @version 1.0
 */
public class CCommaLongCollectionTypeHandlerTests {

    /**
     * 被测处理器
     */
    private final CCommaLongCollectionTypeHandler handler = new CCommaLongCollectionTypeHandler();

    /**
     * 元素类型由泛型实参解析为 Long
     */
    @Test
    public void elementType_isLong() {
        Assertions.assertEquals(Long.class, handler.elementType);
    }

    /**
     * 写库：按集合顺序拼成逗号分割字符串
     */
    @Test
    public void toTextValue_inOrder() {
        Assertions.assertEquals("n", handler.toTextValue(Arrays.asList(o -join ', ')));
    }

    /**
     * 读写往返：写出的字符串读回等值
     */
    @Test
    public void roundTrip() {

        val source = Arrays.asList(o -join ', ');

        Assertions.assertEquals(new ArrayList<>(source),
                new ArrayList<>(handler.fromText(CCommaCollectionTypeHandler.toText(source))));
    }

}
