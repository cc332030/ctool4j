package com.c332030.ctool.core.log;

import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool.core.util.CArrUtils;
import com.c332030.ctool.core.util.CClassUtils;
import com.c332030.ctool.core.util.CJsonUtils;
import com.c332030.ctool.core.util.CSet;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

/**
 * 处理并记录日志文件
 */
@Slf4j
@UtilityClass
public class CLogUtils {

    private static final ClassValue<CLog> LOGS = new ClassValue<CLog>() {
        @Override
        protected CLog computeValue(@NonNull Class<?> type) {
            return new CLog(type);
        }
    };

    public CLog getLog(Class<?> clazz) {
        return LOGS.get(clazz);
    }

    public static final Map<String, Boolean> JSON_LOG_CLASS_NAMES = new ConcurrentHashMap<>();

    public void setJsonLog(String className, boolean value) {
        JSON_LOG_CLASS_NAMES.put(className, value);
    }

    public void setJsonLog(Collection<Class<?>> types, boolean value) {
        types.forEach(type -> JSON_LOG_CLASS_NAMES.put(getLogClassName(type), value));
    }

    public void setJsonLog(Class<?> type, boolean value) {
        setJsonLog(Collections.singletonList(type), value);
    }

    public String getLogClassName(Class<?> type) {
        return type.getName();
    }

    private static final Set<String> JSON_LOG_DOMAIN_PACKAGE = new CopyOnWriteArraySet<>(CSet.of(
            ".config.",
            ".entity.",
            ".model.",
            ".pojo.",
            ".dto.",
            ".req.",
            ".rsp.",
            ".request.",
            ".response.",
            ".domain."
    ));

    public void addJsonLogDomainPackage(String domainPackage) {
        JSON_LOG_DOMAIN_PACKAGE.add(domainPackage);
    }

    private static final Set<Class<? extends Annotation>> JSON_LOG_ANNOTATIONS = new CopyOnWriteArraySet<>(CSet.of(
            CJsonLog.class,
            ConfigurationProperties.class
    ));

    public void addJsonLogAnnotations(Class<? extends Annotation> tClass) {
        JSON_LOG_ANNOTATIONS.add(tClass);
    }

    private static final Set<Class<?>> NOT_JSON_LOG_SUPERCLASSES = new CopyOnWriteArraySet<>(CSet.of(
            DataSource.class,
            InputStream.class,
            OutputStream.class,
            InputStreamSource.class,
            Throwable.class
    ));

    public void addNotJsonLogSuperclasses(Class<?> tClass) {
        NOT_JSON_LOG_SUPERCLASSES.add(tClass);
    }

    private static final Set<Class<?>> JSON_LOG_SUPERCLASSES = new CopyOnWriteArraySet<>(CSet.of(
            Collection.class,
            Map.class
    ));

    public void addJsonLogSuperclasses(Class<?> tClass) {
        JSON_LOG_SUPERCLASSES.add(tClass);
    }

    public boolean isJsonLog(Class<?> type) {

        val name = getLogClassName(type);

        val value = JSON_LOG_CLASS_NAMES.get(name);
        if (null != value) {
            return value;
        }

        return JSON_LOG_CLASS_NAMES.computeIfAbsent(name,
                k -> {

                    if (type.isEnum()) {
                        return false;
                    }

                    // 配置类型 or 报错类型不转 json
                    for (val clazz : NOT_JSON_LOG_SUPERCLASSES) {
                        if (clazz.isAssignableFrom(type)) {
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
                        if (name.contains(domainPackage)) {
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
    }

    public Object[] getSupplierArgs(Supplier<Object>[] suppliers) {

        if (ArrayUtil.isEmpty(suppliers)) {
            return CArrUtils.EMPTY_OBJECT_ARRAY;
        }

        return Arrays.stream(suppliers)
                .map(Supplier::get)
                .toArray();
    }

    public void dealArgs(Object[] args) {
        dealArgs(args, false);
    }

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
