package com.c332030.ctool4j.base.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.SourceVersion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Description: CAbstractProcessor
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CAbstractProcessor}：注解处理器基类，统一源版本约定与资源读取。</p>
 * <ul>
 *   <li>源版本：统一声明支持 Java 8（{@code getSupportedSourceVersion}）。</li>
 *   <li>资源读取：以当前线程上下文类加载器（TCCL）读取类路径资源（{@code loadResource}）。</li>
 * </ul>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>{@code getSupportedSourceVersion} 返回 {@code RELEASE_8}，与项目 Java 8 语言目标一致。</li>
 *   <li>资源读取不依赖处理器自身的 {@code ClassLoader}：注解处理器运行在编译期，资源可能位于
 *   processorpath、classpath、模块层等不同位置，以 TCCL 且回退至本类加载器的方式可覆盖各类装载路径。</li>
 * </ul>
 *
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>TCCL 取不到处理器自身资源</td>
 *     <td>回退到本类的 {@code ClassLoader} 再次尝试</td>
 *   </tr>
 *   <tr>
 *     <td>资源确实不存在</td>
 *     <td>抛 {@link UncheckedIOException}，由调用方决定「中断构建」或「降级跳过生成」</td>
 *   </tr>
 * </table>
 *
 * <h2>适用范围</h2>
 * <ul>
 *   <li>本项目各编译期注解处理器的公共基类。</li>
 * </ul>
 *
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>抽象类，不可直接实例化，子类须实现 {@code process}。</li>
 *   <li>不提供多轮次（round）状态管理，需要时由子类自行维护。</li>
 * </ul>
 *
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>资源内容整体读入字符串（模板资源体量小），不做流式处理。</li>
 * </ul>
 *
 * @since 2026/5/31
 * @version 1.0
 */
public abstract class CAbstractProcessor extends AbstractProcessor {

    /**
     * 支持的 Java 源版本
     *
     * @return Java 8
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    /**
     * 读取类路径下的文本资源并返回其内容
     *
     * <p><b>读取顺序</b>：先以当前线程上下文类加载器（TCCL）读取，取不到再回退到本类的类加载器。</p>
     *
     * @param path 类路径绝对路径，以 {@code /} 开头
     * @return 资源文本内容
     * @throws UncheckedIOException 资源不存在或读取失败时抛出
     */
    protected String loadResource(String path) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = null == loader ? null : loader.getResourceAsStream(path);
        if (null == is) {
            is = getClass().getResourceAsStream(path);
        }
        if (null == is) {
            throw new UncheckedIOException(new IOException("resource not found: " + path));
        }

        try (InputStream stream = is;
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
