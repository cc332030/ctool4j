package com.c332030.ctool4j.spring.test.util;

import com.c332030.ctool4j.spring.util.CAnnotationUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Description: CAnnotationUtilsTests
 * </p>
 *
 * <p>覆盖 CAnnotationUtils 的 getAnnotationValue/getAnnotationAttributeValue，
 * 通过 Mockito mock AnnotatedTypeMetadata 验证正例、属性缺失与无注解场景</p>
 *
 * @since 2026/8/16
 */
public class CAnnotationUtilsTests {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface MyAnno {
        String value() default "";
    }

    // ---------- getAnnotationValue ----------

        /**
     * 对应测试用例 1.1
     */
    @Test
    public void getAnnotationValue() {
        // 正例：存在注解且含 value 属性时返回属性值
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(metadata.getAnnotationAttributes(MyAnno.class.getName()))
            .thenReturn(Collections.singletonMap("value", "hello"));

        val value = CAnnotationUtils.getAnnotationValue(metadata, MyAnno.class);

        Assertions.assertEquals("hello", value);
    }

        /**
     * 对应测试用例 1.2
     */
    @Test
    public void getAnnotationValue_whenAttributeAbsent() {
        // 边界：存在注解但无 value 属性时返回 null
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(metadata.getAnnotationAttributes(MyAnno.class.getName()))
            .thenReturn(Collections.singletonMap("name", "no-value"));

        val value = CAnnotationUtils.getAnnotationValue(metadata, MyAnno.class);

        Assertions.assertNull(value);
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    public void getAnnotationValue_whenNoAnnotation() {
        // 反例：无该注解时返回 null
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(metadata.getAnnotationAttributes(MyAnno.class.getName())).thenReturn(null);

        val value = CAnnotationUtils.getAnnotationValue(metadata, MyAnno.class);

        Assertions.assertNull(value);
    }

    // ---------- getAnnotationAttributeValue ----------

        /**
     * 对应测试用例 1.4
     */
    @Test
    public void getAnnotationAttributeValue() {
        // 正例：存在注解且含指定属性时返回属性值
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(metadata.getAnnotationAttributes(MyAnno.class.getName()))
            .thenReturn(Collections.singletonMap("value", "world"));

        val value = CAnnotationUtils.getAnnotationAttributeValue(metadata, MyAnno.class, "value");

        Assertions.assertEquals("world", value);
    }

        /**
     * 对应测试用例 1.5
     */
    @Test
    public void getAnnotationAttributeValue_whenNoAnnotation() {
        // 反例：无该注解时返回 null
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(metadata.getAnnotationAttributes(MyAnno.class.getName())).thenReturn(null);

        val value = CAnnotationUtils.getAnnotationAttributeValue(metadata, MyAnno.class, "value");

        Assertions.assertNull(value);
    }

}
