package com.c332030.ctool4j.core.log;

import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.cache.impl.CRefClassValue;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.util.CArrUtils;
import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.core.util.CSet;
import com.c332030.ctool4j.definition.annotation.CJsonLog;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.definition.model.result.ICBaseResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.InputStreamSource;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CLogUtils
 * </p>
 *
 * <p>处理并记录日志文件</p>
 * <p>本类自身保留 slf4j 原生 @Slf4j 而非 @CustomLog（原因详见设计文档）</p>
 * <h2>能力目录</h2>
 * <p>{@code CLogUtils} 为日志工具类，提供：</p>
 * <ul>
 *   <li>JSON 化判断与配置：{@code isJsonLog} / {@code setJsonLog} / {@code addJsonLogDomainPackage} / {@code addJsonLogAnnotations} 等</li>
 *   <li>参数处理：{@code toLogArgs} / {@code getSupplierArgs}</li>
 *   <li>可打印数据处理：{@code getPrintAble} / {@code getPrintAbleString} / {@code isPrintAble}</li>
 *   <li>常量：{@code LOGGING_LEVEL} / {@code LOGGING_LEVEL_PREFIX}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>isJsonLog 枚举 / 命中不转集合</td>
 *     <td>返回 false</td>
 *   </tr>
 *   <tr>
 *     <td>isJsonLog 命中转集合 / 非 JDK 类</td>
 *     <td>返回 true</td>
 *   </tr>
 *   <tr>
 *     <td>toLogArgs 转 JSON 失败</td>
 *     <td>禁用该类型转换，记录错误日志</td>
 *   </tr>
 *   <tr>
 *     <td>getPrintAble null</td>
 *     <td>返回 "[null]"</td>
 *   </tr>
 *   <tr>
 *     <td>getPrintAble 不可打印类型</td>
 *     <td>返回 "[类名]" 等占位</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>统一日志参数 JSON 化、敏感数据脱敏（CLogBlob）、可打印数据转换。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>JSON 化规则依赖包名/注解/父类启发式判断，复杂类型需手动 {@code setJsonLog} 指定。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>转 JSON 失败即禁用该类型转换，避免重复异常（核心取舍）。</li>
 *   <li>各类集合基于 CopyOnWriteArraySet 线程安全，支持运行期扩展规则。</li>
 *   <li>本类自身使用 slf4j 原生 {@code @Slf4j} 而非 {@code @CustomLog}：{@code @CustomLog} 生成的 {@code log} 字段初始化会调用</li>
 *   <li>初始化，{@code log} 先于 {@code LOGS} 就绪时触发自引用 NPE。故本类（CLog 的底层实现）保留 {@code @Slf4j}，</li>
 *   <li>不影响后端无关性（CLog 内部本就使用 slf4j 门面）。</li>
 * </ul>
 *
 * @since 1.0
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class CLogUtils {

    /**
     * 日志级别配置前缀
     */
    public final String LOGGING_LEVEL = "logging.level";

    /**
     * 日志级别配置完整前缀
     */
    public final String LOGGING_LEVEL_PREFIX = LOGGING_LEVEL + ".";

    /**
     * 日志缓存
     */
    private static final ClassValue<CLog> LOGS = new ClassValue<CLog>() {
        /**
         * 为类创建日志对象
         *
         * @param type 类
         * @return 日志对象
         */
        @Override
        protected CLog computeValue(@NonNull Class<?> type) {
            return new CLog(type);
        }
    };

    /**
     * 获取日志
     * <ul>
     *   <li>日志获取：{@code getLog(name)} / {@code getLog(Class)}（按类缓存 CLog）</li>
     *   <li>{@code getLog(CLogUtils.class)}，而 {@code getLog} 依赖本类 {@code LOGS}（ClassValue）静态字段，静态字段按声明顺序</li>
     * </ul>
     *
     * @param name 日志名称
     * @return CLog
     */
    public CLog getLog(String name) {
        return new CLog(name);
    }

    /**
     * 获取日志
     * @param clazz 获取日志
     * @return CLog
     */
    public CLog getLog(Class<?> clazz) {
        return LOGS.get(clazz);
    }

    /**
     * 不转 json 的包名
     */
    private static final Set<String> NOT_JSON_LOG_DOMAIN_PACKAGE = new CopyOnWriteArraySet<>(CSet.of(
            ".sun.",
            ".apache."
    ));

    /**
     * 转 json 的包名
     */
    private static final Set<String> JSON_LOG_DOMAIN_PACKAGE = new CopyOnWriteArraySet<>(CSet.of(
            ".config.",
            ".entity.",
            ".model.",
            ".pojo.",
            ".dto.",
            ".param.",
            ".query.",
            ".req.",
            ".rsp.",
            ".request.",
            ".response.",
            ".domain."
    ));

    /**
     * 添加转 json 的包名
     * @param domainPackage 转 json 的包名
     */
    public void addJsonLogDomainPackage(String domainPackage) {
        JSON_LOG_DOMAIN_PACKAGE.add(domainPackage);
    }

    /**
     * 转 json 的注解
     */
    private static final Set<Class<? extends Annotation>> JSON_LOG_ANNOTATIONS = new CopyOnWriteArraySet<>(CSet.of(
            CJsonLog.class,
            ConfigurationProperties.class
    ));

    /**
     * 添加转 json 的注解
     * @param tClass 转 json 的注解
     */
    public void addJsonLogAnnotations(Class<? extends Annotation> tClass) {
        JSON_LOG_ANNOTATIONS.add(tClass);
    }

    /**
     * 不转 json 的父类
     */
    private static final Set<Class<?>> NOT_JSON_LOG_SUPERCLASSES = new CopyOnWriteArraySet<>(CSet.of(
            DataSource.class,
            InputStream.class,
            OutputStream.class,
            InputStreamSource.class,
            Throwable.class
    ));

    /**
     * 添加不转 json 的父类
     * @param tClass 不转 json 的父类
     */
    public void addNotJsonLogSuperclasses(Class<?> tClass) {
        NOT_JSON_LOG_SUPERCLASSES.add(tClass);
    }

    /**
     * 转 json 的父类
     */
    private static final Set<Class<?>> JSON_LOG_SUPERCLASSES = new CopyOnWriteArraySet<>(CSet.of(
            ICBaseResult.class,
            Collection.class,
            Map.class
    ));

    /**
     * 添加转 json 的父类
     * @param tClass 转 json 的父类
     */
    public void addJsonLogSuperclasses(Class<?> tClass) {
        JSON_LOG_SUPERCLASSES.add(tClass);
    }

    /**
     * 是否转换 json 缓存
     */
    public static final CRefClassValue<Boolean> JSON_LOG_CLASS_VALUE = CRefClassValue.of(
            type -> {

                if (type.isEnum()) {
                    return false;
                }

                // 配置类型 or 报错类型不转 json
                for (val clazz : NOT_JSON_LOG_SUPERCLASSES) {
                    if (clazz.isAssignableFrom(type)) {
                        return false;
                    }
                }

                for (val domainPackage : NOT_JSON_LOG_DOMAIN_PACKAGE) {
                    if (type.getName().contains(domainPackage)) {
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
                    if (type.getName().contains(domainPackage)) {
                        return true;
                    }
                }

                for (val clazz : JSON_LOG_ANNOTATIONS) {
                    if (CClassUtils.isAnnotationPresent(type, clazz)) {
                        return true;
                    }
                }

                // 基础数据类型不转 json
                return !CClassUtils.isJdkClass(type);
            }
    );

    /**
     * 设置是否转 json
     * @param types types
     * @param value value
     */
    public void setJsonLog(Collection<Class<?>> types, boolean value) {
        types.forEach(type -> JSON_LOG_CLASS_VALUE.set(type, value));
    }

    /**
     * 设置是否转 json
     * @param type type
     * @param value value
     */
    public void setJsonLog(Class<?> type, boolean value) {
        setJsonLog(Collections.singletonList(type), value);
    }

    /**
     * 是否能转 json
     *
     * <h2>JSON 化判断（isJsonLog）</h2>
     * <ul>
     *   <li>基于 {@code CRefClassValue} 缓存按类判断结果，规则优先级：</li>
     *   <li>枚举不转；命中 {@code NOT_JSON_LOG_SUPERCLASSES}（DataSource/InputStream/OutputStream/Throwable 等）不转；</li>
     *   <li>命中 {@code NOT_JSON_LOG_DOMAIN_PACKAGE}（.sun./.apache.）不转。</li>
     *   <li>命中 {@code JSON_LOG_SUPERCLASSES}（ICBaseResult/Collection/Map）转。</li>
     *   <li>命中 {@code JSON_LOG_DOMAIN_PACKAGE}（.config./.entity./.model. 等）转。</li>
     *   <li>命中 {@code JSON_LOG_ANNOTATIONS}（CJsonLog/ConfigurationProperties）转。</li>
     *   <li>兜底：JDK 类不转，其余转。</li>
     *   <li>各类"添加"方法可动态扩展集合。</li>
     * </ul>
     *
     * @param type type
     * @return boolean*/
    public boolean isJsonLog(Class<?> type) {
        return JSON_LOG_CLASS_VALUE.get(type);
    }

    /**
     * 获取参数 Supplier 的结果
     * @param suppliers Supplier[]
     * @return Object[]
     */
    public Object[] getSupplierArgs(Supplier<Object>[] suppliers) {

        if (ArrayUtil.isEmpty(suppliers)) {
            return CArrUtils.EMPTY_OBJECT_ARRAY;
        }

        return Arrays.stream(suppliers)
                .map(Supplier::get)
                .toArray();
    }

    /**
     * 转为日志可打印参数（默认不打印 null）
     * <p>将可 json 化的参数元素替换为 JSON 字符串（日志专用 mapper：不序列化 null +
     * 标注 CLogBlob 的字段输出 &lt;BLOB&gt; 占位符），返回新数组，不修改调用方入参；null 元素保持不动</p>
     *
     * <h2>参数 JSON 化（toLogArgs）</h2>
     * <ul>
     *   <li>对可 JSON 化参数替换为 JSON 字符串（日志专用 mapper：不序列化 null + CLogBlob 字段输出 {@code &lt;BLOB&gt;} 占位符）；</li>
     *   <li>返回新数组不修改调用方入参；null 元素保持不动。</li>
     *   <li>转 JSON 失败则禁用该类型转换并记录错误日志（有意设计取舍，避免每次日志都抛异常）。</li>
     * </ul>
     *
     * @param args 源参数
     * @return 新数组*/
    public Object[] toLogArgs(Object[] args) {

        if (ArrayUtil.isEmpty(args)) {
            return args;
        }

        val result = new Object[args.length];
        for (int i = 0; i < args.length; i++) {

            var arg = args[i];
            if (null != arg) {
                val argType = arg.getClass();
                if (isJsonLog(argType)) {
                    try {
                        arg = CJsonUtils.toJsonLog(arg);
                    } catch (Exception e) {
                        // 不少类型不支持转 json，虽已适配一部分但仍无法穷尽；
                        // 转 json 失败即禁用该类型的 json 转换（改由手动兼容），
                        // 避免每次日志都触发异常报错，属有意设计取舍
                        setJsonLog(argType, false);
                        log.error("转 json 失败，禁用 json 转换：{}", argType, e);
                    }
                }
            }
            result[i] = arg;
        }
        return result;
    }

    /**
     * 可以打印的父类
     */
    final Set<Class<?>> PRINT_ABLE_SUPERCLASSES = new CopyOnWriteArraySet<>(CSet.of(
        CharSequence.class,
        Number.class,
        Date.class
    ));

    /**
     * 类是否可打印在日志里的缓存
     */
    final CClassValue<Boolean> PRINT_ABLE_CLASS_VALUE = CClassValue.of(type -> {

        if (type.isEnum()) {
            return true;
        }

        for (val clazz : PRINT_ABLE_SUPERCLASSES) {
            if (clazz.isAssignableFrom(type)) {
                return true;
            }
        }

        return CClassUtils.isBasicClass(type);
    });

    /**
     * 类是否可打印在日志里
     * @param type type
     * @return boolean
     */
    public boolean isPrintAble(Class<?> type) {

        if(isJsonLog(type)) {
            return true;
        }

        return PRINT_ABLE_CLASS_VALUE.get(type);
    }

    /**
     * 转换函数缓存
     */
    final CClassValue<CFunction<Object, Object>> PRINT_ABLE_CONVERT_FUNCTION = CClassValue.of(type -> {

        if(byte[].class.isAssignableFrom(type)) {
            return value -> "[byte[" + ((byte[])value).length + "]]";
        }

        if(MultipartFile.class.isAssignableFrom(type)) {
            return value -> {
                val file = (MultipartFile) value;
                return file.getOriginalFilename() + ":" + file.getSize();
            };
        }

        return e -> "[" + type.getName() + "]";
    });

    /**
     * 获取可打印的数据
     *
     * <h2>可打印数据（getPrintAble）</h2>
     * <ul>
     *   <li>可 JSON 化或基础可打印类型（CharSequence/Number/Date/枚举/基本类）原样返回；否则经转换函数</li>
     *   <li>（byte[] → {@code [byte[n]]}、MultipartFile → {@code 文件名:大小}、其他 → {@code [类名]}）。</li>
     * </ul>
     *
     * @param value 源数据
     * @return 可打印的数据*/
    public Object getPrintAble(Object value) {

        if (null == value) {
            return "[null]";
        }

        val argType = value.getClass();
        if (isPrintAble(argType)) {
            return value;
        }

        return PRINT_ABLE_CONVERT_FUNCTION.get(argType)
            .apply(value);
    }

    /**
     * 获取可打印的字符串
     * @param value 源数据
     * @return 字符串
     */
    public String getPrintAbleString(Object value) {

        value = getPrintAble(value);
        if(String.class == value.getClass()) {
            return (String) value;
        }

        return CJsonUtils.toJsonLog(value);
    }

}
