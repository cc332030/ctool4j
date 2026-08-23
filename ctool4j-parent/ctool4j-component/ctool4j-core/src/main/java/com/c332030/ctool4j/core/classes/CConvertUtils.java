package com.c332030.ctool4j.core.classes;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ClassUtil;
import com.c332030.ctool4j.core.cache.impl.CBiClassValue;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * Description: CConvertUtils
 * </p>
 * <p>
 * 默认转换器由静态块人工注册，正确性由人工与测试用例保证；
 * 查找按注册顺序先注册优先，类似类加载双亲委派，原则上不可被后注册的覆盖。
 * </p>
 *
 * @since 2025/11/22
 * @see "doc/design/core/CConvertUtils.adoc"
 * @see "doc/design/core/CConvertUtilsTests.adoc"
 */
@CustomLog
@UtilityClass
public class CConvertUtils {

    private static final List<CClassConverter<?, ?>> CLASS_CONVERTERS = new CopyOnWriteArrayList<>();
    static {

        log.info("初始化默认类型转换");
        val methods = CReflectUtils.getAllMethodsCached(CClassConvert.class);
        methods.stream()
                .filter(CReflectUtils::isStatic)
                .forEach(CConvertUtils::addConverter);
    }

    /**
     * 注册方法为类型转换器
     *
     * @param method 转换方法（入参为源类型，返回值为目标类型）
     */
    public void addConverter(Method method) {

        if (method.getParameterCount() < 1) {
            // 无入参的方法无法确定源类型（getParameterTypes()[0] 会数组越界），跳过注册（Q16）
            log.warn("跳过注册转换器：方法无入参，method: {}", method);
            return;
        }

        @SuppressWarnings("unchecked")
        val fromClass = (Class<Object>) method.getParameterTypes()[0];
        @SuppressWarnings("unchecked")
        val toClass = (Class<Object>) method.getReturnType();
        addConverter(fromClass, toClass, o -> method.invoke(null, o));
    }

    /**
     * 注册类型转换器
     *
     * @param fromClass 源类型
     * @param toClass   目标类型
     * @param converter 转换函数
     * @param <From>    源类型
     * @param <To>      目标类型
     */
    public <From, To> void addConverter(
            Class<From> fromClass,
            Class<To> toClass,
            CFunction<From, To> converter) {

        log.debug("添加映射，fromClass: {}, toClass: {}, converter: {}", fromClass, toClass, converter);

        val classConverter = CClassConverter.<From, To>builder()
                .fromClass(fromClass)
                .toClass(toClass)
                .converter(converter)
                .build();
        CLASS_CONVERTERS.add(classConverter);
    }

    /**
     * 类型转换器缓存（按源类型与目标类型查找）
     * <p>Object 源类型转换器（如 Object→String）为兜底、优先级最低：仅在无更精确
     * 转换器匹配时命中，避免抢占 Date→String 等特殊转换。</p>
     */
    public final CBiClassValue<CFunction<Object, ?>> VALUE_SET_CLASS_VALUE =
            CBiClassValue.of((fromClass, toClass) -> findConverter(fromClass, toClass, true));

    /**
     * 查找类型转换器
     *
     * @param fromClass 源类型
     * @param toClass   目标类型
     * @param includeObjectFallback 是否包含 Object 源兜底转换器（如 Object→String）
     * @return 转换器，未匹配时返回 null
     */
    private static CFunction<Object, ?> findConverter(
            Class<?> fromClass,
            Class<?> toClass,
            boolean includeObjectFallback
    ) {

        if(Collection.class.isAssignableFrom(fromClass)
                || Map.class.isAssignableFrom(fromClass)
                || fromClass.isArray()
        ) {
            return null;
        }

        // ClassUtil.isAssignable(目标, 源) 支持原始类型与包装类等价（如 int←Integer 拆箱、Integer←int 装箱），直接按 SELF 写入
        if(ClassUtil.isAssignable(toClass, fromClass)) {
            return CFunction.SELF;
        }

        CFunction<Object, ?> objectFallback = null;
        for (val classConverter : CLASS_CONVERTERS) {
            if(classConverter.getFromClass().isAssignableFrom(fromClass)
                    && classConverter.getToClass().isAssignableFrom(toClass)) {

                if(classConverter.getFromClass() == Object.class) {
                    // Object 源兜底最后匹配（优先级最低），记录后继续找更精确的
                    if(null == objectFallback) {
                        objectFallback = CObjUtils.anyType(classConverter.getConverter());
                    }
                    continue;
                }

                return CObjUtils.anyType(classConverter.getConverter());
            }
        }

        return includeObjectFallback ? objectFallback : null;
    }

    /**
     * 获取类型转换器
     *
     * @param fromClass 源类型
     * @param toClass   目标类型
     * @param <To>      目标类型
     * @return 转换器，未注册时返回 null
     */
    public <To> CFunction<Object, To> getConverter(Class<?> fromClass, Class<To> toClass) {
        return CObjUtils.anyType(VALUE_SET_CLASS_VALUE.get(fromClass, toClass));
    }

    /**
     * 获取类型转换器（排除 Object 源兜底，如 Object→String）
     * <p>用于按声明类型解析转换路径：若仅 Object 源兜底可匹配，说明运行期实际值
     * 类型可能命中更精确的转换器（如 Date→String），应回退到运行期按实际值类型判断。</p>
     *
     * @param fromClass 源类型
     * @param toClass   目标类型
     * @param <To>      目标类型
     * @return 转换器，未匹配时返回 null
     */
    public <To> CFunction<Object, To> getConverterNoObjectFallback(Class<?> fromClass, Class<To> toClass) {
        return CObjUtils.anyType(findConverter(fromClass, toClass, false));
    }

    /**
     * 转换对象为目标类型
     *
     * @param from    源对象
     * @param toClass 目标类型
     * @param <To>    目标类型
     * @return 转换结果，无可用转换器时返回 null
     */
    public <To> To convert(Object from, Class<To> toClass) {
        if(from == null) {
            return null;
        }

        val converter = getConverter(from.getClass(), toClass);
        if(null == converter) {
            return null;
        }
        return converter.apply(from);
    }

    /**
     * 转换对象为目标类型，返回 Opt
     *
     * @param from    源对象
     * @param toClass 目标类型
     * @param <To>    目标类型
     * @return Opt 包装的转换结果
     */
    public <To> Opt<To> convertOpt(Object from, Class<To> toClass) {
        return Opt.ofNullable(convert(from, toClass));
    }

}
