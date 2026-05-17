package com.c332030.ctool4j.mybatisplus.util;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

/**
 * <p>
 * Description: CMpFieldUtils
 * </p>
 *
 * @since 2026/1/6
 */
@UtilityClass
public class CMpFieldUtils {

    public final Predicate<TableFieldInfo> UPDATE_NOT_NEVER =
        fieldInfo -> FieldStrategy.NEVER != fieldInfo.getUpdateStrategy();

}
