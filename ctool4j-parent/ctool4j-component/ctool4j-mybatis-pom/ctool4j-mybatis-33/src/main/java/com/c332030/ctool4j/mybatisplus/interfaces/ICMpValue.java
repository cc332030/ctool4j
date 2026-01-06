package com.c332030.ctool4j.mybatisplus.interfaces;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.c332030.ctool4j.definition.interfaces.ICValue;

import java.io.Serializable;

/**
 * <p>
 * Description: ICMpValue
 * </p>
 *
 * @since 2025/9/15
 */
public interface ICMpValue<T extends Serializable> extends ICValue<T>, IEnum<T> {

}
