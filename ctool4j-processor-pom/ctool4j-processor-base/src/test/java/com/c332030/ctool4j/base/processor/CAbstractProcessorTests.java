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

/**
 * <p>
 * Description: CAbstractProcessorTests
 * </p>
 *
 * <p>
 * 是 {@link CAbstractProcessor} 的测试用例（对应测试文档
 * <code>doc/design/base/CAbstractProcessorTests.adoc</code>）。
 * </p>
 *
 * @since 2026/8/14
 */
public class CAbstractProcessorTests {

    private TestProcessor processor;

    @BeforeEach
    public void setUp() {
        processor = new TestProcessor();
    }

        /**
     * 对应测试用例 1.1
     */
    @Test
    public void supportedSourceVersion_RELEASE8() {
        Assertions.assertEquals(SourceVersion.RELEASE_8, processor.getSupportedSourceVersion());
    }

        /**
     * 对应测试用例 1.2
     */
    @Test
    public void supportedAnnotationTypes_empty() {
        Assertions.assertTrue(processor.getSupportedAnnotationTypes().isEmpty());
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    public void process_returnsFalse() {
        Assertions.assertFalse(processor.process(Collections.emptySet(), Mockito.mock(RoundEnvironment.class)));
    }

        /**
     * 对应测试用例 1.4
     */
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
