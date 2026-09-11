package com.c332030.ctool4j.spring.util;

import cn.hutool.extra.spring.SpringUtil;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.definition.constant.CTool4jConstants;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * <p>
 * Description: CAutowiredUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAutowiredUtils}：自动注入工具，覆盖「注入」与「扫描」两件事。</p>
 * <ul>
 *   <li>注入：{@code autowired(type)} / {@code autowired(object)} / {@code autowired(type, object, field)}。</li>
 *   <li>字段查询：{@code getFieldMap(clazz)} 取标注 {@code @CAutowired} 的字段。</li>
 *   <li>扫描：{@code autowiredScan(applicationContext)} 扫描并注入全部标记 {@code CAutowiredScan} 的类。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>静态字段以类为宿主注入（{@code object} 为 {@code null}），实例字段以对象为宿主注入。</li>
 *   <li>标记 {@code CAutowiredScan} 的类本身可能是非 Spring Bean 的静态工具类，因此扫描不能走
 *   {@code getBeansWithAnnotation}，只能按包扫描类后逐个注入（由 {@code CAutowiredScanConfiguration}
 *   在启动时触发），保证工具类的静态字段先于业务调用完成注入。</li>
 *   <li>注入源（Bean 从哪来）由调用方显式传入 {@link ApplicationContext}，而非依赖 Hutool 的全局
 *   {@code SpringUtil} 静态上下文，避免「工具类在哪个上下文里初始化」这类隐式依赖。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>扫描到的类没有 {@code @CAutowired} 字段</td>
 *     <td>{@code autowired} 对空字段集合不做任何处理，不影响启动</td>
 *   </tr>
 *   <tr>
 *     <td>字段类型在容器中无对应 Bean</td>
 *     <td>取 Bean 抛异常向上冒泡，启动即失败（快速失败，便于尽早暴露配置缺失）</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要静态访问 Spring Bean 的工具类（{@code @UtilityClass} + {@code @CAutowiredScan} + {@code @CAutowired}）。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不负责 Bean 的生命周期管理，仅写入字段值。</li>
 *   <li>非 Spring 上下文（无 {@code ApplicationContext}）时不可用。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>{@code autowiredScan(applicationContext)} 每次调用都会重新扫描一遍包；仅应在启动时调用一次（由配置类保证）。</li>
 *   <li>依赖 {@code CSpringUtils.getBasePackages()}，要求上下文内存在 {@code @SpringBootApplication} 标注的启动类。</li>
 * </ul>
 *
 * @since 2025/12/23
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CAutowiredUtils {

    /**
     * 注入指定类的全部静态 CAutowired 字段
     *
     * @param type 类
     */
    public void autowired(Class<?> type) {
        autowired(type, null);
    }

    /**
     * 注入对象的全部 CAutowired 字段
     *
     * @param object 对象
     */
    public void autowired(Object object) {
        autowired(object.getClass(), object);
    }

    /**
     * 注入指定类或对象的 CAutowired 字段，静态字段以类注入，实例字段以对象注入
     *
     * @param type   类
     * @param object 对象，为 null 时仅注入静态字段
     */
    public void autowired(Class<?> type, Object object) {

        getFieldMap(type).values().forEach(field -> {

            if(null == object
                && !CReflectUtils.isStatic(field)
            ) {
                return;
            }

            autowired(type, object, field);

        });

    }

    /**
     * 注入单个字段（注入源取自 Hutool 全局 Spring 上下文）
     *
     * <p><b>详细步骤</b>：按字段类型从 Hutool 全局上下文取 Bean，再委托
     * {@link #autowired(Object, Class, Object, Field)} 写入。</p>
     *
     * @param type   类
     * @param object 对象，为 null 时表示静态字段
     * @param field  字段
     */
    public void autowired(Class<?> type, Object object, Field field) {
        autowired(SpringUtil.getBean(field.getType()), type, object, field);
    }

    /**
     * 注入单个字段（注入源由调用方显式指定）
     *
     * @param bean           字段对应的 Bean
     * @param type           类
     * @param object         对象，为 null 时表示静态字段
     * @param field          字段
     */
    @SneakyThrows
    public void autowired(Object bean, Class<?> type, Object object, Field field) {

        val fieldType = field.getType();
        field.set(object, bean);

        log.debug("CAutowired {}{}.{}({})",
            () -> null != object ? "(object)" : "",
            type::getSimpleName,
            field::getName,
            fieldType::getSimpleName
        );

    }

    /**
     * 获取标注了 CAutowired 注解的字段映射
     *
     * @param clazz 类
     * @return 字段名与字段的映射
     */
    public Map<String, Field> getFieldMap(Class<?> clazz) {
        return CReflectUtils.getFieldMap(clazz,
            e -> CClassUtils.isAnnotationPresent(e, CAutowired.class));
    }

    /**
     * 扫描并处理标注指定注解的类
     *
     * <p><b>详细步骤</b>：扫描范围取 {@code CTool4jConstants.BASE_PACKAGE} 与
     * {@link CSpringUtils#getBasePackages()}（各启动类所在包）的并集，逐包扫描后对每个类执行 {@code consumer}。</p>
     *
     * @param annotationClass 注解类型
     * @param consumer        处理回调
     * @param <T>             注解类型
     */
    public <T extends Annotation> void listAnnotatedClassThenDo(
        Class<T> annotationClass,
        CConsumer<Class<Object>> consumer
    ) {

        val basePackages = CCollUtils.concatOne(
            CTool4jConstants.BASE_PACKAGE,
            CSpringUtils.getBasePackages()
        );

        basePackages.forEach(basePackage -> {
            val classes = CClassUtils.listAnnotatedClass(annotationClass, basePackage);
            CCollUtils.forEach(classes, consumer);
        });

    }

    /**
     * 扫描并注入全部标注 {@code CAutowiredScan} 的类的静态 {@code CAutowired} 字段
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>按包扫描全部标注 {@link CAutowiredScan} 的类（含非 Spring Bean 的静态工具类）；</li>
     *   <li>取该类的 Bean，写入其每个静态 {@code CAutowired} 字段；</li>
     *   <li>逐字段写入，字段类型在容器中无对应 Bean 时取 Bean 失败并向上抛（快速失败）。</li>
     * </ol>
     *
     * <p><b>边界与默认值</b>：扫描不到任何标记类时不报错（静默完成）；未注入的字段保持 {@code null}，
     * 由调用方在首次使用时报错。重复调用会重复写入同值，无副作用但不必要。</p>
     *
     * @param applicationContext 注入源上下文
     */
    public void autowiredScan(ApplicationContext applicationContext) {

        val basePackages = CCollUtils.concatOne(
            CTool4jConstants.BASE_PACKAGE,
            CSpringUtils.getBasePackages()
        );

        basePackages.forEach(basePackage -> {
            val classes = CClassUtils.listAnnotatedClass(CAutowiredScan.class, basePackage);
            for (val scanClass : classes) {
                for (val field : getFieldMap(scanClass).values()) {
                    if (!CReflectUtils.isStatic(field)) {
                        continue;
                    }
                    autowired(applicationContext.getBean(field.getType()), scanClass, null, field);
                }
            }
        });

    }

}
