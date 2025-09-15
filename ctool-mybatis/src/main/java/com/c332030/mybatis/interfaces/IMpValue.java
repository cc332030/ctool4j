package com.c332030.mybatis.interfaces;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.c332030.core.interfaces.IValue;

import java.io.Serializable;

/**
 * <p>
 * Description: IMpValue
 * </p>
 *
 * @since 2025/9/15
 */
public interface IMpValue<T extends Serializable> extends IValue<T>, IEnum<T> {

}
