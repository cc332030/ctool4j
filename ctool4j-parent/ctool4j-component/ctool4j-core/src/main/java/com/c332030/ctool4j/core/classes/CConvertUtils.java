package com.c332030.ctool4j.core.classes;

import cn.hutool.core.lang.Opt;
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
 *
 * @since 2025/11/22
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
     */
    public final CBiClassValue<CFunction<Object, ?>> VALUE_SET_CLASS_VALUE =
            CBiClassValue.of((fromClass, toClass) -> {

                if(Collection.class.isAssignableFrom(fromClass)
                        || Map.class.isAssignableFrom(fromClass)
                        || fromClass.isArray()
                ) {
                    return null;
                }

                if(toClass.isAssignableFrom(fromClass)) {
                    return CFunction.SELF;
                }

                for (val classConverter : CLASS_CONVERTERS) {
                    if(classConverter.getFromClass().isAssignableFrom(fromClass)
                            && classConverter.getToClass().isAssignableFrom(toClass)) {
                        return CObjUtils.anyType(classConverter.getConverter());
                    }
                }

                return null;
            });

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
