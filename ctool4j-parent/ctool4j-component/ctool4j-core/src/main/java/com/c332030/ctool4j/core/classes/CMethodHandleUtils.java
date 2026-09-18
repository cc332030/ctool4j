package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.core.util.CLocalCacheUtils;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * <p>
 * Description: CMethodHandleUtils
 * </p>
 * <p>MethodHandle 的创建、缓存与统一 Object 签名适配工具类；跨方法的设计内容见设计文档，用例设计见 {@code CMethodHandleUtilsTests} 的用例目录。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>签名常量：{@code GETTER_HANDLE_TYPE} / {@code SETTER_HANDLE_TYPE}（统一 Object 签名，原始类型由 asType 适配器自动装箱/拆箱）</li>
 *   <li>生成版（不缓存）：{@code toGetterHandle} / {@code toSetterHandle} / {@code toHandle(Method)} / {@code toHandle(Constructor)} / {@code toHandleSpecial}</li>
 *   <li>缓存版（弱 key）：{@code getGetterHandle} / {@code getSetterHandle} / {@code getGetterHandleAsType} / {@code getSetterHandleAsType} / {@code getHandle(Method)} / {@code getHandle(Constructor)}</li>
 *   <li>选用判定：见下「API 选用（一次性 / 多次访问）」</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>取 final 字段的 setter 句柄</td>
 *     <td>不兜底：JDK 禁止 final 字段 unreflectSetter，直接抛 IllegalAccessException（调用方须传非 final 字段）</td>
 *   </tr>
 *   <tr>
 *     <td>原始类型字段</td>
 *     <td>asType 适配器自动装箱/拆箱</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>适用：MethodHandle 的创建、缓存与统一签名适配。</li>
 *   <li>不适用：按类 handle map 缓存归 {@code CReflectUtils}，MethodHandle → Lambda 转换归 {@code CLambdaUtils}（职责分布与不收敛原因见设计文档）。</li>
 * </ul>
 * <h2>API 选用（一次性 / 多次访问）</h2>
 * <ul>
 *   <li>判定维度：获取语句的<b>执行频次</b>与句柄的<b>持有方式</b>，两者共同决定用生成版还是缓存版。</li>
 *   <li><b>一次性</b>（语句只执行一次，句柄由调用方长期持有／存入自身缓存与闭包）：用 {@code toXxxHandle}／{@code toHandle}，
 *   不进句柄缓存——缓存对唯一持有者无复用价值，条目却随 Member 存活而常驻（弱键只在类卸载时回收）。</li>
 *   <li><b>多次访问</b>（同一 Field/Method/Constructor 会被反复获取，且句柄不被调用方持有）：用 {@code getXxxHandle}／{@code getHandle}，
 *   命中即免去重复 unreflect。</li>
 *   <li>频次不可知（对外公共 API，调用方任意）：默认用缓存版，不做无据的优化假设。</li>
 *   <li>注意：{@code getGetterHandleAsType}／{@code getSetterHandleAsType} 只缓存 unreflect 结果，{@code asType} 适配句柄每次调用都会新建，
 *   故一次性场景用缓存版无收益（{@code toXxxHandle(field).asType(...)} 等价且不占缓存）。</li>
 * </ul>
 *
 * @since 2026/6/17
 * @version 1.0
 * @see "doc/design/core/method-handle.adoc"
 */
@UtilityClass
public class CMethodHandleUtils {

    /**
     * getter MethodHandle 的统一签名：以 Object 接收者取 Object 值
     * <p>运行期 invokeExact 无签名适配开销；原始类型字段由 asType 适配器自动装箱/拆箱</p>
     */
    public static final MethodType GETTER_HANDLE_TYPE = MethodType.methodType(Object.class, Object.class);

    /**
     * setter MethodHandle 的统一签名：以 Object 接收者写入 Object 值
     */
    public static final MethodType SETTER_HANDLE_TYPE = MethodType.methodType(void.class, Object.class, Object.class);

    /**
     * 生成字段 getter 方法句柄
     *
     * @param field 字段
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle toGetterHandle(Field field) {
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectGetter(field);
    }

    /**
     * 字段 getter 方法句柄缓存
     * key 弱引用，避免 Field 及其所属类加载器无法回收（与 CReflectUtils.ELEMENT_ANNOTATION_CACHE 一致）
     */
    final Cache<Field, MethodHandle> GETTER_HANDLE_CACHE = CLocalCacheUtils.<Field, MethodHandle>cacheBuilder()
        .weakKeys()
        .build();

    /**
     * 获取字段 getter 方法句柄（带缓存）
     *
     * @param field 字段
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle getGetterHandle(Field field) {
        return GETTER_HANDLE_CACHE.get(field, CMethodHandleUtils::toGetterHandle);
    }

    /**
     * 生成字段 setter 方法句柄
     * <p>不对 final 字段做处理：JDK 禁止 final 字段 unreflectSetter，直接抛 IllegalAccessException。
     * 曾用 {@link Field#set} 包装句柄兜底，但实例 final 字段可写仅 HotSpot 行为，OpenJ9 上 Field#set
     * 对 final 字段静默失效（不抛异常也不写入），跨 JVM 行为不一致，故不做兜底、直接报错；
     * 调用方需确保传入非 final 字段</p>
     *
     * @param field 字段
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle toSetterHandle(Field field) {
        field.setAccessible(true);
        return MethodHandles.lookup().unreflectSetter(field);
    }

    /**
     * 字段 setter 方法句柄缓存
     * key 弱引用，避免 Field 及其所属类加载器无法回收
     */
    final Cache<Field, MethodHandle> SETTER_HANDLE_CACHE = CLocalCacheUtils.<Field, MethodHandle>cacheBuilder()
        .weakKeys()
        .build();

    /**
     * 获取字段 setter 方法句柄（带缓存）
     *
     * @param field 字段
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle getSetterHandle(Field field) {
        return SETTER_HANDLE_CACHE.get(field, CMethodHandleUtils::toSetterHandle);
    }

    /**
     * 获取字段 getter 方法句柄（统一 Object 签名，带缓存）
     * <p>与 {@link #getGetterHandle(Field)} 的差异：此处返回 {@link #GETTER_HANDLE_TYPE} 适配后的句柄，
     * 供 invokeExact 快速路径直接调用；原始类型字段由 asType 适配器自动装箱/拆箱。
     * 自 {@link CReflectUtils} 迁入（返回 MethodHandle 的方法统一归本类）</p>
     * <ul>
     *   <li>{@code getGetterHandleAsType(Field)}：{@code getGetterHandle} + {@code asType(GETTER_HANDLE_TYPE)}（统一 Object 签名，自 {@code CReflectUtils} 私有 {@code getGetterHandle} 迁入）</li>
     * </ul>
     *
     * @param field 字段
     * @return getter 方法句柄（统一 Object 签名）
     */
    public MethodHandle getGetterHandleAsType(Field field) {
        return getGetterHandle(field).asType(GETTER_HANDLE_TYPE);
    }

    /**
     * 获取字段 setter 方法句柄（统一 Object 签名，带缓存）
     * <p>同 {@link #getGetterHandleAsType(Field)} 的签名语义，setter 版本以 {@link #SETTER_HANDLE_TYPE} 适配</p>
     * <ul>
     *   <li>{@code getSetterHandleAsType(Field)}：{@code getSetterHandle} + {@code asType(SETTER_HANDLE_TYPE)}（统一 Object 签名，自 {@code CReflectUtils} 私有 {@code getSetterHandle} 迁入）</li>
     * </ul>
     *
     * @param field 字段
     * @return setter 方法句柄（统一 Object 签名）
     */
    public MethodHandle getSetterHandleAsType(Field field) {
        return getSetterHandle(field).asType(SETTER_HANDLE_TYPE);
    }

    /**
     * 生成方法的方法句柄
     *
     * @param method 方法
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle toHandle(Method method) {
        method.setAccessible(true);
        return MethodHandles.lookup().unreflect(method);
    }

    /**
     * 方法句柄缓存
     * key 弱引用，避免 Method 及其所属类加载器无法回收
     */
    final Cache<Method, MethodHandle> METHOD_HANDLE_CACHE = CLocalCacheUtils.<Method, MethodHandle>cacheBuilder()
        .weakKeys()
        .build();

    /**
     * 获取方法的方法句柄（带缓存）
     *
     * @param method 方法
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle getHandle(Method method) {
        return METHOD_HANDLE_CACHE.get(method, CMethodHandleUtils::toHandle);
    }

    /**
     * 生成 special 方法句柄
     * <ul>
     *   <li>{@code toHandleSpecial(Method, Class)}：special 方法句柄（不缓存）</li>
     * </ul>
     *
     * @param method       方法
     * @param specialToken special 令牌（调用类）
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle toHandleSpecial(Method method, Class<?> specialToken) {
        method.setAccessible(true);
        return MethodHandles.lookup().unreflectSpecial(method, specialToken);
    }

    /**
     * 生成构造器方法句柄
     *
     * @param constructor 构造器
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle toHandle(Constructor<?> constructor) {
        constructor.setAccessible(true);
        return MethodHandles.lookup().unreflectConstructor(constructor);
    }

    /**
     * 构造器方法句柄缓存
     * key 弱引用，避免 Constructor 及其所属类加载器无法回收
     */
    final Cache<Constructor<?>, MethodHandle> CONSTRUCTOR_HANDLE_CACHE =
        CLocalCacheUtils.<Constructor<?>, MethodHandle>cacheBuilder()
            .weakKeys()
            .build();

    /**
     * 获取构造器方法句柄（带缓存）
     *
     * @param constructor 构造器
     * @return 方法句柄
     */
    @SneakyThrows
    public MethodHandle getHandle(Constructor<?> constructor) {
        return CONSTRUCTOR_HANDLE_CACHE.get(constructor, CMethodHandleUtils::toHandle);
    }

}
