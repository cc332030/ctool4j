package com.c332030.ctool4j.mybatisplus.processor;

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
 * <pre>
 * {@code
 * @AutoBizService
 * public class Order {
 *     @CBizId
 *     private String orderNo;
 * }
 * }
 * </pre>
 *
 * @since 2025/05/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CAutoBizService {
}
