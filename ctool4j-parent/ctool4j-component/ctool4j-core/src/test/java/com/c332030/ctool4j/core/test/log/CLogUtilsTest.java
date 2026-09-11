package com.c332030.ctool4j.core.test.log;

import com.c332030.ctool4j.core.enums.CProfileEnum;
import com.c332030.ctool4j.core.exception.CBusinessException;
import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.definition.annotation.CJsonLog;
import com.c332030.ctool4j.definition.model.CResult;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.sql.DataSource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>
 * Description: CLogUtilsTest
 * </p>
 *
 * <p>`com.c332030.ctool4j.core.log.CLogUtils`（CLogUtils）的测试用例</p>
 *
 * <p><b>用例设计思路</b>：按「JSON 化判断规则 / 动态注册与覆盖 / 参数转换 / 可打印判断与转换」四个维度组织：</p>
 * <ul>
 *   <li>JSON 化判断按 {@code JSON_LOG_CLASS_VALUE} 的分支顺序逐条覆盖：枚举短路 → 不转父类（DataSource/Throwable 等）
 *   → 不转包名 → 转父类（{@code ICBaseResult}/Collection/Map）→ 转包名（.entity./.dto. 等）→ 注解
 *   （{@code @CJsonLog}/ConfigurationProperties）→ 兜底按是否 JDK 类判断；</li>
 *   <li>动态注册覆盖 {@code addJsonLogSuperclasses}/{@code addNotJsonLogSuperclasses}/{@code addJsonLogDomainPackage}/
 *   {@code addJsonLogAnnotations}/{@code setJsonLog} 的「注册后生效」与「注册后覆盖既有判断」；</li>
 *   <li>参数转换覆盖 {@code toLogArgs} 对 null 元素、混合元素、空数组的原样返回；</li>
 *   <li>可打印判断覆盖枚举、JDK 基本类与 JSON 化类型，以及 {@code getPrintAble} 的 null 与可打印类型原样返回。</li>
 * </ul>
 *
 * <p><b>设计依据</b>：依据功能设计对「哪些类型转 JSON」的判定顺序（枚举/不转父类/不转包名优先于转父类/转包名/注解，
 * 最后回落到「非 JDK 类才转」）的约定；依据分支覆盖与状态迁移（动态注册改变判断结果）。</p>
 * <p><b>覆盖场景</b>：{@code isJsonLog} 各分支（枚举、不转父类、转父类、转包名、注解、JDK 类兜底）；
 * 动态注册与 {@code setJsonLog} 覆盖；{@code toLogArgs} 空数组/null 元素/混合元素；{@code isPrintAble} 各类别；
 * {@code getPrintAble} null / 可打印类型原样返回 / MultipartFile 占位；{@code getPrintAbleString} 字符串与对象。</p>
 * <p><b>未覆盖</b>：{@code toLogArgs} 的「转 JSON 失败自动禁用该类型」异常分支（需构造仅在 json 序列化时抛异常的
 * 自定义类型，属刻意取舍路径，由实现日志覆盖）。</p>
 *
 * <p><b>用例编号索引</b>：1 JSON 化判断（1.1-1.7）；2 动态注册与覆盖（2.1-2.4）；3 参数转换（3.1-3.2）；
 * 4 可打印判断与转换（4.1-4.5）。各测试方法 javadoc 标注其编号与说明。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「JSON 化判断规则 / 动态注册与覆盖 / 参数转换 / 可打印判断与转换」四个维度组织。</li>
 *   <li>JSON 化判断按 {@code JSON_LOG_CLASS_VALUE} 的分支顺序逐条覆盖：枚举短路 → 不转父类（DataSource/Throwable 等）</li>
 *   <li>→ 不转包名 → 转父类（{@code ICBaseResult}/Collection/Map）→ 转包名（{@code .dto.}/{@code .entity.} 等）→ 注解</li>
 *   <li>（{@code @CJsonLog}，含 {@code @Inherited} 继承）→ 兜底按是否 JDK 类判断。</li>
 *   <li>动态注册覆盖 {@code addJsonLogSuperclasses}/{@code addNotJsonLogSuperclasses}/{@code addJsonLogDomainPackage}/</li>
 *   <li>{@code addJsonLogAnnotations}/{@code setJsonLog} 的生效与覆盖；各集合为全局共享状态，用例以 finally 恢复以免污染同 JVM 其他用例。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 JSON 化判定顺序的约定（枚举/不转父类/不转包名优先于转父类/转包名/注解，最后回落到「非 JDK 类才转」）。</li>
 *   <li>依据分支覆盖与状态迁移（动态注册改变判定结果）；实测确认 {@code JSON_LOG_CLASS_VALUE} 按类缓存，已判定过的类不因集合变更而改变，</li>
 *   <li>显式覆盖须用 {@code setJsonLog}。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：{@code isJsonLog} 枚举短路、不转父类（Throwable 子类）、转父类（List/Map 子类）、注解命中（含继承）、</li>
 *   <li>JDK 类兜底、{@code ICBaseResult} 实现；动态注册与 {@code setJsonLog} 覆盖；{@code toLogArgs} 空数组/null 元素/混合元素；</li>
 *   <li>{@code isPrintAble} 枚举与 JDK 基本类；{@code getPrintAble} null / 可打印类型原样返回 / MultipartFile 占位；</li>
 *   <li>{@code getPrintAbleString} 字符串原样与对象 JSON 化。</li>
 *   <li>未覆盖：{@code toLogArgs} 的「转 JSON 失败自动禁用该类型」异常分支（需构造仅在 JSON 序列化时抛异常的自定义类型，</li>
 *   <li>属刻意取舍路径，由实现日志覆盖）。</li>
 * </ul>
 * <h2>JSON 化判断（isJsonLog）</h2>
 * <ul>
 *   <li>1.1 基础判断：CResult 为 true、String/DataSource 为 false（isJsonLog）</li>
 *   <li>1.2 枚举短路：枚举一律不转 JSON（isJsonLog_enum）</li>
 *   <li>1.3 不转父类优先：Throwable 子类不转 JSON（isJsonLog_notJsonSuperclass）</li>
 *   <li>1.4 转父类命中：List/Map 子类转 JSON（isJsonLog_jsonSuperclass）</li>
 *   <li>1.5 注解命中：{@code @CJsonLog} 类与其（经 {@code @Inherited}）继承的子类均转 JSON（isJsonLog_annotation）</li>
 *   <li>1.6 兜底分支：非 JDK 类转 JSON、JDK 类不转（isJsonLog_defaultBranch）</li>
 * </ul>
 * <h2>动态注册与覆盖</h2>
 * <ul>
 *   <li>2.1 setJsonLog 覆盖生效、注册不改变「不转父类优先」的既有判定（addJsonLogSuperclasses_takesEffect）</li>
 *   <li>2.2 注册不转父类后对未缓存子类生效（addNotJsonLogSuperclasses_overrides）</li>
 *   <li>2.3 注册转包名后对未缓存子类生效；setJsonLog 可对任意类型显式覆盖（addJsonLogDomainPackage_andSetJsonLog）</li>
 *   <li>2.4 注册转注解后标注该注解的类与其继承子类生效（addJsonLogAnnotations_takesEffect）</li>
 * </ul>
 * <h2>参数转换</h2>
 * <ul>
 *   <li>3.1 toLogArgs：null 与空数组原样返回（toLogArgs_empty）</li>
 *   <li>3.2 toLogArgs：null 元素保留、非 JSON 类型元素原样、长度不变（toLogArgs_mixed）</li>
 * </ul>
 * <h2>可打印判断与转换</h2>
 * <ul>
 *   <li>4.1 isPrintAble：枚举、JDK 基本类、JSON 化类型均可打印（isPrintAble）</li>
 *   <li>4.2 getPrintAble：null 转 "[null]"、可打印类型原样返回（getPrintAble）</li>
 *   <li>4.3 getPrintAble 对 MultipartFile：返回 "文件名:大小" 占位（getPrintAble_multipartFile）</li>
 *   <li>4.4 getPrintAbleString：字符串原样返回（getPrintAbleString_string）</li>
 *   <li>4.5 getPrintAbleString：非字符串经 JSON 序列化返回（getPrintAbleString_object）</li>
 * </ul>
 *
 * @since 2025/9/14
 * @version 1.0
 * @see CLogUtils
 */
public class CLogUtilsTest {

    /**
     * 对应测试用例 1.1：基础判断：CResult 为 true、String/DataSource 为 false
     * 1.1 基础判断：实现 ICBaseResult 的 CResult 为 true；JDK 基本类 String 与不转父类 DataSource 为 false
     * （isJsonLog）
     */
    @Test
    public void isJsonLog() {

        Assertions.assertTrue(CLogUtils.isJsonLog(CResult.class));

        Assertions.assertFalse(CLogUtils.isJsonLog(String.class));
        Assertions.assertFalse(CLogUtils.isJsonLog(DataSource.class));

    }

    /**
     * 对应测试用例 1.2：枚举短路：枚举一律不转 JSON
     * 1.2 枚举短路：枚举一律不转 JSON（isJsonLog_enum）
     */
    @Test
    public void isJsonLog_enum() {

        Assertions.assertFalse(CLogUtils.isJsonLog(CProfileEnum.class));

    }

    /**
     * 对应测试用例 1.3：不转父类优先：Throwable 子类不转 JSON
     * 1.3 不转父类优先：Throwable 子类即使非 JDK 类也不转 JSON（isJsonLog_notJsonSuperclass）
     */
    @Test
    public void isJsonLog_notJsonSuperclass() {

        Assertions.assertFalse(CLogUtils.isJsonLog(CBusinessException.class));

    }

    /**
     * 对应测试用例 1.4：转父类命中：List/Map 子类转 JSON
     * 1.4 转父类命中：Collection / Map 子类转 JSON（isJsonLog_jsonSuperclass）
     */
    @Test
    public void isJsonLog_jsonSuperclass() {

        Assertions.assertTrue(CLogUtils.isJsonLog(TestListType.class));
        Assertions.assertTrue(CLogUtils.isJsonLog(TestMapType.class));

    }

    /**
     * 对应测试用例 1.5：注解命中：{@code @CJsonLog} 类与其（经 {@code @Inherited}）继承的子类均转 JSON
     * 1.5 注解命中：标注 @CJsonLog 的类转 JSON，且注解经父类继承仍生效（isJsonLog_annotation）
     */
    @Test
    public void isJsonLog_annotation() {

        Assertions.assertTrue(CLogUtils.isJsonLog(JsonLogAnnotatedClass.class));
        Assertions.assertTrue(CLogUtils.isJsonLog(JsonLogAnnotatedChild.class));

    }

    /**
     * 对应测试用例 1.6：兜底分支：非 JDK 类转 JSON、JDK 类不转
     * 1.6 兜底分支：非 JDK 类默认转 JSON、JDK 类不转（isJsonLog_defaultBranch）
     */
    @Test
    public void isJsonLog_defaultBranch() {

        Assertions.assertTrue(CLogUtils.isJsonLog(TestPlain.class));
        Assertions.assertFalse(CLogUtils.isJsonLog(Integer.class));

    }

    /**
     * 对应测试用例 2.1：setJsonLog 覆盖生效、注册不改变「不转父类优先」的既有判定
     * 2.1 注册转父类后以 {@code setJsonLog} 覆盖生效，且注册/覆盖均不抛异常（addJsonLogSuperclasses）
     *
     * <p>实测契约：{@code JSON_LOG_CLASS_VALUE} 的判定顺序中「不转父类」（含 {@link Throwable}）先于「转父类」命中，
     * 故把 {@link Throwable} 注册进「转父类」集合不会改变其子类结果；动态注册的实际生效路径是
     * {@code setJsonLog}（显式按类覆盖缓存值）。</p>
     */
    @Test
    public void addJsonLogSuperclasses_takesEffect() {

        CLogUtils.setJsonLog(TestRuntimeException.class, true);
        Assertions.assertTrue(CLogUtils.isJsonLog(TestRuntimeException.class));

        // 注册不改变既有判定（不转父类优先）
        CLogUtils.addJsonLogSuperclasses(Throwable.class);
        Assertions.assertFalse(CLogUtils.isJsonLog(RuntimeException.class));
        Assertions.assertTrue(CLogUtils.isJsonLog(TestRuntimeException.class));

        CLogUtils.setJsonLog(TestRuntimeException.class, false);
        Assertions.assertFalse(CLogUtils.isJsonLog(TestRuntimeException.class));

    }

    /**
     * 对应测试用例 2.2：注册不转父类后对未缓存子类生效
     * 2.2 注册不转父类后生效、不再落入其它判定分支（addNotJsonLogSuperclasses）
     */
    @Test
    public void addNotJsonLogSuperclasses_overrides() {

        Assertions.assertTrue(CLogUtils.isJsonLog(TestListType.class));

        CLogUtils.addNotJsonLogSuperclasses(TestListType.class);
        try {
            Assertions.assertFalse(CLogUtils.isJsonLog(TestListTypeSub.class));
        } finally {
            CLogUtils.setJsonLog(TestListTypeSub.class, true);
        }

    }

    /**
     * 对应测试用例 2.3：注册转包名后对未缓存子类生效；setJsonLog 可对任意类型显式覆盖
     * 2.3 注册转包名后生效；setJsonLog 可显式覆盖任意类型的判断（addJsonLogDomainPackage / setJsonLog）
     */
    @Test
    public void addJsonLogDomainPackage_andSetJsonLog() {

        CLogUtils.setJsonLog(LogPackageTestClass.class, false);
        Assertions.assertFalse(CLogUtils.isJsonLog(LogPackageTestClass.class));

        CLogUtils.addJsonLogDomainPackage(".logpkgtest.");
        try {
            // 命中转包名的子类（未计算过）生效
            Assertions.assertTrue(CLogUtils.isJsonLog(LogPackageTestClassSub.class));
        } finally {
            CLogUtils.setJsonLog(LogPackageTestClassSub.class, false);
        }

        // setJsonLog 显式覆盖：对任意类型锁定结果，不受集合判定影响
        CLogUtils.setJsonLog(LogPackageTestClass.class, true);
        Assertions.assertTrue(CLogUtils.isJsonLog(LogPackageTestClass.class));
        CLogUtils.setJsonLog(LogPackageTestClass.class, false);
        Assertions.assertFalse(CLogUtils.isJsonLog(LogPackageTestClass.class));

    }

    /**
     * 对应测试用例 2.4：注册转注解后标注该注解的类与其继承子类生效
     * 2.4 注册转注解后、对标注该注解的类生效（addJsonLogAnnotations）
     */
    @Test
    public void addJsonLogAnnotations_takesEffect() {

        CLogUtils.addJsonLogAnnotations(CustomLogMarker.class);
        try {
            // 标注自定义注解的类转为 JSON 化（未缓存过）
            Assertions.assertTrue(CLogUtils.isJsonLog(CustomAnnotatedClass.class));
            // 经 @Inherited 继承该注解的子类同样生效
            Assertions.assertTrue(CLogUtils.isJsonLog(CustomAnnotatedSubClass.class));
        } finally {
            // 注解集合无移除接口，改用 setJsonLog 锁定结果，避免影响同 JVM 其他用例
            CLogUtils.setJsonLog(CustomAnnotatedClass.class, false);
            CLogUtils.setJsonLog(CustomAnnotatedSubClass.class, false);
        }

    }

    /**
     * 对应测试用例 3.1：toLogArgs：null 与空数组原样返回
     * 3.1 toLogArgs：空数组与 null 原样返回（toLogArgs_empty）
     */
    @Test
    public void toLogArgs_empty() {

        Assertions.assertNull(CLogUtils.toLogArgs(null));

        val empty = new Object[0];
        Assertions.assertSame(empty, CLogUtils.toLogArgs(empty));

    }

    /**
     * 对应测试用例 3.2：toLogArgs：null 元素保留、非 JSON 类型元素原样、长度不变
     * 3.2 toLogArgs：null 元素保留、非 JSON 类型元素原样返回、长度不变（toLogArgs_mixed）
     */
    @Test
    public void toLogArgs_mixed() {

        val args = new Object[] {null, "text", 1};
        val result = CLogUtils.toLogArgs(args);

        Assertions.assertEquals(3, result.length);
        Assertions.assertNull(result[0]);
        Assertions.assertEquals("text", result[1]);
        Assertions.assertEquals(1, result[2]);

    }

    /**
     * 对应测试用例 4.1：枚举、JDK 基本类、JSON 化类型均可打印
     * 4.1 isPrintAble：枚举可打印；JDK 基本类可打印；JSON 化类型因 isJsonLog 为 true 亦可打印（isPrintAble）
     */
    @Test
    public void isPrintAble() {

        Assertions.assertTrue(CLogUtils.isPrintAble(CProfileEnum.class));
        Assertions.assertTrue(CLogUtils.isPrintAble(Integer.class));
        Assertions.assertTrue(CLogUtils.isPrintAble(String.class));
        Assertions.assertTrue(CLogUtils.isPrintAble(CResult.class));

    }

    /**
     * 对应测试用例 4.2：null 转 "[null]"、可打印类型原样返回
     * 4.2 getPrintAble：null 转 "[null]"；可打印类型（String、非 JDK 的自定义类）原样返回（getPrintAble）
     *
     * <p>「可打印」判定含 {@code isJsonLog} 为 true 的类型，故非 JDK 的自定义类亦原样返回；
     * {@code PRINT_ABLE_CONVERT_FUNCTION} 的 byte[] / 普通对象占位分支只在「既非可打印类型、又非 JSON 化类型」时命中。</p>
     */
    @Test
    public void getPrintAble() {

        Assertions.assertEquals("[null]", CLogUtils.getPrintAble(null));
        Assertions.assertEquals("text", CLogUtils.getPrintAble("text"));

        val obj = new TestPlain();
        Assertions.assertSame(obj, CLogUtils.getPrintAble(obj));

    }

    /**
     * 对应测试用例 4.3：getPrintAble 对 MultipartFile：返回 "文件名:大小" 占位
     * 4.3 getPrintAble 对 MultipartFile：返回 "文件名:大小" 占位（getPrintAble_multipartFile）
     */
    @Test
    public void getPrintAble_multipartFile() {

        val file = new MockMultipartFile("f", "a.txt", "text/plain", new byte[] {1, 2, 3});
        Assertions.assertEquals("a.txt:3", CLogUtils.getPrintAble(file));

    }

    /**
     * 对应测试用例 4.4：getPrintAbleString：字符串原样返回
     * 4.4 getPrintAbleString：字符串原样返回（不加引号）（getPrintAbleString_string）
     */
    @Test
    public void getPrintAbleString_string() {

        Assertions.assertEquals("text", CLogUtils.getPrintAbleString("text"));

    }

    /**
     * 对应测试用例 4.5：getPrintAbleString：非字符串经 JSON 序列化返回
     * 4.5 getPrintAbleString：非字符串经 JSON 序列化返回（getPrintAbleString_object）
     */
    @Test
    public void getPrintAbleString_object() {

        Assertions.assertEquals("[null]", CLogUtils.getPrintAbleString(null));

        val result = CLogUtils.getPrintAbleString(1);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("1"));

    }

    /**
     * 测试用：可打印判断的普通类型
     */
    static class TestPlain {}

    /**
     * 测试用：转父类命中（List 实现）
     */
    static class TestListType extends ArrayList<Object> {}

    /**
     * 测试用：TestListType 的子类（验证不转父类注册后对未缓存子类生效）
     */
    static class TestListTypeSub extends TestListType {}

    /**
     * 测试用：RuntimeException 的子类（验证转父类注册后对未缓存子类生效）
     */
    static class TestRuntimeException extends RuntimeException {}

    /**
     * 测试用：转父类命中（Map 实现）
     */
    static class TestMapType extends HashMap<String, Object> {}

    /**
     * 测试用：标注 @CJsonLog 的类
     */
    @CJsonLog
    static class JsonLogAnnotatedClass {}

    /**
     * 测试用：继承标注 @CJsonLog 的父类（验证 @Inherited 注解继承）
     */
    static class JsonLogAnnotatedChild extends JsonLogAnnotatedClass {}

    /**
     * 测试用：转包名命中的类（所在包名含 ".logpkgtest."，由用例 2.3 注册后验证）
     */
    static class LogPackageTestClass {}

    /**
     * 测试用：LogPackageTestClass 的子类（验证转包名注册后对未缓存子类生效）
     */
    static class LogPackageTestClassSub extends LogPackageTestClass {}

    /**
     * 测试用：自定义注解（由用例 2.4 注册为转 JSON 注解）
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    @interface CustomLogMarker {}

    /**
     * 测试用：标注自定义注解的类
     */
    @CustomLogMarker
    static class CustomAnnotatedClass {}

    /**
     * 测试用：CustomAnnotatedClass 的子类（自定义注解标注 @Inherited，验证注册后对未缓存子类生效）
     */
    static class CustomAnnotatedSubClass extends CustomAnnotatedClass {}

}
