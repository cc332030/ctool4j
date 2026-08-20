package com.c332030.ctool4j.web.benchmark;

import com.c332030.ctool4j.core.benchmark.BenchmarkCase;
import com.c332030.ctool4j.core.benchmark.BenchmarkReport;
import com.c332030.ctool4j.core.benchmark.BenchmarkRunner;
import com.c332030.ctool4j.core.validation.CValidUtils;
import com.c332030.ctool4j.web.validation.annotation.CRequired;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: CRequired 注解校验性能对比基准
 * </p>
 * <p>
 * 对比维度：参数校验（字段必填）。
 * 覆盖多类实现方式（满足对比类别 ≥3 类）：
 * </p>
 * <ul>
 *     <li>@CRequired（被测，按类型自动分发校验）</li>
 *     <li>标准 javax.validation 注解（@NotNull/@NotBlank/@NotEmpty，按类型分别标注）</li>
 *     <li>手工 CValidUtils.isValid（基线，不经 Validator）</li>
 * </ul>
 *
 * @since 2026/8/20
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CRequiredValidatorBenchmarkTests {

    /**
     * 基准执行入口（显式运行：mvn test -Dtest=CRequiredValidatorBenchmarkTests -DfailIfNoTests=false）
     * 性能测试类，surefire 打包/常规测试时排除（命名以 BenchmarkTests 结尾）
     */
    @Test
    public void benchmark() {
        BenchmarkReport report = BenchmarkRunner.run(cases(), "CRequired 校验性能对比");
        Path reportPath = Paths.get(System.getProperty("user.dir"), "tmp", "benchmark-report-crequired.md");
        report.writeTo(reportPath);
        System.out.println("性能测试报告已写入: " + reportPath.toAbsolutePath());
    }

    /**
     * 基准用例列表
     */
    public static List<BenchmarkCase> cases() {
        return Arrays.asList(
            new CRequiredCase(),
            new StandardConstraintCase(),
            new ManualValidCase()
        );
    }

    /**
     * @CRequired 标注的待校验 Bean（字段覆盖字符串/集合/Map/数组/其他对象各类分支）
     */
    @Data
    @NoArgsConstructor
    public static class CRequiredBean {

        @CRequired
        private String name;

        @CRequired
        private List<String> tags;

        @CRequired
        private Map<String, Object> ext;

        @CRequired
        private int[] nums;

        @CRequired
        private Object payload;

    }

    /**
     * 标准 javax.validation 注解标注的待校验 Bean（对应 CRequiredBean，按类型选注解）
     */
    @Data
    @NoArgsConstructor
    public static class StandardBean {

        @NotNull
        @NotBlank
        private String name;

        @NotEmpty
        private List<String> tags;

        @NotEmpty
        private Map<String, Object> ext;

        @NotNull
        private int[] nums;

        @NotNull
        private Object payload;

    }

    /**
     * 构造校验值均有效（校验全通过）的 CRequiredBean
     */
    private static CRequiredBean newCRequiredBean() {
        CRequiredBean bean = new CRequiredBean();
        bean.setName("benchmark");
        bean.setTags(Arrays.asList("a", "b"));
        bean.setExt(new HashMap<>());
        bean.getExt().put("k", "v");
        bean.setNums(new int[]{1, 2});
        bean.setPayload(new Object());
        return bean;
    }

    /**
     * 构造校验值均有效（校验全通过）的 StandardBean
     */
    private static StandardBean newStandardBean() {
        StandardBean bean = new StandardBean();
        bean.setName("benchmark");
        bean.setTags(Arrays.asList("a", "b"));
        bean.setExt(new HashMap<>());
        bean.getExt().put("k", "v");
        bean.setNums(new int[]{1, 2});
        bean.setPayload(new Object());
        return bean;
    }

    /**
     * @CRequired 校验（被测）：经 hibernate Validator 执行 CRequiredValidator
     */
    private static class CRequiredCase implements BenchmarkCase {

        private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        private CRequiredBean bean;

        @Override
        public String name() {
            return "@CRequired 校验";
        }

        @Override
        public void prepare() {
            bean = newCRequiredBean();
        }

        @Override
        public Object run() {
            return validator.validate(bean);
        }
    }

    /**
     * 标准 javax.validation 注解校验（常规）：@NotNull/@NotBlank/@NotEmpty
     */
    private static class StandardConstraintCase implements BenchmarkCase {

        private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        private StandardBean bean;

        @Override
        public String name() {
            return "标准注解校验";
        }

        @Override
        public void prepare() {
            bean = newStandardBean();
        }

        @Override
        public Object run() {
            return validator.validate(bean);
        }
    }

    /**
     * 手工 CValidUtils.isValid（基线，不经 Validator）
     */
    private static class ManualValidCase implements BenchmarkCase {

        private CRequiredBean bean;

        @Override
        public String name() {
            return "手工 CValidUtils";
        }

        @Override
        public void prepare() {
            bean = newCRequiredBean();
        }

        @Override
        public Object run() {
            boolean valid = CValidUtils.isValid(bean.getName())
                && CValidUtils.isValid(bean.getTags())
                && CValidUtils.isValid(bean.getExt())
                && CValidUtils.isValid(bean.getNums())
                && CValidUtils.isValid(bean.getPayload());
            return valid;
        }
    }

}
