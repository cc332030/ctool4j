package com.c332030.ctool4j.core.test.classes;

import cn.hutool.core.date.DateUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.test.definition.model.UserDto;
import com.c332030.ctool4j.test.definition.model.UserRsp;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * <p>
 * Description: CBeanUtilsTest
 * </p>
 *
 * <h2>测试架构与组织</h2>
 * <p>测试按"验证职责"分三类，避免单类测试文件膨胀：</p>
 * <ul>
 *   <li>{@code CBeanUtilsCompatibilityTests}（13 用例）：功能完整性测试——重构/优化前后行为一致性验证。</li>
 *   <li>{@code CBeanUtilsMoreTests}（36 用例）：正例、反例、边界与新增行为验证。</li>
 *   <li>{@code CBeanUtilsBenchmarkTests}（12 用例）+ {@code CBenchmarkRunner}：性能对比基准，独立于单元测试执行。</li>
 * </ul>
 * <h2>测试手法：内嵌旧语义参考实现</h2>
 * <p>兼容性测试的核心手法：在测试类内嵌旧语义参考实现（{@code oldCopy}/{@code oldCopyMap}/{@code oldToMap}/ {@code oldCopyFromArr}，按重构前"copy 经 toMap 中转、运行期按实际值类型转换"语义实现）， 逐字段对比新旧实现结果 equal。</p>
 * <p>设计考量：</p>
 * <ul>
 *   <li>对比基准选"旧语义参考实现"而非断言具体值：即使具体值变化也能暴露语义差异；若断言具体值，</li>
 *   <li>重构后新值正确但语义不同（如 Date 格式化 vs toString）无法被发现。</li>
 *   <li>参考实现独立于被测实现书写（不调用被测方法），保证基准不受重构污染。</li>
 *   <li>覆盖场景刻意包含"语义易错点"：objectStr 优先级、集合跳过、无转换器跳过、原始类型同型、</li>
 *   <li>宽声明类型（Object/父类）等——这些是计划期/运行期分派最容易出偏差的地方。</li>
 * </ul>
 * <h2>用例设计依据</h2>
 * <ul>
 *   <li>依据功能设计的功能目标与计划预计算方案：copy 直连目标字段、</li>
 *   <li>运行期仅遍历字段数组 + null 判断</li>
 *   <li>依据需求对边界行为的约定：null 过滤、key 冲突抛异常、结果不可变、空结果 {@code CMap.of()}、JDK 源不复制</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖）：宽声明类型回退、无转换器跳过、objectStr 优先级为分派分支</li>
 * </ul>
 * <h2>覆盖场景</h2>
 * <ul>
 *   <li>全字段复制：基础/包装/日期/Object/父类型声明/继承/null/final/集合/无转换器字段</li>
 *   <li>objectStr 优先级：Object/Serializable 声明实际 Date 转 String 命中格式化（{@code copyObjectStrLowestPriority}）</li>
 *   <li>JDK 源不复制（{@code toMapJdkClass} 等）、null 源/目标边界</li>
 *   <li>Map 源：基础/null/类型转换/final/集合跳过</li>
 *   <li>四个复制入口：Object+Class/supplier、Map+Class/supplier</li>
 *   <li>copyFromArr 反序覆盖、copyList/copyListFromMap</li>
 *   <li>toMap：null 过滤/final/集合/不可变/下划线/json 命名/null/JDK 源/key 冲突抛异常</li>
 *   <li>newInstance：正常类、无 public 无参构造器抛异常</li>
 *   <li>无转换器跳过：{@code copyNoConverterSkip}（StringBuilder→StringBuffer）</li>
 *   <li>原始类型同型：{@code copyPrimitiveTypeFields}（int→int 直接写入）</li>
 *   <li>类型转换覆盖（CClassConvert 注册转换器、对象路径计划期 fast 写入）：</li>
 *   <li>包装→基础拆箱 {@code copyWrapperToPrimitive}、Long→int {@code copyLongToInt}、int→Long {@code copyIntToLong}</li>
 *   <li>String→数字/布尔 {@code copyStrToNumber}（toInt/toLong/toBigDecimal/toFloat/toDouble/toBoolean）</li>
 *   <li>数字/布尔→String {@code copyNumberToStr}（intStr/longStr/bigDecimalStr/floatStr/doubleStr/booleanStr）</li>
 *   <li>日期互转 {@code copyDateConvert}（parseDateTime→String↔Date、toMills/fromMills→Date↔Long、</li>
 *   <li>toInstant/toDate→Date↔Instant、formatDateTime→Date→String）</li>
 *   <li>数字→BigDecimal {@code copyToBigDecimal}（int/long/Integer/Long/double/float→BigDecimal）</li>
 *   <li>BigDecimal→double/float、Float→double {@code copyDecimalToFloatDouble}</li>
 *   <li>数字互转 {@code copyNumConvert}（long/Long→Integer 走 toInt(long)/toInt(Long)、Integer→Long 走</li>
 *   <li>toLong(Integer)、Integer→long 走 longValue(Integer)——包装与原始非等价故走转换器而非 SELF）</li>
 *   <li>String→原始 float/double {@code copyStrToPrimitiveFloatDouble}（floatValue(String)/doubleValue(String)，</li>
 *   <li>与 String→Float/Double 的 toFloat/toDouble 区分）</li>
 *   <li>ICValue 枚举→Integer/String {@code copyEnumToValue}（toEnumIntegerValue/toEnumStringValue）</li>
 * </ul>
 * <h2>用例设计要点与边界</h2>
 * <ul>
 *   <li>异常输入用例覆盖任意非法值：如 {@code CClassConvertTests} 的 toInt/toLong 覆盖 "1.5"、"1,000"、</li>
 *   <li>"!@#"、"0x1F"、超长数字等（不局限于一种看似合理的非法值）。</li>
 *   <li>边界行为锁定期望：null key 过滤、null 值跳过、key 冲突抛 IllegalStateException、结果不可变</li>
 *   <li>（{@code Collections.unmodifiableMap}）、空结果返回 {@code CMap.of()}。</li>
 *   <li>已知取舍（测试侧）：原始类型同型（int→int）旧实现跳过、新实现写入，兼容性测试按新语义断言</li>
 *   <li>并标注差异；集合父类型实际持集合同理。</li>
 * </ul>
 * <h2>已知限制与未覆盖场景</h2>
 * <ul>
 *   <li>{@code copy(Object, CSupplier)} / {@code copyList(Collection, CSupplier)} 经 Map 中转：已知</li>
 *   <li>缺陷（原始类型同型字段经 Map 中转丢失）已连带解决——CConvertUtils 以</li>
 *   <li>{@code ClassUtil.isAssignable} 判定可直接赋值后，装箱值经 Map 中转可拆箱直写原始类型字段；</li>
 *   <li>已补兼容性用例 {@code copySupplierViaMapConsistency} 锁定 supplier 入口与直接路径一致行为。</li>
 *   <li>极端场景未专门设计用例：超大字段数 Bean（基准仅覆盖 8 字段典型 Bean）、深继承层级</li>
 *   <li>同名字段冲突（按名称匹配的固有语义）、自引用 Bean（copy 仅按同名字段复制、非深拷贝，</li>
 *   <li>无循环风险）。风险低，暂不补，记录备查。</li>
 *   <li>多线程并发预热竞争：计划缓存基于 {@code ClassValue}（线程安全），未做专门并发压力测试。</li>
 * </ul>
 * <h2>兼容性考量（能测的与不能测的）</h2>
 * <p>能测（已执行）：</p>
 * <ul>
 *   <li>功能完整性（重构前后行为一致）：{@code CBeanUtilsCompatibilityTests} 13 用例，内嵌旧语义</li>
 *   <li>参考实现逐字段对比。</li>
 *   <li>跨操作系统：CBeanUtils 无平台特定 API（无文件/网络/环境变量依赖），Java 跨平台语义</li>
 *   <li>天然成立；已在 Windows 实测，Linux 未执行（理论一致，记录备查）。</li>
 * </ul>
 * <p>不能测（环境受限，记录用例设计与考量）：</p>
 * <ul>
 *   <li>跨 JDK 版本升级：项目 Java 8 目标，依赖 {@code java.lang.invoke.MethodHandle}、</li>
 *   <li>{@code java.lang.ClassValue}（JDK7 引入的稳定 API）与反射 Field（目标为用户类而非 JDK 内部类，</li>
 *   <li>无 JDK9+ 模块化 setAccessible 限制）；本机仅 JDK 8，更高版本（9/11/17/21）未实测。</li>
 *   <li>若未来有对应环境需补充升级验证。</li>
 *   <li>多线程并发压力：本地无压测基准，未实测高并发下缓存 get 竞争开销（ClassValue 已保证</li>
 *   <li>线程安全，风险低）。</li>
 * </ul>
 * <h2>性能数据（100000 次迭代，第二轮全部初始化后计时）</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>实现方式</th>
 *     <th>Avg(ns/op)</th>
 *     <th>说明</th>
 *   </tr>
 *   <tr>
 *     <td>手工 setter</td>
 *     <td>29.9</td>
 *     <td>基线</td>
 *   </tr>
 *   <tr>
 *     <td>cglib BeanCopier</td>
 *     <td>33.0</td>
 *     <td>对比</td>
 *   </tr>
 *   <tr>
 *     <td>CBeanUtils.copy（复用目标）</td>
 *     <td>107.2</td>
 *     <td>~16 次 handle 调用 ≈ 6.7 ns/次</td>
 *   </tr>
 *   <tr>
 *     <td>手工 toMap</td>
 *     <td>149.9</td>
 *     <td>基线</td>
 *   </tr>
 *   <tr>
 *     <td>CBeanUtils.copy</td>
 *     <td>225.4</td>
 *     <td>相对 Spring 927.6 快 4.1 倍、hutool 6138.2 快 27 倍</td>
 *   </tr>
 *   <tr>
 *     <td>CBeanUtils.toMap</td>
 *     <td>353.2</td>
 *     <td>相对手工 149.9 慢 2.4 倍，瓶颈在 map 写入（预期内）</td>
 *   </tr>
 *   <tr>
 *     <td>Spring BeanUtils</td>
 *     <td>927.6</td>
 *     <td>对比</td>
 *   </tr>
 *   <tr>
 *     <td>hutool BeanUtil</td>
 *     <td>6138.2</td>
 *     <td>对比</td>
 *   </tr>
 * </table>
 * <p>MethodHandle 动态分发（含 Object 签名类型检查）相对编译期静态内联存在固有差距， 排除字节码方向后无法进一步消除。</p>
 * <h2>对象源复制</h2>
 * <ul>
 *   <li>1.1.1 对象→对象基础复制（copyObjectToObject）</li>
 *   <li>1.1.2 对象复制跳过集合与数组字段（copyObjectToObjectSkipCollectionAndArray）</li>
 *   <li>1.1.3 对象复制跳过 final 字段（copyObjectToObjectSkipFinal）</li>
 *   <li>1.1.4 无转换器字段跳过（copyNoConverterSkip）</li>
 *   <li>1.1.5 父类型声明字段回退运行期分派（copyParentDeclaredTypeFallback）</li>
 *   <li>1.1.6 用户 DTO → 响应对象复制（copyUserDtoToRsp）</li>
 * </ul>
 * <h2>Map 源复制</h2>
 * <ul>
 *   <li>1.2.1 Map→对象基础复制（copyMapToObject）</li>
 *   <li>1.2.2 Map→对象 null 值跳过（copyMapToObjectSkipNull）</li>
 *   <li>1.2.3 Map→对象类型转换（copyMapToObjectTypeConvert）</li>
 *   <li>1.2.4 Map→对象结果不可变（copyMapUnmodifiable）</li>
 *   <li>1.2.5 Map 源复制跳过集合与数组字段（copyClassEntrySkipCollectionAndArray）</li>
 * </ul>
 * <h2>复制边界</h2>
 * <ul>
 *   <li>1.3.1 源/目标为 null 边界（copyNullEdge）</li>
 *   <li>1.3.2 JDK 类源不复制（copyJdkClassSource）</li>
 *   <li>1.3.3 对象→对象复制（copyClassEntry 相关覆盖）</li>
 *   <li>1.3.4 复制合并已有对象（copyMerge，CBeanUtilsTest）</li>
 *   <li>1.3.5 类型不匹配复制（copyTypeUnmatched，CBeanUtilsTest）</li>
 *   <li>1.3.6 复制跳过集合与 Map（copySkipCollectionAndMap，CBeanUtilsTest）</li>
 * </ul>
 * <h2>集合/数组复制（copyList/copyListFromMap/copyFromArr）</h2>
 * <ul>
 *   <li>2.1 对象列表复制（copyList）</li>
 *   <li>2.2 Map 列表复制（copyListFromMap）</li>
 *   <li>2.3 数组反序覆盖（copyFromArr）</li>
 * </ul>
 * <h2>对象转 Map（toMap）</h2>
 * <ul>
 *   <li>3.1 基础转 Map（toMapBasic）</li>
 *   <li>3.2 下划线命名（toMapUnderline）</li>
 *   <li>3.3 json 命名（toMapJsonName）</li>
 *   <li>3.4 key 冲突抛异常（toMapConflict）</li>
 *   <li>3.5 空对象转空 Map（toMapEmpty）</li>
 *   <li>3.6 null 源转空 Map（toMapNull）</li>
 *   <li>3.7 含 final 字段转 Map（toMapIncludesFinal）</li>
 *   <li>3.8 JDK 类源转空 Map（toMapJdkClass）</li>
 * </ul>
 * <h2>类型转换（copy 时类型转换）</h2>
 * <ul>
 *   <li>4.1 包装→基础拆箱（copyWrapperToPrimitive）</li>
 *   <li>4.2 Long→int（copyLongToInt）</li>
 *   <li>4.3 int→Long（copyIntToLong）</li>
 *   <li>4.4 数字/布尔→String（copyNumberToStr：intStr/longStr/bigDecimalStr/floatStr/doubleStr/booleanStr）</li>
 *   <li>4.5 日期互转（copyDateConvert：String↔Date、Date↔Long、Date↔Instant、Date→String）</li>
 *   <li>4.6 数字→BigDecimal（copyToBigDecimal）</li>
 *   <li>4.7 BigDecimal→double/float、Float→double（copyDecimalToFloatDouble）</li>
 *   <li>4.8 数字互转（copyNumConvert）</li>
 *   <li>4.9 String→原始 float/double（copyStrToPrimitiveFloatDouble）</li>
 *   <li>4.10 枚举→Integer/String（copyEnumToValue）</li>
 *   <li>4.11 objectStr 优先级最低（copyObjectStrLowestPriority）</li>
 *   <li>4.12 String→数字/布尔（copyStrToNumber：toInt/toLong/toBigDecimal/toFloat/toDouble/toBoolean）</li>
 *   <li>4.13 对象间类型转换（copyTypeConvert，CBeanUtilsTest：UserDto↔UserRsp 的 sex/status/score/grade/时间互转）</li>
 * </ul>
 * <h2>复制兼容性</h2>
 * <ul>
 *   <li>5.1.1 全字段复制兼容（copyFullCompatibility）</li>
 *   <li>5.1.2 原始类型同型写入兼容（copyPrimitiveTypeFields，按新语义断言并标注差异）</li>
 *   <li>5.1.3 JDK 源兼容（copyJdkSourceCompatibility）</li>
 *   <li>5.1.4 null 源兼容（copyNullSourceCompatibility）</li>
 *   <li>5.1.5 Map 源兼容（copyMapCompatibility）</li>
 *   <li>5.1.6 entry 入口兼容（copyEntryCompatibility）</li>
 *   <li>5.1.7 数组反序覆盖兼容（copyFromArrCompatibility）</li>
 *   <li>5.1.8 列表复制兼容（copyListCompatibility）</li>
 *   <li>5.1.9 supplier 入口经 Map 中转与直连一致性（copySupplierViaMapConsistency）</li>
 * </ul>
 * <h2>转 Map 兼容性</h2>
 * <ul>
 *   <li>5.2.1 toMap 兼容（toMapCompatibility）</li>
 *   <li>5.2.2 toMap 命名兼容（toMapNamedCompatibility）</li>
 *   <li>5.2.3 toMap JDK/null 兼容（toMapJdkNullCompatibility）</li>
 * </ul>
 * <h2>newInstance 兼容性</h2>
 * <ul>
 *   <li>5.3.1 newInstance 正常与无参构造器缺失（newInstanceCompatibility）</li>
 * </ul>
 * <h2>性能基准（BenchmarkTests，独立于单元测试）</h2>
 * <ul>
 *   <li>6.1 copy/toMap 各入口与手工基线、cglib/Spring/hutool/Jackson 对比（100000 迭代，两轮：预热 + 计时），结果与分析见设计文档 §5 性能数据</li>
 * </ul>
 *
 * @since 2025/11/20
 * @version 1.0
 */
public class CBeanUtilsTest {

    /**
     * 测试属性复制时合并已有对象
     * 对应测试用例 1.3.4：复制合并已有对象（copyMerge，CBeanUtilsTest）
     */
    @Test
    public void copyMerge() {

        val username = "c332030";
        val password = "332030";
        val age = 18;

        val user = UserDto.builder()
                .userName(username)
                .age(age)
                .build();

        val passwordOnly = UserDto.builder()
                .password(password)
                .build();

        val userNew = CBeanUtils.copy(user, UserDto.class);
        CBeanUtils.copy(passwordOnly, userNew);

        Assertions.assertEquals(username, userNew.getUserName());
        Assertions.assertEquals(password, userNew.getPassword());
        Assertions.assertEquals(age, userNew.getAge());

    }

    /**
     * 测试属性类型不匹配时的复制行为
     * 对应测试用例 1.3.5：类型不匹配复制（copyTypeUnmatched，CBeanUtilsTest）
     */
    @Test
    public void copyTypeUnmatched() {

        val intValue = 1;
        val bigDecimalValue = BigDecimal.valueOf(intValue);

        val user1 = UserDto.builder()
                .amount(1)
                .build();

        val userRsp1 = CBeanUtils.copy(user1, UserRsp.class);
        Assertions.assertEquals(bigDecimalValue, userRsp1.getAmount());

        val userRsp2 = UserRsp.builder()
                .amount(bigDecimalValue)
                .build();
        val user2 = CBeanUtils.copy(userRsp2, UserDto.class);
        Assertions.assertNull(user2.getAmount());

    }

    /**
     * 测试属性复制时的类型转换
     * 对应测试用例 4.13：对象间类型转换（copyTypeConvert，CBeanUtilsTest：UserDto↔UserRsp 的 sex/status/score/grade/时间互转）
     */
    @Test
    public void copyTypeConvert() {

        val sexInt = 1;
        val statusInt = 1;
        val score = 99;
        val grade = new Long(5);

        val dateStr = "2025-11-20 00:00:00";

        val user = UserDto.builder()
                .sex(sexInt)
                .status(String.valueOf(statusInt))
                .score(score)
                .grade(grade)
                .createTime(DateUtil.parse(dateStr))
                .updateTime(dateStr)
                .build();

        val userRsp = CBeanUtils.copy(user, UserRsp.class);
        Assertions.assertEquals(String.valueOf(sexInt), userRsp.getSex());
        Assertions.assertEquals(statusInt, userRsp.getStatus());
        Assertions.assertEquals(score, userRsp.getScore());
        Assertions.assertEquals(grade.intValue(), userRsp.getGrade());
        Assertions.assertEquals(dateStr, userRsp.getCreateTime());
        Assertions.assertEquals(dateStr, DateUtil.formatDateTime(userRsp.getUpdateTime()));

    }

    /**
     * 测试属性复制时跳过集合与 Map
     * 对应测试用例 1.3.6：复制跳过集合与 Map（copySkipCollectionAndMap，CBeanUtilsTest）
     */
    @Test
    public void copySkipCollectionAndMap() {

        val roles = CList.of("role1", "role1");
        val tags = CMap.of("tag1", "tag1");
        val user = UserDto.builder()
                .roles(roles)
                .tags(tags)
                .build();

        val userRsp = CBeanUtils.copy(user, UserRsp.class);
        Assertions.assertNull(userRsp.getRoles());
        Assertions.assertNull(userRsp.getTags());

    }

}
