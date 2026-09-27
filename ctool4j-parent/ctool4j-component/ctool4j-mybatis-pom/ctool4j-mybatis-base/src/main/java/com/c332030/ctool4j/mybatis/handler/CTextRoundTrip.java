package com.c332030.ctool4j.mybatis.handler;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Description: CTextRoundTrip
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CTextRoundTrip}（{@code @UtilityClass}）提供"元素类型是否支持按值往返"的<b>取值探针</b>：
 * 一组探针取值（{@link #PROBE_VALUES}），用它们去验证某类型是否满足"同一取值只有一种文本写法"。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>探针而不是白名单</b>：判据是"该类型对探针取值是否都按值往返成立"，靠取值验证得出，
 *   故不需要、也不应该维护"允许哪些类型"的清单——清单每漏一个类型就漏放一个缺陷，
 *   而取值探针按行为判定、新增类型无须改代码。</li>
 *   <li><b>探针取值挑的是"同一取值的另一种写法"</b>：整数值的浮点、大小与浮点混用、与数字型文本同形的取值——
 *   这些正是按值往返的失效来源。常规取值（两种布尔写法、数字、空串、空白、含分隔符文本、数字型字符串）
 *   不构成失效来源，故不进探针。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：本类是取值清单，不参与运行时分支；取值验证在调用方（{@link CCommaCollectionTypeHandler} 构造期）。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>以单值文本承载对象、且要求按值往返的转换端点（当前为 {@link CCommaCollectionTypeHandler}）。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不要求按值往返的存储格式（JSON、带类型前缀的编码）不适用：它们把类型信息写进文本，不存在本探针要防的问题。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>探针覆盖的是"同一取值多种文本写法"这一失效形态，不覆盖"文本无法表达该取值"（如集合元素自身是对象）
 *   ——后者表现为转换抛异常或返回 null，由调用方按脏数据处理。</li>
 * </ul>
 *
 * @see CCommaCollectionTypeHandler
 * @since 2026/9/27
 * @version 1.0
 */
@UtilityClass
public class CTextRoundTrip {

    /**
     * 探针取值：与底值同形、可能是"同一取值的另一种写法"的那些取值
     *
     * <p>命中即说明该类型按值往返不成立（写出去的文本读回来不是同一个取值）。</p>
     */
    public final List<Object> PROBE_VALUES = Arrays.asList(
        // 短整型：数值与 int 的文本同形
        (short) 1,
        (byte) 1,
        // 浮点：整数值的浮点与整型文本同形
        1.0D,
        0.0D,
        -1.0D,
        1.0F,
        0.0F,
        1.5D,
        1.5F
    );

}
