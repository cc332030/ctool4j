package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.definition.function.CSupplier;
import com.c332030.ctool4j.definition.function.StringFunction;
import com.c332030.ctool4j.definition.function.ToStringFunction;
import com.google.common.base.CaseFormat;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.helpers.MessageFormatter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CStrUtils
 * </p>
 *
 * @since 2024/3/15
 */
@CustomLog
@UtilityClass
public class CStrUtils {

    public static final String NULL = "null";

    public static final String UNDEFINED = "undefined";

    public static final Character SINGLE_QUOTATION = '\'';

    public static final Character DOUBLE_QUOTATION = '"';

    public static final String DEFAULT_SEPARATOR = ",";


    public static final Set<Character> NOT_AVAILABLE_CHARACTERS = CSet.of(
            SINGLE_QUOTATION, DOUBLE_QUOTATION
    );

    public static final Set<String> NOT_AVAILABLE_STRINGS = CSet.of(
            NULL, UNDEFINED
    );

    public static final String UTF8_BOM = "\uFEFF";

    /**
     * 字符串格式化
     * @param template 模板 "My name is {}, I come from {}"
     * @param params 参数
     * @return formatted string
     */
    public String format(String template, Object... params) {

        if(StrUtil.isBlank(template)) {
            return template;
        }

        return MessageFormatter.arrayFormat(template, params).getMessage();
    }

    /**
     * 字符串格式化
     * @param key 模板
     * @param stringLookup 字符串属性查找函数
     * @return formatted string
     */
    public String formatLookup(String key, StringFunction<?> stringLookup) {
        return formatLookup(key, stringLookup, null);
    }

    /**
     * 模板字符串格式化
     * @param key 模板
     * @param stringLookup 模板字符串查找函数
     * @param defaultValue 默认值
     * @return formatted string
     */
    public String formatLookup(String key, StringFunction<?> stringLookup, String defaultValue) {

        if(StrUtil.isEmpty(key)) {
            return defaultValue;
        }

        val value = stringLookup.apply(key);
        if(value == null) {
            return defaultValue;
        }

        val strValue = StrUtil.toStringOrNull(value);
        return ObjUtil.defaultIfNull(strValue, defaultValue);
    }

    /**
     * 字符串格式化
     * @param template 模板 "My name is ${name}, I come from ${country}"
     * @param stringLookup 字符串查找
     * @return formatted string
     */
    public String format(String template, StringFunction<?> stringLookup) {
        return format(template, stringLookup, null);
    }

    /**
     * 字符串格式化
     * @param template 模板 "My name is ${name}, I come from ${country}"
     * @param stringLookup 字符串查找
     * @param defaultValue 默认值
     * @return formatted string
     */
    public String format(String template, StringFunction<?> stringLookup, String defaultValue) {

        if(StrUtil.isBlank(template)) {
            return template;
        }

        return new StringSubstitutor(key -> formatLookup(key, stringLookup, defaultValue))
                .replace(template);
    }

    /**
     * 字符串格式化
     * @param template 模板 "My name is ${name}, I come from ${country}"
     * @param params 参数
     * @param defaultValue 默认值
     * @return formatted string
     */
    public String format(String template, Map<String, ?> params, String defaultValue) {

        if(StrUtil.isBlank(template)) {
            return template;
        }

        params = CMapUtils.defaultEmpty(params);
        return format(template, params::get, defaultValue);
    }

    /**
     * 字符串格式化
     * @param template 模板 "My name is ${name}, I come from ${country}"
     * @param params 参数
     * @return formatted string
     */
    public String formatNullToEmpty(String template, Map<String, ?> params) {
        return format(template, params, StrUtil.EMPTY);
    }

    /**
     * 字符串格式化
     * @param template 模板 "My name is ${name}, I come from ${country}"
     * @param object 参数
     * @return formatted string
     */
    public String formatByObject(String template, Object object) {
        return formatByObject(template, object, StrUtil.EMPTY);
    }

    /**
     * 字符串格式化
     * @param template 模板 "My name is ${name}, I come from ${country}"
     * @param object 参数
     * @param defaultValue 默认值
     * @return formatted string
     */
    public String formatByObject(String template, Object object, String defaultValue) {

        if(StrUtil.isBlank(template)) {
            return template;
        }

        val beanMap = CBeanUtils.toMap(object);
        return format(template, beanMap, defaultValue);
    }

    /**
     * 字符串格式化
     * @param template 模板 "My name is ${name}, I come from ${country}"
     * @param object 参数
     * @return formatted string
     */
    public String formatByObjectNullToEmpty(String template, Object object) {
        return formatByObject(template, object, StrUtil.EMPTY);
    }

    /**
     * 大写下划线 转 报文头：TRACE_ID - Trace-CId
     * @param value 待转换值
     * @return 转换结果
     */
    public String upperUnderscoreToHeaderName(String value) {
        val splits = value.split("_");
        return Arrays.stream(splits)
                .map(CStrUtils::upperUnderscoreToUpperCamel)
                .collect(Collectors.joining("-"));
    }

    /**
     * 大写下划线 转 驼峰-首字母大写
     * @param value 待转换值
     * @return 转换结果
     */
    public String upperUnderscoreToLowerCamel(String value)  {

        val result = CaseFormat.UPPER_UNDERSCORE
                .converterTo(CaseFormat.LOWER_CAMEL)
                .convert(value);
        return Optional.ofNullable(result)
                .orElseThrow(() -> new RuntimeException("'大写下划线'转'驼峰-首字母小写'失败：" + value));
    }

    /**
     * 大写下划线 转 驼峰-首字母大写
     * @param value 待转换值
     * @return 转换结果
     */
    public String upperUnderscoreToUpperCamel(String value)  {

        val result = CaseFormat.UPPER_UNDERSCORE
                .converterTo(CaseFormat.UPPER_CAMEL)
                .convert(value);
        return Optional.ofNullable(result)
                .orElseThrow(() -> new RuntimeException("'大写下划线'转'驼峰-首字母大写'失败：" + value));
    }

    /**
     * 驼峰-首字符大写 转 大写下划线
     * @param value 待转换值
     * @return 转换结果
     */
    public String upperCamelToUpperUnderscore(String value)  {

        val result = CaseFormat.UPPER_CAMEL
                .converterTo(CaseFormat.UPPER_UNDERSCORE)
                .convert(value);
        return Optional.ofNullable(result)
                .orElseThrow(() -> new RuntimeException("'驼峰-首字母大写'转'大写下划线'失败：" + value));
    }

    /**
     * 获取字符串里面所有大写字母
     * @param value 待转换值
     * @return 大写字母组合字符串
     */
    public String getAllUpperChar(String value) {
        return value.replaceAll("[^A-Z]", "");
    }

    /**
     * 去除字符串前后空格
     * @param string 待处理字符串
     * @return 处理结果
     */
    public String trim(String string) {
        return string == null ? null : string.trim();
    }

    /**
     * 转为字符串
     * @param obj 待转换对象
     * @return 转换结果
     */
    public String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    /**
     * 转为字符串，并转为指定类型
     * @param obj 待转换对象
     * @param function 转换函数
     * @param <T> 待转换类型泛型
     * @return 转换结果
     */
    public <T> T toStringThenConvert(Object obj, StringFunction<T> function) {
        return function.apply(toString(obj));
    }

    /**
     * 转为字符串
     * @param t 待转换对象
     * @param function 待转换对象转换函数
     * @param <T> 待转换类型泛型
     * @return 待转换对象转换结果
     */
    public <T> String toString(T t, ToStringFunction<T> function) {
        return toString(function.apply(t));
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @param separator 分割符
     * @param convert 转换函数
     * @param collectionFunction 集合创建函数
     * @param <T> 待转换类型泛型
     * @param <C> 结果泛型
     * @return 分割结果
     */
    public <T, C extends Collection<T>> C split(
            String value,
            String separator,
            Function<String, T> convert,
            Function<Integer, C> collectionFunction
    ) {

        if(StrUtil.isBlank(value)) {
            return collectionFunction.apply(0);
        }

        separator = StrUtil.nullToDefault(separator, ",");

        val arr = value.split(separator);
        val collection = collectionFunction.apply(arr.length);

        for (var str : arr) {
            str = toAvailable(str);
            if(StrUtil.isNotEmpty(str)) {
                collection.add(convert.apply(str));
            }
        }

        return collection;
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @param separator 分割符
     * @return 分割结果
     */
    public List<String> splitToList(String value, String separator) {
        return split(value, separator, Function.identity(), CCollUtils::newList);
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @param separator 分隔符
     * @param convert 转换函数
     * @param <T> 待转换类型泛型
     * @return 分割结果
     */
    public <T> List<T> splitToList(String value, String separator, Function<String, T> convert) {
        return split(value, separator, convert, CCollUtils::newList);
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @param convert 转换函数
     * @param <T> 待转换类型泛型
     * @return 分割结果
     */
    public <T> List<T> splitToList(String value, Function<String, T> convert) {
        return split(value, null, convert, CCollUtils::newList);
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @return 分割结果
     */
    public List<String> splitToList(String value) {
        return splitToList(value, Function.identity());
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @return 分割结果
     */
    public List<Integer> splitToIntegerList(String value) {
        return splitToList(value, Integer::parseInt);
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @return 分割结果
     */
    public List<Long> splitToLongList(String value) {
        return splitToList(value, Long::parseLong);
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @param separator 分割符
     * @return 分割结果
     */
    public Set<String> splitToSet(String value, String separator) {
        return split(value, separator, Function.identity(), CCollUtils::newLinkedSet);
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @param convert 转换函数
     * @param <T> 待转换类型泛型
     * @return 分割结果
     */
    public <T> Set<T> splitToSet(String value, Function<String, T> convert) {
        return split(value, null, convert, CCollUtils::newLinkedSet);
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @return 分割结果
     */
    public Set<String> splitToSet(String value) {
        return splitToSet(value, Function.identity());
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @return 分割结果
     */
    public Set<Integer> splitToIntegerSet(String value) {
        return splitToSet(value, Integer::parseInt);
    }

    /**
     * 分割字符串
     * @param value 待分割字符串
     * @return 分割结果
     */
    public static Set<Long> splitToLongSet(String value) {
        return splitToSet(value, Long::parseLong);
    }

    /**
     * 分割字符串
     * @param string 待分割字符串
     * @param separatorEntry 分割符
     * @param separatorKeyValue 分割符
     * @param kFunction 键转换函数
     * @param vFunction 值转换函数
     * @param mapFunction 集合创建函数
     * @param <K> Map key 泛型
     * @param <V> Map value 泛型
     * @return 分割结果
     */
    public static <K, V> Map<K, V> splitToMap(
            String string,
            String separatorEntry, String separatorKeyValue,
            Function<String, K> kFunction, Function<String, V> vFunction,
            Function<Integer, Map<K, V>> mapFunction
    ) {

        if(StrUtil.isBlank(string)) {
            return mapFunction.apply(0);
        }

        separatorEntry = StrUtil.nullToDefault(separatorEntry, ";");
        separatorKeyValue = StrUtil.nullToDefault(separatorKeyValue, ":");

        val arr = string.split(separatorEntry);
        val map = mapFunction.apply(arr.length);

        for (var str : arr) {

            str = str.trim();
            if(!str.isEmpty()) {

                val keyValueArr = str.split(separatorKeyValue);
                if(keyValueArr.length >= 2) {
                    val key = keyValueArr[0].trim();
                    val value = keyValueArr[1].trim();
                    map.put(kFunction.apply(key), vFunction.apply(value));
                }
            }
        }

        return map;
    }

    /**
     * 分割字符串
     * @param string 待分割字符串
     * @param kFunction 键转换函数
     * @param vFunction 值转换函数
     * @param <K> Map key 泛型
     * @param <V> Map value 泛型
     * @return 分割结果
     */
    public static <K, V> Map<K, V> splitToMap(String string, Function<String, K> kFunction, Function<String, V> vFunction) {
        return splitToMap(string, null, null, kFunction, vFunction, CCollUtils::newLinkedMap);
    }

    /**
     * 分割字符串
     * @param string 待分割字符串
     * @return 分割结果
     */
    public static Map<String, String> splitToMap(String string) {
        return splitToMap(string, Function.identity(), Function.identity());
    }

    /**
     * 连接字符串
     * @param separator 分割符
     * @param strings 字符串数组
     * @return 连接后的字符串
     */
    public static String concat(String separator, String... strings) {

        if(ArrayUtil.isEmpty(strings)) {
            return null;
        }

        return Arrays.stream(strings)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.joining(separator));
    }

    /**
     * 不为空则获取
     * @param value 待校验值
     * @param supplier 取值方法
     * @param <T> 值泛型
     * @return 值
     */
    public static <T> T notEmptyThenGet(String value, CSupplier<T> supplier) {
        return CObjUtils.ifThenGet(StrUtil.isNotEmpty(value), supplier);
    }

    /**
     * 不为空则获取
     * @param value 待校验值
     * @param supplier 取值方法
     * @param <T> 值泛型
     * @return 值
     */
    public static <T> T notBlackThenGet(String value, CSupplier<T> supplier) {
        return CObjUtils.ifThenGet(StrUtil.isNotBlank(value), supplier);
    }

    /**
     * 转换字符串，如果为空则返回默认值
     * @param o 源字符串
     * @param function 转换函数
     * @param <R> 泛型
     * @return 转换后的字符串
     */
    public static <R> R convertNotEmpty(String o, Function<String, R> function) {
        return convertNotEmpty(o, function, null);
    }

    /**
     * 转换字符串，如果为空则返回默认值
     * @param o 源字符串
     * @param function 转换函数
     * @param defaultValue 默认值
     * @param <R> R
     * @return 转换后的字符串
     */
    public static <R> R convertNotEmpty(String o, Function<String, R> function, R defaultValue) {

        if(StrUtil.isBlank(o)) {
            return defaultValue;
        }

        val value = function.apply(o);
        if(Objects.isNull(value)) {
            return defaultValue;
        }

        return value;
    }

    /**
     * 转换字符串，如果为空则返回默认值
     * @param o 源字符串
     * @param function 转换函数
     * @return 转换后的字符串
     * @param <R> R
     */
    public static <R> R convertNotBlank(String o, Function<String, R> function) {
        return convertNotBlank(o, function, null);
    }

    /**
     * 转换字符串，如果为空则返回默认值
     * @param o 源字符串
     * @param function 转换函数
     * @param defaultValue 默认值
     * @return 转换后的字符串
     * @param <R> R
     */
    public static <R> R convertNotBlank(String o, Function<String, R> function, R defaultValue) {

        if(StrUtil.isBlank(o)) {
            return defaultValue;
        }

        val value = function.apply(o);
        if(Objects.isNull(value)) {
            return defaultValue;
        }

        return value;
    }

    /**
     * 获取字符
     * @param string 字符串
     * @param index 索引
     * @return 字符
     */
    public static Character charAt(String string, int index) {

        if(index < 0 || index >= string.length()) {
            return null;
        }
        return string.charAt(index);
    }

    /**
     * 是否可用字符串
     * @param string 字符串
     * @return 结果
     */
    public static boolean isAvailable(String string) {
        return StrUtil.isNotEmpty(toAvailable(string));
    }

    /**
     * 转换成可用字符串
     * @param string 待转换字符串
     * @return 可用字符串
     */
    public static String toAvailable(String string) {

        string = CStrUtils.trim(string);
        if(StrUtil.isEmpty(string)) {
            return null;
        }

        if(NOT_AVAILABLE_STRINGS.contains(string)) {
            return null;
        }

        var needSubString = false;

        var start = 0;
        var end = string.length() - 1;

        // 删除字符串前后的引号
        while (NOT_AVAILABLE_CHARACTERS.contains(charAt(string, start))) {
            start++;
            needSubString = true;
        }
        while (NOT_AVAILABLE_CHARACTERS.contains(charAt(string, end))) {
            end--;
            needSubString = true;
        }

        if(needSubString) {
            if(start >= end) {
                return null;
            }
            string = StrUtil.sub(string, start, end + 1);
        }

        return string;
    }

    /**
     * 转换成可用字符串
     * @param strings 待转换字符串数组
     * @return 可用字符串
     */
    public static List<String> toAvailable(String[] strings) {
        return toAvailable(Arrays.asList(strings));
    }

    /**
     * 转换成可用字符串
     * @param strings 待转换字符串集合
     * @return 可用字符串
     */
    public static List<String> toAvailable(Collection<String> strings) {
        return strings.stream()
                .map(CStrUtils::toAvailable)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
                ;
    }

    /**
     * 转换成可用字符串
     * @param string 待转换字符串
     * @param function 转换函数
     * @param <T> 泛型
     * @return 可用字符串
     */
    public static <T> T convertAvailable(String string, StringFunction<T> function) {

        val value = toAvailable(string);
        if(null == value) {
            return null;
        }

        return function.apply(value);
    }

    /**
     * 字符串连接
     * @param collection 集合
     * @param convert 转换函数
     * @param <T> 源对象泛型
     * @return 字符串
     */
    public <T> String join(Collection<T> collection, CFunction<T, String> convert) {
        return join(collection, convert, null);
    }

    /**
     * 字符串连接
     * @param collection 集合
     * @param convert 转换函数
     * @param separator 分隔符
     * @param <T> 源对象泛型
     * @return 字符串
     */
    public <T> String join(Collection<T> collection, CFunction<T, String> convert, String separator) {

        if(null == separator) {
            separator = DEFAULT_SEPARATOR;
        }

        val objects = CCollUtils.convert(collection, convert);
        return StrUtil.join(separator, objects);
    }

    /**
     * 获取默认字符串
     * @param strings 字符串数组
     * @return 默认字符串
     */
    public static String defaultString(String... strings) {

        if(ArrayUtil.isEmpty(strings)) {
            return null;
        }

        for(val string : strings) {
            if(StrUtil.isNotEmpty(string)) {
                return string;
            }
        }

        return null;
    }

    /**
     * 字符串最好一个数字自增
     * @param str 待自增字符串
     * @return 自增后的字符串
     */
    public String incrLastNum(String str) {

        if(StrUtil.isEmpty(str)) {
            return str;
        }

        val lastIndex = str.lastIndexOf("-");
        if(lastIndex < 0) {
            return str;
        }

        val numStr = StrUtil.subSuf(str, lastIndex + 1);
        if(StrUtil.isEmpty(numStr)) {
            return str;
        }

        try {
            val numInt = Integer.parseInt(numStr);
            return StrUtil.subPre(str, lastIndex) + "-" + (numInt + 1);
        } catch (Exception e) {
            log.error("字符串自增失败，str: {}", str, e);
        }

        return str;
    }

}
