package com.c332030.ctool4j.spring.processor;

import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.processing.Filer;
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
 * 是 {@link CAutowiredScanProcessor} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>单独验证处理器的声明式约定（源版本、注解类型）与 process 的各条分支。</li>
 *   <li>「模板内联」是本处理器最重要的设计约束：生成能力必须在无任何 classpath 资源的前提下可用，
 *   因此用反射读取 {@code TEMPLATE} 常量并直接调用生成逻辑验证，无需真实编译期。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对注解类型声明、生成入口与零外部资源依赖的约定。</li>
 *   <li>依据等价类/分支覆盖：有字段/无字段、类元素/非类元素、注解轮次/空轮次。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：源版本、注解类型集合、{@code TEMPLATE} 非空且占位符齐全、无字段类不生成源码、字段元素跳过生成。</li>
 *   <li>未覆盖：真实编译期注解处理的端到端流程（由集成构建覆盖）。</li>
 * </ul>
 * <h2>注解处理器行为</h2>
 * <ul>
 *   <li>1.1 支持 Java 8 源版本（{@code supportedSourceVersion_RELEASE8}）</li>
 *   <li>1.2 注解类型含 AutowiredScan（{@code supportedAnnotationTypes_containsAutowiredScan}）</li>
 *   <li>1.3 无字段类不创建源码文件（{@code generate_withoutFields_skipsGeneration}）</li>
 *   <li>1.4 process 未声明注解轮次返回 true（{@code process_emptyAnnotations_returnsTrue}）</li>
 *   <li>1.5 process 字段元素跳过生成（{@code process_fieldElement_skipsGeneration}）</li>
 *   <li>1.6 模板内联且零外部资源（{@code template_inlinedWithoutResource}）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 * @see CAutowiredScanProcessor
 */
public class CAutowiredScanProcessorTests {

    private CAutowiredScanProcessor processor;
    private ProcessingEnvironment processingEnv;
    private RoundEnvironment roundEnv;
    private Filer filer;

    /**
     * 每个用例执行前的准备：构造处理器与 mock 处理环境
     */
    @BeforeEach
    public void setUp() {
        processor = new CAutowiredScanProcessor();
        filer = Mockito.mock(Filer.class);
        processingEnv = Mockito.mock(ProcessingEnvironment.class);
        Mockito.when(processingEnv.getMessager()).thenReturn(Mockito.mock(Messager.class));
        Mockito.when(processingEnv.getFiler()).thenReturn(filer);
        Mockito.when(processingEnv.getSourceVersion()).thenReturn(SourceVersion.RELEASE_8);
        roundEnv = Mockito.mock(RoundEnvironment.class);
        processor.init(processingEnv);
    }

    /**
     * 对应测试用例 1.1：支持 Java 8 源版本
     */
    @Test
    public void supportedSourceVersion_RELEASE8() {
        Assertions.assertEquals(SourceVersion.RELEASE_8, processor.getSupportedSourceVersion());
    }

    /**
     * 对应测试用例 1.2：注解类型含 AutowiredScan
     */
    @Test
    public void supportedAnnotationTypes_containsAutowiredScan() {
        Assertions.assertTrue(processor.getSupportedAnnotationTypes().contains(CAutowiredScan.class.getName()));
    }

    /**
     * 对应测试用例 1.3：无字段类不创建源码文件
     */
    @Test
    public void generate_withoutFields_skipsGeneration() throws Exception {
        // 边界：被注解类无 CAutowired 字段时不生成空注入类，输出 WARNING 后返回
        TypeElement classElement = Mockito.mock(TypeElement.class);
        Mockito.when(classElement.getKind()).thenReturn(ElementKind.CLASS);
        Mockito.when(classElement.getQualifiedName()).thenReturn(new NameStub("com.example.NoField"));
        Mockito.when(classElement.getEnclosedElements()).thenReturn(Collections.emptyList());

        generator().invoke(processor, classElement);

        Mockito.verify(filer, Mockito.never()).createSourceFile(Mockito.anyString(), Mockito.<Element>any());
    }

    /**
     * 对应测试用例 1.4：process 未声明注解轮次返回 true
     */
    @Test
    public void process_emptyAnnotations_returnsTrue() {
        Assertions.assertTrue(processor.process(Collections.emptySet(), roundEnv));
    }

    /**
     * 对应测试用例 1.5：process 字段元素跳过生成
     */
    @Test
    public void process_fieldElement_skipsGeneration() throws Exception {
        // 边界：被注解元素不是类（如字段）时跳过生成
        Element fieldElement = Mockito.mock(Element.class);
        Mockito.when(fieldElement.getKind()).thenReturn(ElementKind.FIELD);
        Mockito.doReturn(Collections.singleton(fieldElement))
            .when(roundEnv).getElementsAnnotatedWith(Mockito.<TypeElement>any());

        Assertions.assertTrue(
            processor.process(Collections.singleton(Mockito.mock(TypeElement.class)), roundEnv));
        Mockito.verify(filer, Mockito.never()).createSourceFile(Mockito.anyString(), Mockito.<Element>any());
    }

    /**
     * 对应测试用例 1.6：模板内联且零外部资源
     */
    @Test
    public void template_inlinedWithoutResource() throws Exception {
        // 核心约束：模板必须是类内常量，构建期不读取任何 classpath 资源（避免并行/多加载器下资源不可见）
        Field templateField = CAutowiredScanProcessor.class.getDeclaredField("TEMPLATE");
        templateField.setAccessible(true);
        String template = (String) templateField.get(null);

        Assertions.assertNotNull(template);
        Assertions.assertFalse(template.isEmpty());
        Assertions.assertTrue(template.contains("${packageName}"));
        Assertions.assertTrue(template.contains("${initClassName}"));
        Assertions.assertTrue(template.contains("${imports}"));
        Assertions.assertTrue(template.contains("${constructorParams}"));
        Assertions.assertTrue(template.contains("${constructorAssignments}"));
    }

    private java.lang.reflect.Method generator() throws Exception {
        java.lang.reflect.Method method =
            CAutowiredScanProcessor.class.getDeclaredMethod("generateInitClass", TypeElement.class);
        method.setAccessible(true);
        return method;
    }

    /**
     * javax.lang.model.element.Name 的最小实现，仅用于可为 null 的项名断言
     */
    private static class NameStub implements javax.lang.model.element.Name {

        private final String value;

        NameStub(String value) {
            this.value = value;
        }

        @Override
        public boolean contentEquals(CharSequence cs) {
            return value.contentEquals(cs);
        }

        @Override
        public int length() {
            return value.length();
        }

        @Override
        public char charAt(int index) {
            return value.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return value.subSequence(start, end);
        }

        @Override
        public String toString() {
            return value;
        }

    }

}
