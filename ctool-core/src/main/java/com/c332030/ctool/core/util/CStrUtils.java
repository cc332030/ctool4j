package com.c332030.ctool.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool.core.function.StringFunction;
import com.c332030.ctool.core.function.ToStringFunction;
import com.google.common.base.CaseFormat;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.helpers.MessageFormatter;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CStrUtils
 * </p>
 *
 * @since 2024/3/15
 */
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

    public String formatLookup(String key, StringFunction<?> stringLookup) {
        return formatLookup(key, stringLookup, null);
    }

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
     * @param defaultValue 默认值
     * @return formatted string
     */
    public String formatByObject(String template, Object object, String defaultValue) {
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
     * 大写下划线 转 报文头：TRACE_ID -> Trace-Id
     */
    public String upperUnderscoreToHeaderName(String value) {
        val splits = value.split("_");
        return Arrays.stream(splits)
                .map(CStrUtils::upperUnderscoreToUpperCamel)
                .collect(Collectors.joining("-"));
    }

    /**
     * 大写下划线 转 驼峰-首字母大写
     * Upper underscore to lower camel
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
     * Upper underscore to upper camel
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
     * Upper underscore to lower camel
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
     */
    public String getAllUpperChar(String value) {
        return value.replaceAll("[^A-Z]", "");
    }

    public String trim(String string) {
        return string == null ? null : string.trim();
    }

    public String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    public <T> T toStringThenConvert(Object obj, StringFunction<T> function) {
        return function.apply(toString(obj));
    }

    public <T> String toString(T t, ToStringFunction<T> function) {
        return toString(function.apply(t));
    }

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

    public List<String> splitToList(String value, String separator) {
        return split(value, separator, Function.identity(), CCollUtils::newList);
    }
    public <T> List<T> splitToList(String value, Function<String, T> convert) {
        return split(value, null, convert, CCollUtils::newList);
    }
    public List<String> splitToList(String value) {
        return splitToList(value, Function.identity());
    }
    public List<Integer> splitToIntegerList(String value) {
        return splitToList(value, Integer::parseInt);
    }
    public List<Long> splitToLongList(String value) {
        return splitToList(value, Long::parseLong);
    }

    public Set<String> splitToSet(String value, String separator) {
        return split(value, separator, Function.identity(), CCollUtils::newLinkedSet);
    }
    public <T> Set<T> splitToSet(String value, Function<String, T> convert) {
        return split(value, null, convert, CCollUtils::newLinkedSet);
    }
    public Set<String> splitToSet(String value) {
        return splitToSet(value, Function.identity());
    }
    public Set<Integer> splitToIntegerSet(String value) {
        return splitToSet(value, Integer::parseInt);
    }
    public static Set<Long> splitToLongSet(String value) {
        return splitToSet(value, Long::parseLong);
    }

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

    public static <K, V> Map<K, V> splitToMap(String string, Function<String, K> kFunction, Function<String, V> vFunction) {
        return splitToMap(string, null, null, kFunction, vFunction, CCollUtils::newLinkedMap);
    }

    public static Map<String, String> splitToMap(String string) {
        return splitToMap(string, Function.identity(), Function.identity());
    }

    public static String concat(String separator, String... strings) {

        if(ArrayUtil.isEmpty(strings)) {
            return null;
        }

        return Arrays.stream(strings)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.joining(separator));
    }

    public static <T> T notEmptyThenGet(String value, Supplier<T> supplier) {
        return CObjUtils.ifThenGet(StrUtil.isNotEmpty(value), supplier);
    }

    public static <T> T notBlackThenGet(String value, Supplier<T> supplier) {
        return CObjUtils.ifThenGet(StrUtil.isNotBlank(value), supplier);
    }
    public static <R> R convertNotEmpty(String o, Function<String, R> function) {
        return StrUtil.isNotEmpty(o) ? function.apply(o) : null;
    }

    public static <R> R convertNotBlank(String o, Function<String, R> function) {
        return StrUtil.isNotBlank(o) ? function.apply(o) : null;
    }

    public static Character charAt(String string, int index) {

        if(index < 0 || index >= string.length()) {
            return null;
        }
        return string.charAt(index);
    }

    public static boolean isAvailable(String string) {
        return StrUtil.isNotEmpty(toAvailable(string));
    }

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

    public static List<String> toAvailable(String[] strings) {
        return toAvailable(Arrays.asList(strings));
    }

    public static List<String> toAvailable(Collection<String> strings) {
        return strings.stream()
                .map(CStrUtils::toAvailable)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
                ;
    }

    public static <T> T convertAvailable(String string, StringFunction<T> function) {

        val value = toAvailable(string);
        if(null == value) {
            return null;
        }

        return function.apply(value);
    }

    public <T> String join(Collection<T> collection, Function<T, String> convert) {
        return join(collection, convert, null);
    }

    public <T> String join(Collection<T> collection, Function<T, String> convert, String separator) {

        if(null == separator) {
            separator = DEFAULT_SEPARATOR;
        }

        val objects = CCollUtils.convert(collection, convert);
        return StrUtil.join(separator, objects);
    }

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

}
