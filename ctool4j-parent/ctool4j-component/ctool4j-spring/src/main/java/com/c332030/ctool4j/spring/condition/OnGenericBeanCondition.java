package com.c332030.ctool4j.spring.condition;

import cn.hutool.core.map.MapUtil;
import com.c332030.ctool4j.spring.annotation.ConditionalOnGenericBean;
import lombok.CustomLog;
import lombok.val;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.ResolvableType;
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
public class OnGenericBeanCondition implements ConfigurationCondition {

    @NonNull
    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

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

        String[] beanNames = beanFactory.getBeanNamesForType(targetType);
        return beanNames.length > 0;
    }

}
