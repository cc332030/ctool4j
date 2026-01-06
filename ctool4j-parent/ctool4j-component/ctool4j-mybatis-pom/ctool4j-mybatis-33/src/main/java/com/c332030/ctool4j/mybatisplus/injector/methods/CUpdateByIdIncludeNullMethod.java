package com.c332030.ctool4j.mybatisplus.injector.methods;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.c332030.ctool4j.mybatisplus.injector.CAbstractMethod;
import com.c332030.ctool4j.mybatisplus.injector.CSqlMethod;
import lombok.val;
import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Description: CUpdateByIdIncludeNullMethod
 * </p>
 *
 * @since 2026/1/6
 */
@Component
public class CUpdateByIdIncludeNullMethod extends CAbstractMethod {

    public CUpdateByIdIncludeNullMethod() {
        super(CSqlMethod.UPDATE_BY_ID_INCLUDE_NULL);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {

        val logicDelete = tableInfo.isLogicDelete();
        val additional = optlockVersion(tableInfo) + tableInfo.getLogicDeleteSql(true, true);
        val sql = String.format(
            sqlMethod.getSql(), tableInfo.getTableName(),
            sqlSet(logicDelete, false, tableInfo, true, ENTITY, ENTITY_DOT),
            tableInfo.getKeyColumn(), ENTITY_DOT + tableInfo.getKeyProperty(), additional
        );
        val sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return addUpdateMappedStatement(mapperClass, modelClass, sqlMethod.getMethod(), sqlSource);
    }

}
