package com.c332030.ctool4j.spring.condition;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.c332030.ctool4j.spring.annotation.ConditionalOnGenericBean;
import lombok.CustomLog;
import lombok.val;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * <p>
 * Description: OnGenericBeanCondition
 * </p>
 *
 * @since 2025/11/4
 */
@CustomLog
@Order(Ordered.LOWEST_PRECEDENCE)
public class OnGenericBeanCondition implements Condition {

    @Override
    public boolean matches(@NonNull ConditionContext context, AnnotatedTypeMetadata metadata) {

        val attributes = metadata.getAllAnnotationAttributes(ConditionalOnGenericBean.class.getName());
        if (MapUtil.isEmpty(attributes)) {
            return false;
        }

        val rawType = (Class<?>) attributes.getFirst("type");
        Assert.notNull(rawType, "type must not be null");

        val genericTypes = (Class<?>[]) attributes.getFirst("genericTypes");
        Assert.notNull(genericTypes, "genericTypes must not be null");

        // 构建泛型类型：如 GenericService<String>
        val targetType = ResolvableType.forClassWithGenerics(rawType, genericTypes);

        // 检查容器中是否存在该泛型类型的 Bean
        val beanFactory = context.getBeanFactory();
        if(null == beanFactory) {
            log.debug("No bean for type: {} with genericTypes: {}",
                    () -> rawType, () -> Arrays.toString(genericTypes));
            return false;
        }

        val beanNames = beanFactory.getBeanNamesForType(targetType);
        return ArrayUtil.isNotEmpty(beanNames);
    }

}
