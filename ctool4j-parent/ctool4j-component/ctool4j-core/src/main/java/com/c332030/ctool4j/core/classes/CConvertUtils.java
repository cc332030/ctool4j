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

    private static final List<ClassConverter<?, ?>> CLASS_CONVERTERS = new CopyOnWriteArrayList<>();
    static {

        log.info("初始化默认类型转换");
        val methods = CReflectUtils.getMethods(CClassConvert.class);
        methods.stream()
                .filter(CReflectUtils::isStatic)
                .forEach(CConvertUtils::addConverter);
    }

    public void addConverter(Method method) {

        @SuppressWarnings("unchecked")
        val fromClass = (Class<Object>) method.getParameterTypes()[0];
        @SuppressWarnings("unchecked")
        val toClass = (Class<Object>) method.getReturnType();
        addConverter(fromClass, toClass, o -> method.invoke(null, o));
    }

    public <From, To> void addConverter(
            Class<From> fromClass,
            Class<To> toClass,
            CFunction<From, To> converter) {

        log.debug("添加映射，fromClass: {}, toClass: {}, converter: {}", fromClass, toClass, converter);

        val classConverter = ClassConverter.<From, To>builder()
                .fromClass(fromClass)
                .toClass(toClass)
                .converter(converter)
                .build();
        CLASS_CONVERTERS.add(classConverter);
    }

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

    public <To> CFunction<Object, To> getConverter(Class<?> fromClass, Class<To> toClass) {
        return CObjUtils.anyType(VALUE_SET_CLASS_VALUE.get(fromClass, toClass));
    }

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

    public <To> Opt<To> convertOpt(Object from, Class<To> toClass) {
        return Opt.ofNullable(convert(from, toClass));
    }

}
