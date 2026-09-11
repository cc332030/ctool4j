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
 * 是 {@link CAutoBizServiceProcessor} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>通过 Mockito 模拟 ProcessingEnvironment/RoundEnvironment，验证处理器的源版本、注解类型、模板加载与 process 各分支。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对处理器源版本、注解类型声明、模板加载、process 返回约定的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：支持 Java 8；注解类型含 AutoBizService；init 加载模板；process 未 init/空注解返回约定；字段元素跳过生成。</li>
 *   <li>未覆盖：真实编译期注解处理生成业务服务的端到端流程。</li>
 * </ul>
 * <h2>注解处理器行为</h2>
 * <ul>
 *   <li>1.1 支持 Java 8 源版本（{@code supportedSourceVersion_RELEASE8}）</li>
 *   <li>1.2 注解类型含 AutoBizService（{@code supportedAnnotationTypes_containsAutoBizService}）</li>
 *   <li>1.3 init 加载模板（{@code init_loadsTemplate}）</li>
 *   <li>1.4 process 未 init 返回 true（{@code process_beforeInit_returnsTrue}）</li>
 *   <li>1.5 process 空注解返回 true（{@code process_emptyAnnotations_returnsTrue}）</li>
 *   <li>1.6 process 字段元素跳过生成（{@code process_fieldElement_skipsGeneration}）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CAutoBizServiceProcessorTests {

    private CAutoBizServiceProcessor processor;
    private ProcessingEnvironment processingEnv;
    private RoundEnvironment roundEnv;

    /**
     * 每个用例执行前的准备
     */
    @BeforeEach
    public void setUp() {
        processor = new CAutoBizServiceProcessor();
        processingEnv = Mockito.mock(ProcessingEnvironment.class);
        Mockito.when(processingEnv.getMessager()).thenReturn(Mockito.mock(Messager.class));
        roundEnv = Mockito.mock(RoundEnvironment.class);
    }

        /**
         * 对应测试用例 1.1：支持 Java 8 源版本（{@code supportedSourceVersion_RELEASE8}）
         */
    @Test
    public void supportedSourceVersion_RELEASE8() {
        Assertions.assertEquals(SourceVersion.RELEASE_8, processor.getSupportedSourceVersion());
    }

        /**
         * 对应测试用例 1.2：注解类型含 AutoBizService（{@code supportedAnnotationTypes_containsAutoBizService}）
         */
    @Test
    public void supportedAnnotationTypes_containsAutoBizService() {
        Assertions.assertTrue(processor.getSupportedAnnotationTypes().contains(CAutoBizService.class.getName()));
    }

        /**
         * 对应测试用例 1.3：init 加载模板（{@code init_loadsTemplate}）
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
         * 对应测试用例 1.4：process 未 init 返回 true（{@code process_beforeInit_returnsTrue}）
         */
    @Test
    public void process_beforeInit_returnsTrue() {
        Assertions.assertTrue(processor.process(Collections.emptySet(), roundEnv));
    }

        /**
         * 对应测试用例 1.5：process 空注解返回 true（{@code process_emptyAnnotations_returnsTrue}）
         */
    @Test
    public void process_emptyAnnotations_returnsTrue() {
        processor.init(processingEnv);

        Assertions.assertTrue(processor.process(Collections.emptySet(), roundEnv));
    }

        /**
         * 对应测试用例 1.6：process 字段元素跳过生成（{@code process_fieldElement_skipsGeneration}）
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
