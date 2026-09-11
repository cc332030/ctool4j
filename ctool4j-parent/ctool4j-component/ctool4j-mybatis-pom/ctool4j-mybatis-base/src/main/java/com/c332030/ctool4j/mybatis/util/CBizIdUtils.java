package com.c332030.ctool4j.mybatis.util;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.util.CIdUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.definition.annotation.CBizId;
import com.c332030.ctool4j.mybatisplus.service.ICBizIdService;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.Field;

/**
 * <p>
 * Description: CBizIdUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBizIdUtils}：业务ID工具。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>获取/生成实体业务ID，校验 bizId 字段为 String</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无 @CBizId 字段返回 null</p>
 * <h2>适用范围</h2>
 * <p>业务ID生成</p>
 * <h2>不适用与边界场景</h2>
 * <p>依赖反射与 CClassValue</p>
 * <h2>已知限制与取舍</h2>
 * <p>依赖反射与 CClassValue</p>
 *
 * @since 2025/12/3
 * @version 1.0
 */
@UtilityClass
public class CBizIdUtils {

    final CClassValue<Field> FIELD_BIZ_ID_CLASS_VALUE = CClassValue.of(type -> {

        val fields = CReflectUtils.getAllFieldMap(type);
        for (val field : fields.values()) {
            val annotation = field.getAnnotation(CBizId.class);
            if (null != annotation) {

                CAssert.equals(field.getType(), String.class, "bizId type must be String");
                return field;
            }
        }

        return null;
    });

    /**
     * 获取业务ID
     * @param entityClass 实体类
     * @return 业务ID
     */
    public String getBizId(Class<?> entityClass) {
        return getBizId(entityClass, Integer.MAX_VALUE);
    }

    /**
     * 获取业务ID
     * @param entityClass 实体类
     * @param length 长度
     * @return 业务ID
     */
    public String getBizId(Class<?> entityClass, int length) {
        return CIdUtils.nextIdWithPrefix(entityClass, length);
    }

    /**
     * 获取业务ID
     * @param entity 实体
     * @return 业务ID
     */
    public String getBizId(Object entity) {

        val field = FIELD_BIZ_ID_CLASS_VALUE.get(entity.getClass());
        if (null == field) {
            return null;
        }

        return CReflectUtils.getValue(entity, field);
    }

    /**
     * 设置业务ID
     * @param entity 实体
     * @param bizIdService 业务ID服务
     */
    public void setBizId(Object entity, ICBizIdService<?> bizIdService) {

        val field = FIELD_BIZ_ID_CLASS_VALUE.get(entity.getClass());
        if (null == field) {
            return;
        }

        CReflectUtils.setValue(entity, field, bizIdService.getBizId());

    }

}
