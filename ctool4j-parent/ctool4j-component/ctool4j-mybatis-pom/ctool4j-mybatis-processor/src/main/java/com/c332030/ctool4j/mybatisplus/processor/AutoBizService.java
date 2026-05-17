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
 * @AutoBizService(bizIdField = "orderNo", bizIdColumn = "order_no")
 * public class Order extends CBaseEntity<Long> {
 *     private String orderNo;
 * }
 * }
 * </pre>
 *
 * <p>将自动生成 IOrderService 接口，包含以下方法：</p>
 * <ul>
 *   <li>getByOrderNo(String orderNo)</li>
 *   <li>listByOrderNo(String orderNo)</li>
 *   <li>countByOrderNo(String orderNo)</li>
 *   <li>updateByOrderNo(Entity entity)</li>
 *   <li>removeByOrderNo(String orderNo)</li>
 *   <li>...</li>
 * </ul>
 *
 * @since 2025/05/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AutoBizService {

    /**
     * 业务ID字段名（Java字段名，如 orderNo）
     */
    String bizIdField();

    /**
     * 业务ID数据库列名（如 order_no）
     * 如果不指定，默认使用 bizIdField 的下划线形式
     */
    String bizIdColumn() default "";

    /**
     * 生成的 Service 接口名后缀
     * 默认为 "Service"，即实体名 + Service
     */
    String serviceSuffix() default "Service";

    /**
     * 是否生成 Impl 实现类
     * 默认不生成，由用户自行继承 CServiceImpl
     */
    boolean generateImpl() default false;
}
