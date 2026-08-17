package com.c332030.ctool4j.base.processor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.Set;

public class CAbstractProcessorTests {

    private TestProcessor processor;

    @BeforeEach
    public void setUp() {
        processor = new TestProcessor();
    }

    @Test
    public void supportedSourceVersion_RELEASE8() {
        Assertions.assertEquals(SourceVersion.RELEASE_8, processor.getSupportedSourceVersion());
    }

    @Test
    public void supportedAnnotationTypes_empty() {
        Assertions.assertTrue(processor.getSupportedAnnotationTypes().isEmpty());
    }

    @Test
    public void process_returnsFalse() {
        Assertions.assertFalse(processor.process(Collections.emptySet(), Mockito.mock(RoundEnvironment.class)));
    }

    @Test
    public void init_noException() {
        Assertions.assertDoesNotThrow(() -> processor.init(Mockito.mock(ProcessingEnvironment.class)));
    }

    /**
     * CAbstractProcessor 的测试子类：process 为抽象方法（AbstractProcessor 未提供默认实现），
     * 子类必须实现；这里返回 false 验证基类约定的最小实现可正常使用
     */
    private static class TestProcessor extends CAbstractProcessor {

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            return false;
        }
    }

}
