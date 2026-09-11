package com.c332030.ctool4j.excel.test.util;

import lombok.Data;

/**
 * <p>
 * Description: CExcelTestBean
 * </p>
 *
 * <h2>功能说明</h2>
 * <p>{@code CExcelTestBean} 为 Excel 测试辅助实体（{@code @Data}），含 {@code id}/{@code name}/{@code desc} 字段，供 Excel 读写测试（{@code CExcelUtilsTests} 等）作为测试数据载体。</p>
 * <h2>适用场景</h2>
 * <ul>
 *   <li>Excel 功能测试的辅助数据实体。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助类；字段简单。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/8/14
 * @version 1.0
 */
@Data
public class CExcelTestBean {

    private Integer id;

    private String name;

    private String desc;
}
