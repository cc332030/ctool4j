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

    @Test
    public void supportedSourceVersion_RELEASE8() {
        Assertions.assertEquals(SourceVersion.RELEASE_8, processor.getSupportedSourceVersion());
    }

    @Test
    public void supportedAnnotationTypes_containsAutoBizService() {
        Assertions.assertTrue(processor.getSupportedAnnotationTypes().contains(CAutoBizService.class.getName()));
    }

    @Test
    public void init_loadsTemplate() throws Exception {
        processor.init(processingEnv);

        Field templateField = CAutoBizServiceProcessor.class.getDeclaredField("template");
        templateField.setAccessible(true);
        String template = (String) templateField.get(processor);

        Assertions.assertNotNull(template);
        Assertions.assertFalse(template.isEmpty());
    }

    @Test
    public void process_beforeInit_returnsTrue() {
        Assertions.assertTrue(processor.process(Collections.emptySet(), roundEnv));
    }

    @Test
    public void process_emptyAnnotations_returnsTrue() {
        processor.init(processingEnv);

        Assertions.assertTrue(processor.process(Collections.emptySet(), roundEnv));
    }

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
