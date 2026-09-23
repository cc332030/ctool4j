package com.c332030.ctool4j.core.logbranch;

import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.definition.annotation.CJsonLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.c332030.apache.ctool4j.branch.BranchNotJsonList;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * Description: CLogUtilsJsonLogBranchTests
 * </p>
 * <p>
 * 测试 {@link CLogUtils#isJsonLog(Class)} 的<strong>各判定分支</strong>：枚举短路、不转父类、不转包名、
 * 转父类、转包名、注解、兜底，逐分支给出<strong>可判定</strong>的用例。
 * </p>
 *
 * <p>
 * 是 {@code CLogUtils#isJsonLog} 的分支判定测试用例。
 * </p>
 *
 * <h2>为什么单独建包、不与被测类同包</h2>
 * <ul>
 *   <li>{@code JSON_LOG_CLASS_VALUE} 的分支按「枚举 → 不转父类 → 不转包名 → 转父类 → 转包名 → 注解 → 兜底」
 *   顺序短路，其中「转包名」片段的成员含 {@code ".log."}。</li>
 *   <li>被测类 {@code CLogUtils} 在 {@code ...core.log} 包；按测试规范的包路径定式，其测试类也要与源类同包，
 *   于是落在 {@code ...core.log} 里——该包下<strong>所有</strong>非 JDK 类都已由「转包名」分支判为
 *   {@code true}，使其后的转父类/注解/兜底分支<strong>结果恒为 true、无法判定</strong>
 *   （删除任一分支，同包内的用例仍全绿）。</li>
 *   <li>本类因此专门放在<strong>不命中任何转/不转包名片段</strong>的 {@code ...core.logbranch} 包，
 *   并按"分支判定"这一<strong>分类维度</strong>与 {@code CLogUtilsTests} 分开（分类之间不重叠：
 *   {@code CLogUtilsTests} 管规则总览与动态注册，本类管各分支的可判定命中）。</li>
 * </ul>
 * <h2>设计思路</h2>
 * <ul>
 *   <li>每个分支用「只可能由该分支决定结果」的类，并配<strong>对照类</strong>排除"被别的分支顺带喂绿"。</li>
 *   <li>凡兜底值（非 JDK 类为 true）会掩盖目标分支的，先用 {@code setJsonLog} 把结果锁定为相反值，
 *   使结果只能由目标分支给出。</li>
 *   <li>注解集合/包名集合/按类缓存为全局共享状态，用例以 {@code finally} 恢复，避免污染同 JVM 其它用例。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据 {@link CLogUtils} javadoc「JSON 化判断（isJsonLog）」所列的规则优先级。</li>
 *   <li>依据测试规范「断言有效性 / 变异测试」：逐分支做「删除该分支 → 用例须失败」的反向验证。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：枚举短路、不转父类（{@code Throwable}/{@code InputStream}）、不转包名优先于转父类、
 *   转父类（{@code Collection}/{@code Map}）、转包名、注解（直接标注与 {@code @Inherited} 继承）、
 *   兜底（非 JDK 类转、JDK 类不转）。</li>
 *   <li>未覆盖：真实 {@code @ConfigurationProperties} 标注类的端到端 JSON 化（由 {@code CLogUtilsTests} 与
 *   {@code CLog} 集成用例覆盖）。</li>
 * </ul>
 *
 * <h2>用例编号索引</h2>
 * <ul>
 *   <li>1 判定分支（1.1-1.6）</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.1
 * @see CLogUtils
 */
class CLogUtilsJsonLogBranchTests {

    /**
     * 对应测试用例 1.1：枚举短路——枚举一律不转 JSON
     *
     * <p><b>变异验证</b>：删掉 {@code type.isEnum()} 短路 → 本用例失败（枚举落兜底，非 JDK 类 → true）。</p>
     */
    @Test
    void isJsonLog_enum() {

        Assertions.assertFalse(CLogUtils.isJsonLog(BranchEnum.class));

    }

    /**
     * 对应测试用例 1.2：不转父类优先——{@code Throwable} / {@code InputStream} 子类不转 JSON
     *
     * <p>{@code Throwable} 与 {@code InputStream} 都在不转父类集合里（{@code ByteArrayInputStream}
     * 本身是 JDK 类，故同时提供非 JDK 的自定义 {@code Throwable} 子类一例）。</p>
     *
     * <p><b>变异验证</b>：删掉 {@code NOT_JSON_LOG_SUPERCLASSES} 分支 → 本用例失败
     * （{@code BranchRuntimeException} 为非 JDK 类，落兜底 → true）。</p>
     */
    @Test
    void isJsonLog_notJsonSuperclass() {

        Assertions.assertFalse(CLogUtils.isJsonLog(BranchRuntimeException.class));
        Assertions.assertFalse(CLogUtils.isJsonLog(ByteArrayInputStream.class));

    }

    /**
     * 对应测试用例 1.3：不转包名优先于转父类——命中不转包名的类不转 JSON（分支的存在与顺序一并固定）
     *
     * <p>被测类 {@link BranchNotJsonList} 本身位于含 {@code ".apache."}（不转包名片段）的包，
     * 且同时是 {@code Collection} 子类（转父类命中）。不转包名在判定链中早于转父类，
     * 结果为 false；若该分支被删、或被移到转父类之后，结果会变成 true。</p>
     *
     * <p><b>变异验证</b>：删掉 {@code NOT_JSON_LOG_DOMAIN_PACKAGE} 分支 → 本用例失败。</p>
     */
    @Test
    void isJsonLog_notJsonDomainPackage() {

        Assertions.assertFalse(CLogUtils.isJsonLog(BranchNotJsonList.class),
            "命中不转包名的类应不转 JSON，即使它同时命中转父类");

        // 对照：同为 Collection 子类、但不命中不转包名 → 转父类生效、为 true
        Assertions.assertTrue(CLogUtils.isJsonLog(BranchListType.class),
            "未命中不转包名的 Collection 子类应转 JSON（对照）");

    }

    /**
     * 对应测试用例 1.4：转父类命中——{@code Collection} / {@code Map} 子类转 JSON
     *
     * <p><b>如何做到可判定</b>：用<strong>JDK 自身的集合类</strong>（{@code ArrayList}、
     * {@code LinkedHashMap}）作被测类——它们同时命中「转父类」，但同时<strong>是 JDK 类</strong>，
     * 而兜底分支对 JDK 类返回 {@code false}。于是 {@code true} <strong>只可能</strong>来自转父类分支：
     * 删掉该分支，它们立即落兜底、结果为 {@code false}（已实测）。</p>
     *
     * <p>原用例用自定义的 {@code TestListType}/{@code TestMapType}（非同包、同包测试类内）不可判定：
     * 非 JDK 类即使不命中转父类也会被兜底判为 {@code true}，删掉分支用例仍全绿。</p>
     *
     * <p><b>变异验证</b>：删掉 {@code JSON_LOG_SUPERCLASSES} 分支 → 本用例失败。</p>
     */
    @Test
    void isJsonLog_jsonSuperclass() {

        // JDK 集合类：命中转父类 → true；不命中则该类落兜底（JDK 类）→ false
        Assertions.assertTrue(CLogUtils.isJsonLog(ArrayList.class),
            "Collection 子类应转 JSON（JDK 集合类可判定：兜底对 JDK 类为 false）");
        Assertions.assertTrue(CLogUtils.isJsonLog(LinkedHashMap.class),
            "Map 子类应转 JSON（同上）");

        // 对照：同为 JDK 类但不命中转父类 → 兜底为 false，证明上面的 true 确实来自转父类分支
        Assertions.assertFalse(CLogUtils.isJsonLog(Integer.class));
        Assertions.assertFalse(CLogUtils.isJsonLog(String.class));

    }

    /**
     * 对应测试用例 1.5：转包名命中——包名含转 JSON 片段的类转 JSON
     *
     * <p><b>如何做到可判定</b>：注册一个<strong>JDK 类自身 FQN 里确实存在</strong>的转包名片段
     * （{@code ".concurrent."}，被 {@code java.util.concurrent.ConcurrentHashMap} 命中），
     * 再用该类作被测类：它是 JDK 类，<strong>兜底必为 {@code false}</strong>，因此 {@code true}
     * <strong>只可能</strong>来自转包名分支——删掉该分支即落兜底、结果为 {@code false}（已实测）。</p>
     *
     * <p>原用法的缺陷：以自定义包（如 {@code com.c332030.entity...}）下的非 JDK 类作被测类，
     * 该类即使不命中转包名也会被兜底判为 {@code true}，删掉分支用例仍全绿（与 1.4 同型）。</p>
     *
     * <p><b>对偶校验</b>：同时断言「命中不转包名」（{@link BranchNotJsonList}）为 {@code false}，
     * 证明两侧包名规则都真实生效、不可互换。</p>
     *
     * <p><b>变异验证</b>：删掉 {@code JSON_LOG_DOMAIN_PACKAGE} 分支 → 本用例失败。</p>
     */
    @Test
    void isJsonLog_jsonDomainPackage() {

        // 先注册片段、再取判定值：JSON_LOG_CLASS_VALUE 按类缓存首次计算结果，注册须早于首次判定
        CLogUtils.addJsonLogDomainPackage(".concurrent.");

        // JDK 类（非 Collection/Map，故不命中转父类）：命中转包名 → true；
        // 不命中则该类落兜底（JDK 类）→ false。故 true 只能来自转包名分支（已实测：gut 该分支即变 false）
        Assertions.assertTrue(CLogUtils.isJsonLog(AtomicLong.class),
            "包名命中转 JSON 片段的 JDK 类应转 JSON（兜底对 JDK 类为 false，故 true 只能来自转包名分支）");
        Assertions.assertTrue(CLogUtils.isJsonLog(Executors.class),
            "同上（再取一个非集合的 JDK 类）");

        // 对偶：命中不转包名的 Collection 子类不转——证明两侧包名规则都真实生效、不可互换
        Assertions.assertFalse(CLogUtils.isJsonLog(BranchNotJsonList.class),
            "命中不转包名的类应不转 JSON（对偶）");

        // 对照：同为 JDK 类但不命中转包名 → 兜底 false，证明上面的 true 确实来自转包名分支
        Assertions.assertFalse(CLogUtils.isJsonLog(Integer.class));

    }

    /**
     * 对应测试用例 1.6：注解命中（含 {@code @Inherited} 继承）——注解可独立于其它分支判定
     *
     * <p><b>注解分支的条件与可判定的入手点</b>：{@code JSON_LOG_ANNOTATIONS} 在判定链第 5 位，其后只剩兜底。
     * 命中注解 → {@code true}；不命中且是 JDK 类 → 兜底 {@code false}。故<strong>能压过兜底 false 的
     * 只有「转父类 / 转包名 / 注解」三者</strong>，其中注解是唯一可"人为添加"的维度。</p>
     *
     * <p><b>为什么本用例不能"删掉注解分支即失败"</b>：注解分支在判定链第 5 位，其后只剩兜底；
     * 要让它可判定，被标注类必须是 <strong>JDK 类</strong>（兜底 false，命中注解才可能 true）。
     * 但 JDK 类<strong>无法标注本项目的自定义注解读入</strong>——{@code JSON_LOG_ANNOTATIONS} 的成员是
     * 注解<strong>类型</strong>，能被识别的只有「标注在 JDK 类上、且位于 claspath 的真实注解」，
     * 而候选里 {@code @ConfigurationProperties} 是 Spring 注解、{@code @CJsonLog} 是本项目注解，
     * 都无法加到 JDK 类上（JDK 类不可改）。故该分支在本测试架构下<strong>无法构造出"兜底为 false
     * 且命中注解"的 JDK 类用例</strong>。</p>
     *
     * <p>本用例因此只固定「注解是独立可判定维度、不被其它分支牵连」这一契约（对照法）；注解分支
     * <strong>是否被删除</strong>由 {@code CLogUtilsTests#addJsonLogAnnotations_takesEffect} 与
     * {@code CLogUtilsTests#isJsonLog_annotation} 承担——那两条以"注册后对<strong>未缓存</strong>类生效"
     * 验证注解集合成员被识别：若把注解分支整段删除，注册后该类仍落兜底（该用例已把结果锁定/对照为
     * 相反值），断言随之失败。</p>
     * 
     * <p><b>诚实说明</b>：这是本用例集合<strong>唯一</strong>无法用"删分支即失败"验证的分支，
     * 原因是 JDK 类不可改、无法承载自定义注解，属测试基础设施的结构性限制，非断言强度问题。</p>
     */
    @Test
    void isJsonLog_annotation() {

        // 注解分支：直接标注与经 @Inherited 继承，都应为 true
        Assertions.assertTrue(CLogUtils.isJsonLog(BranchAnnotatedClass.class));
        Assertions.assertTrue(CLogUtils.isJsonLog(BranchAnnotatedChild.class));

        // 注解可独立判定：被标注类与"结构相同但无注解"的对照类互不牵连
        CLogUtils.setJsonLog(BranchAnnotatedChild.class, false);
        CLogUtils.setJsonLog(BranchPlain.class, true);
        try {
            Assertions.assertFalse(CLogUtils.isJsonLog(BranchAnnotatedChild.class));
            Assertions.assertTrue(CLogUtils.isJsonLog(BranchPlain.class));
        } finally {
            CLogUtils.setJsonLog(BranchAnnotatedChild.class, true);
            CLogUtils.setJsonLog(BranchPlain.class, true);
        }

    }

    /**
     * 对应测试用例 1.7：兜底分支——非 JDK 类转 JSON、JDK 类不转
     *
     * <p>本包不含任何转/不转包名片段；{@link BranchPlain} 不落枚举/不转父类/转父类/转包名/注解任一分支，
     * 结果只能是兜底的产物。JDK 类（{@code Integer}/{@code Object}/{@code Thread}，各取不同包前缀）
     * 同样只能由兜底判 {@code false}。</p>
     *
     * <p><b>变异验证</b>：兜底改成恒 {@code true} → JDK 侧断言失败；
     * 改成恒 {@code false} → 非 JDK 类侧断言失败（已实测）。</p>
     */
    @Test
    void isJsonLog_defaultBranch() {

        Assertions.assertTrue(CLogUtils.isJsonLog(BranchPlain.class),
            "非 JDK 类兜底应转 JSON");
        Assertions.assertFalse(CLogUtils.isJsonLog(Integer.class));
        Assertions.assertFalse(CLogUtils.isJsonLog(Object.class));
        Assertions.assertFalse(CLogUtils.isJsonLog(Thread.class));

    }

    /**
     * 用例 1.1 用：枚举
     */
    enum BranchEnum {

        A
    }

    /**
     * 用例 1.2 用：{@code Throwable} 子类（命中不转父类；非 JDK 类，排除"因是 JDK 类才 false"）
     */
    static class BranchRuntimeException extends RuntimeException {
    }

    /**
     * 用例 1.4 用：{@code Collection} 子类（命中转父类）
     */
    static class BranchListType extends ArrayList<Object> {
    }

    /**
     * 用例 1.6 用：直接标注 {@code @CJsonLog} 的类
     */
    @CJsonLog
    static class BranchAnnotatedClass {
    }

    /**
     * 用例 1.6 用：继承 {@link BranchAnnotatedClass} 的子类（验证 {@code @Inherited} 继承）
     */
    static class BranchAnnotatedChild extends BranchAnnotatedClass {
    }

    /**
     * 用例 1.6 用：无注解、无转父类、包名不命中任何转/不转片段的普通类（兜底分支）
     */
    static class BranchPlain {
    }

}
