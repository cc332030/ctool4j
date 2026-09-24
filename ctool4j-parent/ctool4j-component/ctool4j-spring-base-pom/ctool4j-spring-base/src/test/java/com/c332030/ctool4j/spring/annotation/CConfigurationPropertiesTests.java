package com.c332030.ctool4j.spring.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CConfigurationPropertiesTests
 * </p>
 *
 * <p>
 * 是 {@link CConfigurationProperties} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「注解契约」编写用例：注解的价值全在它向 Spring 暴露的属性取值上，故断言点放在
 *   「Spring 合并解析出的 {@code @ConfigurationProperties} 是否与本注解声明一致」，
 *   而不是断言注解类自身的结构——后者只说明「写了一个注解」，说明不了「Spring 认不认」。</li>
 *   <li>用 Spring 的 {@code AnnotatedElementUtils.findMergedAnnotation} 取值：它是属性绑定链路上的真实入口，
 *   据此判断别名映射是否生效。</li>
 *   <li>注解只承载前缀一项，两个绑定开关的默认行为都在元注解上给定，故断言按来源分开写：
 *   {@code ignoreInvalidFields} 的元注解默认是 {@code false}，实测取到 {@code true} 即钉住
 *   「元注解使用点上的 {@code true} 生效」；{@code ignoreUnknownFields} 由元注解自身默认提供，
 *   本注解不声明，故只断言其取值、不把来源算在本注解上。</li>
 *   <li>不测「由本注解改到 {@code false}」：本注解没有该属性元素，无法表达强校验取值——
 *   该能力属元注解，测它只是元注解的结论（由 {@code CConfigurationPropertiesBindingTests} 从绑定链路取证）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据类级「设计要点」：本注解只承载前缀；非法值开关由元注解使用点声明为 {@code true}，必须显式声明否则取不到。</li>
 *   <li>依据 Spring 官方文档：{@code @AliasFor} 的显式别名须在合并解析时改写到元注解的对应属性。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖（本类）：注解声明与属性元素集合、默认值与显式前缀取值、两个开关由元注解取到的默认取值。</li>
 *   <li>未覆盖：由本注解表达强校验（本注解不承载该开关）；实际绑定容器中的配置属性（由 {@code CConfigurationPropertiesBindingTests} 覆盖）。</li>
 * </ul>
 * <h2>用例目录</h2>
 * <ul>
 *   <li>1.1 默认值合并解析（{@code defaults}）</li>
 *   <li>1.2 显式前缀取值合并解析（{@code explicitPrefix}）</li>
 *   <li>1.3 声明与目标范围（{@code declaration}）</li>
 * </ul>
 *
 * @since 2026/9/21
 * @version 1.6
 * @see CConfigurationProperties
 * @see CConfigurationPropertiesBindingTests
 */
public class CConfigurationPropertiesTests {

    /**
     * 默认取值：未声明前缀
     */
    @CConfigurationProperties
    static class DefaultsConfig {

    }

    /**
     * 显式取值：前缀由 value 指定
     */
    @CConfigurationProperties("c-test")
    static class ExplicitConfig {

    }

    /**
     * 对应测试用例 1.1：默认值合并解析
     */
    @Test
    public void defaults() {

        ConfigurationProperties merged = AnnotatedElementUtils.findMergedAnnotation(
            DefaultsConfig.class, ConfigurationProperties.class);

        Assertions.assertNotNull(merged, "本注解须能合并解析出 @ConfigurationProperties");
        Assertions.assertEquals("", merged.prefix(), "未声明前缀时为空串");
        Assertions.assertTrue(merged.ignoreUnknownFields(),
            "忽略未匹配属性默认 true（来源为元注解默认值，本注解不声明）");
        Assertions.assertTrue(merged.ignoreInvalidFields(),
            "忽略非法属性默认 true（元注解默认 false，故须由本注解的元注解使用点声明到 true）");
    }

    /**
     * 对应测试用例 1.2：显式前缀取值合并解析
     */
    @Test
    public void explicitPrefix() {

        ConfigurationProperties merged = AnnotatedElementUtils.findMergedAnnotation(
            ExplicitConfig.class, ConfigurationProperties.class);

        Assertions.assertNotNull(merged);
        Assertions.assertEquals("c-test", merged.prefix(), "value 须透传为 @ConfigurationProperties 的 prefix");
        Assertions.assertTrue(merged.ignoreUnknownFields(), "未声明开关时仍取元注解默认值 true");
        Assertions.assertTrue(merged.ignoreInvalidFields(), "未声明开关时仍取本注解的元注解使用点上的 true");
    }

    /**
     * 对应测试用例 1.3：声明与目标范围
     *
     * <p>断言注解的元注解声明与属性元素集合，防止后续改动无意扩大或收紧目标范围（如误改为可标注方法）、
     * 或增删属性时未同步本类的用例与文档。</p>
     */
    @Test
    public void declaration() {

        Assertions.assertTrue(CConfigurationProperties.class.isAnnotationPresent(Documented.class),
            "须标注 @Documented，使注解内容进入产物文档");
        Assertions.assertTrue(CConfigurationProperties.class.isAnnotationPresent(Inherited.class),
            "须标注 @Inherited（本仓库注解一致约定；元注解 @ConfigurationProperties 自身未标注，"
                + "故子类会继承前缀）");

        Target target = CConfigurationProperties.class.getAnnotation(Target.class);
        Assertions.assertNotNull(target);
        Assertions.assertEquals(Arrays.asList(ElementType.TYPE), Arrays.asList(target.value()),
            "只可标注类型：元注解 @ConfigurationProperties 为 @Target({TYPE, METHOD})，本注解只保留类型");

        Retention retention = CConfigurationProperties.class.getAnnotation(Retention.class);
        Assertions.assertNotNull(retention);
        Assertions.assertEquals(RetentionPolicy.RUNTIME, retention.value(),
            "运行时保留，供 Spring 在绑定阶段读取");

        List<String> attributes = Arrays.stream(CConfigurationProperties.class.getDeclaredMethods())
            .map(method -> method.getName())
            .sorted()
            .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList("value"), attributes,
            "属性元素只有前缀一项：两个绑定开关都不在本注解上复述，"
                + "非法值开关落在元注解的使用点上、未知键开关取元注解默认值");
    }

}
