package com.c332030.ctool4j.mybatis.util;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.definition.annotation.CBizId;
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

        val fields = CReflectUtils.getFields(type);
        for (val field : fields.values()) {
            val annotation = field.getAnnotation(CBizId.class);
            if (null != annotation) {

                CAssert.equals(field.getType(), String.class, "bizId type must be String");
                return field;
            }
        }

        return null;
    });

    public String getBizId(Object entity) {

        val field = FIELD_BIZ_ID_CLASS_VALUE.get(entity.getClass());
        if (null == field) {
            return null;
        }

        return CReflectUtils.getValue(entity, field);
    }

    public void setBizId(Object entity, String bizId) {

        val field = FIELD_BIZ_ID_CLASS_VALUE.get(entity.getClass());
        if (null == field) {
            return;
        }

        CReflectUtils.setValue(entity, field, bizId);

    }

}
