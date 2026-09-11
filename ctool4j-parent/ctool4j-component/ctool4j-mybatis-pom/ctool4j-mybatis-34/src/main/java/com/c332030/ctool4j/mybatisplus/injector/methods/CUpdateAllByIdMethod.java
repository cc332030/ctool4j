package com.c332030.ctool4j.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.extension.injector.methods.AlwaysUpdateSomeColumnById;
import com.c332030.ctool4j.mybatisplus.injector.CMpSqlMethod;
import com.c332030.ctool4j.mybatisplus.injector.ICMpMethod;
import com.c332030.ctool4j.mybatisplus.util.CMpFieldUtils;

/**
 * <p>
 * Description: CUpdateAllByIdMethod
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CUpdateAllByIdMethod}：按ID更新所有字段方法。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>生成 UPDATE 全部字段的注入方法</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>全字段更新 SQL</p>
 * <h2>不适用与边界场景</h2>
 * <p>注入方法</p>
 * <h2>已知限制与取舍</h2>
 * <p>注入方法</p>
 *
 * @since 2026/1/6
 * @version 1.0
 */
public class CUpdateAllByIdMethod extends AlwaysUpdateSomeColumnById implements ICMpMethod {

    private static final long serialVersionUID = 1L;

    /**
     * 构造方法，指定无需更新的字段
     */
    public CUpdateAllByIdMethod() {
        super(CMpFieldUtils.UPDATE_NOT_NEVER);
    }

    /**
     * 返回 UPDATE_ALL_BY_ID 方法名
     *
     * @param sqlMethod 默认 SQL 方法
     * @return 方法名
     */
    @Override
    public String getMethod(SqlMethod sqlMethod) {
        return CMpSqlMethod.UPDATE_ALL_BY_ID.getMethod();
    }

}
