package com.c332030.ctool4j.mybatis.model;

import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CNumUtils;
import com.c332030.ctool4j.doc.annotation.CSchema;
import lombok.val;

import java.util.List;

/**
 * <p>
 * Description: ICPageResult
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICPageResult} 为分页结果契约接口，字段语义对齐 MyBatis-Plus 的 {@code IPage}，提供：</p>
 * <ul>
 *   <li>{@code getCurrent()}：当前页</li>
 *   <li>{@code getSize()}：页大小</li>
 *   <li>{@code getTotal()}：总记录数</li>
 *   <li>{@code getPages()}：总页数</li>
 *   <li>{@code getRecords()}：数据列表</li>
 *   <li>{@code hasNext()} / {@code hasPrevious()}：是否存在下一页 / 上一页</li>
 * </ul>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>契约不引用 MyBatis-Plus 类型：无法依赖 MP 的项目（如接口契约模块）可用实现类 {@code CPageResult} 承载分页结果。</li>
 *   <li>各 getter 均有默认实现（current=1、size=0、total=0、pages=0、records 为空列表），实现类不覆写也不会返回 null。</li>
 *   <li>响应侧分页字段取 current/size（对齐 {@code IPage}），与请求侧 {@code ICPage} 的 pageNum/pageSize 命名不同，以便由 MP 分页对象直接填充。</li>
 *   <li>{@code hasNext} / {@code hasPrevious} 不属 getter 命名，不参与 JSON 序列化；对 null 字段按 0 处理，不抛 NPE。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>实现类未覆写 getter</td>
 *     <td>返回默认值（1 / 0 / 0 / 0 / 空列表）</td>
 *   </tr>
 *   <tr>
 *     <td>hasNext / hasPrevious 依赖字段为 null</td>
 *     <td>按 0 处理，返回 false</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>分页查询结果的响应契约（对外接口、内部调用返回值），尤其是不依赖 MyBatis-Plus 的引用方。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>分页请求参数（当前页、页大小、排序）用 {@code ICPage} / {@code CPage} / {@code CPageReq}。</li>
 *   <li>本接口不能作反序列化目标：Feign 返回值、Jackson 反序列化等需实例化的场合须用实现类 {@code CPageResult}；接口用于服务端返回声明与引用方的只读契约。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>不含排序字段：排序属请求侧语义，引入排序会绑定 MP 的 {@code OrderItem}，破坏「契约不依赖 MP」这一目标。</li>
 *   <li>接口不声明 set 方法：写入能力由实现类 {@code CPageResult}（lombok 访问器）承担。</li>
 *   <li>{@code hasNext} / {@code hasPrevious} 依赖 {@code pages} 字段，不由 {@code total} 与 {@code size} 自动推算：只填 total/size 而未提供 pages 时两者恒为 false；需要翻页判断时须提供 pages（经 MP 分页对象转换时已带入）。</li>
 * </ul>
 *
 * @since 2026/9/19
 * @version 1.0
 */
public interface ICPageResult<T> {

    /**
     * 当前页
     *
     * @return 当前页，默认 1
     */
    @CSchema("当前页")
    default Long getCurrent() {
        return 1L;
    }

    /**
     * 页大小
     *
     * @return 页大小，默认 0（未提供分页信息）
     */
    @CSchema("页大小")
    default Long getSize() {
        return 0L;
    }

    /**
     * 总记录数
     *
     * @return 总记录数，默认 0
     */
    @CSchema("总记录数")
    default Long getTotal() {
        return 0L;
    }

    /**
     * 总页数
     *
     * @return 总页数，默认 0（未提供分页信息）
     */
    @CSchema("总页数")
    default Long getPages() {
        return 0L;
    }

    /**
     * 数据列表
     *
     * @return 数据列表，默认空列表（不可修改）
     */
    @CSchema("数据列表")
    default List<T> getRecords() {
        return CList.of();
    }

    /**
     * 是否存在下一页（当前页小于总页数）
     *
     * @return 是否存在下一页
     */
    default boolean hasNext() {
        val current = CNumUtils.defaultZero(getCurrent());
        val pages = CNumUtils.defaultZero(getPages());
        return current < pages;
    }

    /**
     * 是否存在上一页（当前页大于 1）
     *
     * @return 是否存在上一页
     */
    default boolean hasPrevious() {
        return CNumUtils.defaultZero(getCurrent()) > 1;
    }

}
