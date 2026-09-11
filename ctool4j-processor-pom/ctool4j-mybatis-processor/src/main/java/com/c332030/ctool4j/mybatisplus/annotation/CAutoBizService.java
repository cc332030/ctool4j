package com.c332030.ctool4j.mybatisplus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Description: 自动生成业务 Service 接口的注解
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * &#64;CAutoBizService
 * public interface IOrderNo {
 *     String getOrderNo();
 * }}</pre>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAutoBizService}：自动业务服务注解。</p>
 * <h2>设计要点</h2>
 * <ul>
 *   <li>标注在接口上，自动生成业务服务实现</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <p>无</p>
 * <h2>适用范围</h2>
 * <p>自动生成业务服务</p>
 * <h2>不适用与边界场景</h2>
 * <p>配合处理器</p>
 * <h2>已知限制与取舍</h2>
 * <p>配合处理器</p>
 *
 * @since 2025/05/16
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CAutoBizService {

}
