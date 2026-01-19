package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.interfaces.ICValue;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * Description: CValueUtils
 * </p>
 *
 * @since 2025/12/4
 */
@UtilityClass
public class CValueUtils {

    public <T> T getValue(ICValue<T> iValue) {
        return CObjUtils.convert(iValue, ICValue::getValue);
    }

    public <T> void setValue(ICValue<T> iValue, CConsumer<T> consumer) {
        if(null != iValue) {
            consumer.accept(iValue.getValue());
        }
    }

}
