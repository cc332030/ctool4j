package com.c332030.ctool4j.core.log;

import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.cache.impl.CRefClassValue;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.definition.annotation.CJsonLog;
import com.c332030.ctool4j.definition.model.result.ICBaseResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.InputStreamSource;
import org.springframework.lang.NonNull;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

/**
 * 处理并记录日志文件
 */
@Slf4j
@UtilityClass
public class CLogUtils {

    /**
     * 日志缓存
     */
    private static final ClassValue<CLog> LOGS = new ClassValue<CLog>() {
        @Override
        protected CLog computeValue(@NonNull Class<?> type) {
            return new CLog(type);
        }
    };

    /**
     * 获取日志
     * @param name 日志名称
     * @return CLog
     */
    public CLog getLog(String name) {
        return new CLog(name);
    }

    /**
     * 获取日志
     * @param clazz 获取日志
     * @return CLog
     */
    public CLog getLog(Class<?> clazz) {
        return LOGS.get(clazz);
    }

    /**
     * 不转 json 的包名
     */
    private static final Set<String> NOT_JSON_LOG_DOMAIN_PACKAGE = new CopyOnWriteArraySet<>(CSet.of(
            ".sun.",
            ".apache."
    ));

    /**
     * 转 json 的包名
     */
    private static final Set<String> JSON_LOG_DOMAIN_PACKAGE = new CopyOnWriteArraySet<>(CSet.of(
            ".config.",
            ".entity.",
            ".model.",
            ".pojo.",
            ".dto.",
            ".param.",
            ".query.",
            ".req.",
            ".rsp.",
            ".request.",
            ".response.",
            ".domain."
    ));

    /**
     * 添加转 json 的包名
     * @param domainPackage 转 json 的包名
     */
    public void addJsonLogDomainPackage(String domainPackage) {
        JSON_LOG_DOMAIN_PACKAGE.add(domainPackage);
    }

    /**
     * 转 json 的注解
     */
    private static final Set<Class<? extends Annotation>> JSON_LOG_ANNOTATIONS = new CopyOnWriteArraySet<>(CSet.of(
            CJsonLog.class,
            ConfigurationProperties.class
    ));

    /**
     * 添加转 json 的注解
     * @param tClass 转 json 的注解
     */
    public void addJsonLogAnnotations(Class<? extends Annotation> tClass) {
        JSON_LOG_ANNOTATIONS.add(tClass);
    }

    /**
     * 不转 json 的父类
     */
    private static final Set<Class<?>> NOT_JSON_LOG_SUPERCLASSES = new CopyOnWriteArraySet<>(CSet.of(
            DataSource.class,
            InputStream.class,
            OutputStream.class,
            InputStreamSource.class,
            Throwable.class
    ));

    /**
     * 添加不转 json 的父类
     * @param tClass 不转 json 的父类
     */
    public void addNotJsonLogSuperclasses(Class<?> tClass) {
        NOT_JSON_LOG_SUPERCLASSES.add(tClass);
    }

    /**
     * 转 json 的父类
     */
    private static final Set<Class<?>> JSON_LOG_SUPERCLASSES = new CopyOnWriteArraySet<>(CSet.of(
            ICBaseResult.class,
            Collection.class,
            Map.class
    ));

    /**
     * 添加转 json 的父类
     * @param tClass 转 json 的父类
     */
    public void addJsonLogSuperclasses(Class<?> tClass) {
        JSON_LOG_SUPERCLASSES.add(tClass);
    }

    /**
     * 是否转换 json 缓存
     */
    public static final CRefClassValue<Boolean> JSON_LOG_CLASS_VALUE = CRefClassValue.of(
            type -> {

                if (type.isEnum()) {
                    return false;
                }

                // 配置类型 or 报错类型不转 json
                for (val clazz : NOT_JSON_LOG_SUPERCLASSES) {
                    if (clazz.isAssignableFrom(type)) {
                        return false;
                    }
                }

                for (val domainPackage : NOT_JSON_LOG_DOMAIN_PACKAGE) {
                    if (type.getName().contains(domainPackage)) {
                        return false;
                    }
                }

                // 实现 Serializable.class 转 json 和 java base 包不转 json 冲突
                for (val clazz : JSON_LOG_SUPERCLASSES) {
                    if (clazz.isAssignableFrom(type)) {
                        return true;
                    }
                }

                for (val domainPackage : JSON_LOG_DOMAIN_PACKAGE) {
                    if (type.getName().contains(domainPackage)) {
                        return true;
                    }
                }

                for (val clazz : JSON_LOG_ANNOTATIONS) {
                    if (CClassUtils.isAnnotationPresent(type, clazz)) {
                        return true;
                    }
                }

                // 基础数据类型不转 json
                return !CClassUtils.isBasicClass(type);
            }
    );

    /**
     * 设置是否转 json
     * @param types types
     * @param value value
     */
    public void setJsonLog(Collection<Class<?>> types, boolean value) {
        types.forEach(type -> JSON_LOG_CLASS_VALUE.set(type, value));
    }

    /**
     * 设置是否转 json
     * @param type type
     * @param value value
     */
    public void setJsonLog(Class<?> type, boolean value) {
        setJsonLog(Collections.singletonList(type), value);
    }

    /**
     * 是否能转 json
     * @param type type
     * @return boolean
     */
    public boolean isJsonLog(Class<?> type) {
        return JSON_LOG_CLASS_VALUE.get(type);
    }

    /**
     * 获取 Supplier<Object>[] 的参数
     * @param suppliers Supplier<Object>[]
     * @return Object[]
     */
    public Object[] getSupplierArgs(Supplier<Object>[] suppliers) {

        if (ArrayUtil.isEmpty(suppliers)) {
            return CArrUtils.EMPTY_OBJECT_ARRAY;
        }

        return Arrays.stream(suppliers)
                .map(Supplier::get)
                .toArray();
    }

    /**
     * 处理参数
     * @param args args
     */
    public void dealArgs(Object[] args) {
        dealArgs(args, false);
    }

    /**
     * 处理参数
     * @param args args
     * @param nonNull 是否不打印 null
     */
    public void dealArgs(Object[] args, boolean nonNull) {

        if (ArrayUtil.isEmpty(args)) {
            return;
        }

        for (int i = 0; i < args.length; i++) {

            var arg = args[i];
            if (null != arg) {
                val argType = arg.getClass();
                if (isJsonLog(argType)) {
                    try {
                        args[i] = nonNull
                                ? CJsonUtils.toJsonNonNull(arg)
                                : CJsonUtils.toJson(arg);
                    } catch (Exception e) {
                        setJsonLog(argType, false);
                        log.error("转 json 失败，禁用 json 转换：{}", argType, e);
                    }
                }
            }
        }
    }

}
