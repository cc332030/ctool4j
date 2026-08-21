package com.c332030.ctool4j.mybatisplus.processor;

import com.c332030.ctool4j.mybatisplus.annotation.CAutoBizService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.Field;
import java.util.Collections;

/**
 * <p>
 * Description: CAutoBizServiceProcessorTests
 * </p>
 *
 * <p>
 * 是 {@link CAutoBizServiceProcessor} 的测试用例（对应测试文档
 * <code>doc/design/mybatisplus/CAutoBizServiceProcessorTests.adoc</code>）。
 * </p>
 *
 * @since 2026/8/14
 */
public class CAutoBizServiceProcessorTests {

    private CAutoBizServiceProcessor processor;
    private ProcessingEnvironment processingEnv;
    private RoundEnvironment roundEnv;

    @BeforeEach
    public void setUp() {
        processor = new CAutoBizServiceProcessor();
        processingEnv = Mockito.mock(ProcessingEnvironment.class);
        Mockito.when(processingEnv.getMessager()).thenReturn(Mockito.mock(Messager.class));
        roundEnv = Mockito.mock(RoundEnvironment.class);
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
    public void supportedAnnotationTypes_containsAutoBizService() {
        Assertions.assertTrue(processor.getSupportedAnnotationTypes().contains(CAutoBizService.class.getName()));
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    public void init_loadsTemplate() throws Exception {
        processor.init(processingEnv);

        Field templateField = CAutoBizServiceProcessor.class.getDeclaredField("template");
        templateField.setAccessible(true);
        String template = (String) templateField.get(processor);

        Assertions.assertNotNull(template);
        Assertions.assertFalse(template.isEmpty());
    }

        /**
     * 对应测试用例 1.4
     */
    @Test
    public void process_beforeInit_returnsTrue() {
        Assertions.assertTrue(processor.process(Collections.emptySet(), roundEnv));
    }

        /**
     * 对应测试用例 1.5
     */
    @Test
    public void process_emptyAnnotations_returnsTrue() {
        processor.init(processingEnv);

        Assertions.assertTrue(processor.process(Collections.emptySet(), roundEnv));
    }

        /**
     * 对应测试用例 1.6
     */
    @Test
    public void process_interfaceWithoutMethod_skipsGeneration() {
        processor.init(processingEnv);

        TypeElement interfaceElement = Mockito.mock(TypeElement.class);
        Mockito.when(interfaceElement.getKind()).thenReturn(ElementKind.INTERFACE);
        Mockito.when(interfaceElement.getEnclosedElements()).thenReturn(Collections.emptyList());
        Mockito.doReturn(Collections.<Element>singleton(interfaceElement))
            .when(roundEnv).getElementsAnnotatedWith(Mockito.<TypeElement>any());

        Assertions.assertTrue(
            processor.process(Collections.singleton(Mockito.mock(TypeElement.class)), roundEnv));
        Mockito.verify(processingEnv, Mockito.never()).getFiler();
    }

}
