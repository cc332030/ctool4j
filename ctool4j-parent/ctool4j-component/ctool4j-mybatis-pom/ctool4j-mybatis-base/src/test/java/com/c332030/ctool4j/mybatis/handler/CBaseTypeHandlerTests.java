package com.c332030.ctool4j.mybatis.handler;

import lombok.val;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.Mockito.*;

/**
 * <p>
 * Description: CBaseTypeHandlerTests
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>覆盖 {@link CBaseTypeHandler} 把 MyBatis 四个 JDBC 端点收敛为 {@code toTextValue}/{@code fromText} 的接线：
 * 写入端、三个读取端（结果集按列名/列号、存储过程），以及读侧列值为 null 的边界。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>用一个最小测试子类（{@code toTextValue}/{@code fromText} 原样透传），使断言只反映<b>端点接线</b>，
 *   不掺入任何格式语义。</li>
 *   <li>三个读端点必须走同一份 {@code fromText}——这是本基类存在的意义，故逐端验证而不是只验一个。</li>
   <li>写侧 null（走框架模板的 {@code setParameter} → {@code setNull}）不在本类范围：那是 {@code BaseTypeHandler} 的既有行为，非本基类新增契约。</li>
 * </ul>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>{@link CBaseTypeHandler} 及其所有子类的端点接线契约。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>JDBC 对象用 Mockito 打桩，不连库：只验"调没调、传了什么"，不验驱动行为。</li>
 * </ul>
 *
 * @see CBaseTypeHandler
 * @since 2026/9/24
 * @version 1.0
 */
public class CBaseTypeHandlerTests {

    /**
     * 被测处理器（最小实现：原样透传）
     */
    private final TestHandler handler = new TestHandler();

    /**
     * 写入端：对象经 {@code toTextValue} 转换后写入第 i 个占位符
     *
     * @throws Exception 由 JDBC 接口声明抛出
     */
    @Test
    public void setNonNullParameter_writesConvertedText() throws Exception {

        val ps = mock(PreparedStatement.class);

        handler.setNonNullParameter(ps, 2, "v", JdbcType.VARCHAR);

        verify(ps).setString(2, "v");
    }

    /**
     * 读取端：按列名读取，走同一份 {@code fromText}
     *
     * @throws Exception 由 JDBC 接口声明抛出
     */
    @Test
    public void getNullableResult_byColumnName() throws Exception {

        val rs = mock(ResultSet.class);
        when(rs.getString("col")).thenReturn("v");

        Assertions.assertEquals("v", handler.getNullableResult(rs, "col"));
    }

    /**
     * 读取端：按列号读取，与按列名同源
     *
     * @throws Exception 由 JDBC 接口声明抛出
     */
    @Test
    public void getNullableResult_byColumnIndex() throws Exception {

        val rs = mock(ResultSet.class);
        when(rs.getString(3)).thenReturn("v");

        Assertions.assertEquals("v", handler.getNullableResult(rs, 3));
    }

    /**
     * 读取端：存储过程结果集，与结果集读取同源
     *
     * @throws Exception 由 JDBC 接口声明抛出
     */
    @Test
    public void getNullableResult_byCallableStatement() throws Exception {

        val cs = mock(CallableStatement.class);
        when(cs.getString(1)).thenReturn("v");

        Assertions.assertEquals("v", handler.getNullableResult(cs, 1));
    }

    /**
     * 读取端：列值为 null 时由 {@code fromText} 决定语义（本测试子类原样返回 null）
     *
     * @throws Exception 由 JDBC 接口声明抛出
     */
    @Test
    public void getNullableResult_nullColumn() throws Exception {

        val rs = mock(ResultSet.class);
        when(rs.getString("col")).thenReturn(null);

        Assertions.assertNull(handler.getNullableResult(rs, "col"));
    }

    /**
     * 测试用处理器：不做任何格式转换，使断言只反映端点接线
     */
    static class TestHandler extends CBaseTypeHandler<String> {

        @Override
        protected String toTextValue(String value) {
            return value;
        }

        @Override
        protected String fromText(String text) {
            return text;
        }
    }

}
