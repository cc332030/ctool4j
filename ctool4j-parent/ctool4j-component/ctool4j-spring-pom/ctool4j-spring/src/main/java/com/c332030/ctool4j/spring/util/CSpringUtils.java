package com.c332030.ctool4j.spring.util;

import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.enums.CProfileEnum;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.definition.constant.CTool4jConstants;
import com.c332030.ctool4j.definition.function.ToStringFunction;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
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
 * <h2>能力目录</h2>
 * <p>{@code CSpringUtils}：Spring 工具。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>Spring 容器相关工具</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>Spring 操作</p>
 * <h2>不适用与边界场景</h2>
 * <p>静态工具</p>
 * <h2>已知限制与取舍</h2>
 * <p>静态工具</p>
 *
 * @since 2025/9/10
 * @version 1.0
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
        return CSpringConfigBeans.getApplicationContext();
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
     * 获取指定类型的 bean
     *
     * @param tClass bean 类型
     * @param <T>    泛型
     * @return bean
     */
    public <T> T getBean(Class<T> tClass) {
        return getApplicationContext().getBean(tClass);
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

    /**
     * 获取启动类所在的基础包集合
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>取容器中全部标注 {@code SpringBootApplication} 的 Bean（含组合注解，如
     *   {@code CSpringBootApplication}）；</li>
     *   <li>逐个读取其 {@code scanBasePackages}：非空时取各值（去空白、去重），
     *   为空时回退为启动类所在包；</li>
     *   <li>返回全部基础包（去重、保持发现顺序）。</li>
     * </ol>
     *
     * <p><b>边界与兜底</b>：容器中不存在启动类时不报错——扫描范围并非只能来自启动类，
     * 框架基础包与使用方显式声明的包同样有效；无启动类时按剩余来源返回，
     * 三者皆空才说明确实没有可扫描范围。</p>
     *
     * @return 基础包集合
     */
    public Set<String> getBasePackages() {

        val basePackages = new LinkedHashSet<String>();
        basePackages.add(CTool4jConstants.BASE_PACKAGE);

        val applicationContext = getApplicationContext();
        val autowiredTypes = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        autowiredTypes.values().forEach(springApplication -> {

            // 用 Spring 的元注解感知查找，兼容 @SpringBootApplication 的组合注解（如 CSpringBootApplication）
            Class<?> mainApplicationClass = AopUtils.getTargetClass(springApplication);
            CAssert.notNull(mainApplicationClass, "mainApplicationClass 不能为空");

            SpringBootApplication springBootAppAnnotation =
                AnnotationUtils.findAnnotation(mainApplicationClass, SpringBootApplication.class);
            CAssert.notNull(springBootAppAnnotation, "mainApplicationClass 未标识 @SpringBootApplication");

            String[] scanBasePackages = springBootAppAnnotation.scanBasePackages();
            if(ArrayUtil.isNotEmpty(scanBasePackages)) {

                for (String scanBasePackage : scanBasePackages) {
                    String basePackage = CStrUtils.toAvailable(scanBasePackage);
                    CCollUtils.addIgnoreBlank(basePackages, basePackage);
                }
            } else {
                CCollUtils.addIgnoreBlank(basePackages, mainApplicationClass.getPackage().getName());
            }
        });

        basePackages.addAll(CSpringConfigBeans.getBasePackages());
        return basePackages;
    }

    /**
     * 通过参数最多的构造方法创建实例，参数从容器中获取
     *
     * @param type 实例类型
     * @param <T>  实例类型
     * @return 创建好的实例
     */
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

        Object[] params = Arrays.stream(constructor.getParameterTypes())
            .map(parameterType -> getBean(parameterType))
            .toArray();
        return CReflectUtils.newInstance(constructor, params);
    }

    /**
     * 获取当前激活的环境
     *
     * <p>取上下文的 {@code Environment#getActiveProfiles()} 首项；无激活环境时返回 {@code null} 由
     * {@link CProfileEnum#of(String)} 兜底。</p>
     *
     * @return 当前激活的环境
     */
    public CProfileEnum getActiveProfile() {
        val activeProfiles = getApplicationContext().getEnvironment().getActiveProfiles();
        return CProfileEnum.of(ArrayUtil.isEmpty(activeProfiles) ? null : activeProfiles[0]);
    }

    /**
     * 获取当前激活的环境，获取失败时返回 null
     *
     * @return 当前激活的环境；获取失败时返回 null
     */
    public CProfileEnum getActiveProfileDefaultNull() {
        try {
            return getActiveProfile();
        } catch (Exception e) {
            log.debug("get profile error", e);
            return null;
        }
    }

    /**
     * 获取当前激活环境的文本
     *
     * @return 当前激活环境的文本
     */
    public String getActiveProfileText() {
        return getActiveProfile().getText();
    }

    /**
     * 获取当前激活环境的文本，获取失败时返回 null
     *
     * @return 当前激活环境的文本；获取失败时返回 null
     */
    public String getActiveProfileTextDefaultNull() {
        return CObjUtils.convert(getActiveProfileDefaultNull(), CProfileEnum::getText);
    }

    /**
     * 为消息追加当前环境文本后缀
     *
     * @param message 原始消息
     * @return 追加环境文本后缀后的消息；无环境文本时返回原始消息
     */
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

        CProfileEnum profile = getActiveProfileDefaultNull();
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
     * 配置后缀，特定配置不加后缀
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
     * 配置后缀，PROD 不加后缀
     * @param text 文本
     * @return 带配置前缀的文本
     */
    public String profileSuffixExcludeProd(String text) {
        return profileSuffixExclude(text, CProfileEnum.PROD_PROFILES);
    }

}
