package com.c332030.ctool4j.spring.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.enums.CProfileEnum;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.definition.function.ToStringFunction;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Consumer;

/**
 * <p>
 * Description: CSpringUtils
 * </p>
 *
 * @since 2025/9/10
 */
@CustomLog
@UtilityClass
public class CSpringUtils {

    /**
     * 获取当前应用分组
     * @return 应用分组
     */
    public String getApplicationGroup() {
        return CSpringConfigBeans.getSpringApplicationConfig().getGroup();
    }

    /**
     * 获取当前应用名称
     * @return 应用名称
     */
    public String getApplicationName() {
        return CSpringConfigBeans.getSpringApplicationConfig().getName();
    }

    /**
     * 判断 event 的源是否是当前上下文
     * @param event 事件
     * @return 是否是当前上下文的 event
     */
    public boolean isCurrentContextEvent(ApplicationEvent event) {

        val source = event.getSource();
        if (!(source instanceof ApplicationContext)) {
            return false;
        }

        return isCurrentContext((ApplicationContext)source);
    }

    /**
     * 判断 applicationContext 是否是当前上下文
     * @param applicationContext 应用上下文
     * @return 是否是当前上下文
     */
    public boolean isCurrentContext(ApplicationContext applicationContext) {
        return getApplicationContext() == applicationContext;
    }

    /**
     * 获取当前应用上下文
     * @return 当前应用上下文
     */
    public ApplicationContext getApplicationContext() {
        return SpringUtil.getApplicationContext();
    }

    /**
     * 获取指定类型的 bean
     * @param tClass bean 类型
     * @return bean
     * @param <T> 泛型
     */
    public <T> T getBean(Class<T> tClass) {
        return SpringUtil.getBean(tClass);
    }

    /**
     * 获取有指定注解的所有 bean
     * @param tClass bean 类型
     * @return bean map
     */
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> tClass) {
        return getApplicationContext().getBeansWithAnnotation(tClass);
    }

    /**
     * 获取指定类型并注入属性
     * @param tClass bean 类型
     * @param consumers 属性注入
     * @param <T> 泛型
     */
    @SafeVarargs
    public <T> void wireBean(Class<T> tClass, Consumer<T>... consumers) {
        val bean = getBean(tClass);
        for (val consumer : consumers) {
            consumer.accept(bean);
        }
    }

    public Set<String> getBasePackages() {

        val springApplicationMap = getBeansWithAnnotation(SpringBootApplication.class);
        CAssert.notEmpty(springApplicationMap, "springApplicationMap 不能为空");

        val basePackages = new LinkedHashSet<String>();
        springApplicationMap.values().forEach(springApplication -> {

            val mainApplicationClass = springApplication.getClass();
            CAssert.notNull(mainApplicationClass, "mainApplicationClass 不能为空");

            val springBootAppAnnotation = mainApplicationClass.getAnnotation(SpringBootApplication.class);
            CAssert.notNull(springApplication, "mainApplicationClass 未标识 @SpringBootApplication");

            val scanBasePackages = springBootAppAnnotation.scanBasePackages();
            if(ArrayUtil.isNotEmpty(scanBasePackages)) {

                for (val scanBasePackage : scanBasePackages) {
                    val basePackage = CStrUtils.toAvailable(scanBasePackage);
                    CCollUtils.addIgnoreBlank(basePackages, basePackage);
                }
            } else {
                CCollUtils.addIgnoreBlank(basePackages, mainApplicationClass.getPackage().getName());
            }
        });

        return basePackages;
    }

    @SneakyThrows
    public <T> T newInstance(Class<T> type) {

        val constructors = CReflectUtils.getAllConstructors(type)
            .entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getKey))
            .map(Map.Entry::getValue)
            .orElseThrow(() -> new RuntimeException("type " + type + " 没有构造方法"))
            ;

        if(constructors.size() > 1) {
            throw new RuntimeException("不支持多个相同参数构造方法的初始化，type: " + type + " ");
        }

        @SuppressWarnings("unchecked")
        val constructor = (Constructor<? extends T>) constructors.get(0);
        constructor.setAccessible(true);

        val params = Arrays.stream(constructor.getParameterTypes())
            .map(SpringUtil::getBean)
            .toArray();
        return CReflectUtils.newInstance(constructor, params);
    }

    public CProfileEnum getActiveProfile() {
        return CProfileEnum.of(SpringUtil.getActiveProfile());
    }

    public CProfileEnum getActiveProfileDefaultNull() {
        try {
            return getActiveProfile();
        } catch (Exception e) {
            log.debug("get profile error", e);
            return null;
        }
    }

    public String getActiveProfileText() {
        return getActiveProfile().getText();
    }

    public String getActiveProfileTextDefaultNull() {
        return CObjUtils.convert(getActiveProfileDefaultNull(), CProfileEnum::getText);
    }

    public String profileTextSuffix(String message) {

        val profileText = getActiveProfileTextDefaultNull();
        if(profileText == null) {
            return message;
        }

        return message + "-" +  profileText;
    }

    /**
     * 根据配置进行处理
     * @param dealFunction 根据配置的处理
     * @param excludeProfiles 不加前缀的配置
     * @return 带配置的文本
     */
    public String dealByProfileExclude(
        ToStringFunction<CProfileEnum> dealFunction,
        Set<CProfileEnum> excludeProfiles
    ) {

        var profile = getActiveProfileDefaultNull();
        if(profile == null
            || excludeProfiles.contains(profile)
        ) {
            return dealFunction.apply(null);
        }

        return dealFunction.apply(profile);
    }

    /**
     * 配置前缀，特定配置不加前缀
     * @param text 文本
     * @param excludeProfiles 不加前缀的配置
     * @return 带配置前缀的文本
     */
    public String profilePrefixExclude(String text, Set<CProfileEnum> excludeProfiles) {
        return dealByProfileExclude(
            profile -> {
                if(null == profile) {
                    return text;
                }
                return profile.name() + text;
            },
            excludeProfiles
        );
    }

    /**
     * 配置后缀，特定配置不加前缀
     * @param text 文本
     * @param excludeProfiles 不加前缀的配置
     * @return 带配置前缀的文本
     */
    public String profileSuffixExclude(String text, Set<CProfileEnum> excludeProfiles) {
        return dealByProfileExclude(
            profile -> {
                if(null == profile) {
                    return text;
                }
                return text + profile.name();
            },
            excludeProfiles
        );
    }

    /**
     * 配置前缀
     * @param text 文本
     * @return 带配置前缀的文本
     */
    public String profilePrefix(String text) {
        return profilePrefixExclude(text, Collections.emptySet());
    }

    /**
     * 配置前缀，PROD 不加前缀
     * @param text 文本
     * @return 带配置前缀的文本
     */
    public String profilePrefixExcludeProd(String text) {
        return profilePrefixExclude(text, CProfileEnum.PROD_PROFILES);
    }

    /**
     * 配置后缀
     * @param text 文本
     * @return 带配置前缀的文本
     */
    public String profileSuffix(String text) {
        return profileSuffixExclude(text, Collections.emptySet());
    }

    /**
     * 配置后缀，PROD 不加前缀
     * @param text 文本
     * @return 带配置前缀的文本
     */
    public String profileSuffixExcludeProd(String text) {
        return profileSuffixExclude(text, CProfileEnum.PROD_PROFILES);
    }

}
