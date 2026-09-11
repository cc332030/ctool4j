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
 * <p>MethodHandle 的创建、缓存与统一 Object 签名适配工具类，功能设计与用例设计见设计文档。</p>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底</th>
 *     <th>触发条件</th>
 *     <th>边界</th>
 *   </tr>
 * </table>
 * <p>| 类无无参构造器 | CAssert.notNull 快速失败 | newInstance(Class) 且目标类无无参构造器 | - | 静态/final 字段 | getValue/setValue 回退 Field.get/set | 非静态字段无对应 handle | -</p>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>适用：MethodHandle 的创建、缓存与统一签名（{@code CMethodHandleUtils}）、按类 handle map 缓存（{@code CReflectUtils}）、MethodHandle → Lambda 转换（{@code CLambdaUtils}）</li>
 *   <li>不适用：反射元数据查询（字段/方法/构造器/注解）仍归 {@code CReflectUtils}；Lambda 业务语义（非 MethodHandle 场景）不在 {@code CLambdaUtils}</li>
 *   <li>已知取舍：{@code getGetterHandle}/{@code getGetterHandleAsType} 并存；统一签名常量收敛至 {@code CMethodHandleUtils}（原 {@code CReflectUtils}/{@code CBeanUtils} 重复定义已消除）</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>原方案</b></p>
 * <p>曾将上述 MethodHandle 相关代码全部收敛到 {@code CMethodHandleUtils}，并删除 {@code CLambdaUtils}：</p>
 * <ul>
 *   <li>从 {@code CReflectUtils} 迁入：统一签名常量、按类缓存的 handle map 及其查询方法、无参构造器 handle 缓存</li>
 *   <li>从 {@code CLambdaUtils} 整体迁入：{@code LOOKUP}/{@code getLookup}、{@code getGetterMethodHandle}/{@code getSetterMethodHandle}、{@code getFieldGetLambda}/{@code getFieldSetLambda}</li>
 *   <li>删除 {@code CBeanUtils} 重复常量，改引 {@code CMethodHandleUtils} 常量</li>
 *   <li>删除 {@code CLambdaUtils} 类，测试并入 {@code CMethodHandleUtilsTests}</li>
 * </ul>
 * <p><b>撤销原因</b></p>
 * <p>收敛后 {@code CMethodHandleUtils} 承载职责过多（handle 生成/缓存、签名适配、按类 map 缓存、构造器缓存、Lookup、Lambda 转换）， 按类缓存的 handle map 与 Lambda 转换能力并不适合全部塞入 MethodHandle 工具类： 职责边界反而更模糊。决定不使用该类收敛方案，整体还原到原分布。</p>
 * <p><b>还原方式</b></p>
 * <ul>
 *   <li>{@code CMethodHandleUtils}：只保留 handle 生成与缓存（toXxxHandle/getXxxHandle），移除迁移进来的常量、按类缓存、Lookup 与 Lambda 转换</li>
 *   <li>{@code CReflectUtils}：恢复统一签名常量、按类 handle map 缓存与查询方法、无参构造器 handle 缓存</li>
 *   <li>{@code CLambdaUtils}：恢复原类（Lookup、按类权限方法句柄、Lambda 转换），原测试一并还原</li>
 *   <li>{@code CBeanUtils}：恢复重复常量（与 {@code CReflectUtils} 各自持有，属历史现状）</li>
 *   <li>行为约定：还原后各 API 签名、语义、缓存键与缓存策略与收敛前完全一致</li>
 * </ul>
 * <p><b>CMethodHandleUtils</b></p>
 * <p>handle 生成与缓存（统一入口）：</p>
 * <ul>
 *   <li>{@code GETTER_HANDLE_TYPE}/{@code SETTER_HANDLE_TYPE}：统一 Object 签名（运行期 invokeExact 无签名适配开销；原始类型字段由 asType 适配器自动装箱/拆箱），自 {@code CReflectUtils}/{@code CBeanUtils} 收敛（消除两处重复定义）</li>
 *   <li>{@code toGetterHandle(Field)} / {@code getGetterHandle(Field)}：getter handle 生成 + 按 Field 弱 key 缓存（原始签名）</li>
 *   <li>{@code toSetterHandle(Field)} / {@code getSetterHandle(Field)}：setter handle 生成 + 缓存（原始签名）</li>
 *   <li>{@code toHandle(Method)} / {@code getHandle(Method)}：方法 handle 生成 + 缓存</li>
 *   <li>{@code toHandle(Constructor)} / {@code getHandle(Constructor)}：构造器 handle 生成 + 缓存</li>
 * </ul>
 * <p><b>CReflectUtils</b></p>
 * <ul>
 *   <li>{@code GETTER_HANDLE_MAP_CLASS_VALUE}/{@code SETTER_HANDLE_MAP_CLASS_VALUE}：按类缓存的实例字段（非静态，setter 另排除 final）handle map，生成函数引用 {@code CMethodHandleUtils} 的 asType 版本（统一 Object 签名，供 invokeExact 快速路径直接调用）</li>
 *   <li>{@code getGetterHandleMap(Class)}/{@code getSetterHandleMap(Class)}：查询入口，供 {@code getValue}/{@code setValue} 快速路径使用（handle 不存在时回退 {@code Field.get/set}）</li>
 *   <li>{@code NO_ARG_CONSTRUCTOR_HANDLE_CLASS_VALUE}：按类缓存无参构造器 handle，供 {@code newInstance(Class)} 快速路径</li>
 * </ul>
 * <p><b>收敛调整（MethodHandle/MethodType 相关内容归入 CMethodHandleUtils）</b></p>
 * <ul>
 *   <li>返回值为 {@code MethodHandle} 的方法统一归 {@code CMethodHandleUtils}：</li>
 *   <li>{@code getGetterHandleAsType(Field)}/{@code getSetterHandleAsType(Field)} 自 {@code CReflectUtils} 私有</li>
 *   <li>{@code getGetterHandle(Field)}/{@code getSetterHandle(Field)} 迁入（合并为公共方法 + 复用现有缓存，消除重复实现）。</li>
 *   <li>{@code MethodType} 常量（{@code GETTER_HANDLE_TYPE}/{@code SETTER_HANDLE_TYPE}）统一归 {@code CMethodHandleUtils}：</li>
 *   <li>消除 {@code CReflectUtils}/{@code CBeanUtils} 的重复定义，两处改为引用公共常量。</li>
 *   <li>项目内无返回 {@code MethodType} 的方法，故仅收敛常量。</li>
 *   <li>Lookup 链路删除：原 {@code CLambdaUtils.getLookup(Class)}（LOOKUP.in(clazz) 提升权限）仅服务于</li>
 *   <li>{@code getGetterMethodHandle}/{@code getSetterMethodHandle}，而后者仅服务于 {@code getFieldGetLambda}/{@code getFieldSetLambda}，</li>
 *   <li>形成同包互引闭环且无外部价值。简化后 {@code getFieldGetLambda}/{@code getFieldSetLambda} 直接引用</li>
 *   <li>asType 方法（setAccessible 语义），{@code LOOKUP}/{@code getLookup}/{@code getGetterMethodHandle}/{@code getSetterMethodHandle} 全部删除。</li>
 * </ul>
 * <p><b>CLambdaUtils</b></p>
 * <ul>
 *   <li>{@code getFieldGetLambda(Field)}/{@code getFieldSetLambda(Field)}：MethodHandle → {@code CFunction}/{@code CBiConsumer} 转换（委托 {@code MethodHandle.invoke} 调用，JDK 8 的 LambdaMetafactory 不支持 getField/putField 类方法句柄），内部引用 {@code CMethodHandleUtils} 的 asType 方法（setAccessible 语义，不受跨包访问级别限制，故无需 clazz 参数）</li>
 * </ul>
 * <p><b>语义区分</b></p>
 *
 * @since 2026/6/17
 * @version 1.0
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
