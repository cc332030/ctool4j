package com.c332030.ctool4j.mybatisplus.processor;

import org.junit.jupiter.api.Test;

import javax.annotation.processing.Processor;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * Description: 注解处理器注册测试
 * </p>
 *
 * @since 2025/05/16
 */
class ProcessorRegistrationTest {

    @Test
    void testProcessorIsRegistered() {
        ServiceLoader<Processor> loader = ServiceLoader.load(Processor.class);
        
        boolean found = false;
        for (Processor processor : loader) {
            if (processor instanceof AutoBizServiceProcessor) {
                found = true;
                break;
            }
        }
        
        assertTrue(found, "AutoBizServiceProcessor should be registered in META-INF/services");
    }

    @Test
    void testSupportedAnnotationTypes() {
        AutoBizServiceProcessor processor = new AutoBizServiceProcessor();
        
        var supportedTypes = processor.getSupportedAnnotationTypes();
        
        assertEquals(1, supportedTypes.size());
        assertTrue(supportedTypes.contains(AutoBizService.class.getName()));
    }

}
