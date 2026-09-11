package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.*;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CMapUtils
 * </p>
 *
 * <p>Map 工具类，提供：</p>
 * <ul>
 *   <li>读取与兜底：{@link #get}/{@link #getOrDefault}（空入参安全）、{@link #put}（空入参不写入）、{@link #defaultEmpty}、{@link #toStringValueMap}；</li>
 *   <li>创建：{@code newMap}、{@link #newEnumMap}、{@link #newIgnoreCaseMap}；</li>
 *   <li>键值映射：{@link #mapKey}、{@link #mapValue}、{@link #map}（转换结果为 null 的条目过滤）；</li>
 *   <li>过滤：{@link #filter}、{@link #filterKey}、{@link #filterValue}；</li>
 *   <li>合并：{@code merge}（冲突取第一个或自定义合并，返回不可变 Map）；</li>
 *   <li>其他：{@link #toAvailableStrMap}、{@code compare}（打印差异表格）、{@code computeIfAbsent}；</li>
 *   <li>TypeReference 常量：{@link #MAP_STRING_OBJECT_TYPE_REFERENCE}、{@link #LIST_MAP_STRING_OBJECT_TYPE_REFERENCE}、{@link #MAP_STRING_STRING_TYPE_REFERENCE}。</li>
 * </ul>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMapUtils}（{@code @UtilityClass}）提供项目统一的 Map 操作入口，能力分为六组：</p>
 * <ul>
 *   <li>读取与空兜底：{@code get}、{@code getOrDefault}、{@code put}（空入参安全）、{@code defaultEmpty}、{@code toStringValueMap}</li>
 *   <li>创建：{@code newMap}（按类型分派 LinkedHashMap / EnumMap）、{@code newEnumMap}、{@code newIgnoreCaseMap}</li>
 *   <li>键值映射：{@code mapKey}、{@code mapValue}、{@code map}（转换结果为 null 的条目过滤）</li>
 *   <li>过滤：{@code filter}、{@code filterKey}、{@code filterValue}</li>
 *   <li>合并：{@code merge}（冲突取第一个或自定义合并函数，返回不可变 Map）、{@code toAvailableStrMap}（去空白 + 关键字过滤）</li>
 *   <li>其他：{@code computeIfAbsent}、{@code compare}（打印差异表格）、三个 Jackson {@code TypeReference} 常量</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>get / getOrDefault：map 空或 key 空</td>
 *     <td>get 返回 null / getOrDefault 返回 defaultValue，不写入</td>
 *   </tr>
 *   <tr>
 *     <td>put 任一入参为 null</td>
 *     <td>不写入，返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>defaultEmpty 空 / null Map</td>
 *     <td>返回空 Map</td>
 *   </tr>
 *   <tr>
 *     <td>map 空 Map / 转换后 key 或 value 为 null</td>
 *     <td>返回空 Map / 跳过该条目</td>
 *   </tr>
 *   <tr>
 *     <td>merge 空数组或 null</td>
 *     <td>返回空 Map</td>
 *   </tr>
 *   <tr>
 *     <td>merge 冲突 key</td>
 *     <td>默认取第一个（可自定义 merge）</td>
 *   </tr>
 *   <tr>
 *     <td>newEnumMap 非枚举类型</td>
 *     <td>抛 IllegalArgumentException</td>
 *   </tr>
 *   <tr>
 *     <td>computeIfAbsent 已有值</td>
 *     <td>直接返回，不调用 supplier</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Map 创建、键值映射 / 过滤 / 合并、空值安全写入等通用 Map 操作。</li>
 *   <li>项目内统一 Map 处理写法（替代手写 {@code HashMap} 判空与 stream 收集）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>{@code newMap} 需传非 null 的 object / type；EnumMap 需枚举类型。</li>
 *   <li>{@code map} / {@code filter} 依赖转换函数，转换结果为 null 的条目会被丢弃（不保留 null）。</li>
 *   <li>需要保留 null 条目时本类不适用。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>null key / value 过滤保证结果 Map 无 null 条目，牺牲「保留 null」能力换取安全。</li>
 *   <li>{@code merge} 返回不可变 LinkedHashMap，保证合并结果不被修改。</li>
 *   <li>{@code getOrDefault} 采用 JDK {@code Map#getOrDefault} 语义：键存在但映射为 null 时同样返回默认值。</li>
 *   <li>无并发保障：本类不对传入 Map 做同步，并发场景由调用方保证（或用 {@code ConcurrentHashMap}）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>空入参语义</b></p>
 * <ul>
 *   <li>{@code put} 的 map / key / value 任一为 null 时不写入并返回 null。</li>
 *   <li>{@code defaultEmpty} 对 null 或空 Map 返回空 Map（{@code CMap.of()}），非空返回原引用。</li>
 *   <li>{@code map} / {@code merge} / {@code filter} 对空 Map 返回空 Map；转换后的 key / value 为 null 的条目被过滤。</li>
 * </ul>
 * <p><b>读取语义</b></p>
 * <ul>
 *   <li>{@code getOrDefault} 在 map 为空（null / 空 Map）或 key 为空时直接返回 defaultValue，否则返回 {@code Map#getOrDefault} 结果。</li>
 *   <li>因此「键存在但映射为 null」与「键不存在」返回一致（均为 defaultValue），遵循 JDK {@code Map#getOrDefault} 语义。</li>
 * </ul>
 * <p><b>Map 创建</b></p>
 * <ul>
 *   <li>{@code newEnumMap} 要求 type 必须是枚举，否则抛 {@code IllegalArgumentException}。</li>
 *   <li>{@code newIgnoreCaseMap} 返回 {@code TreeMap(String.CASE_INSENSITIVE_ORDER)}，键大小写不敏感。</li>
 * </ul>
 * <p><b>映射与过滤</b></p>
 * <ul>
 *   <li>{@code map} 逐条转换，key / value 任一转换结果为 null 时跳过该条目（不写入结果 Map）。</li>
 *   <li>{@code filter} 系列按 {@code CBiPredicate} / {@code CPredicate} 过滤后经 {@code Collectors.toMap} 收集。</li>
 * </ul>
 * <p><b>合并</b></p>
 * <ul>
 *   <li>{@code merge} 合并多个 Map，null value 条目过滤；默认冲突取第一个（{@code (e1, e2) -&gt; e1}），可传 {@code mergeFunction} 自定义。</li>
 *   <li>返回不可变 {@code LinkedHashMap}，避免结果被调用方修改。</li>
 * </ul>
 *
 * @since 2024/2/26
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CMapUtils {

    /**
     * 获取键对应的值（空入参安全）
     *
     * <p>等价于 {@code getOrDefault(map, key, null)}：map 为空（null 或空 Map）或 key 为空
     * （null、空字符串、空集合/数组等）时返回 null，不写入、不抛异常。</p>
     *
     * @param map Map
     * @param key 键
     * @param <K> 键泛型
     * @param <V> 值泛型
     * @return 键对应的值；map/key 为空或键不存在时返回 null
     */
    public <K, V> V get(Map<K, V> map, K key) {
        return getOrDefault(map, key, null);
    }

    /**
     * 获取键对应的值，为空或不存在时返回默认值
     *
     * <p>map 为空（null 或空 Map）或 key 为空（null、空字符串、空集合/数组等）时直接返回
     * {@code defaultValue}；否则返回 {@link Map#getOrDefault(Object, Object)} 的结果
     * （键不存在或映射为 null 时返回 {@code defaultValue}）。</p>
     * <ul>
     *   <li>{@code get} 等价于 {@code getOrDefault(map, key, null)}。</li>
     * </ul>
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @param <K>          键泛型
     * @param <V>          值泛型
     * @return 键对应的值；map/key 为空、键不存在或映射为 null 时返回 defaultValue
     */
    public <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        if (MapUtil.isEmpty(map) || ObjectUtil.isEmpty(key)) {
            return defaultValue;
        }

        return map.getOrDefault(key, defaultValue);
    }

    /**
     * 设置 map 值
     *
     * @param map Map
     * @param k   键
     * @param v   值
     * @param <K> 键泛型
     * @param <V> 值泛型
     * @return 值
     */
    public <K, V> V put(Map<K, V> map, K k, V v) {
        if (Objects.isNull(map) || Objects.isNull(k) || Objects.isNull(v)) {
            return null;
        }

        return map.put(k, v);
    }

    /**
     * 默认空 map
     *
     * @param map Map
     * @param <K> 键泛型
     * @param <V> 值泛型
     * @return 原 Map 或 空 Map
     */
    public <K, V> Map<K, V> defaultEmpty(Map<K, V> map) {
        return MapUtil.isEmpty(map) ? Collections.emptyMap() : map;
    }

    /**
     * Map[String, Object] 映射
     */
    public final TypeReference<Map<String, Object>> MAP_STRING_OBJECT_TYPE_REFERENCE =
        new TypeReference<Map<String, Object>>() {
        };

    /**
     * List[Map[String, Object]]映射
     */
    public final TypeReference<List<Map<String, Object>>> LIST_MAP_STRING_OBJECT_TYPE_REFERENCE =
        new TypeReference<List<Map<String, Object>>>() {
        };

    /**
     * Map[String, String] 映射
     */
    public final TypeReference<Map<String, String>> MAP_STRING_STRING_TYPE_REFERENCE =
        new TypeReference<Map<String, String>>() {
        };

    /**
     * 将 Map[String, Object] 转换为 Map[String, String]
     *
     * @param map Map[String, Object]
     * @return Map[String, String]
     */
    public Map<String, String> toStringValueMap(Map<String, Object> map) {

        if (null == map) {
            return null;
        }

        val stringStringMap = new LinkedHashMap<String, String>(map.size());
        map.forEach((k, v) -> stringStringMap.put(k, Objects.toString(v, null)));
        return stringStringMap;
    }

    /**
     * 创建 Map
     * <ul>
     *   <li>{@code newMap(object, size)} / {@code newMap(type, size)}：type 为枚举返回 {@code EnumMap}，否则返回 {@code LinkedHashMap}（保证插入序）。</li>
     * </ul>
     *
     * @param object 键值（任意一个）
     * @param size   大小
     * @param <K>    键泛型
     * @param <V>    值泛型
     * @return Map
     */
    public <K, V> Map<K, V> newMap(K object, Integer size) {

        if (null == object) {
            throw new IllegalArgumentException("object can't be null");
        }

        return newMap(object.getClass(), size);
    }

    /**
     * 创建 Map
     *
     * @param type 键类
     * @param size 大小
     * @param <K>  键泛型
     * @param <V>  值泛型
     * @return Map
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> newMap(Class<?> type, Integer size) {

        if (type.isEnum()) {
            return newEnumMap((Class<Enum<?>>) type);
        }
        return new LinkedHashMap<>(size);
    }

    /**
     * 创建 EnumMap
     *
     * <p>type 必须是枚举类型，否则快速失败；返回的 EnumMap 以 type 为键类型、空初始容量。</p>
     *
     * @param type 枚举类，不能为空且必须是枚举类型
     * @param <K>  键泛型（实际为 {@code type} 对应的枚举类型，因调用方以通配符传入故不精确）
     * @param <V>  值泛型
     * @return 空 EnumMap
     * @throws IllegalArgumentException type 不是枚举类型
     */
    @SuppressWarnings({
        "unchecked",
        "rawtypes"
    })
    /**
     * 创建 EnumMap：要求 type 必须是枚举类型
     */
    public <K, V> Map<K, V> newEnumMap(Class<? extends Enum<?>> type) {

        if (!type.isEnum()) {
            throw new IllegalArgumentException(type.getName() + " is not an enum");
        }

        return new EnumMap(type);
    }

    /**
     * 创建忽略大小写排序的 Map
     *
     * @param <V> 值泛型
     * @return Map
     */
    public <V> TreeMap<String, V> newIgnoreCaseMap() {
        return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Map 键映射
     *
     * @param map       原 Map
     * @param keyMapper 键映射
     * @param <K1>      原键泛型
     * @param <K2>      新键泛型
     * @param <V>       值泛型
     * @return 新 Map
     */
    public <K1, K2, V> Map<K2, V> mapKey(
        Map<K1, V> map,
        CFunction<K1, K2> keyMapper
    ) {
        return map(map, keyMapper, CFunction.self());
    }

    /**
     * Map 值映射
     *
     * @param map         原 Map
     * @param valueMapper 值映射
     * @param <K>         键泛型
     * @param <V1>        原值泛型
     * @param <V2>        新值泛型
     * @return 新 Map
     */
    public <K, V1, V2> Map<K, V2> mapValue(
        Map<K, V1> map,
        CFunction<V1, V2> valueMapper
    ) {
        return map(map, CFunction.self(), valueMapper);
    }

    /**
     * Map 键值映射
     *
     * @param map         原 Map
     * @param keyMapper   键映射
     * @param valueMapper 值映射
     * @param <K1>        原键泛型
     * @param <V1>        原值泛型
     * @param <K2>        新键泛型
     * @param <V2>        新值泛型
     * @return 新 Map
     */
    public <K1, V1, K2, V2> Map<K2, V2> map(
        Map<K1, V1> map,
        CFunction<K1, K2> keyMapper,
        CFunction<V1, V2> valueMapper
    ) {

        if (MapUtil.isEmpty(map)) {
            return Collections.emptyMap();
        }

        val map2 = new LinkedHashMap<K2, V2>(map.size());
        map.forEach((k1, v1) -> {

            val k2 = CObjUtils.convert(k1, keyMapper);
            val v2 = CObjUtils.convert(v1, valueMapper);
            if (Objects.isNull(k2)
                || Objects.isNull(v2)
            ) {
                return;
            }

            map2.put(k2, v2);

        });

        return map2;
    }

    /**
     * Map 按键值条件过滤
     *
     * @param map       Map
     * @param predicate 过滤条件
     * @param <K>       键泛型
     * @param <V>       值泛型
     * @return 过滤后的 Map
     */
    public <K, V> Map<K, V> filter(Map<K, V> map, CBiPredicate<K, V> predicate) {
        if (MapUtil.isEmpty(map)) {
            return CMap.of();
        }

        return map.entrySet().stream()
            .filter(entry -> predicate.test(entry.getKey(), entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Map 按键过滤
     *
     * @param map       Map
     * @param predicate 过滤条件
     * @param <K>       键泛型
     * @param <V>       值泛型
     * @return 过滤后的 Map
     */
    public <K, V> Map<K, V> filterKey(Map<K, V> map, CPredicate<K> predicate) {
        return filter(map, (k, v) -> predicate.test(k));
    }

    /**
     * Map 按值过滤
     *
     * @param map       Map
     * @param predicate 过滤条件
     * @param <K>       键泛型
     * @param <V>       值泛型
     * @return 过滤后的 Map
     */
    public <K, V> Map<K, V> filterValue(Map<K, V> map, CPredicate<V> predicate) {
        return filter(map, (k, v) -> predicate.test(v));
    }

    /**
     * 合并多个 Map（键冲突时取第一个）
     *
     * @param maps Map 数组
     * @param <K>  键泛型
     * @param <V>  值泛型
     * @return 合并后的不可变 Map
     */
    @SafeVarargs
    public <K, V> Map<K, V> merge(Map<K, V>... maps) {
        return merge(
            (e1, e2) -> e1,
            maps
        );
    }

    /**
     * 转换 Map 的键值为可用字符串
     *
     * @param map 原 Map
     * @return 键值均可用的新 Map
     */
    public Map<String, String> toAvailableStrMap(Map<String, String> map) {
        return map(
            map,
            CStrUtils::toAvailable,
            CStrUtils::toAvailable
        );
    }

    /**
     * 按指定合并函数合并多个 Map 为一个不可变 Map
     *
     * @param mergeFunction 值合并函数，冲突时生效
     * @param maps          待合并的 Map 列表
     * @param <K>           键类型
     * @param <V>           值类型
     * @return 合并后的不可变 Map
     */
    @SafeVarargs
    public <K, V> Map<K, V> merge(BinaryOperator<V> mergeFunction, Map<K, V>... maps) {

        if (ArrayUtil.isEmpty(maps)) {
            return CMap.of();
        }

        return Arrays.stream(maps)
            .filter(MapUtil::isNotEmpty)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .filter(entry -> Objects.nonNull(entry.getValue()))
            .collect(CCollectors.toUnmodifiableLinkedMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                mergeFunction
            ));
    }

    /**
     * 对比多个 Map（按 hashCode 命名，打印差异表格）
     *
     * @param objs         待对比的 Map 列表
     * @param showFunction 值显示函数
     * @param <V>          值类型
     */
    public <V> void compare(
        List<Map<String, V>> objs,
        ToStringFunction<V> showFunction
    ) {
        compare(
            objs,
            e -> String.valueOf(e.hashCode()),
            CFunction.self(),
            showFunction
        );
    }

    /**
     * 对比多个对象的字段差异（打印差异表格）
     *
     * @param objs            待对比的对象列表
     * @param getNameFunction 对象名称函数
     * @param toMapFunction   对象转字段 Map 函数
     * @param showFunction    值显示函数
     * @param <T>             对象类型
     * @param <V>             值类型
     */
    public <T, V> void compare(
        List<T> objs,
        ToStringFunction<T> getNameFunction,
        CFunction<T, Map<String, V>> toMapFunction,
        ToStringFunction<V> showFunction
    ) {

        if (CollUtil.isEmpty(objs)) {
            return;
        }

        val mergedMap = new LinkedHashMap<String, Map<Object, V>>();

        val sb = new StringBuilder("\n");
        for (val obj : objs) {

            val map = toMapFunction.apply(obj);
            map.forEach((key, value) ->
                mergedMap.computeIfAbsent(key, k -> new HashMap<>())
                    .put(obj, value));
        }

        val tables = new ArrayList<List<String>>(objs.size() + 1);

        val xColumn = new ArrayList<String>(mergedMap.size() + 1);
        xColumn.add("");
        tables.add(xColumn);

        for (val obj : objs) {
            val list = new ArrayList<String>(mergedMap.size() + 1);
            list.add(getNameFunction.apply(obj));
            tables.add(list);
        }

        mergedMap.forEach((key, map) -> {

            xColumn.add(key);
            for (int i = 0; i < objs.size(); i++) {

                val obj = objs.get(i);
                val columnList = tables.get(i + 1);
                val showValue = Opt.ofNullable(map.get(obj))
                    .map(showFunction)
                    .orElse("-");
                columnList.add(showValue);
            }

        });

        val columnWidthList = new ArrayList<Integer>();
        tables.forEach(columnList -> {

            val maxWidth = columnList.stream()
                .mapToInt(CStrUtils::getPrintWidth)
                .max()
                .orElse(0);
            columnWidthList.add(maxWidth);
        });

        val lineSize = tables.get(0).size();
        for (int i = 0; i < lineSize; i++) {
            for (int i1 = 0; i1 < tables.size(); i1++) {

                val columnList = tables.get(i1);
                val column = columnList.get(i);

                val width = columnWidthList.get(i1) + 2;

                String columnReal;
                if (i1 == 0) {
                    columnReal = CStrUtils.fillAfter(column, ' ', width);
                } else {
                    columnReal = CStrUtils.fillSide(column, ' ', width);
                }

                sb.append(columnReal);
            }
            sb.append("\n");
        }

        log.info("\n{}", sb);

    }

    /**
     * 获取键对应的值，不存在时通过供应商计算并放入 Map
     *
     * <h2>computeIfAbsent</h2>
     * <ul>
     *   <li>先 {@code map.get(key)}，非 null 直接返回；否则 {@code map.computeIfAbsent(key, mappingFunction)}。</li>
     *   <li>提供 {@code CSupplier} 与 {@code CFunction} 两个重载，前者仅在生产时求值。</li>
     * </ul>
     *
     * @param map           Map
     * @param key           键
     * @param valueSupplier 值供应商
     * @param <K>           键泛型
     * @param <V>           值泛型
     * @return 值*/
    public <K, V> V computeIfAbsent(Map<K, V> map, K key, CSupplier<V> valueSupplier) {
        return computeIfAbsent(map, key, k -> valueSupplier.get());
    }

    /**
     * 获取键对应的值，不存在时通过映射函数计算并放入 Map
     *
     * @param map             Map
     * @param key             键
     * @param mappingFunction 映射函数
     * @param <K>             键泛型
     * @param <V>             值泛型
     * @return 值
     */
    public <K, V> V computeIfAbsent(Map<K, V> map, K key, CFunction<K, V> mappingFunction) {

        val handle = map.get(key);
        if (null != handle) {
            return handle;
        }
        return map.computeIfAbsent(key, mappingFunction);
    }

}
