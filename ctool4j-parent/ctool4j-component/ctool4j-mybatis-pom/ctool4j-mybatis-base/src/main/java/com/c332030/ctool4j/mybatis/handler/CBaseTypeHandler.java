package com.c332030.ctool4j.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>
 * Description: CBaseTypeHandler
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBaseTypeHandler} 是"<b>单列 ↔ 对象</b>"类型处理器的公共基类：
 * 把 MyBatis 要求的四个 JDBC 端点收敛成两个方法。</p>
 * <ul>
 *   <li>子类只实现 {@code fromText(String)}（读：列字符串 → 对象）与 {@code toTextValue(T)}（写：对象 → 列字符串）</li>
 *   <li>四个端点、null 模板、三个读重载的统一都由本类完成，子类不必再管</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>只收敛端点，不定义格式</b>：本类不管"对象长什么样"，只负责"对象 ⇄ 单列字符串"的接线；
 *   具体格式（逗号集合、JSON、单值编码）由子类决定。</li>
 *   <li><b>null 由框架模板处理</b>：{@code BaseTypeHandler} 已在 {@code setParameter}/{@code getResult} 内判空，
 *   本类与子类都<b>不需要</b>再写 null 分支。</li>
 *   <li><b>三个读端点语义相同</b>：{@code ResultSet}（列名 / 列号）与 {@code CallableStatement}（存储过程）
 *   在"单列字符串"这一抽象下没有区别，故统一委托给 {@link #fromText(String)}——这正是子类<b>可以不管</b>的部分。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <ul>
 *   <li>无兜底：转换结果原样返回，格式层的容错（如脏数据跳过）由子类在 {@code fromText} 内决定。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>以单个字符串列承载对象（集合、JSON、编码值）的类型处理器基类。</li>
 *   <li>需要与具体业务格式解耦、避免重复书写 JDBC 端点的 handler。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>多列映射、二进制列（{@code getBytes}/{@code setBytes}）、非字符串列不适用：本类只接 {@code String} 列。</li>
 *   <li>需要区分"按列名 / 按列号读取"行为的场景不适用（本类刻意统一）。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>读写都落在 {@code String} 上：列类型非字符型时依赖 JDBC 驱动的隐式转换，或改为直接继承
 *   {@code BaseTypeHandler} 自定义端点。</li>
 * </ul>
 *
 * @param <T> 对象类型
 * @since 2026/9/24
 * @version 1.0
 */
public abstract class CBaseTypeHandler<T> extends BaseTypeHandler<T> {

    /**
     * 写库：对象转单列字符串
     *
     * @param value 对象（非 null，框架已判空）
     * @return 列字符串
     */
    protected abstract String toTextValue(T value);

    /**
     * 读库：单列字符串转对象
     *
     * @param text 列字符串
     * @return 对象；格式层容错由实现决定
     */
    protected abstract T fromText(String text);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, toTextValue(parameter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return fromText(rs.getString(columnName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return fromText(rs.getString(columnIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return fromText(cs.getString(columnIndex));
    }

}
