package com.c332030.ctool4j.spring.processor;

import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
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
 * Description: CAutowiredScanProcessorTests
 * </p>
 *
 * <p>
 * 是 {@link CAutowiredScanProcessor} 的测试用例（对应测试文档
 * <code>doc/design/spring/CAutowiredScanProcessorTests.adoc</code>）。
 * </p>
 *
 * @since 2026/8/14
 */
public class CAutowiredScanProcessorTests {

    private CAutowiredScanProcessor processor;
    private ProcessingEnvironment processingEnv;
    private RoundEnvironment roundEnv;

    @BeforeEach
    public void setUp() {
        processor = new CAutowiredScanProcessor();
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
    public void supportedAnnotationTypes_containsAutowiredScan() {
        Assertions.assertTrue(processor.getSupportedAnnotationTypes().contains(CAutowiredScan.class.getName()));
    }

        /**
     * 对应测试用例 1.3
     */
    @Test
    public void init_loadsTemplate() throws Exception {
        processor.init(processingEnv);

        Field templateField = CAutowiredScanProcessor.class.getDeclaredField("template");
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
    public void process_fieldElement_skipsGeneration() {
        processor.init(processingEnv);

        Element fieldElement = Mockito.mock(Element.class);
        Mockito.when(fieldElement.getKind()).thenReturn(ElementKind.FIELD);
        Mockito.doReturn(Collections.singleton(fieldElement))
            .when(roundEnv).getElementsAnnotatedWith(Mockito.<TypeElement>any());

        Assertions.assertTrue(
            processor.process(Collections.singleton(Mockito.mock(TypeElement.class)), roundEnv));
        Mockito.verify(processingEnv, Mockito.never()).getFiler();
    }

}
