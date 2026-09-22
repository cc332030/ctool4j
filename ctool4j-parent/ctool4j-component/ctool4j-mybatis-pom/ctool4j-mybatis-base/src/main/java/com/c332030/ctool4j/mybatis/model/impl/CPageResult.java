package com.c332030.ctool4j.mybatis.model.impl;

import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.mybatis.model.ICPageResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * <p>
 * Description: CPageResult
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CPageResult} 为分页结果模型（实现 {@code ICPageResult}），提供：</p>
 * <ul>
 *   <li>字段：{@code current} / {@code size} / {@code total} / {@code pages} / {@code records}</li>
 *   <li>{@code of(total, records)}：由总数与数据列表构造</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>纯数据模型，不引用 MyBatis-Plus 类型（契约定义、适用场景与边界见 {@code ICPageResult}）。</li>
 *   <li>字段语义与 JSON 结构与 {@code IPage} 对齐（current / size / total / pages / records），引用方由 {@code IPage} 切换到本类时字段名不变；排序字段 orders 不在本类（见 {@code ICPageResult} 已知限制）。</li>
 *   <li>lombok {@code @Data} / {@code @SuperBuilder} / {@code @NoArgsConstructor} / {@code @AllArgsConstructor} 生成访问器、builder 与构造器；对象创建用 builder。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未设置分页信息</td>
 *     <td>current=1、size=0、total=0、pages=0、records 为空列表（不为 null）</td>
 *   </tr>
 *   <tr>
 *     <td>{@code of(total, records)} 只传总数与数据列表</td>
 *     <td>当前页 / 页大小 / 总页数保持默认值（1 / 0 / 0），不推断</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>分页查询结果的响应体、Feign 内部调用契约的返回值。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>分页请求参数用 {@code CPage} / {@code CPageReq}；由 MP 分页对象转换用 {@code CMpPageUtils.getPageResult}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>默认 {@code records} 为不可修改空列表（{@code CList.of()}）：反序列化前若没有可写成员而 JSON 带 records，会因合并写入抛 {@code UnsupportedOperationException}；本类由 lombok 生成 setter，反序列化走 setter 替换，不受影响。</li>
 *   <li>{@code current} / {@code size} / {@code total} / {@code pages} 为包装类型：允许 null 表达「未提供」，翻页判断侧按 0 兜底（见 {@code ICPageResult}）。</li>
 * </ul>
 *
 * @since 2026/9/19
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CPageResult<T> implements ICPageResult<T> {

    @Builder.Default
    Long current = 1L;

    @Builder.Default
    Long size = 0L;

    @Builder.Default
    Long total = 0L;

    @Builder.Default
    Long pages = 0L;

    @Builder.Default
    List<T> records = CList.of();

    /**
     * 由总数与数据列表构造分页结果
     *
     * <p>仅填充 {@code total} 与 {@code records}，其余字段保持默认值（current=1、size=0、pages=0）；
     * 分页信息完整时应使用 builder，或由 MP 分页对象经 {@code CMpPageUtils.getPageResult} 转换。</p>
     *
     * @param total   总记录数
     * @param records 数据列表
     * @param <T>     数据类型
     * @return 分页结果
     */
    public static <T> CPageResult<T> of(Long total, List<T> records) {
        return CPageResult.<T>builder()
            .total(total)
            .records(records)
            .build();
    }

}
