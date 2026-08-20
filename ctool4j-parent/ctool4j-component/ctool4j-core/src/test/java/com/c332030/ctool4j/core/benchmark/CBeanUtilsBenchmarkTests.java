package com.c332030.ctool4j.core.benchmark;

import cn.hutool.core.bean.BeanUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.cglib.beans.BeanCopier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: CBeanUtils 性能对比基准
 * </p>
 * <p>
 * 对比维度：对象属性复制（copy）与对象转 map（toMap）。
 * 每个维度覆盖多类实现方式（实现原理各不相同，满足对比类别 ≥3 类）：
 * </p>
 * <ul>
 *     <li>反射：CBeanUtils（被测）、Spring BeanUtils、hutool BeanUtil</li>
 *     <li>字节码生成：cglib BeanCopier</li>
 *     <li>序列化中转：Jackson convertValue</li>
 *     <li>编译期直接赋值（基线）：手工 setter / 手工循环</li>
 * </ul>
 *
 * @since 2026/8/16
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CBeanUtilsBenchmarkTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 基准执行入口（显式运行：mvn test -Dtest=CBeanUtilsBenchmarkTests -DfailIfNoTests=false）
     * 性能测试类，surefire 打包/常规测试时排除（命名以 BenchmarkTests 结尾）
     */
    @Test
    public void benchmark() {
        CBenchmarkReport report = CBenchmarkRunner.run(cases(), "CBeanUtils 属性复制性能对比");
        Path reportPath = Paths.get(System.getProperty("user.dir"), "tmp", "benchmark-report-cbeanutils.md");
        report.writeTo(reportPath);
        System.out.println("性能测试报告已写入: " + reportPath.toAbsolutePath());
    }

    /**
     * 基准用例列表
     */
    public static List<CBenchmarkCase> cases() {
        return Arrays.asList(
                new CBeanUtilsCopyCase(),
                new CBeanUtilsCopyOldPathCase(),
                new CBeanUtilsCopyReuseTargetCase(),
                new SpringBeanUtilsCopyCase(),
                new HutoolBeanUtilCopyCase(),
                new CglibBeanCopierCase(),
                new JacksonConvertCase(),
                new ManualSetterCase(),

                new CBeanUtilsToMapCase(),
                new HutoolBeanUtilToMapCase(),
                new JacksonToMapCase(),
                new ManualToMapCase()
        );
    }

    /**
     * 被测 Bean（8 个字段，覆盖基础类型、包装类型与集合）
     */
    @Data
    @NoArgsConstructor
    public static class BenchBean {

        private String name;

        private Integer age;

        private int level;

        private long score;

        private double ratio;

        private boolean active;

        private String remark;

        private List<String> tags;

    }

    private static BenchBean newSource() {
        BenchBean source = new BenchBean();
        source.setName("benchmark");
        source.setAge(30);
        source.setLevel(5);
        source.setScore(10000L);
        source.setRatio(0.85d);
        source.setActive(true);
        source.setRemark("性能对比基准数据");
        source.setTags(Arrays.asList("a", "b", "c"));
        return source;
    }

    private static BenchBean manualCopy(BenchBean source) {
        BenchBean target = new BenchBean();
        target.setName(source.getName());
        target.setAge(source.getAge());
        target.setLevel(source.getLevel());
        target.setScore(source.getScore());
        target.setRatio(source.getRatio());
        target.setActive(source.isActive());
        target.setRemark(source.getRemark());
        target.setTags(source.getTags());
        return target;
    }

    private static Map<String, Object> manualToMap(BenchBean source) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", source.getName());
        map.put("age", source.getAge());
        map.put("level", source.getLevel());
        map.put("score", source.getScore());
        map.put("ratio", source.getRatio());
        map.put("active", source.isActive());
        map.put("remark", source.getRemark());
        map.put("tags", source.getTags());
        return map;
    }

    /**
     * CBeanUtils.copy（被测，反射 + 值转换）
     */
    private static class CBeanUtilsCopyCase implements CBenchmarkCase {

        private BenchBean source;

        @Override
        public String name() {
            return "CBeanUtils.copy";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return CBeanUtils.copy(source, BenchBean.class);
        }
    }

    /**
     * CBeanUtils.copy 旧实现路径（经 toMap 中转，模拟修复前组合方式，用于对比修复效果）
     */
    private static class CBeanUtilsCopyOldPathCase implements CBenchmarkCase {

        private BenchBean source;

        @Override
        public String name() {
            return "CBeanUtils.copy(经Map)";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return CBeanUtils.copy(CBeanUtils.toMap(source), BenchBean.class);
        }
    }

    /**
     * CBeanUtils.copy 直连路径（复用目标实例，隔离 newInstance 反射构造开销）
     */
    private static class CBeanUtilsCopyReuseTargetCase implements CBenchmarkCase {

        private BenchBean source;

        private BenchBean target;

        @Override
        public String name() {
            return "CBeanUtils.copy(复用目标)";
        }

        @Override
        public void prepare() {
            source = newSource();
            target = new BenchBean();
        }

        @Override
        public Object run() {
            return CBeanUtils.copy(source, target);
        }
    }

    /**
     * Spring BeanUtils.copyProperties（反射实现）
     */
    private static class SpringBeanUtilsCopyCase implements CBenchmarkCase {

        private BenchBean source;

        @Override
        public String name() {
            return "Spring BeanUtils";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            BenchBean target = new BenchBean();
            BeanUtils.copyProperties(source, target);
            return target;
        }
    }

    /**
     * hutool BeanUtil.copyProperties（反射实现）
     */
    private static class HutoolBeanUtilCopyCase implements CBenchmarkCase {

        private BenchBean source;

        @Override
        public String name() {
            return "hutool BeanUtil";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return BeanUtil.copyProperties(source, BenchBean.class);
        }
    }

    /**
     * cglib BeanCopier（字节码生成实现）
     */
    private static class CglibBeanCopierCase implements CBenchmarkCase {

        private BenchBean source;

        private BeanCopier copier;

        @Override
        public String name() {
            return "cglib BeanCopier";
        }

        @Override
        public void prepare() {
            source = newSource();
            copier = BeanCopier.create(BenchBean.class, BenchBean.class, false);
        }

        @Override
        public Object run() {
            BenchBean target = new BenchBean();
            copier.copy(source, target, null);
            return target;
        }
    }

    /**
     * Jackson convertValue（序列化中转实现）
     */
    private static class JacksonConvertCase implements CBenchmarkCase {

        private BenchBean source;

        @Override
        public String name() {
            return "Jackson convertValue";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return OBJECT_MAPPER.convertValue(source, BenchBean.class);
        }
    }

    /**
     * 手工 setter（编译期直接赋值基线）
     */
    private static class ManualSetterCase implements CBenchmarkCase {

        private BenchBean source;

        @Override
        public String name() {
            return "手工 setter";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return manualCopy(source);
        }
    }

    /**
     * CBeanUtils.toMap（被测，反射）
     */
    private static class CBeanUtilsToMapCase implements CBenchmarkCase {

        private BenchBean source;

        @Override
        public String name() {
            return "CBeanUtils.toMap";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return CBeanUtils.toMap(source);
        }
    }

    /**
     * hutool BeanUtil.beanToMap（反射实现）
     */
    private static class HutoolBeanUtilToMapCase implements CBenchmarkCase {

        private BenchBean source;

        @Override
        public String name() {
            return "hutool beanToMap";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return BeanUtil.beanToMap(source);
        }
    }

    /**
     * Jackson convertValue 转 map（序列化中转实现）
     */
    private static class JacksonToMapCase implements CBenchmarkCase {

        private BenchBean source;

        @Override
        public String name() {
            return "Jackson toMap";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return OBJECT_MAPPER.convertValue(source, Map.class);
        }
    }

    /**
     * 手工循环（编译期直接赋值基线）
     */
    private static class ManualToMapCase implements CBenchmarkCase {

        private BenchBean source;

        @Override
        public String name() {
            return "手工 toMap";
        }

        @Override
        public void prepare() {
            source = newSource();
        }

        @Override
        public Object run() {
            return manualToMap(source);
        }
    }

}
