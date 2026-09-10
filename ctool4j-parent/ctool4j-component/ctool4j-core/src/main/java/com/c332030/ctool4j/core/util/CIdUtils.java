package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.annotation.CBizId;
import com.c332030.ctool4j.definition.function.StringFunction;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

/**
 * <p>
 * Description: CIdUtils
 * </p>
 *
 * @since 2025/11/27
 * @see "doc/design/core/CIdUtils.adoc"
 * @see "doc/design/core/CIdUtilsTests.adoc"
 */
@UtilityClass
public class CIdUtils {

    /**
     * UUID v7 生成器：懒加载（{@link CLazyRef} 封装双重检查锁，线程安全）。
     * 不在此急切初始化，避免类加载时依赖可选的 java-uuid-generator；
     * 仅调用 UUID/simpleUUID 时才初始化，nextId/getPrefix 等强能力方法不受影响。
     */
    private final CLazyRef<TimeBasedEpochGenerator> UUID_V7_GENERATOR =
        CLazyRef.of(() -> Generators.timeBasedEpochGenerator());

    /**
     * 生成 UUID 字符串
     *
     * @return UUID 字符串
     */
    public String UUID() {
        return UUID_V7_GENERATOR.get().generate()
            .toString()
            ;
    }

    /**
     * 没有 '-' 的uuid
     *
     * @return 没有 '-' 的 UUID 字符串
     */
    public String simpleUUID() {
        return UUID()
            .replace("-", "")
            ;
    }

    /**
     * 生成雪花 ID
     *
     * @return 雪花 ID
     */
    public Long nextId() {
        return IdUtil.getSnowflakeNextId();
    }

    private final CClassValue<String> CLASS_PREFIX = CClassValue.of(type -> {

        val idPrefixAnno = type.getAnnotation(CBizId.class);
        val idPrefix = CObjUtils.convert(idPrefixAnno, CBizId::value);
        if(StrUtil.isNotBlank(idPrefix)) {
            return idPrefix;
        }

        return type.getSimpleName()
                .replaceAll("[^A-Z]|DO", "");
    });

    /**
     * 获取类 ID 前缀（优先注解值，否则取类名首字母）
     *
     * @param clazz 类
     * @return ID 前缀
     */
    public String getPrefix(Class<?> clazz) {
        return CLASS_PREFIX.get(clazz);
    }

    /**
     * 获取类 ID 前缀（截取指定长度）
     *
     * @param clazz  类
     * @param length 长度
     * @return ID 前缀
     */
    public String getPrefix(Class<?> clazz, int length) {

        val prefix = getPrefix(clazz);
        return prefix.substring(0, Math.min(length, prefix.length()));
    }

    /**
     * 生成带前缀的雪花 ID
     *
     * @param prefix 前缀
     * @return 带前缀的雪花 ID
     */
    public String nextIdWithPrefix(String prefix) {
        return prefix + nextId();
    }

    /**
     * 生成带类 ID 前缀的雪花 ID
     *
     * @param clazz 类
     * @return 带前缀的雪花 ID
     */
    public String nextIdWithPrefix(Class<?> clazz) {
        return nextIdWithPrefix(getPrefix(clazz));
    }

    /**
     * 生成带类 ID 前缀的雪花 ID（前缀截取指定长度）
     *
     * @param clazz  类
     * @param length 前缀长度
     * @return 带前缀的雪花 ID
     */
    public String nextIdWithPrefix(Class<?> clazz, int length) {
        return nextIdWithPrefix(getPrefix(clazz, length));
    }

    /**
     * 从 ID 中解析前缀并转换
     *
     * @param id      ID
     * @param convert 转换函数
     * @param <T>     转换结果类型
     * @return 转换后的前缀，无前缀时返回 null
     */
    public <T> T getPrefixFromId(String id, StringFunction<T> convert) {

        val prefix = getPrefixFromId(id);
        if(StrUtil.isEmpty(prefix)){
            return null;
        }

        return convert.apply(prefix);
    }

    /**
     * 从 ID 中解析前缀（数字前的字符）
     *
     * @param id ID
     * @return 前缀，无前缀时返回 null
     */
    public String getPrefixFromId(String id) {

        if(StrUtil.isEmpty(id)) {
            return null;
        }

        var index = 0;
        while (index < id.length() && !CharUtil.isNumber(id.charAt(index))) {
            index++;
        }

        if(index == 0) {
            return null;
        }

        return id.substring(0, index);
    }

    /**
     * 生成 Nano ID（默认 21 字符，URL 安全字母表 A-Za-z0-9_-，SecureRandom）
     * <p>封装开源 jnanoid 的 {@link NanoIdUtils#randomNanoId()}，比 UUID 更短、更友好。</p>
     *
     * @return Nano ID 字符串
     */
    public String nanoId() {
        return NanoIdUtils.randomNanoId();
    }

    /**
     * 生成指定长度的 Nano ID（URL 安全字母表 A-Za-z0-9_-，SecureRandom）
     *
     * @param size 长度（需 &gt; 0）
     * @return Nano ID 字符串
     */
    public String nanoId(int size) {
        return NanoIdUtils.randomNanoId(
            NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
            NanoIdUtils.DEFAULT_ALPHABET,
            size
        );
    }

    /**
     * 生成 ULID（26 字符，Crockford Base32 大写，128 位 = 48 位毫秒时间戳 + 80 位随机）
     * <p>封装开源 ulid-creator 的 {@link UlidCreator#getMonotonicUlid()}：字符串字典序即生成时间序（时间可排序），
     * 同一毫秒内单调递增，严格保证可排序；可用于需"短 + 可排序"的 ID 场景。
     * 依赖为可选（optional），使用方需引入 ulid-creator。</p>
     *
     * @return ULID 字符串
     */
    public String ulid() {
        return UlidCreator.getMonotonicUlid()
            .toString();
    }

}
