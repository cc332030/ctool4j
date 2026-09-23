package com.c332030.ctool4j.web.exception.condition;

import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.spring.util.CAnnotationUtils;
import com.c332030.ctool4j.web.exception.annotation.ConditionalOnMissingExceptionHandler;
import lombok.CustomLog;
import lombok.val;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Description: ConditionalOnMissingExceptionHandlerCondition
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>核心方法 {@code matches(ConditionContext, AnnotatedTypeMetadata)}：</p>
 * <ul>
 *   <li>从注解元数据读取要检查的异常类型：{@code valueName()}（全限定类名，字符串比较）优先，其次 {@code value()}</li>
 *   <li>取容器中所有 {@code ControllerAdvice} bean 的<b>类型</b>（{@code getBeanNamesForAnnotation} + {@code getType}，只读 bean 定义、不实例化 bean）</li>
 *   <li>无任何 {@code ControllerAdvice} 类型时返回 true（启用默认 Handler）</li>
 *   <li>按 advice 类型读取其 {@code @ExceptionHandler} 声明（<b>按类型缓存，只解析一次</b>），命中该异常类型或其类名时返回 false（禁用默认 Handler）</li>
 *   <li>均未处理时返回 true（启用默认 Handler）</li>
 *   <li>{@code clearDeclarationsCache()}：清空按 advice 类型的声明集缓存（应用启动完成时调用一次）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>注解不存在</td>
 *     <td>{@code Assert.notNull} 抛异常</td>
 *   </tr>
 *   <tr>
 *     <td>beanFactory 为 null</td>
 *     <td>{@code Assert.notNull} 抛异常</td>
 *   </tr>
 *   <tr>
 *     <td>beanFactory 不可列举（非 {@code ListableBeanFactory}）</td>
 *     <td>按「无 ControllerAdvice」处理，返回 true</td>
 *   </tr>
 *   <tr>
 *     <td>无 ControllerAdvice</td>
 *     <td>返回 true，启用默认 Handler</td>
 *   </tr>
 *   <tr>
 *     <td>某 bean 名取不到类型（延迟初始化等）</td>
 *     <td>跳过该 bean，继续遍历；记 debug 日志，不中断条件判断</td>
 *   </tr>
 *   <tr>
 *     <td>advice 无方法或方法无 {@code @ExceptionHandler}</td>
 *     <td>该 advice 的声明集为空，继续遍历</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>框架内置异常处理器的条件装配，避免与业务方自定义处理器冲突。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不做 Spring 的「最近匹配」与 cause 回退解析：只判「容器里有没有声明处理该类型的处理器」，
 *   因此业务方声明处理器超类（如用 {@code @ExceptionHandler(RuntimeException.class)} 接 {@code IllegalStateException}）时本条件不判为命中，
 *   该场景由 {@code @Order} 档位（见 {@code CExceptionHandlerOrder}）决定归属。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>声明集<b>按 advice 类型缓存</b>（{@code CClassValue}，即 JDK {@code ClassValue}）：按类弱关联，<b>键的类被卸载时</b>条目才释放。
 *   应用自身的 advice 类型由本应用类加载器长期持有，等于在应用存活期间不会自行释放，故由
 *   {@link #clearDeclarationsCache()} 在应用启动完成时显式清空（触发点见 {@code CWebInit#onStarted()}）。</li>
 *   <li>「同类结果恒定」只说明<b>缓存不会变脏</b>，不等于<b>不必清理</b>：清空的收益是释放内存、代价是清空后同进程再次装配时声明集被重算。</li>
 *   <li>清理入口由调用方按「容器就绪」这一时点触发；本类<b>不自行监听事件</b>——它是 {@code Condition}（装配期由容器实例化），
 *   监听事件需要容器 Bean 的身份，而条件类在装配期求值、不应依赖自身被注册为 Bean。</li>
 *   <li><b>不缓存</b>「容器 → advice 类型清单」：容器类型在装配期才逐步注册、同容器可被反复装配，
 *   按键缓存会在容器实例被回收后长期驻留（条目数随容器数无界增长），而正确性又要求每次都读当前 bean 定义。</li>
 *   <li>只采集 {@code @ExceptionHandler} <b>显式声明</b>的类型：裸 {@code @ExceptionHandler}（{@code value} 为空）在 Spring 里兜底一切异常，
 *   但不计入声明集——否则注解走默认值的 {@code @ConditionalOnMissingExceptionHandler(Throwable.class)} 会被误判为「已有处理器」。</li>
 *   <li>按类名比较（{@code valueName}）只比类型名、不比继承关系（与按类型比较一致：{@code @ExceptionHandler} 声明的是具体类型）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>性能：一次解析、多处复用</b></p>
 * <ul>
 *   <li>旧实现每评估一个 handler 条件就重新 {@code getBeansWithAnnotation}（<b>会实例化</b>全部 {@code ControllerAdvice}）、
 *   再逐个反射遍历其全部方法——成本为「handler 数 × advice 数 × 方法数」。现改为：</li>
 *   <li><b>按 advice 类型</b>缓存其 {@code @ExceptionHandler} 声明集（类型集合 + 类名集合），由 {@code CExceptionHandlerDeclarations#of} 构建一次、经 {@code CClassValue} 按类弱关联；</li>
 *   <li>advice 类型清单<b>每次读容器当前的 bean 定义</b>（{@code getBeanNamesForAnnotation} + {@code getType}，均为轻量查找、不实例化 bean），不跨容器缓存。</li>
 * </ul>
 * <p><b>不实例化 bean</b></p>
 * <ul>
 *   <li>取类型走 {@code ListableBeanFactory#getBeanNamesForAnnotation} + {@code #getType}（仅 bean 定义），
 *   不触发 {@code ControllerAdvice} 的实例化——条件评估期过早实例化会绕过后续装配顺序、也可能在 bean 未就绪时失败。</li>
 * </ul>
 * <p><b>类名匹配（valueName）</b></p>
 * <ul>
 *   <li>{@code valueName} 非空时只取类名、<b>不解析目标类</b>，与各 advice 的 {@code @ExceptionHandler} 声明类型名比较——
 *   使容器私有/可选依赖的类型（如 Tomcat 的 {@code ClientAbortException}）在缺失该类的环境也能完成条件判断。</li>
 * </ul>
 * <p><b>空安全</b></p>
 * <ul>
 *   <li>beanFactory 不可列举、类型清单为空时按「无 ControllerAdvice」处理，返回 true 启用默认 Handler。</li>
 * </ul>
 *
 * @since 2026/4/9
 * @version 1.4
 * @see "doc/design/web/exception-fallback.adoc"
 */
@CustomLog
public class ConditionalOnMissingExceptionHandlerCondition implements Condition {

    /**
     * 注解属性名：待检查异常类型的全限定类名
     */
    private static final String VALUE_NAME = "valueName";

    /**
     * advice 类型 → 其 {@code @ExceptionHandler} 声明集（按类缓存，构建一次、被全部条件评估复用）
     * <p>由 {@link #clearDeclarationsCache()} 在应用启动完成时清空。</p>
     */
    private static final CClassValue<CExceptionHandlerDeclarations> DECLARATIONS_CLASS_VALUE =
        CClassValue.of(CExceptionHandlerDeclarations::of);

    /**
     * 匹配条件：无任何 ControllerAdvice 或均未处理该异常类型时返回 true
     *
     * @param context  条件上下文
     * @param metadata 注解元数据
     * @return 是否满足条件
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        val annotationType = ConditionalOnMissingExceptionHandler.class;

        // 优先按类名匹配（valueName）：容器私有/可选依赖的异常类型（如 Tomcat 的 ClientAbortException）
        // 在缺失该类的环境无法解析为 Class，按字符串比较可避免类加载失败
        String valueName = CAnnotationUtils.getAnnotationAttributeValue(metadata, annotationType, VALUE_NAME);
        val byName = null != valueName && !valueName.isEmpty();
        Class<Throwable> throwableClass = byName ? null : CAnnotationUtils.getAnnotationValue(metadata, annotationType);
        if (!byName) {
            Assert.notNull(throwableClass, () -> "注解不存在：" + annotationType.getName());
        }

        val throwableClassName = byName ? valueName : throwableClass.getSimpleName();

        val beanFactory = context.getBeanFactory();
        Assert.notNull(beanFactory, "beanFactory must not be null");

        // 容器不可列举（自定义 BeanFactory）时无法枚举 advice：按「无 ControllerAdvice」处理，启用默认 Handler
        if (!(beanFactory instanceof ListableBeanFactory)) {
            log.debug("enable default @ExceptionHandler for {} because beanFactory is not listable", throwableClassName);
            return true;
        }

        val adviceTypes = resolveAdviceTypes((ListableBeanFactory) beanFactory);
        if (ArrayUtil.isEmpty(adviceTypes)) {
            log.debug("enable default @ExceptionHandler for {} because no ControllerAdvice defined", throwableClassName);
            return true;
        }

        for (val adviceType : adviceTypes) {

            // 按 advice 类型缓存：同一 advice 的声明集只解析一次，与「有几个 handler 条件要评估」无关
            val declarations = DECLARATIONS_CLASS_VALUE.get(adviceType);

            // valueName 按类名比较、value 按类型比较
            val matched = byName
                ? declarations.declaredNames().contains(valueName)
                : declarations.declaredTypes().contains(throwableClass);
            if (matched) {
                log.debug("disable default @ExceptionHandler for {} because {} defined",
                    throwableClassName, adviceType.getSimpleName());
                return false;
            }

        }

        log.debug("enable default @ExceptionHandler for {} because no existed defined", throwableClassName);
        return true;
    }

    /**
     * 清空按 advice 类型的声明集缓存
     *
     * <p><b>适用场景</b>：应用启动完成时调用一次（{@code CWebInit#onStarted()}）——装配期已过，
     * 声明集不再被读取，继续持有只会占用内存。</p>
     *
     * <p><b>详细设计</b>：清的是 {@code DECLARATIONS_CLASS_VALUE} <b>本实例记录过的键</b>——
     * 该对象只被本类使用，故清的就是本类产生的那份；容器类型清单本就不缓存、不在此列。</p>
     *
     * <p><b>代价与边界</b>：清空后若同进程内还有容器重新装配，声明集会被重新计算
     * （判定语义不变，只是多付一次解析成本）。</p>
     */
    public static void clearDeclarationsCache() {

        DECLARATIONS_CLASS_VALUE.clear();

    }

    /**
     * 单个 advice 的 {@code @ExceptionHandler} 声明集
     * <p>作为按 advice 类型的缓存值：同一 advice 被多个 handler 条件评估时只构建一次。</p>
     *
     * @param declaredTypes 声明处理的异常类型集合
     * @param declaredNames 声明处理的异常类型<b>全限定类名</b>集合（用于注解走 {@code valueName} 的类名比较）
     */
    private static class CExceptionHandlerDeclarations {

        /**
         * 声明处理的异常类型
         */
        private final Set<Class<?>> declaredTypes;

        /**
         * 声明处理的异常类型全限定类名
         */
        private final Set<String> declaredNames;

        /**
         * 构造声明集
         *
         * @param declaredTypes 声明处理的异常类型
         * @param declaredNames 声明处理的异常类型全限定类名
         */
        private CExceptionHandlerDeclarations(Set<Class<?>> declaredTypes, Set<String> declaredNames) {
            this.declaredTypes = declaredTypes;
            this.declaredNames = declaredNames;
        }

        /**
         * 解析指定 advice 类型的方法，构建其 {@code @ExceptionHandler} 声明集
         * <p>只读方法上的注解、不实例化 bean；只采集 {@code @ExceptionHandler} 显式声明的类型。</p>
         *
         * @param adviceType advice 类型
         * @return 声明集
         */
        private static CExceptionHandlerDeclarations of(Class<?> adviceType) {

            Set<Class<?>> declaredTypes = new HashSet<>();
            Set<String> declaredNames = new HashSet<>();

            val methods = CReflectUtils.getAllMethodsCached(adviceType);
            for (val method : methods) {

                val annotations = method.getAnnotationsByType(ExceptionHandler.class);
                for (val annotation : annotations) {

                    // 只采集注解显式声明的类型：裸 @ExceptionHandler（value 为空）按 Spring 语义兜底一切异常，
                    // 但它「不是」声明处理 Throwable——若把它折成 Throwable.class，会让注解走默认值的
                    // @ConditionalOnMissingExceptionHandler(Throwable.class) 被误判为已有处理器（回归）。
                    for (val value : annotation.value()) {
                        declaredTypes.add(value);
                        // declaredNames 存全限定类名：与 valueName 的契约同口径
                        declaredNames.add(value.getName());
                    }

                }

            }

            if (declaredTypes.isEmpty()) {
                return new CExceptionHandlerDeclarations(CSet.of(), CSet.of());
            }

            return new CExceptionHandlerDeclarations(declaredTypes, declaredNames);
        }

        /**
         * 获取声明处理的异常类型集合
         *
         * @return 异常类型集合
         */
        private Set<Class<?>> declaredTypes() {
            return declaredTypes;
        }

        /**
         * 获取声明处理的异常类型全限定类名集合
         *
         * @return 异常类型全限定类名集合
         */
        private Set<String> declaredNames() {
            return declaredNames;
        }

    }

    /**
     * 取容器中全部 {@code ControllerAdvice} 的类型
     * <p>只读 bean 定义（{@code getBeanNamesForAnnotation} + {@code getType}），不实例化 bean；
     * 单个 bean 名取不到类型时跳过并记 debug 日志，不中断条件判断。</p>
     *
     * @param beanFactory 可列举的 bean 工厂
     * @return advice 类型清单（无 advice 时为空，不为 {@code null}）
     */
    private static Class<?>[] resolveAdviceTypes(ListableBeanFactory beanFactory) {

        val beanNames = beanFactory.getBeanNamesForAnnotation(ControllerAdvice.class);
        if (ArrayUtil.isEmpty(beanNames)) {
            return new Class<?>[0];
        }

        val adviceTypes = new ArrayList<Class<?>>(beanNames.length);
        for (val beanName : beanNames) {

            try {
                adviceTypes.add(beanFactory.getType(beanName));
            } catch (Exception e) {
                // 取不到类型（延迟初始化、bean 定义异常等）：跳过该 bean，不影响其余 advice 的判定
                log.debug("skip ControllerAdvice bean {} because type is not resolvable", beanName, e);
            }

        }

        return adviceTypes.toArray(new Class<?>[0]);
    }

}
