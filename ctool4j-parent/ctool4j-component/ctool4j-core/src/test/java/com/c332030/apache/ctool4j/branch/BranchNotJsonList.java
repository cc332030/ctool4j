package com.c332030.apache.ctool4j.branch;

import java.util.ArrayList;

/**
 * 用例夹具：位于包名含 {@code ".apache."}（{@code CLogUtils} 的<strong>不转 JSON 包名</strong>片段）
 * 的 {@code Collection} 子类——同时命中「不转包名」与「转父类」，用于验证不转包名的优先级。
 *
 * <p>必须是<strong>顶层类</strong>：{@code isJsonLog} 按 {@code Class#getName()} 判定，嵌套类的
 * 名称会带上外层类名而不含此处所需的包片段。</p>
 *
 * @since 2026/9/18
 * @version 1.0
 */
public class BranchNotJsonList extends ArrayList<Object> {
}
