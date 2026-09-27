package com.c332030.ctool4j.mybatis.handler;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.definition.constant.CConstants;
import lombok.CustomLog;
import lombok.val;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;

/**
 * <p>
 * Description: CCommaCollectionTypeHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCommaCollectionTypeHandler} 是"集合 ↔ 逗号分割字符串"的 MyBatis 类型处理器基类（如 {@code 1,2,3}）。</p>
 * <ul>
 *   <li>{@code toText(Collection)}：集合转逗号分割字符串（静态；供 wrapper 的 set 复用）</li>
 *   <li>{@code setNonNullParameter} / {@code getNullableResult}：MyBatis TypeHandler 的写读两个端点</li>
 *   <li><b>元素类型自动解析</b>：由子类声明的泛型实参确定，子类不再实现任何转换</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>元素类型自动解析</b>：构造时沿类层级读取 {@code extends CCommaCollectionTypeHandler<X>} 的实参 X，
 *   转换由 {@code Convert.convert(X, value, null)} 通用完成——<b>新增元素类型只需一个"只声明类型"的子类</b>，
 *   不必为每种类型各写一份转换实现。</li>
 *   <li><b>为何仍需子类</b>：MyBatis-Plus 的 {@code @TableField(typeHandler = XxxHandler.class)} 要求
 *   <b>具体类</b>且需无参构造，注解参数无法传泛型——这是框架约束，单个泛型类覆盖不了；
 *   子类因此退化为类型具体化（如 {@code extends CCommaCollectionTypeHandler<Integer>}）。</li>
 *   <li><b>脏数据容错</b>：单值转换失败返回 {@code null}（该元素被跳过，并记 debug 日志），不让整行读取失败；
 *   空串/{@code null} 返回 {@code null} 而非空集合，与写侧"空集合写 {@code null}"对称。</li>
 *   <li><b>元素类型须支持按值往返</b>：本类按单值文本存储，要求"同一取值写出去、读回来仍是同一个取值"。
 *   构造期用取值探针（{@link CTextRoundTrip}）验证；不满足即构造失败并指明失败取值与落库/读回结果——
 *   否则写侧与读侧的文本写法一旦对不上，就会写成功、读不回来，且全程无异常、无日志
 *   （如 {@code java.util.Date} 落库为 {@code "Thu Jan 01 08:00:00 CST 1970"} 这类依赖时区与本地化的文本，
 *   与读侧解析出的取值不等、且同一时刻的文本随环境变化）。</li>
 *   <li><b>包装层不下沉</b>：本类是纯静态类型转换，不需要容器，故落在 base 层（{@code ctool4j-mybatis-base}）、两侧不复制。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>子类未把泛型实参具体化</td>
 *     <td>构造即抛 {@code IllegalStateException} 并指明类名，避免运行期静默按错误类型转换</td>
 *   </tr>
 *   <tr>
 *     <td>集合中的 {@code null} 元素</td>
 *     <td>写与读两侧都跳过</td>
 *   </tr>
 *   <tr>
 *     <td>单值无法转换（脏数据）</td>
 *     <td>返回 {@code null}，该元素被跳过、记 debug 日志，不影响同一行的其余元素</td>
 *   </tr>
 *   <tr>
 *     <td>元素类型不支持按值往返</td>
 *     <td>构造即抛 {@code IllegalStateException}，指明失败取值与落库/读回结果</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>实体中以逗号分割字符串落库的集合字段，如 {@code @TableField(typeHandler = CCommaIntegerCollectionTypeHandler.class)
 *   private List<Integer> freeDays;}</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>元素值本身需要包含逗号时：分隔符固定为英文逗号，此时应改用 JSON 序列化的类型处理器。</li>
 *   <li>需要排序或去重语义时由读取方自行处理：本类只保证原顺序、不做去重（默认返回 {@code List}）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>元素类型必须在子类（或其父类）上具体化；解析交给 Hutool {@code TypeUtil}，故多层泛型透传也能解析。</li>
 *   <li>元素类型须支持按值往返（如 {@code String}/{@code Integer}/{@code Long}）；按值往返对运行时的"值"
 *   成立、不涉及类型异构的边界：把这些类型相互转换后得到的取值本身往返成立，故跨类型转换不构成本条的失效来源。</li>
 *   <li>拼接与分割交给本项目 {@code CStrUtils}（{@code join}/{@code splitToList}），元素转换交给 Hutool {@code Convert}。</li>
 * </ul>
 *
 * @param <T> 集合元素类型
 * @since 2026/9/24
 * @version 1.1
 */
@CustomLog
public abstract class CCommaCollectionTypeHandler<T extends Serializable> extends CBaseTypeHandler<Collection<T>> {

    /**
     * 元素类型：由子类泛型实参解析，构造时确定
     */
    protected final Class<T> elementType;

    /**
     * 解析子类声明的元素类型
     */
    protected CCommaCollectionTypeHandler() {
        this.elementType = resolveElementType();
        assertTextRoundTrip();
    }

    /**
     * 集合转逗号分割字符串（与落库格式一致）。
     * <p>
     * 供 wrapper 的 set 复用——wrapper 的 set 不走实体注解上的 TypeHandler，需要与落库一致的字符串值。
     *
     * @param values 集合
     * @return 逗号分割字符串；集合为空、或元素全为 null（无可输出值）时返回 null
     */
    public static String toText(Collection<?> values) {
        return CStrUtils.join(values, CConstants.COMMA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String toTextValue(Collection<T> values) {
        return toText(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<T> fromText(String value) {

        if (StrUtil.isBlank(value)) {
            return null;
        }

        // 脏数据由 convertQuietly 收敛为 null，再交给 filterNull 过滤，与原顺序一起保持
        return CCollUtils.filterNull(CStrUtils.splitToList(value, this::convertQuietly));
    }

    /**
     * 单值转换：无法转换时返回 null（该元素被跳过，不让整行读取失败）
     *
     * @param text 单个字符串值
     * @return 元素值；无法转换返回 null
     */
    private T convertQuietly(String text) {

        try {
            return Convert.convert(elementType, text);
        } catch (Exception e) {
            log.debug("convert failed, skip element: {}", text, e);
            return null;
        }
    }

    /**
     * 校验元素类型支持"对象 ⇄ 单值文本"的往返转换
     *
     * <p>本类把集合元素拼成逗号分隔的<b>单值文本</b>落库，故元素类型必须能按值往返：把一个取值写出去、
     * 再读回来，得到的仍是同一个取值。按值往返要求类型上取"同一个值"只有一种写法，编译期无处可查，
     * 只能在构造期用<b>取值探针</b>验证。</p>
     *
     * <p><b>为什么必须有这一道</b>：不满足往返的类型用它写库时值仍然拼得出来，读回来却落在另一个值上
     * （如 {@code java.util.Date} 写出时为 {@code "Thu Jan 01 08:00:00 CST 1970"}、读回解析出的时刻与之不等）
     * ——写成功、读不回来，且<b>没有任何异常或日志</b>；更糟的是"往返不成立"会把同一个集合的多个元素折叠成
     * 同一个值，落库后不可恢复。这里在构造期直接拒绝，把它变成立刻可见的启动失败。</p>
     *
     * <p>探针取 {@link CTextRoundTrip#PROBE_VALUES}：大小与浮点混用、与数字型文本同形等
     * "换一种写法可能就是同一个值"的取值，命中即说明该类型按值往返不成立。</p>
     *
     * @throws IllegalStateException 元素类型不支持按值往返时（附失败取值与原因）
     * @see CTextRoundTrip
     */
    private void assertTextRoundTrip() {
        for (val candidate : CTextRoundTrip.PROBE_VALUES) {
            // 先归到元素类型（探针取值是"另一种写法"，需比"本类型的同一个值"）；
            // 归不过去说明该取值不在本类型的定义域内，与按值往返无关，跳过
            val value = Convert.convertQuietly(elementType, candidate);
            if (null == value) {
                continue;
            }
            val text = Convert.convert(String.class, value);
            if (!Objects.equals(value, Convert.convertQuietly(elementType, text))) {
                throw new IllegalStateException("元素类型 " + elementType.getName()
                        + " 不支持按值往返：取值 " + value + " 落库为 " + text
                        + "，读回却得到 " + Convert.convertQuietly(elementType, text)
                        + "（本处理器按单值文本存储，要求同一取值只有一种写法）");
            }
        }
    }

    /**
     * 解析子类声明的泛型实参作为元素类型
     *
     * @return 元素类型
     * @throws IllegalStateException 子类未把泛型实参具体化时
     */
    @SuppressWarnings("unchecked")
    private Class<T> resolveElementType() {

        // 复用 Hutool 的泛型解析：读取子类（或其父类）声明的实参，如 extends CCommaCollectionTypeHandler<Integer>
        Type argument = TypeUtil.getTypeArgument(getClass().getGenericSuperclass(), 0);
        if (argument instanceof Class) {
            return (Class<T>) argument;
        }

        throw new IllegalStateException("无法解析元素类型：" + getClass().getName()
                + "，请继承时具体化泛型（如 extends CCommaCollectionTypeHandler<Integer>）");
    }

}
