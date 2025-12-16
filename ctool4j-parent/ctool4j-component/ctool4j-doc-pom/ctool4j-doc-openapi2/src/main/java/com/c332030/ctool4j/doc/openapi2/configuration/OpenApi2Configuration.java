package com.c332030.ctool4j.doc.openapi2.configuration;

import com.c332030.ctool4j.core.util.CArrUtils;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;

/**
 * <p>
 * Description: OpenApi2Configuration
 * </p>
 *
 * @since 2025/12/16
 */
@Configuration
public class OpenApi2Configuration {

    private static final Class<? extends Annotation>[] REQUIREED_ANNOTATIONS = CArrUtils.getArr(
        NotNull.class,
        NotBlank.class,
        NotEmpty.class
    );

    @Bean
    public ModelPropertyBuilderPlugin requiredNotEmptyPlugin() {
        return new ModelPropertyBuilderPlugin() {

            @Override
            public boolean supports(@NonNull DocumentationType documentationType) {
                return true;
            }

            @Override
            public void apply(ModelPropertyContext context) {

                context.getBeanPropertyDefinition()
                    .map(BeanPropertyDefinition::getField)
                    .filter(field -> {
                        val annotations = field.getAllAnnotations();
                        return annotations.hasOneOf(REQUIREED_ANNOTATIONS);
                    })
                    .ifPresent(e -> context.getBuilder().required(true));

            }
        };
    }

}
