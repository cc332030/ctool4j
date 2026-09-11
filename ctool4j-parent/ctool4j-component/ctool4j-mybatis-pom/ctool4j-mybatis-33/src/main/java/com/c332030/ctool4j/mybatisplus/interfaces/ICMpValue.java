package com.c332030.ctool4j.mybatisplus.interfaces;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.c332030.ctool4j.definition.interfaces.ICValue;

import java.io.Serializable;

/**
 * <p>
 * Description: ICMpValue
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICMpValue}：值接口。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>定义值获取</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>值接口</p>
 * <h2>不适用与边界场景</h2>
 * <p>接口</p>
 * <h2>已知限制与取舍</h2>
 * <p>接口</p>
 *
 * @since 2025/9/15
 * @version 1.0
 */
public interface ICMpValue<T extends Serializable> extends ICValue<T>, IEnum<T> {

}
