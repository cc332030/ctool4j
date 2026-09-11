package com.c332030.ctool4j.core.classes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.cache.impl.CBiClassValue;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CList;
import com.c332030.ctool4j.core.util.CMap;
import com.c332030.ctool4j.core.util.CMapUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.definition.function.CSupplier;
import com.c332030.ctool4j.definition.function.ToStringFunction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CBeanUtils
 * </p>
 *
 * <p>JavaBean 属性复制与对象转 Map 工具类：对象间属性复制（{@code copy} 直连字段、避免 toMap 中转）、
 * 对象转 Map（{@code toMap} 系列，支持原名/下划线/注解 key）、Map 转对象、数组/集合批量复制。</p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>属性复制：{@link #copy(Object, Object)}（核心，直连字段）、{@link #copy(Map, Object)}（Map 源）、
 *   {@link #copy(Object, Class)} / {@link #copy(Object, CSupplier)}（创建目标）、
 *   {@link #copyList(Collection, Class)} / {@link #copyListFromMap(Collection, Class)}（批量）、
 *   {@link #copyFromArr(Object[], Object)}（数组反序覆盖）。</li>
 *   <li>对象转 Map：{@link #toMap(Object)}、{@link #toMapJsonName(Object)}、{@link #toMapUnderlineName(Object)}、
 *   {@link #toMap(Object, ToStringFunction)}、{@link #toMap(Object, Class, CFunction)}。</li>
 * </ul>
 *
 * <h2>设计思路总述</h2>
 * <ul>
 *   <li><b>计划预计算</b>：按 (源类, 目标类) / 按类预计算复制计划与转 Map 计划，
 *   缓存于 {@code CClassValue} / {@code CBiClassValue}（基于 {@code java.lang.ClassValue}，线程安全、按类弱关联）；
 *   字段配对、转换器解析、final/集合剔除全部在计划期完成，运行期仅遍历字段数组 + null 判断，
 *   性能接近手工 setter。计划缓存键为 Class，被类加载器持有，无泄漏问题。</li>
 *   <li><b>方法句柄</b>：getter/setter 统一适配为 Object 签名 MethodHandle（{@code Object -> Object}、
 *   {@code (Object, Object) -> void}），运行期 {@code invokeExact} 无签名适配开销；handle 经
 *   {@link CMethodHandleUtils} 获取（Caffeine 弱 key 缓存），无参构造器按类缓存。</li>
 *   <li><b>快慢路径分离</b>：计划期已解析转换路径的字段进 fast 条目（直接写 / 转换后写）；
 *   Object 声明、仅 Object 源兜底可匹配等无法计划期确定的字段进 fallback 条目，
 *   运行期按实际值类型查预计算动作（跳过/直接写/转换），首次出现时预热并缓存。</li>
 *   <li><b>兜底转换器优先级最低</b>：Object→String 兜底在 {@code CConvertUtils.findConverter} 中只记录不返回，
 *   保证 Date→String 等更精确转换不被抢占。</li>
 * </ul>
 * <p>详细设计、详细步骤、兜底与已知限制见各方法 javadoc（本类 javadoc 只保留总结）。</p>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CBeanUtils {

    /**
     * 复制计划缓存：按 (源类, 目标类) 对缓存
     */
    private static final CBiClassValue<CopyPlan> COPY_PLAN_BI_CLASS_VALUE = CBiClassValue.of(CBeanUtils::getCopyPlan);

    /**
     * 转 map 计划缓存：按类缓存
     */
    private static final CClassValue<ToMapPlan> TO_MAP_PLAN_CLASS_VALUE = CClassValue.of(CBeanUtils::getToMapPlan);

    /**
     * 空复制计划（JDK 源类使用，热路径零操作）
     */
    private static final CopyPlan EMPTY_COPY_PLAN = CopyPlan.builder()
            .fastEntries(new CopyEntry[0])
            .fallbackEntries(new CopyEntry[0])
            .build();

    /**
     * 空转 map 计划（JDK 类使用，热路径零操作）
     */
    private static final ToMapPlan EMPTY_TO_MAP_PLAN = ToMapPlan.builder()
            .entries(new ToMapEntry[0])
            .build();

    /**
     * map 属性复制到对象
     *
     * <p><b>详细步骤</b>（源为 map 无法预设字段，逐条走旧流程，不走计划路径）：</p>
     * <ol>
     *   <li>{@code fromMap} 或 {@code to} 为 null → 原样返回 {@code to}（不做任何写入）；</li>
     *   <li>取目标类实例字段表（{@link CReflectUtils#getInstanceFieldMap(Class)}）；</li>
     *   <li>逐条遍历 map：字段名在目标类中不存在、值为 null、字段为 static 或 final → 跳过该条；</li>
     *   <li>经 {@link CConvertUtils#convertOpt(Object, Class)} 转换：有结果则用 setter 方法句柄
     *   （{@link CMethodHandleUtils#getSetterHandle(Field)}）写入，无结果（null）则跳过。</li>
     * </ol>
     *
     * <p><b>边界与取舍</b>：键不匹配即静默跳过（不抛错）；值转换失败（无转换器）同样静默跳过；
     * 目标 final 字段不可写故跳过。因无法预计算字段配对，本方法性能低于
     * {@link #copy(Object, Object)} 计划路径，仅用于源本身即 Map 的场景。</p>
     *
     * @param fromMap 源 map
     * @param to 目标对象
     * @return 目标对象；入参为空时原样返回
     * @param <To> 目标对象泛型
     */
    public <To> To copy(Map<String, ?> fromMap, To to) {

        if(null == fromMap || null == to) {
            return to;
        }

        val toFieldMap = CReflectUtils.getInstanceFieldMap(to.getClass());
        fromMap.forEach((fromKey, fromValue) -> {

            val toField = toFieldMap.get(fromKey);
            if(null == toField
                    || null == fromValue
                    || CReflectUtils.isStatic(toField)
                    || CReflectUtils.isFinal(toField)
            ) {
                return;
            }

            CConvertUtils.convertOpt(fromValue, toField.getType())
                    .ifPresent((CConsumer<Object>) toValue -> setValueWithHandle(to, toField, toValue));

        });

        return to;
    }

    /**
     * 对象属性复制（核心方法，直连字段）
     *
     * <p><b>详细设计</b>：走计划（预热）路径，字段级校验与转换查找均不在运行期做。
     * 按 (源类, 目标类) 取 {@link CopyPlan}（{@code COPY_PLAN_BI_CLASS_VALUE}），
     * 运行期仅遍历两个字段数组，无其他耗时操作，预热后性能约等同于直接 set。</p>
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>{@code from} 或 {@code to} 为 null → 返回 {@code to}；</li>
     *   <li>取复制计划（首次调用触发计划期计算）；JDK 源类命中空计划，
     *   两个循环均为空、热路径零判断（保持"JDK 类不拷贝"原语义）；</li>
     *   <li>fast 循环：getter 取值 → null 跳过 → {@code converter == CFunction.SELF} 时直接 setter 写入；
     *   否则转换器 {@code apply}，转换结果非 null 才写入（转换器可能返回 null）；</li>
     *   <li>fallback 循环：getter 取值 → null 跳过 → 按 (实际值类型, 目标声明类型) 取预计算动作
     *   （{@code VALUE_ACTION_BI_CLASS_VALUE}）→ 结果非 null 时 setter 写入；</li>
     *   <li>返回 {@code to}（目标对象本身，便于链式使用）。</li>
     * </ol>
     *
     * <p><b>计划期规则</b>：对目标类每个实例字段按序判定——JDK 源类返回空计划；目标 final 字段、
     * 源无同名字段、源声明集合/Map/数组 一律剔除；
     * {@code Object.class != fromType && ClassUtil.isAssignable(toType, fromType)}
     * （目标在前、源在后，支持原始/包装等价）进 fast 条目（SELF）；
     * Object 声明或"仅 Object 兜底可匹配"（{@code getConverterNoObjectFallback} 为 null）进 fallback 条目；
     * 其余进 fast 条目（转换后写）。</p>
     *
     * <p><b>兜底</b>：源/目标 null 原样返回；值转换后为 null 跳过；fallback 动作解析为 EMPTY 时跳过写入。</p>
     *
     * <p><b>边界与已知取舍</b>：目标 final 字段不可写；null 值跳过；集合/Map/数组字段不复制。
     * 源字段声明类型为集合父类型（Iterable/Serializable 等）且实际持有集合时，本实现由旧实现的"跳过"
     * 变为"直接写入"（Object 声明已回退旧逻辑，其余集合父类型为消除运行期 instanceof 检查的必要取舍）；
     * 原始类型同型字段（int→int 等）旧实现无转换器跳过、现直接写入（旧缺口修复）。</p>
     *
     * @param from 源对象
     * @param to   目标对象
     * @return 目标对象；入参为空时原样返回
     * @param <To> 目标对象泛型
     */
    @SneakyThrows
    public <To> To copy(Object from, To to) {

        if(null == from || null == to) {
            return to;
        }

        // JDK 源类由计划期返回空计划（热路径零判断），保持"JDK 类不拷贝"原语义
        val plan = COPY_PLAN_BI_CLASS_VALUE.get(from.getClass(), to.getClass());
        for (val entry : plan.fastEntries) {

            val fromValue = entry.getterHandle.invokeExact(from);
            if(null == fromValue) {
                continue;
            }

            if(entry.converter == CFunction.SELF) {
                entry.setterHandle.invokeExact((Object) to, fromValue);
                continue;
            }

            // 转换器可能返回 null，运行时保留判空
            val toValue = entry.converter.apply(fromValue);
            if(null != toValue) {
                entry.setterHandle.invokeExact((Object) to, toValue);
            }
        }

        // 计划期未解析转换路径的字段（Object 声明、仅 Object 源兜底可匹配、原始类型等），
        // 运行期按实际值类型一次查表分派（跳过/直接写/转换），判断与查找均在预热期完成
        for (val entry : plan.fallbackEntries) {

            val fromValue = entry.getterHandle.invokeExact(from);
            if(null == fromValue) {
                continue;
            }

            val toValue = VALUE_ACTION_BI_CLASS_VALUE.get(fromValue.getClass(), entry.toType).apply(fromValue);
            if(null != toValue) {
                entry.setterHandle.invokeExact((Object) to, toValue);
            }
        }

        return to;
    }

    /**
     * 对象属性复制
     * @param fromMap 属性 map
     * @param toClass 目标对象类
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Map<String, ?> fromMap, Class<To> toClass) {
        if(null == fromMap) {
            return null;
        }
        return copy(fromMap, CReflectUtils.newInstance(toClass));
    }

    /**
     * 对象属性复制
     * <p>直接新建目标对象走字段复制路径（避免 Map 中转），语义与 copy(Object, To) 一致</p>
     * @param from 源对象
     * @param toClass 目标对象类
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Object from, Class<To> toClass) {
        return copy(from, CReflectUtils.newInstance(toClass));
    }

    /**
     * 对象属性复制
     * @param fromMap 源 map
     * @param toSupplier 目标对象提供者
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Map<String, ?> fromMap, CSupplier<To> toSupplier) {
        if(null == fromMap) {
            return null;
        }
        return copy(fromMap, toSupplier.get());
    }

    /**
     * 对象属性复制
     * @param from 源对象
     * @param toSupplier 目标对象提供者
     * @param <To> 目标对象泛型
     * @return 目标对象
     */
    public <To> To copy(Object from, CSupplier<To> toSupplier) {
        return copy(toMap(from), toSupplier);
    }

    /**
     * 集合对象属性复制
     * @param fromCollection 源集合
     * @param toSupplier 目标对象提供者
     * @param <To> 目标对象泛型
     * @return 目标对象集合
     */
    public <To> List<To> copyListFromMap(Collection<? extends Map<String, ?>> fromCollection, CSupplier<To> toSupplier) {
        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return fromCollection.stream()
                .filter(Objects::nonNull)
                .map(from -> copy(from, toSupplier))
                .collect(Collectors.toList());
    }

    /**
     * 集合对象属性复制
     * @param fromCollection 源集合
     * @param toClass 目标对象类
     * @param <To> 目标对象泛型
     * @return 目标对象集合
     */
    public <To> List<To> copyListFromMap(Collection<? extends Map<String, ?>> fromCollection, Class<To> toClass) {
        return copyListFromMap(fromCollection, () -> CReflectUtils.newInstance(toClass));
    }

    /**
     * 集合对象属性复制
     * @param fromCollection 源集合
     * @param toClass 目标对象类型
     * @param <To> 目标对象泛型
     * @return 目标对象集合
     */
    public <To> List<To> copyList(Collection<?> fromCollection, Class<To> toClass) {
        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return copyList(fromCollection, () -> CReflectUtils.newInstance(toClass));
    }

    /**
     * 集合对象属性复制
     * @param fromCollection 源集合
     * @param toSupplier 目标对象获取方法
     * @param <To> 目标对象泛型
     * @return 目标对象集合
     */
    public <To> List<To> copyList(Collection<?> fromCollection, CSupplier<To> toSupplier) {
        if(CollUtil.isEmpty(fromCollection)) {
            return CList.of();
        }
        return copyListFromMap(CCollUtils.convert(fromCollection, CBeanUtils::toMap), toSupplier);
    }

    /**
     * 对象转 map，使用 json 属性名
     * @param object 源对象
     * @return 值 map
     */
    public Map<String, Object> toMapJsonName(Object object) {
        if(null == object) {
            return CMap.of();
        }
        return toMap(object, JsonProperty.class, JsonProperty::value);
    }

    /**
     * 对象转 map，使用 下划线 属性名
     * @param object 源对象
     * @return 值 map
     */
    public Map<String, Object> toMapUnderlineName(Object object) {
        if(null == object) {
            return CMap.of();
        }
        return toMap(object, field -> StrUtil.toUnderlineCase(field.getName()));
    }

    /**
     * 对象转 map，使用注解 key
     * @param object 源对象
     * @param annotationClass 注解类
     * @param annotationValueFunction 注解值获取方法
     * @param <T> 注解泛型
     * @return 值 map
     */
    public <T extends Annotation> Map<String, Object> toMap(
            Object object,
            Class<T> annotationClass,
            CFunction<T, String> annotationValueFunction
    ) {
        if(null == object) {
            return CMap.of();
        }
        return toMap(object, field -> CReflectUtils.getFieldName(field, annotationClass, annotationValueFunction));
    }

    /**
     * 对象转 map
     * @param object 对象
     * @return 值 map
     */
    public Map<String, Object> toMap(Object object) {
        if(null == object) {
            return CMap.of();
        }
        return toMap(object, Field::getName);
    }

    /**
     * 对象转 map（核心方法，字段名由入参函数决定）
     *
     * <p><b>详细设计</b>：走计划（预热）路径。按类取 {@link ToMapPlan}
     * （{@code TO_MAP_PLAN_CLASS_VALUE}：字段 + getter MethodHandle，含 final 字段，不剔除集合字段），
     * 运行期遍历计划数组即可，校验与字段收集均在计划期完成，性能瓶颈仅在 map 写入。</p>
     *
     * <p><b>详细步骤</b>：</p>
     * <ol>
     *   <li>{@code object} 为 null → 返回空 map（{@link CMap#of()}）；</li>
     *   <li>取转 Map 计划；JDK 类命中空计划（保持"JDK 类转 map 为空"原语义）；</li>
     *   <li>按计划长度预分配可变 map，遍历条目：key 为 null 跳过 → getter 取值 → 值为 null 跳过 →
     *   {@code putIfAbsent} 写入；写入时已有非空旧值说明 key 冲突，抛 {@link IllegalStateException}；</li>
     *   <li>结果 map 为空返回 {@link CMap#of()}，否则返回不可变视图。</li>
     * </ol>
     *
     * <p><b>边界与已知取舍</b>：final 字段值同样进入 map；null key 过滤、null 值跳过；
     * key 冲突（不同字段映射到同名 key）抛 {@link IllegalStateException}（与 merge 语义一致）。</p>
     *
     * @param object 对象
     * @param getFieldNameFunction 获取字段名方法
     * @return 对象值 map（不可变）；object 为 null 时返回空 map
     */
    @SneakyThrows
    public Map<String, Object> toMap(Object object, ToStringFunction<Field> getFieldNameFunction) {

        if(null == object) {
            return CMap.of();
        }

        // JDK 类由计划期返回空计划（热路径零判断），保持"JDK 类转 map 为空"原语义
        val plan = TO_MAP_PLAN_CLASS_VALUE.get(object.getClass());
        val map = CMapUtils.<String, Object>newMap(String.class, plan.entries.length);
        for (val entry : plan.entries) {

            val key = getFieldNameFunction.apply(entry.field);
            if(null == key) {
                continue;
            }

            val value = entry.getterHandle.invokeExact(object);
            if(null == value) {
                continue;
            }

            // key 冲突且双值非空时抛 IllegalStateException（与 merge 语义一致）
            val oldValue = map.putIfAbsent(key, value);
            if(null != oldValue) {
                throw new IllegalStateException("Conflict key: " + key + ", v1: " + oldValue + ", v2: " + value);
            }
        }

        if(map.isEmpty()) {
            return CMap.of();
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * 对象数组元素属性复制，反顺序遍历
     *
     * <p><b>详细设计</b>：从数组末尾向前遍历，逐个把元素复制到同一目标对象，
     * 使数组靠前的元素覆盖靠后的元素（后者先写、前者后写），便于按优先级传入多来源对象。</p>
     *
     * <p><b>详细步骤</b>：数组为空（null 或长度 0）→ 原样返回 {@code to}；
     * 否则下标自 {@code length - 1} 递减到 0，逐个调用 {@link #copy(Object, Object)}（空元素在其内部被跳过），
     * 最终返回 {@code to}。</p>
     *
     * @param fromArr 源对象数组
     * @param to 目标对象
     * @return 目标对象；数组为空时原样返回
     * @param <To> 目标对象泛型
     */
    public <To> To copyFromArr(Object[] fromArr, To to) {

        if(ArrayUtil.isEmpty(fromArr)) {
            return to;
        }

        for (int i = fromArr.length-1; i >= 0; i--) {
            val source = fromArr[i];
            CBeanUtils.copy(source, to);
        }

        return to;
    }

    /**
     * 对象数组元素属性复制，反顺序遍历
     * @param fromArr 源对象数组
     * @param toClass 目标对象类
     * @return 目标对象
     * @param <To> 目标对象泛型
     */
    public <To> To copyFromArr(Object[] fromArr, Class<To> toClass) {

        if(ArrayUtil.isEmpty(fromArr)) {
            return null;
        }

        val to = CReflectUtils.newInstance(toClass);
        return copyFromArr(fromArr, to);
    }

    /**
     * 值类型分派缓存：按 (实际值类型, 目标类型) 缓存处理动作（跳过/直接写/转换）
     * <p>fallback 字段运行期仅按实际值类型一次查表；集合判断、可赋值判断与转换器
     * 查找均在该值类型首次出现时（预热）完成并缓存，热路径只剩 null 判断。</p>
     */
    private static final CBiClassValue<CFunction<Object, ?>> VALUE_ACTION_BI_CLASS_VALUE =
            CBiClassValue.of(CBeanUtils::resolveValueAction);

    /**
     * 解析值类型处理动作
     *
     * @param valueClass 实际值类型
     * @param toClass    目标字段声明类型
     * @return 处理动作：集合/无转换器返回空动作（跳过）、可赋值返回 {@link CFunction#SELF}、否则返回转换器
     */
    private static CFunction<Object, ?> resolveValueAction(Class<?> valueClass, Class<?> toClass) {

        // 集合/Map/数组值：旧语义一律跳过（目标字段类型兼容也不写入）
        if(Collection.class.isAssignableFrom(valueClass)
                || Map.class.isAssignableFrom(valueClass)
                || valueClass.isArray()
        ) {
            return CFunction.EMPTY;
        }

        // ClassUtil.isAssignable(目标, 源) 支持原始类型与包装类等价（如 int←Integer 拆箱），直接按 SELF 写入
        if(ClassUtil.isAssignable(toClass, valueClass)) {
            return CFunction.SELF;
        }

        // 完整查找（含 Object 源兜底，已降为最低优先级），保证 Date→String 等特殊转换优先
        val converter = CConvertUtils.getConverter(valueClass, toClass);
        if(null == converter) {
            return CFunction.EMPTY;
        }
        return converter;
    }

    /**
     * 构建复制计划
     * <p>计划期剔除目标 final 字段、源对象无同名字段、源声明集合/Map/数组字段；
     * 声明类型可赋值目标类型直接进入快路径（直接写入），可解析出精确转换器的进入
     * 快路径（转换后写入），其余进入回退路径（运行期按实际值类型查预计算动作）。
     * Object 声明类型运行期实际类型不可预知（如持有 Date 时计划期命中 Object→String
     * 的 toString 转换，而旧实现命中 Date→String 的格式化转换），统一回退旧逻辑保证语义一致。
     * Object→String（objectStr）为兜底转换器、优先级最低：计划期解析排除该兜底
     * （{@link CConvertUtils#getConverterNoObjectFallback}），仅 Object 兜底可匹配的字段
     * 同样回退运行期，避免声明类型宽于实际类型时（如声明 Serializable 持有 Date）
     * objectStr 抢占 Date→String 等更精确转换。
     * JDK 源类返回空计划（判断按类恒定，置于计划期仅计算一次，热路径零判断）。</p>
     */
    private static CopyPlan getCopyPlan(Class<?> fromClass, Class<?> toClass) {

        // JDK 源类无实例字段可拷贝（判断结果按类恒定，置于计划期仅计算一次）
        if(CClassUtils.isJdkClass(fromClass)) {
            return EMPTY_COPY_PLAN;
        }

        val fromFieldMap = CReflectUtils.getInstanceFieldMap(fromClass);
        val fastEntries = new ArrayList<CopyEntry>();
        val fallbackEntries = new ArrayList<CopyEntry>();
        for (val toField : CReflectUtils.getInstanceFieldMap(toClass).values()) {

            if(CReflectUtils.isFinal(toField)) {
                continue; // final 字段不可写
            }

            val fromField = fromFieldMap.get(toField.getName());
            if(null == fromField) {
                continue; // 源对象无同名字段
            }

            val fromType = fromField.getType();
            val toType = toField.getType();

            // 源声明集合/Map/数组：值必为集合，旧语义一律跳过（计划期确定，不生成条目）
            if(Collection.class.isAssignableFrom(fromType)
                    || Map.class.isAssignableFrom(fromType)
                    || fromType.isArray()
            ) {
                continue;
            }

            val getterHandle = CMethodHandleUtils.getGetterHandleAsType(fromField);
            val setterHandle = CMethodHandleUtils.getSetterHandleAsType(toField);

            // 声明类型可直接赋值：计划期确定直接写入（Object 声明除外——值类型不确定，
            // 可能持有集合，需回退运行期按实际值类型分派）；
            // ClassUtil.isAssignable(目标, 源) 支持原始类型与包装类等价（如 int←Integer 拆箱、Integer←int 装箱）
            if(Object.class != fromType && ClassUtil.isAssignable(toType, fromType)) {
                fastEntries.add(newFastEntry(getterHandle, setterHandle, CFunction.SELF));
                continue;
            }

            // Object 声明与仅 Object 源兜底（如 Object→String）可匹配的字段回退运行期：
            // 运行期按实际值类型查预计算动作，保证 Date→String 等特殊转换不被 objectStr 抢占
            val converter = Object.class == fromType
                    ? null
                    : CConvertUtils.getConverterNoObjectFallback(fromType, toType);

            if(null == converter) {
                fallbackEntries.add(newFallbackEntry(getterHandle, setterHandle, toType));
            } else {
                fastEntries.add(newFastEntry(getterHandle, setterHandle, converter));
            }
        }

        val fastEntriesArray = fastEntries.toArray(new CopyEntry[0]);
        val fallbackEntriesArray = fallbackEntries.toArray(new CopyEntry[0]);
        return CopyPlan.builder()
                .fastEntries(fastEntriesArray)
                .fallbackEntries(fallbackEntriesArray)
                .build();
    }

    /**
     * 构建转 map 计划（字段 + getter MethodHandle，含 final 与集合字段）
     */
    private static ToMapPlan getToMapPlan(Class<?> objClass) {

        // JDK 类转 map 为空（判断结果按类恒定，置于计划期仅计算一次）
        if(CClassUtils.isJdkClass(objClass)) {
            return EMPTY_TO_MAP_PLAN;
        }

        val fieldMap = CReflectUtils.getInstanceFieldMap(objClass);
        val entries = new ArrayList<ToMapEntry>(fieldMap.size());
        for (val field : fieldMap.values()) {
            val getterHandle = CMethodHandleUtils.getGetterHandleAsType(field);
            val entry = ToMapEntry.builder()
                    .field(field)
                    .getterHandle(getterHandle)
                    .build();
            entries.add(entry);
        }
        val entriesArray = entries.toArray(new ToMapEntry[0]);
        return ToMapPlan.builder()
                .entries(entriesArray)
                .build();
    }

    /**
     * 构建快路径复制条目（直接写入或转换后写入）
     *
     * @param getterHandle getter 方法句柄
     * @param setterHandle setter 方法句柄
     * @param converter 转换器（{@link CFunction#SELF} 表示直接写入）
     * @return 复制条目
     */
    private static CopyEntry newFastEntry(MethodHandle getterHandle, MethodHandle setterHandle, CFunction<Object, ?> converter) {
        return CopyEntry.builder()
                .getterHandle(getterHandle)
                .setterHandle(setterHandle)
                .converter(converter)
                .build();
    }

    /**
     * 构建回退路径复制条目（运行期按实际值类型分派）
     *
     * @param getterHandle getter 方法句柄
     * @param setterHandle setter 方法句柄
     * @param toType 目标字段声明类型
     * @return 复制条目
     */
    private static CopyEntry newFallbackEntry(MethodHandle getterHandle, MethodHandle setterHandle, Class<?> toType) {
        return CopyEntry.builder()
                .getterHandle(getterHandle)
                .setterHandle(setterHandle)
                .toType(toType)
                .build();
    }

    /**
     * 使用缓存的 setter 方法句柄写入字段值
     */
    @SneakyThrows
    private static void setValueWithHandle(Object to, Field toField, Object toValue) {
        CMethodHandleUtils.getSetterHandle(toField).invoke(to, toValue);
    }

    /**
     * 复制计划：按 (源类, 目标类) 预计算的字段级复制动作
     * <p>fastEntries 为计划期已解析转换路径的字段（直接写入或转换后写入）；
     * fallbackEntries 为计划期未解析转换路径的字段（运行期按实际值类型判断）。</p>
     */
    @Builder
    private static final class CopyPlan {

        final CopyEntry[] fastEntries;

        final CopyEntry[] fallbackEntries;

    }

    /**
     * 复制条目
     * <p>converter 为 {@link CFunction#SELF} 时直接写入，否则转换后写入；
     * converter 为 null 时走回退逻辑（toType 为目标字段声明类型，供运行期判断）。</p>
     */
    @Builder
    private static final class CopyEntry {

        final MethodHandle getterHandle;

        final MethodHandle setterHandle;

        final CFunction<Object, ?> converter;

        final Class<?> toType;

    }

    /**
     * 转 map 计划：按类预计算的字段与 getter 方法句柄（含 final 字段）
     */
    @Builder
    private static final class ToMapPlan {

        final ToMapEntry[] entries;

    }

    /**
     * 转 map 条目
     */
    @Builder
    private static final class ToMapEntry {

        final Field field;

        final MethodHandle getterHandle;

    }

}
