package com.c332030.ctool4j.mybatis.handler;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.c332030.ctool4j.core.util.CCollUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.definition.constant.CConstants;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;

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
 *   <li><b>脏数据容错</b>：单值转换失败返回 {@code null}（该元素被跳过），不让整行读取失败；
 *   空串/{@code null} 返回 {@code null} 而非空集合，与写侧"空集合写 {@code null}"对称。</li>
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
 *     <td>返回 {@code null}，该元素被跳过，不影响同一行的其余元素</td>
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
 *   <li>拼接与分割交给本项目 {@code CStrUtils}（{@code join}/{@code splitToList}），元素转换交给 Hutool {@code Convert}。</li>
 * </ul>
 *
 * @param <T> 集合元素类型
 * @since 2026/9/24
 * @version 1.0
 */
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
            return null;
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
