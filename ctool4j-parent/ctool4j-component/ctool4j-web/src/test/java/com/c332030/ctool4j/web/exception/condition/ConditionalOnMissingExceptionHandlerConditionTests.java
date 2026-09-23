package com.c332030.ctool4j.web.exception.condition;

import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotationFilter;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.RepeatableContainers;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Description: ConditionalOnMissingExceptionHandlerConditionTests
 * </p>
 *
 * <h2>测试用例目录</h2>
 * <ul>
 *   <li>1.1 valueName 无匹配：条件成立，被标注 bean 装配（valueNameNotMatched_beanRegistered）</li>
 *   <li>1.2 valueName 有匹配（容器内 advice 处理了该类名）：条件不成立，被标注 bean 不装配（valueNameMatched_beanSkipped）</li>
 *   <li>1.3 valueName 指向不存在的类（模拟 Jetty 无 Tomcat 类的场景）：条件成立且不抛异常（valueNameClassAbsent_beanRegistered）</li>
 *   <li>1.4 value 按类型匹配（回归）：容器内 advice 处理该类型时被标注 bean 不装配（valueMatched_beanSkipped）</li>
 *   <li>1.5 多个条件 bean 共享同一份 advice 声明集（按 advice 类型缓存）：同一容器内两个条件各自判定，结果互不影响（multipleHandlers_sharedDeclarations）</li>
 *   <li>1.6 父类继承的 {@code @ExceptionHandler} 计入子类 advice 的声明集（superclassDeclared_matched）</li>
 *   <li>1.7 裸 {@code @ExceptionHandler}（未声明类型）不得抑制注解走默认值的 {@code Throwable} 处理器（bareEmptyValue_throwableHandlerRegistered）</li>
 *   <li>1.8 beanFactory 为 null（非预期形态）：抛 {@code IllegalArgumentException}、不静默放行（nullBeanFactory_throws）</li>
 *   <li>1.9 容器内无任何 {@code ControllerAdvice}：条件成立、处理器装配（noControllerAdvice_beanRegistered）</li>
 *   <li>1.10 advice 无 {@code @ExceptionHandler} 方法：声明集为空，不抑制任何处理器（noExceptionHandlerMethod_beanRegistered）</li>
 *   <li>1.11 某 advice 的 bean 名取不到类型：跳过该 bean，其余 advice 的命中判定不受影响（unresolvableAdviceType_otherAdviceStillMatched）</li>
 *   <li>1.12 容器装配顺序：条件装配期已注册的 advice 生效、后注册的 advice 不影响此前结论（装配期读当前 bean 定义、不跨容器缓存）</li>
 *   <li>1.13 启动完成清理：{@code clearDeclarationsCache()} 可被调用，且清空后条件仍按当前容器正确判定（clearDeclarationsCache_evictsAndKeepsCorrect）</li>
 * </ul>
 *
 * <h2>测试设计</h2>
 * <ul>
 *   <li>依据测试方法（正例/分支覆盖）：valueName 命中与未命中、类名不存在、value 分支回归、advice 声明集复用、继承，以及空 value 的边界（裸 {@code @ExceptionHandler} 不计入声明集）。</li>
 *   <li>本次补齐的兜底路径（1.8~1.11）：容器入参异常、空 advice 清单、声明集为空、单个 advice 类型不可解析——
 *   四条都是条件类改动后新增/改动的分支，且各自断言"条件成立与否"这一可观测结果。</li>
 *   <li>条件类里"容器不可列举（非 ListableBeanFactory）"的降级分支<b>不可达</b>：{@code ConditionContext#getBeanFactory()}
 *   声明返回 {@code ConfigurableListableBeanFactory}，其本身即 {@code ListableBeanFactory}，故该分支无法经契约构造（详见 1.8 与 1.11 的桩说明）。</li>
 *   <li>覆盖场景：见上方编号索引；用 {@code AnnotationConfigApplicationContext} 装配真实容器，按 bean 是否注册断言条件结果。</li>
 *   <li>未覆盖：真实容器（Jetty）下的端到端装配——1.3 以"类名不存在仍可判断"覆盖该兼容性要点。</li>
 *   <li>夹具隔离：本测试的 advice 夹具带 {@code @Profile}（条件测试专用 profile），避免被同包的 Boot 测试上下文组件扫描到。</li>
 *   <li>依据：条件注解为类名匹配时不解析目标类，故缺失该类的环境不因类加载失败而中断装配。</li>
 * </ul>
 *
 * @since 2026/9/18
 * @version 1.2
 * @see ConditionalOnMissingExceptionHandler
 */
public class ConditionalOnMissingExceptionHandlerConditionTests {

    /**
     * 条件测试专用 profile：隔离本类夹具，避免进入其它测试的组件扫描
     */
    private static final String PROFILE = "ctool4j-condition-tests";

    /**
     * 对应测试用例 1.1：valueName 无匹配：被标注 bean 装配
     */
    @Test
    public void valueNameNotMatched_beanRegistered() {

        try (val context = newContext()) {

            context.register(ValueNameHandler.class);
            context.refresh();

            Assertions.assertEquals(1, context.getBeanNamesForType(ValueNameHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.2：valueName 有匹配：被标注 bean 不装配
     */
    @Test
    public void valueNameMatched_beanSkipped() {

        try (val context = newContext()) {

            context.register(IOExceptionAdvice.class, ValueNameHandler.class);
            context.refresh();

            Assertions.assertEquals(0, context.getBeanNamesForType(ValueNameHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.3：valueName 指向不存在的类：条件成立且不抛异常（跨容器兼容要点）
     */
    @Test
    public void valueNameClassAbsent_beanRegistered() {

        try (val context = newContext()) {

            context.register(AbsentClassNameHandler.class);
            context.refresh();

            Assertions.assertEquals(1, context.getBeanNamesForType(AbsentClassNameHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.4：value 按类型匹配（回归）：有匹配时不装配
     */
    @Test
    public void valueMatched_beanSkipped() {

        try (val context = newContext()) {

            context.register(IOExceptionAdvice.class, ValueTypeHandler.class);
            context.refresh();

            Assertions.assertEquals(0, context.getBeanNamesForType(ValueTypeHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.5：同一容器内多个条件 bean 共享同一份 advice 声明集，判定结果互不影响
     * <p>固化「按 advice 类型缓存声明集」的行为：同一容器内不同目标类型的条件各判一次，
     * 命中与未命中的结论都与单条判定时一致（缓存只共享解析结果、不共享结论）。</p>
     */
    @Test
    public void multipleHandlers_sharedDeclarations() {

        try (val context = newContext()) {

            context.register(IOExceptionAdvice.class, ValueNameHandler.class, ValueTypeHandler.class);
            context.refresh();

            // 两个条件 bean 的目标都是 IOException，容器内既有 advice 处理该类：均不装配
            Assertions.assertEquals(0, context.getBeanNamesForType(ValueNameHandler.class).length);
            Assertions.assertEquals(0, context.getBeanNamesForType(ValueTypeHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.6：父类继承的 {@code @ExceptionHandler} 计入子类 advice 的声明集
     * <p>父类上声明的 {@code @ExceptionHandler(IOException.class)} 同样使子类 advice 命中该类，条件不成立。</p>
     */
    @Test
    public void superclassDeclared_matched() {

        try (val context = newContext()) {

            context.register(SubclassAdvice.class, ValueTypeHandler.class);
            context.refresh();

            Assertions.assertEquals(0, context.getBeanNamesForType(ValueTypeHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.7：裸 {@code @ExceptionHandler} 不得抑制注解走默认值的 {@code Throwable} 处理器
     * <p>裸 {@code @ExceptionHandler}（未声明类型）在 Spring 里兜底一切异常，但只看注解属性它「没有」声明任何类型。
     * 若把这种形态折算成声明了 {@code Throwable.class}，则注解走默认属性（{@code value} 默认 {@code Throwable.class}）的
     * 处理器会被判为「容器已有同类型处理器」而拒绝装配——本用例固化「裸形态不计入声明集」，
     * 保证内置的 {@code @ConditionalOnMissingExceptionHandler} 用法（不带 {@code value}）不被误伤。</p>
     */
    @Test
    public void bareEmptyValue_throwableHandlerRegistered() {

        try (val context = newContext()) {

            context.register(BareEmptyValueAdvice.class, DefaultValueThrowableHandler.class);
            context.refresh();

            // 注解走默认值，命中的是 Throwable；裸 @ExceptionHandler 未声明类型，不得使该处理器让位
            Assertions.assertEquals(1, context.getBeanNamesForType(DefaultValueThrowableHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.8：beanFactory 为 null（非预期入参）抛 {@code IllegalArgumentException}
     * <p>容器上下文取不到 beanFactory 属非预期形态；条件类对此用 {@code Assert.notNull} 快速失败，而不是静默返回 true。
     * 该分支与"无 ControllerAdvice → 返回 true"是两条不同的路径，不可因取值恰好都成立而被并入同一个用例。</p>
     */
    @Test
    public void nullBeanFactory_throws() {

        val condition = new ConditionalOnMissingExceptionHandlerCondition();
        val metadata = metadata(IOException.class);

        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> condition.matches(nullBeanFactoryContext(), metadata)
        );
    }

    /**
     * 对应测试用例 1.9：容器内无任何 {@code ControllerAdvice} 时条件成立（启用默认 Handler）
     * <p>遍历入口改为"先取 advice 类型清单"后，"清单为空"成为最先返回的一条路径；该结论须与改动前一致——
     * 无任何 advice 即不抑制任何处理器。</p>
     */
    @Test
    public void noControllerAdvice_beanRegistered() {

        try (val context = newContext()) {

            context.register(ValueTypeHandler.class);
            context.refresh();

            Assertions.assertEquals(1, context.getBeanNamesForType(ValueTypeHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.10：advice 无 {@code @ExceptionHandler} 方法时声明集为空，不抑制任何处理器
     * <p>占位用的 {@code @ControllerAdvice}（无异常处理方法）不构成"已处理某类型"，被标注的处理器仍装配。</p>
     */
    @Test
    public void noExceptionHandlerMethod_beanRegistered() {

        try (val context = newContext()) {

            context.register(NoExceptionHandlerAdvice.class, ValueTypeHandler.class);
            context.refresh();

            Assertions.assertEquals(1, context.getBeanNamesForType(ValueTypeHandler.class).length);
        }
    }

    /**
     * 对应测试用例 1.11：某 advice 的 bean 名取不到类型时跳过该 bean，其余 advice 的判定不受影响
     * <p>容器里同时有一个"类型不可解析"的 advice 名与一个真实处理 {@code IOException} 的 advice：
     * 取类型失败只跳过前者并记 debug 日志，不得中断条件判断；后者仍使被标注处理器让位。
     * 该形态以自写的 bean 工厂桩表达——真实容器里 {@code getType} 对已知 bean 名不抛错，缺陷无从触发。</p>
     */
    @Test
    public void unresolvableAdviceType_otherAdviceStillMatched() {

        val condition = new ConditionalOnMissingExceptionHandlerCondition();

        Assertions.assertFalse(
            condition.matches(unresolvableAdviceFactoryContext(), metadata(IOException.class)),
            "不可解析的 advice 名须被跳过，其余 advice 仍使条件不成立"
        );
    }

    /**
     * 对应测试用例 1.12：advice 类型清单须每次读当前 bean 定义，不得按容器实例缓存
     * <p>条件评估发生在容器装配期，advice 是随后才注册的：同一个 bean 工厂第一次评估时还没有 advice、
     * 第二次评估时已有处理 {@code IOException} 的 advice。两次结论必须不同（先 true、后 false）——
     * 若把清单按容器实例缓存，第二次会命中第一次的空结果，后注册的 advice 被永久漏掉。</p>
     */
    @Test
    public void adviceListReadFreshPerEvaluation() {

        val condition = new ConditionalOnMissingExceptionHandlerCondition();
        val context = adviceListGrowingContext();

        Assertions.assertTrue(
            condition.matches(context, metadata(IOException.class)),
            "装配期尚无 advice 时条件须成立（启用默认处理器）"
        );
        Assertions.assertFalse(
            condition.matches(context, metadata(IOException.class)),
            "同一容器中后注册的 advice 须被本次评估读到（不得沿用上次的空清单）"
        );
    }

    /**
     * 构建 advice 名单会增长的 bean 工厂桩：首次 {@code getBeanNamesForAnnotation} 返回空、之后返回一个 advice 名
     *
     * @return 条件上下文
     */
    private static ConditionContext adviceListGrowingContext() {

        val calls = new int[1];
        val beanNames = new String[]{"growing"};
        val adviceTypes = new Class<?>[]{IOExceptionAdvice.class};

        val factory = (ConfigurableListableBeanFactory) Proxy.newProxyInstance(
            ConditionalOnMissingExceptionHandlerConditionTests.class.getClassLoader(),
            new Class<?>[]{ConfigurableListableBeanFactory.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getBeanNamesForAnnotation":
                        // 第一次：容器装配期还没有 advice；第二次：advice 已注册
                        return 0 == calls[0]++ ? new String[0] : beanNames;
                    case "getType":
                        return adviceTypes[Arrays.asList(beanNames).indexOf(args[0])];
                    case "equals":
                        return proxy == args[0];
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "toString":
                        return "growingAdviceFactory";
                    default:
                        throw new UnsupportedOperationException(method.toString());
                }
            }
        );

        val context = mock(ConditionContext.class);
        when(context.getBeanFactory()).thenReturn(factory);

        return context;
    }

    /**
     * 构建 bean 工厂桩：{@code getBeanNamesForAnnotation} 给出两个 advice 名，
     * 其中 {@code ghost} 的 {@code getType} 抛错、{@code real} 返回声明处理 {@code IOException} 的 advice 类型
     * <p>两种 advice 形态各对应一条返回路径：{@code ghost} 覆盖"取类型失败须跳过"，{@code real} 覆盖"其余 advice 照常判定"。</p>
     *
     * @return 条件上下文
     */
    private static ConditionContext unresolvableAdviceFactoryContext() {

        val context = mock(ConditionContext.class);
        when(context.getBeanFactory()).thenReturn(beanFactory(
            new String[]{"ghost", "real"},
            new Class<?>[]{null, IOExceptionAdvice.class}
        ));

        return context;
    }

    /**
     * 构建可列举的 bean 工厂桩：按 bean 名给出 advice 类型（{@code null} 表示该名的类型不可解析）
     *
     * @param beanNames   {@code getBeanNamesForAnnotation} 的返回值
     * @param adviceTypes 与 bean 名一一对应的类型（{@code null} 表示 {@code getType} 抛错）
     * @return bean 工厂桩
     */
    private static ConfigurableListableBeanFactory beanFactory(String[] beanNames, Class<?>[] adviceTypes) {

        return (ConfigurableListableBeanFactory) Proxy.newProxyInstance(
            ConditionalOnMissingExceptionHandlerConditionTests.class.getClassLoader(),
            new Class<?>[]{ConfigurableListableBeanFactory.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getBeanNamesForAnnotation":
                        return beanNames;
                    case "getType":
                        val index = Arrays.asList(beanNames).indexOf(args[0]);
                        if (0 > index || null == adviceTypes[index]) {
                            // 真实容器里 .class 取不到时 bean 工厂就抛这个：条件类须捕获后跳过该 bean
                            throw new ClassNotFoundException("unresolvable advice type: " + args[0]);
                        }
                        return adviceTypes[index];
                    case "equals":
                        return proxy == args[0];
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "toString":
                        return "conditionTestBeanFactory";
                    default:
                        throw new UnsupportedOperationException(method.toString());
                }
            }
        );
    }

    /**
     * 构建 {@code beanFactory} 为 null 的条件上下文
     *
     * @return 条件上下文
     */
    private static ConditionContext nullBeanFactoryContext() {

        val context = mock(ConditionContext.class);
        when(context.getBeanFactory()).thenReturn(null);

        return context;
    }

    /**
     * 构建目标类型走 {@code value} 属性的注解元数据
     * <p>{@code AnnotatedTypeMetadata} 由 JDK 动态代理实现：把合成的注解实例交给 Spring 的 {@code MergedAnnotations}
     * 解析（合成的注解无法用 {@code StandardAnnotationMetadata} 构造器表达），语义与容器对真实注解的解析一致。</p>
     *
     * @param type 目标异常类型
     * @return 注解元数据
     */
    private static AnnotatedTypeMetadata metadata(Class<? extends Throwable> type) {
        return metadata("value", type);
    }

    /**
     * 构建以指定属性承载目标类型的注解元数据
     *
     * @param attributeName 属性名（{@code value} 或 {@code valueName}）
     * @param attributeValue 属性值（类型或全限定类名）
     * @return 注解元数据
     */
    private static AnnotatedTypeMetadata metadata(String attributeName, Object attributeValue) {

        val attributes = new HashMap<String, Object>();
        attributes.put("value", Throwable.class);
        attributes.put("valueName", "");
        attributes.put(attributeName, attributeValue);

        val annotations = mergedAnnotations(syntheticAnnotation(attributes));

        return (AnnotatedTypeMetadata) Proxy.newProxyInstance(
            ConditionalOnMissingExceptionHandlerConditionTests.class.getClassLoader(),
            new Class<?>[]{AnnotatedTypeMetadata.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getAnnotations":
                        return annotations;
                    case "isAnnotated":
                        return annotationAttributes(annotations, (String) args[0]) != null;
                    case "getAllAnnotationAttributes":
                        return null;
                    default:
                        return annotationAttributes(annotations, (String) args[0]);
                }
            }
        );
    }

    /**
     * 合成一个 {@code @ConditionalOnMissingExceptionHandler} 注解实例（属性值由调用方给定）
     *
     * @param attributes 注解属性
     * @return 注解实例
     */
    private static Annotation syntheticAnnotation(Map<String, Object> attributes) {

        return (Annotation) Proxy.newProxyInstance(
            ConditionalOnMissingExceptionHandlerConditionTests.class.getClassLoader(),
            new Class<?>[]{ConditionalOnMissingExceptionHandler.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "annotationType":
                        return ConditionalOnMissingExceptionHandler.class;
                    case "toString":
                        return "@ConditionalOnMissingExceptionHandler" + attributes;
                    case "hashCode":
                        return attributes.hashCode();
                    case "equals":
                        return false;
                    default:
                        return attributes.get(method.getName());
                }
            }
        );
    }

    /**
     * 把合成的注解实例交由 Spring 解析为 {@code MergedAnnotations}
     * <p>{@code TypeMappedAnnotations} 是包内可见的实现，故按 Spring 自身的构造签名反射创建；
     * 不复制其解析逻辑，保证与容器对真实注解的解析结果同源。</p>
     *
     * @param annotations 注解实例
     * @return 合并注解视图
     */
    private static Object mergedAnnotations(Annotation... annotations) {

        try {
            val type = Class.forName("org.springframework.core.annotation.TypeMappedAnnotations");
            val constructor = type.getDeclaredConstructor(
                Object.class, Annotation[].class, RepeatableContainers.class, AnnotationFilter.class
            );
            constructor.setAccessible(true);

            return constructor.newInstance("synthetic", annotations, RepeatableContainers.none(), AnnotationFilter.PLAIN);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("无法构建 Test 用的注解元数据", e);
        }
    }

    /**
     * 取合并注解视图里指定注解类型的属性表
     *
     * @param annotations 合并注解视图
     * @param annotationTypeName 注解全限定类名
     * @return 属性表；无该注解时返回 null
     */
    private static Map<String, Object> annotationAttributes(Object annotations, String annotationTypeName) {

        try {
            for (val annotation : (Iterable<?>) annotations) {

                val getType = annotation.getClass().getMethod("getType");
                getType.setAccessible(true);
                if (!annotationTypeName.equals(((Class<?>) getType.invoke(annotation)).getName())) {
                    continue;
                }

                val asMap = annotation.getClass().getMethod("asMap", MergedAnnotation.Adapt[].class);
                asMap.setAccessible(true);

                @SuppressWarnings("unchecked")
                val attributes = (Map<String, Object>) asMap.invoke(annotation, (Object) new MergedAnnotation.Adapt[0]);
                return attributes;
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("无法读取 Test 用的注解属性", e);
        }

        return null;
    }

    /**
     * 对应测试用例 1.13：{@code clearDeclarationsCache()} 之后条件仍按当前容器正确判定
     *
     * <p><b>为什么这么断言</b>：清空的作用是释放内存，它<b>不改变判定语义</b>——
     * 故可判定的观测面是"清空前后结论一致、且后续容器照常取值"。
     * 缓存条目本身是 {@code CClassValue} 的私有状态，"清了几个条目"不构成契约级观测点；
     * 该对象自己的清空行为另由 {@code CClassValueTests} 的用例（4.2~4.4）逐个钉住。</p>
     *
     * <p>本用例覆盖的是本类的责任：<b>清理入口可被调用、且不影响后续判定</b>。</p>
     */
    @Test
    public void clearDeclarationsCache_evictsAndKeepsCorrect() {

        try (val context = newContext()) {

            context.register(IOExceptionAdvice.class, ValueNameHandler.class);
            context.refresh();

            // 声明集已按 advice 类型建立：advice 声明了 IOException，条件 bean 不装配
            Assertions.assertEquals(0, context.getBeanNamesForType(ValueNameHandler.class).length);
        }

        // 启动完成清理
        ConditionalOnMissingExceptionHandlerCondition.clearDeclarationsCache();

        // 清理后仍按当前容器正确判定：装 advice → 不装配
        try (val context = newContext()) {

            context.register(IOExceptionAdvice.class, ValueNameHandler.class);
            context.refresh();

            Assertions.assertEquals(0, context.getBeanNamesForType(ValueNameHandler.class).length);
        }

        // 清理后仍按当前容器正确判定：未装 advice → 装配（不是残留的旧结论）
        try (val context = newContext()) {

            context.register(ValueNameHandler.class);
            context.refresh();

            Assertions.assertEquals(1, context.getBeanNamesForType(ValueNameHandler.class).length);
        }

    }

    /**
     * 构建已激活条件测试 profile 的容器
     *
     * @return 应用上下文
     */
    private static AnnotationConfigApplicationContext newContext() {

        val context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles(PROFILE);

        return context;
    }

    /**
     * 裸 {@code @ExceptionHandler} 的 advice：未声明异常类型（Spring 语义兜底一切），但注解属性里没有任何类型
     */
    @Profile(PROFILE)
    @ControllerAdvice
    static class BareEmptyValueAdvice {

        /**
         * 未声明异常类型
         *
         * @param e 异常
         */
        @ExceptionHandler
        public void handleAny(Throwable e) {
        }

    }

    /**
     * 注解走默认属性的条件 bean：{@code value} 为默认的 {@code Throwable.class}（不带 {@code valueName}）
     */
    @Profile(PROFILE)
    @ConditionalOnMissingExceptionHandler
    static class DefaultValueThrowableHandler {

    }

    /**
     * 按类名匹配（valueName = IOException）的条件 bean
     */
    @Profile(PROFILE)
    @ControllerAdvice
    @ConditionalOnMissingExceptionHandler(valueName = "java.io.IOException")
    static class ValueNameHandler {

    }

    /**
     * 按类名匹配、且类不存在的条件 bean（模拟容器私有类型缺失的环境）
     */
    @Profile(PROFILE)
    @ControllerAdvice
    @ConditionalOnMissingExceptionHandler(valueName = "org.apache.catalina.connector.ClientAbortException")
    static class AbsentClassNameHandler {

    }

    /**
     * 按类型匹配（value = IOException）的条件 bean
     */
    @Profile(PROFILE)
    @ControllerAdvice
    @ConditionalOnMissingExceptionHandler(IOException.class)
    static class ValueTypeHandler {

    }

    /**
     * 父类 advice：在父类方法上声明 {@code @ExceptionHandler}，用于验证继承方法计入子类声明集
     */
    @Profile(PROFILE)
    @ControllerAdvice
    static class BaseAdvice {

        /**
         * 处理 IOException（声明在父类）
         *
         * @param e IO 异常
         */
        @ExceptionHandler(IOException.class)
        public void handle(IOException e) {
        }

    }

    /**
     * 子类 advice：继承 {@link BaseAdvice}，并声明一个未指定异常类型的 {@code @ExceptionHandler}
     * <p>用于验证父类声明的方法计入子类 advice 的声明集；裸 {@code @ExceptionHandler} 不参与声明集（见 1.7）。</p>
     */
    @Profile(PROFILE)
    @ControllerAdvice
    static class SubclassAdvice extends BaseAdvice {

        /**
         * 未声明异常类型：Spring 语义即兜底一切异常
         *
         * @param e 异常
         */
        @ExceptionHandler
        public void handleAny(Throwable e) {
        }

    }

    /**
     * 无异常处理方法的 advice：仅占位，声明集为空
     */
    @Profile(PROFILE)
    @ControllerAdvice
    static class NoExceptionHandlerAdvice {

    }

    /**
     * 声明处理 IOException 的 advice：使上述两个条件 bean 均不装配
     */
    @Profile(PROFILE)
    @ControllerAdvice
    static class IOExceptionAdvice {

        /**
         * 处理 IOException
         *
         * @param e IO 异常
         */
        @ExceptionHandler(IOException.class)
        public void handle(IOException e) {
        }

    }

}
