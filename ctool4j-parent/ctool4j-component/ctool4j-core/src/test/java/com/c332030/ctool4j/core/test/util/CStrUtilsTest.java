package com.c332030.ctool4j.core.test.util;

import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.CStrUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CStrUtilsTest
 * </p>
 * <p>`com.c332030.ctool4j.core.util.CStrUtils`（CStrUtils）的测试用例</p>
 *
 * <p><b>用例设计思路</b>：聚焦易出错且出错后难发现的方法，按「模板格式化 / 末尾数字自增 / 中文提取 / 字符串清洗」四个维度组织：</p>
 * <ul>
 *   <li>格式化：模板参数缺失时按统一兜底值（空串）落位，验证占位符替换不残留；</li>
 *   <li>自增：覆盖正常自增与全部提前返回分支（空串、无 "-"、"-" 结尾、末段非数字）与多位数进位；</li>
 *   <li>中文提取：覆盖纯中文、纯非中文、中英数混排、含转义 / 标点 / 邮箱等符号；</li>
 *   <li>字符串清洗：覆盖两侧引号、单侧引号、仅引号、空串等边界（Q16 修复点）。</li>
 * </ul>
 *
 * <p><b>设计依据</b>：依据功能设计对格式化兜底值、自增失败原样返回、中文提取按 Unicode 区间过滤、
 * {@code toAvailable} 单侧引号保留的约定；依据等价类/边界值/分支覆盖。</p>
 * <p><b>覆盖场景</b>：{@code format} 模板参数缺失；{@code incrLastNum} 正常/进位/无 "-"/"-" 结尾/末段非数字/空串；
 * {@code chineseOnly} 纯中文/纯非中文/混排/含符号；{@code toAvailable} 双引号/单引号/仅引号/空串。</p>
 * <p><b>未覆盖</b>：{@code splitToList}/{@code join}/{@code concat}/{@code repeat}/{@code fillAfter} 等其余入口
 * （当前测试聚焦上述易错方法，其余入口由后续批次补充）。</p>
 *
 * <p><b>用例编号索引</b>：1 模板格式化（1.1）；2 末尾数字自增（2.1-2.4）；3 中文提取（3.1）；4 字符串清洗（4.1）。
 * 各测试方法 javadoc 标注其编号与说明。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「格式化 / 自增 / 中文提取 / 可用性处理」多个维度组织，聚焦易出错且出错后难发现的入口。</li>
 *   <li>格式化验证缺失参数以空串兜底。</li>
 *   <li>自增覆盖正常自增与<b>全部提前返回/失败分支</b>：无 "-"、"-" 结尾、末段非数字、空串/null、解析失败（超 int 范围），</li>
 *   <li>并覆盖多位数进位。</li>
 *   <li>中文提取覆盖中文、混合、纯数字、纯字母、含特殊字符等多样本，以及空串/null 边界（原样返回）。</li>
 *   <li>可用性处理（toAvailable）覆盖单侧/双侧引号、引号全覆盖、空值边界（Q16 回归）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对格式化兜底、自增规则（失败原样返回）、中文提取正则、toAvailable 引号语义的约定。</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖/回归）：引号各种形态、自增各提前返回分支与进位、中文混合、</li>
 *   <li>格式化缺失参数、空串/null 边界。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：format 缺失参数空串兜底；incrLastNum 正常自增 / 多位数进位 / 无 "-" / "-" 结尾 / 末段非数字 / 空串 / null /</li>
 *   <li>超 int 范围解析失败；chineseOnly 多种样本（纯中文/混合/纯数字/纯字母/含符号）与空串/null 原样返回；</li>
 *   <li>toAvailable 单侧/双侧引号、全引号、空值。</li>
 *   <li>未覆盖：{@code splitToList}/{@code join}/{@code concat}/{@code repeat}/{@code fillAfter} 等其余入口（当前测试聚焦格式化/自增/中文/</li>
 *   <li>可用性核心路径，其余入口可后续批次补充）。</li>
 * </ul>
 * <h2>格式化</h2>
 * <ul>
 *   <li>1.1 format 缺失参数：{@code ${name}} 缺失时以空串兜底（formatNullToEmpty）</li>
 * </ul>
 * <h2>末尾数字自增</h2>
 * <ul>
 *   <li>2.1 incrLastNum：{@code c332030-1} → {@code c332030-2}（incrLastNum）</li>
 *   <li>2.2 多位数与进位：{@code -9} → {@code -10}、{@code -99} → {@code -100}（incrLastNum_multiDigitCarry）</li>
 *   <li>2.3 边界：无 "-" / "-" 结尾 / 末段非数字 / 空串 / null 原样返回（incrLastNum_invalidSuffix_returnsOriginal）</li>
 *   <li>2.4 超 int 范围解析失败：原样返回（incrLastNum_unparsable_returnsOriginal）</li>
 * </ul>
 * <h2>中文提取</h2>
 * <ul>
 *   <li>3.1 chineseOnly：中文/混合/纯数字/纯字母/特殊字符等多样本，空串与 null 原样返回（chineseOnly）</li>
 * </ul>
 * <h2>可用性处理（toAvailable）</h2>
 * <ul>
 *   <li>4.1 引号处理：单侧/双侧引号保留非引号字符；{@code ''}/{@code '}/空返回 null（toAvailableSingleQuote，Q16 回归）</li>
 * </ul>
 *
 * @since 2025/9/16
 * @version 1.0
 * @see CStrUtils
 */
@CustomLog
public class CStrUtilsTest {

    /**
     * 测试模板中缺失参数格式化为空字符串
     * 对应测试用例 1.1：format 缺失参数：{@code ${name}} 缺失时以空串兜底
     */
    @Test
    public void formatNullToEmpty() {

        val template = "My name is ${name}";

        val paramMap = CMap.of("name2", "c332030");

        val result = CStrUtils.format(template, paramMap::get, StrUtil.EMPTY);

        Assertions.assertEquals("My name is ", result);

    }

    /**
     * 测试字符串末尾数字自增
     * 对应测试用例 2.1：{@code c332030-1} → {@code c332030-2}
     */
    @Test
    public void incrLastNum() {

        val string = "c332030-1";
        val result = CStrUtils.incrLastNum(string);

        Assertions.assertEquals("c332030-2", result);

    }

    /**
     * 对应测试用例 2.2：多位数与进位：{@code -9} → {@code -10}、{@code -99} → {@code -100}
     * 2.2 多位数与进位：-9 → -10、-99 → -100（incrLastNum）
     */
    @Test
    public void incrLastNum_multiDigitCarry() {

        Assertions.assertEquals("id-10", CStrUtils.incrLastNum("id-9"));
        Assertions.assertEquals("id-100", CStrUtils.incrLastNum("id-99"));

    }

    /**
     * 对应测试用例 2.3：边界：无 "-" / "-" 结尾 / 末段非数字 / 空串 / null 原样返回
     * 2.3 边界：无 "-" / "-" 结尾 / 末段非数字 / 空串 一律原样返回（incrLastNum）
     */
    @Test
    public void incrLastNum_invalidSuffix_returnsOriginal() {

        Assertions.assertEquals("abc", CStrUtils.incrLastNum("abc"));
        Assertions.assertEquals("abc-", CStrUtils.incrLastNum("abc-"));
        Assertions.assertEquals("abc-x", CStrUtils.incrLastNum("abc-x"));
        Assertions.assertEquals("", CStrUtils.incrLastNum(""));
        Assertions.assertNull(CStrUtils.incrLastNum(null));

    }

    /**
     * 对应测试用例 2.4：超 int 范围解析失败：原样返回
     * 2.4 末段含负号或超 int 范围：解析失败按约定原样返回（incrLastNum）
     */
    @Test
    public void incrLastNum_unparsable_returnsOriginal() {

        Assertions.assertEquals("abc-2147483648", CStrUtils.incrLastNum("abc-2147483648"));

    }

    /**
     * 测试提取字符串中的中文（含空入参边界）
     * 对应测试用例 3.1：中文/混合/纯数字/纯字母/特殊字符等多样本，空串与 null 原样返回
     */
    @Test
    public void chineseOnly() {

        Assertions.assertEquals("张三", CStrUtils.chineseOnly("c张三Zhang123"));
        Assertions.assertEquals("李四", CStrUtils.chineseOnly(".李四Li456"));
        Assertions.assertEquals("王五", CStrUtils.chineseOnly("-王五Wang_789"));
        Assertions.assertEquals("测试", CStrUtils.chineseOnly("\\测试test123@example.com"));
        Assertions.assertEquals("只有中文", CStrUtils.chineseOnly("只有中文"));
        Assertions.assertEquals("", CStrUtils.chineseOnly("123456"));
        Assertions.assertEquals("", CStrUtils.chineseOnly("ABCdef"));
        Assertions.assertEquals("混合中文", CStrUtils.chineseOnly("a混合123ABC中文test"));
        // 边界：空串与 null 原样返回（不抛异常、不返回空串兜底）
        Assertions.assertEquals("", CStrUtils.chineseOnly(""));
        Assertions.assertNull(CStrUtils.chineseOnly(null));

    }

    /**
     * 测试 toAvailable 对单侧引号的处理（Q16 修复：start == end 时保留该非引号字符）
     * 对应测试用例 4.1：引号处理：单侧/双侧引号保留非引号字符；{@code ''}/{@code '}/空返回 null（toAvailableSingleQuote，Q16 回归）
     */
    @Test
    public void toAvailableSingleQuote() {

        Assertions.assertEquals("a", CStrUtils.toAvailable("'a"));
        Assertions.assertEquals("a", CStrUtils.toAvailable("'a'"));
        Assertions.assertEquals("ab", CStrUtils.toAvailable("'ab'"));
        Assertions.assertEquals("a", CStrUtils.toAvailable("a'"));
        Assertions.assertNull(CStrUtils.toAvailable("''"));
        Assertions.assertNull(CStrUtils.toAvailable("'"));
        Assertions.assertNull(CStrUtils.toAvailable(""));

    }

}
