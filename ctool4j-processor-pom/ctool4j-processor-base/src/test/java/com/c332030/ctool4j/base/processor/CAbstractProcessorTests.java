package com.c332030.ctool4j.base.processor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.UncheckedIOException;

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
 * 是 {@link CAbstractProcessor} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>通过子类 TestProcessor 验证基类约定的支持源版本、注解类型、process 与 init 的默认行为。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 Java 8 源版本支持、process/init 约定行为的约定。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：支持 Java 8；注解类型默认空；process 返回 false；init 不抛异常；资源读取成功路径与不存在路径。</li>
 *   <li>未覆盖：真实编译期注解处理流程。</li>
 * </ul>
 * <h2>注解处理器基类行为</h2>
 * <ul>
 *   <li>1.1 支持 Java 8 源版本（{@code supportedSourceVersion_RELEASE8}）</li>
 *   <li>1.2 注解类型默认空（{@code supportedAnnotationTypes_empty}）</li>
 *   <li>1.3 process 返回 false（{@code process_returnsFalse}）</li>
 *   <li>1.4 init 不抛异常（{@code init_noException}）</li>
 *   <li>1.5 读取存在的资源返回内容（{@code loadResource_exist_returnsContent}）</li>
 *   <li>1.6 读取不存在的资源抛 UncheckedIOException（{@code loadResource_missing_throws}）</li>
 * </ul>
 *
 * @since 2026/8/14
 * @version 1.0
 */
public class CAbstractProcessorTests {

    private TestProcessor processor;

    /**
     * 每个用例执行前的准备
     */
    @BeforeEach
    public void setUp() {
        processor = new TestProcessor();
    }

        /**
         * 对应测试用例 1.1：支持 Java 8 源版本（{@code supportedSourceVersion_RELEASE8}）
         */
    @Test
    public void supportedSourceVersion_RELEASE8() {
        Assertions.assertEquals(SourceVersion.RELEASE_8, processor.getSupportedSourceVersion());
    }

        /**
         * 对应测试用例 1.2：注解类型默认空（{@code supportedAnnotationTypes_empty}）
         */
    @Test
    public void supportedAnnotationTypes_empty() {
        Assertions.assertTrue(processor.getSupportedAnnotationTypes().isEmpty());
    }

        /**
         * 对应测试用例 1.3：process 返回 false（{@code process_returnsFalse}）
         */
    @Test
    public void process_returnsFalse() {
        Assertions.assertFalse(processor.process(Collections.emptySet(), Mockito.mock(RoundEnvironment.class)));
    }

        /**
         * 对应测试用例 1.4：init 不抛异常（{@code init_noException}）
         */
    @Test
    public void init_noException() {
        Assertions.assertDoesNotThrow(() -> processor.init(Mockito.mock(ProcessingEnvironment.class)));
    }

    /**
     * 对应测试用例 1.5：读取存在的资源返回内容
     */
    @Test
    public void loadResource_exist_returnsContent() {
        // 正例：类路径下存在该资源时原样返回其文本内容
        Assertions.assertEquals("hello\n", processor.loadResourceForTest("/templates/test-resource.txt"));
    }

    /**
     * 对应测试用例 1.6：读取不存在的资源抛 UncheckedIOException
     */
    @Test
    public void loadResource_missing_throws() {
        // 反例：资源不存在时抛 UncheckedIOException（受检异常包装），由调用方决定是否中断构建
        Assertions.assertThrowsExactly(
            UncheckedIOException.class,
            () -> processor.loadResourceForTest("/templates/not-exist.txt"));
    }

    /**
     * CAbstractProcessor 的测试子类：process 为抽象方法（AbstractProcessor 未提供默认实现），
     * 子类必须实现；这里返回 false 验证基类约定的最小实现可正常使用。
     * loadResourceForTest 仅用于把 protected 的资源读取能力暴露给测试断言。
     */
    private static class TestProcessor extends CAbstractProcessor {

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            return false;
        }

        String loadResourceForTest(String path) {
            return loadResource(path);
        }
    }

}
