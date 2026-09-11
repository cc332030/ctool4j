package com.c332030.ctool4j.transaction.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * <p>
 * Description: CTransactional
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CTransactional} 是 Spring {@code @Transactional} 的元注解封装（{@code @AliasFor} 映射）， 为调用方提供默认值更友好的事务声明式注解。它本身被 {@code @Transactional} 标注， 可通过 Spring 的 {@code AnnotatedElementUtils} 合并解析出等价的 {@code @Transactional} 语义。</p>
 * <p>提供四个常用属性映射：</p>
 * <ul>
 *   <li>{@code propagation}：传播行为，默认 {@code Propagation.REQUIRED}</li>
 *   <li>{@code isolation}：隔离级别，默认 {@code Isolation.DEFAULT}</li>
 *   <li>{@code readOnly}：只读，默认 {@code false}</li>
 *   <li>{@code rollbackFor}：回滚异常类型，默认 {@code Exception.class}</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>不指定任何属性</td>
 *     <td>使用默认值：REQUIRED / DEFAULT / false / Exception</td>
 *   </tr>
 *   <tr>
 *     <td>类或方法任一层级标注</td>
 *     <td>均可被 Spring 识别（{@code @Target} 含 TYPE 与 METHOD）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要声明式事务的 Service 类或方法，希望统一使用项目自定义的事务注解风格。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不新增 Spring {@code @Transactional} 之外的属性；需要更多事务配置（如 {@code noRollbackFor}、</li>
 *   <li>{@code timeout}、{@code transactionManager}）时需直接使用 {@code @Transactional} 或扩展本注解。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>仅封装了最常用的 4 个属性，其余 Spring 事务属性未透传，使用完整事务能力时需回退到</li>
 *   <li>{@code @Transactional}。</li>
 *   <li>{@code rollbackFor} 默认 {@code Exception.class}，与 Spring 默认行为（运行时异常回滚、受检异常</li>
 *   <li>默认不回滚）在受检异常处理上有所不同——本注解默认对受检异常也回滚，调用方需注意。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>通过 {@code @AliasFor(annotation = Transactional.class)} 逐属性映射到 Spring 原注解，</li>
 *   <li>使 {@code @CTransactional} 标注的类/方法可被 Spring 事务基础设施识别为事务性。</li>
 *   <li>默认值与 Spring {@code @Transactional} 的默认值保持一致，仅在 {@code rollbackFor} 上显式</li>
 *   <li>声明默认 {@code Exception.class}（等价于 Spring 默认的全部异常回滚语义）。</li>
 *   <li>{@code @Target} 支持类型与方法两级；{@code @Inherited} 使子类继承父类上的事务注解。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>纯注解声明，无业务逻辑；通过标准 Spring 元注解机制实现语义等价。</li>
 *   <li>属性均通过 {@code @AliasFor} 显式指定映射目标，避免依赖隐式别名解析。</li>
 * </ul>
 *
 * @since 2025/9/21
 * @version 1.0
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)

@Transactional
public @interface CTransactional {

    @AliasFor(annotation = Transactional.class)
    Propagation propagation() default Propagation.REQUIRED;

    @AliasFor(annotation = Transactional.class)
    Isolation isolation() default Isolation.DEFAULT;

    @AliasFor(annotation = Transactional.class)
    boolean readOnly() default false;

    @AliasFor(annotation = Transactional.class)
    Class<? extends Throwable>[] rollbackFor() default Exception.class;

}
