package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.core.validation.CValidUtils;
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
 *   <li>扫描包缓存清理：{@code clearScannedBasePackages(basePackages)}。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>静态字段以类为宿主注入（{@code object} 为 {@code null}），实例字段以对象为宿主注入。</li>
 *   <li>标记 {@code CAutowiredScan} 的类本身可能是非 Spring Bean 的静态工具类，因此扫描不能走
 *   {@code getBeansWithAnnotation}，只能按包扫描类后逐个注入（由 {@code CAutowiredScanConfiguration}
 *   在启动时触发），保证工具类的静态字段先于业务调用完成注入。</li>
 *   <li>注入源（Bean 从哪来）可由调用方显式传入 {@link ApplicationContext}；不显式传入时经
 *   {@link CSpringUtils#getBean(Class)} 取值（自有上下文优先、为空时兜底 Hutool），
 *   取值口径只写在该一处、不在各调用点重复判定。</li>
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
     * 启动期已扫描过的包：同一批包在启动阶段会被多处（{@code autowiredScan}、{@code listAnnotatedClassThenDo}）
     * 反复传入，重复扫描同一包只重复付出类扫描成本。
     * <p>仅在启动阶段有效——启动完成后不再有新的包需要扫描，故由
     * {@link #clearScannedBasePackages(Set)} 在应用启动完成时释放</p>
     */
    private final Set<String> SCANNED_BASE_PACKAGES = new LinkedHashSet<>();

    /**
     * 记录启动期已扫描的包
     *
     * @param basePackage 包名；空白包名不记录
     */
    private void recordScannedBasePackage(String basePackage) {

        val available = CStrUtils.toAvailable(basePackage);
        if (CValidUtils.isNotValid(available)) {
            return;
        }

        SCANNED_BASE_PACKAGES.add(available);
    }

    /**
     * 清除启动期扫描包缓存
     *
     * <p><b>详细设计</b>：移除 {@code basePackages} 已记录过的包；未记录过的包无副作用。</p>
     * <p><b>适用范围</b>：由应用启动完成回调调用一次——启动阶段结束后不再有扫描需求，
     * 缓存继续持有只会占用内存（集合元素为包名字符串，量级为启动类所在包的数量）。</p>
     *
     * @param basePackages 待清除的包集合；为 {@code null} 或空集合时无操作
     */
    public void clearScannedBasePackages(Set<String> basePackages) {

        if (CValidUtils.isNotValid(basePackages)) {
            return;
        }

        basePackages.forEach(SCANNED_BASE_PACKAGES::remove);
    }

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
     * 注入单个字段（注入源取自框架自有上下文）
     *
     * <p><b>详细步骤</b>：按字段类型经 {@link CSpringUtils#getBean(Class)} 取 Bean，再委托
     * {@link #autowired(Object, Class, Object, Field)} 写入——本方法不直接依赖 Hutool {@code SpringUtil}，
     * 取值口径（框架自有上下文优先、为空时兜底 Hutool）统一由 {@code CSpringUtils#getBean} 承载，
     * 避免「工具类在哪个上下文里初始化」这类隐式依赖在调用点各写一遍。</p>
     *
     * @param type   类
     * @param object 对象，为 null 时表示静态字段
     * @param field  字段
     */
    public void autowired(Class<?> type, Object object, Field field) {
        autowired(CSpringUtils.getBean(field.getType()), type, object, field);
    }

    /**
     * 注入单个字段（注入源由调用方显式指定）
     *
     * <p><b>字段写入</b>：委托 {@link CReflectUtils#setValue(Object, Field, Object)} 写入（非 final 字段走缓存的
     * setter 方法句柄快速路径），不直接调用原生反射 {@code Field#set}。</p>
     *
     * @param bean           字段对应的 Bean
     * @param type           类
     * @param object         对象，为 null 时表示静态字段
     * @param field          字段
     */
    @SneakyThrows
    public void autowired(Object bean, Class<?> type, Object object, Field field) {

        val fieldType = field.getType();
        CReflectUtils.setValue(object, field, bean);

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
            recordScannedBasePackage(basePackage);
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
            recordScannedBasePackage(basePackage);
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
