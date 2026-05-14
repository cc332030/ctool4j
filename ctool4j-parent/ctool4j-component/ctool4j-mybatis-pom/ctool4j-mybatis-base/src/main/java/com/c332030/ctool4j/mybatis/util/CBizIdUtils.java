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
 * @since 2025/12/3
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
