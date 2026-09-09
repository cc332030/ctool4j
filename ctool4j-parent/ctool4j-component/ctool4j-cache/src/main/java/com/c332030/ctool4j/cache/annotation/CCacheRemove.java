package com.c332030.ctool4j.cache.annotation;

import com.c332030.ctool4j.cache.aop.CDefaultCacheIdConverter;
import com.c332030.ctool4j.cache.aop.ICCacheIdConverter;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CCacheRemove
 * </p>
 *
 * <p>
 * 标注在 update/delete 方法上，方法执行成功后删除（释放）对应缓存。
 * 与 {@link CCacheable} 配合使用：相同的 {@code namespace} + 相同的 key 解析规则定位到
 * 被缓存的数据，方法成功执行后将其从本地 / Redis 缓存中移除，避免缓存到过期。
 * </p>
 * <p>
 * 与 {@link CCacheUpdate} 的区别：本注解只删除缓存，不写入新值；通常在 delete 场景使用。
 * </p>
 *
 * @see "doc/design/cache/CCacheRemove.adoc"
 * @see "doc/design/cache/CCacheRemoveUpdateAspectTests.adoc"
 * @since 2026/9/11
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CCacheRemove {

    /**
     * 自定义缓存 key 的简单 el 表达式，与 {@link CCacheable#key()} 语义一致。
     * <p>为空时走默认逻辑（第一个参数的 {@code @CCacheId} 字段）。</p>
     *
     * @return 缓存 key 简单 el 表达式
     */
    String key() default "";

    /**
     * 是否本地缓存
     * @return true 本地缓存，false redis 缓存
     */
    boolean local() default true;

    /**
     * 缓存 id 生成类
     * @return 缓存 id 生成类
     */
    Class<? extends ICCacheIdConverter<?, ?>> idConverter() default CDefaultCacheIdConverter.class;

    /**
     * 缓存命名空间类（须与对应的 {@link CCacheable#namespace()} 一致）
     * @return 缓存命名空间类
     */
    Class<?> namespace();

}
