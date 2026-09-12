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
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code UUID} / {@code simpleUUID}：生成带/不带 '-' 的随机 UUID 字符串（UUID v4，无序）——强能力</li>
 *   <li>{@code UUIDv7} / {@code simpleUUIDv7}：生成带/不带 '-' 的时间有序 UUID 字符串（UUID v7）——<b>可选能力</b></li>
 *   <li>{@code nextId}：生成雪花 ID（Long）（core 强能力，基于 hutool）</li>
 *   <li>{@code ulid}：生成 ULID 字符串（26 字符，Crockford Base32 大写，时间可排序）——<b>可选能力</b></li>
 *   <li>否则取类名大写字母去 {@code DO}）</li>
 *   <li>{@code nextIdWithPrefix}：生成带前缀（字符串/类前缀）的雪花 ID</li>
 *   <li>{@code getPrefixFromId}：从 ID 中解析前缀（数字前的字符）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>getPrefixFromId 空 ID/null</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>getPrefixFromId 无前缀（ID 以数字开头）</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>getPrefixFromId(String, convert) 无前缀</td>
 *     <td>返回 null，不调用 convert</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要 UUID（随机 v4 / 时间有序 v7）/ 雪花 ID / Nano ID 作为主键/业务 ID。</li>
 *   <li>需要按类生成带稳定前缀的雪花 ID（{@code @CBizId} 注解或类名约定）。</li>
 *   <li>需要较短、URL 友好、无序的 ID（Nano ID）。</li>
 *   <li>需要较短、URL 友好、时间可排序的 ID（ULID）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>前缀解析只识别"数字前的字符"；ID 中后续数字不影响前缀解析。</li>
 *   <li>getPrefix 按类缓存，运行期类上加/改 {@code @CBizId} 注解不会刷新（转换器静态注册，无动态需求）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>前缀缓存无失效机制：{@code @CBizId} 注解理论上静态不可变，风险低（沿用 CClassValue 约定）。</li>
 *   <li>{@code getPrefixFromId} 语义简单（数字前字符），不做复杂的前缀分隔规则。</li>
 *   <li>Nano ID 依赖 jnanoid（{@code com.aventrix.jnanoid:jnanoid} 2.0.0，MIT，零传递依赖）：该库为社区事实标准 Java nanoid 实现、多年稳定（版本未再更新），算法简单可靠；Nano ID 无序、不含时间信息，不适合需要排序/可追溯的 ID。</li>
 *   <li>ULID 依赖 ulid-creator（{@code com.github.f4b6a3:ulid-creator} 5.2.4，MIT，Java 8）。</li>
 *   <li>{@code UUIDv7}/{@code simpleUUIDv7}、{@code nanoId}、{@code ulid} 为<b>可选能力</b>：若使用方未引入对应依赖却调用对应方法，会抛 {@code NoClassDefFoundError}；未调用对应方法时不影响 {@code UUID}/{@code simpleUUID}/{@code nextId}/{@code getPrefix} 等强能力（生成器懒加载）。判断 ID 是否需要可排序时选型：UUID v7/雪花（时间有序）、UUID v4/Nano ID（无序）、ULID（短时间有序）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>可选依赖说明（重要）</b></p>
 * <p>以下 ID 生成能力对应的实现库均为 <b>Maven {@code optional} 可选依赖</b>（默认<b>不会</b>传递给使用 ctool4j-core 的下游）， 使用方用到对应方法时须在自己的 pom 显式引入：</p>
 * <ul>
 *   <li>{@code UUIDv7} / {@code simpleUUIDv7}（UUID v7）依赖 {@code java-uuid-generator}</li>
 *   <li>{@code ulid} 依赖 {@code ulid-creator}</li>
 * </ul>
 * <pre>
 * &lt;!-- 使用 CIdUtils.UUIDv7/simpleUUIDv7 时引入 --&gt;
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.fasterxml.uuid&lt;/groupId&gt;
 *     &lt;artifactId&gt;java-uuid-generator&lt;/artifactId&gt;
 * &lt;/dependency&gt;
 * &lt;!-- 使用 CIdUtils.nanoId 时引入 --&gt;
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.aventrix.jnanoid&lt;/groupId&gt;
 *     &lt;artifactId&gt;jnanoid&lt;/artifactId&gt;
 * &lt;/dependency&gt;
 * &lt;!-- 使用 CIdUtils.ulid 时引入 --&gt;
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.github.f4b6a3&lt;/groupId&gt;
 *     &lt;artifactId&gt;ulid-creator&lt;/artifactId&gt;
 * &lt;/dependency&gt;
 * </pre>
 * <p><b>UUID 生成（UUID / simpleUUID）</b></p>
 * <ul>
 *   <li>默认实现使用 hutool {@code IdUtil.fastUUID()}（随机 UUID v4，长度 36 位、带 '-'），
 *   随机源为 {@code ThreadLocalRandom}（非 {@code SecureRandom}）——<b>无序</b>、不携带时间信息，
 *   换取更短的生成耗时；不需要可排序时选它。</li>
 *   <li>hutool 为 ctool4j-core 的<b>强依赖</b>（{@code nextId} 亦用之），故 {@code UUID}/{@code simpleUUID} 为强能力，
 *   不涉及可选依赖与懒加载。</li>
 *   <li>输出为带 '-' 的标准 UUID 字符串（36 位）；{@code simpleUUID} 在其基础上去除 '-'（32 位）。</li>
 *   <li>需要<b>时间有序</b>的 UUID 时用 {@code UUIDv7} / {@code simpleUUIDv7}（见下）。</li>
 * </ul>
 * <p><b>UUID v7 生成（UUIDv7 / simpleUUIDv7）</b></p>
 * <ul>
 *   <li>使用 {@code com.fasterxml.uuid.Generators.timeBasedEpochGenerator()} 生成 <b>UUID v7</b>（时间有序），</li>
 *   <li>相比 v4 随机 UUID，索引友好、可排序，且无需维护状态即可保证全局唯一。</li>
 *   <li>生成器以类级字段 {@code UUID_V7_GENERATOR} 懒加载缓存复用，双重检查锁样板由 {@code CLazyRef} 统一封装，
 *   避免每次调用重复创建实例、也避免在工具类里散落 DCL 代码；</li>
 *   <li>仅在调用 {@code UUIDv7}/{@code simpleUUIDv7} 时才初始化，类加载不触发；该生成器本身<b>线程安全</b></li>
 *   <li>（内部 {@code synchronized} 保护时间戳/熵状态），可安全并发共享。</li>
 *   <li>字段声明为 {@code CLazyRef&lt;Object&gt;} + lambda 且<b>返回类型擦除为 {@code Object}</b>：{@code CLazyRef.of(() -&gt; (Object) Generators.timeBasedEpochGenerator())}，</li>
 *   <li>擦除是<b>必需</b>的（非可选优化）：若声明为 {@code CLazyRef&lt;TimeBasedEpochGenerator&gt;} 且 lambda 返回具体类型，</li>
 *   <li>lambda 引导生成的 {@code instantiatedMethodType} 会引用该可选类型，导致 {@code CIdUtils} <b>类初始化即加载</b></li>
 *   <li>java-uuid-generator，在未引入该依赖的模块（如 {@code ctool4j-mybatis-base}）中抛 {@code NoClassDefFoundError}。</li>
 *   <li>输出为带 '-' 的标准 UUID 字符串（36 位）；{@code simpleUUIDv7} 在其基础上去除 '-'（32 位）。</li>
 * </ul>
 * <p><b>前缀计算规则（CLASS_PREFIX）</b></p>
 * <ul>
 *   <li>优先取 {@code @CBizId} 注解的 {@code value}（非空白时）。</li>
 *   <li>无注解值回退取类简单名的所有大写字母并去除 {@code DO} 子串（如 {@code CIdUtilsTests} → {@code CIUT}）。</li>
 *   <li>按类缓存（{@code CClassValue}，基于 {@code java.lang.ClassValue}，线程安全、按类弱关联），避免重复计算。</li>
 * </ul>
 *
 * @since 2025/11/27
 * @version 1.1
 */
@UtilityClass
public class CIdUtils {

    /**
     * UUID v7 生成器：懒加载（{@link CLazyRef} 封装双重检查锁，线程安全）。
     * 不在此急切初始化，避免类加载时依赖可选的 java-uuid-generator；
     * 仅调用 UUIDv7/simpleUUIDv7 时才初始化，UUID/nextId/getPrefix 等强能力方法不受影响。
     * <p>
     * 注意：泛型与 lambda 返回类型必须擦除为 {@link Object}（{@code (Object) ...}）。
     * 若声明为 {@code CLazyRef<TimeBasedEpochGenerator>} 且 lambda 返回具体类型，lambda 引导生成的
     * {@code instantiatedMethodType} 会引用 {@link TimeBasedEpochGenerator}，导致类初始化阶段即加载该
     * 可选依赖、在未引入 java-uuid-generator 的模块中抛 {@code NoClassDefFoundError}（详见源码 javadoc 与该功能设计文档）。
     * </p>
     * <p>
     * 此处刻意使用 lambda 而非方法引用（{@code Generators::timeBasedEpochGenerator}）：方法引用在类初始化
     * 阶段即需解析目标类型，会提前加载可选依赖，与上述延迟目标冲突；故抑制"可替换为方法引用"告警。
     * </p>
     */
    @SuppressWarnings("all")
    private final CLazyRef<Object> UUID_V7_GENERATOR =
        CLazyRef.of(() -> Generators.timeBasedEpochGenerator());

    /**
     * 生成 UUID 字符串（随机 UUID v4，36 位、带 '-'）
     * <ul>
     *   <li>封装 hutool {@code IdUtil.fastUUID()}：随机源为 {@code ThreadLocalRandom}（非 {@code SecureRandom}），</li>
     *   <li>版本位为 4、variant 位为 {@code 8/9/a/b}；<b>无序</b>、不含时间信息，生成速度快。</li>
     *   <li><b>易误用点</b>：随机源非密码学安全（{@code ThreadLocalRandom}），<b>不得</b>用于令牌、密钥、会话 ID 等安全敏感场景；</li>
     *   <li>安全敏感场景须使用密码学安全的随机源（NIST SP 800-63B），或改用 {@link #UUIDv7()}（其熵源为共享 {@code SecureRandom}）。</li>
     *   <li>需要可排序的 UUID 时改用 {@link #UUIDv7()}。</li>
     * </ul>
     *
     * @return 随机 UUID 字符串（36 位、带 '-'）
     */
    public String UUID() {
        return IdUtil.fastUUID();
    }

    /**
     * 生成不带 '-' 的随机 UUID 字符串（随机 UUID v4 去连字符，32 位）
     *
     * @return 不带 '-' 的随机 UUID 字符串（32 位）
     */
    public String simpleUUID() {
        return UUID()
            .replace("-", "")
            ;
    }

    /**
     * 生成 UUID v7 字符串（时间有序，36 位、带 '-'）
     * <ul>
     *   <li>{@code UUIDv7()} 取 {@code get()} 后强转为 {@code TimeBasedEpochGenerator} 再 {@code generate()}。</li>
     *   <li>依赖 Maven {@code optional} 的可选依赖 {@code java-uuid-generator}：未引入该依赖的使用方调用本方法会抛</li>
     *   <li>{@code NoClassDefFoundError}（生成器懒加载，未调用不影响其他能力）。</li>
     * </ul>
     *
     * @return UUID v7 字符串（36 位、带 '-'，时间有序）
     */
    public String UUIDv7() {
        return ((TimeBasedEpochGenerator) UUID_V7_GENERATOR.get()).generate()
            .toString()
            ;
    }

    /**
     * 生成不带 '-' 的 UUID v7 字符串（32 位，时间有序）
     * <p>同样依赖可选依赖 {@code java-uuid-generator}，未引入时调用抛 {@code NoClassDefFoundError}。</p>
     *
     * @return 不带 '-' 的 UUID v7 字符串（32 位）
     */
    public String simpleUUIDv7() {
        return UUIDv7()
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
     * <h2>前缀截取（getPrefix(Class, length)）</h2>
     * <ul>
     *   <li>取 {@code prefix.substring(0, min(length, prefix.length()))}：length 超前缀长度时返回完整前缀。</li>
     * </ul>
     * <ul>
     *   <li>{@code getPrefix(Class)} / {@code getPrefix(Class, length)}：计算类的 ID 前缀（优先 {@code @CBizId} 注解值，</li>
     * </ul>
     *
     * @param clazz 类
     * @return ID 前缀*/
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
     * <h2>前缀解析（getPrefixFromId）</h2>
     * <ul>
     *   <li>从 ID 开头扫描到第一个数字字符，取之前的字符为前缀；无前缀（ID 以数字开头或为空）返回 null。</li>
     * </ul>
     *
     * @param id      ID
     * @param convert 转换函数
     * @param <T>     转换结果类型
     * @return 转换后的前缀，无前缀时返回 null*/
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
     * <h2>Nano ID 生成（nanoId / nanoId(size)）</h2>
     * <ul>
     *   <li>复用<b>开源 jnanoid</b>（{@code com.aventrix.jnanoid:jnanoid}）的 {@code NanoIdUtils}：</li>
     *   <li>{@code randomNanoId()}：默认 21 字符、URL 安全字母表 {@code A-Za-z0-9_-}、{@code SecureRandom}。</li>
     *   <li>{@code randomNanoId(Random, char[], int)}：自定义随机源/字母表/长度，nanoId(size) 用默认 SecureRandom 与默认字母表。</li>
     *   <li>相比 UUID（36 位）更短、URL 友好、无序；适合需要较短但唯一性足够（21 字符）的场景。</li>
     *   <li>{@code SecureRandom}（{@code NanoIdUtils.DEFAULT_NUMBER_GENERATOR}）线程安全，可并发共享。</li>
     *   <li>依赖为可选（optional）。</li>
     * </ul>
     * <ul>
     *   <li>{@code nanoId} / {@code nanoId(size)}：生成 Nano ID 字符串（默认 21 字符，URL 安全字母表，无序）——<b>可选能力</b></li>
     *   <li>{@code nanoId} / {@code nanoId(size)} 依赖 {@code jnanoid}</li>
     * </ul>
     *
     * @return Nano ID 字符串*/
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
     * <h2>ULID 生成（ulid）</h2>
     * <ul>
     *   <li>复用<b>开源 ulid-creator</b>（{@code com.github.f4b6a3:ulid-creator}）的 {@code UlidCreator.getMonotonicUlid()}（标准加密 ULID）。</li>
     *   <li>格式：26 字符、大写 Crockford Base32（{@code 0-9A-HJKMNP-TV-Z}，不含 {@code I/L/O/U}），128 位 = 48 位毫秒时间戳 + 80 位随机。</li>
     *   <li>特性：字符串字典序即生成时间序（可排序）、<b>同一毫秒内单调递增</b>（严格保证排序）、前 10 字符时间戳可解码回毫秒、短于 UUID。</li>
     *   <li>选用 {@code getMonotonicUlid()} 而非 {@code getUlid()}：后者同一毫秒内随机后缀不保证递增（同毫秒并发生成可能乱序），</li>
     *   <li>与"时间可排序"的设计意图不符；monotonic 版确保时间戳相同后缀递增，真正满足可排序。</li>
     *   <li>相比 Nano ID：同为 URL 友好但<b>时间可排序</b>；适合需"短 + 可排序"的 ID。</li>
     *   <li>依赖为可选（optional）。</li>
     * </ul>
     *
     * @return ULID 字符串*/
    public String ulid() {
        return UlidCreator.getMonotonicUlid()
            .toString();
    }

}
