package com.c332030.ctool4j.spring.interfaces;

import com.c332030.ctool4j.spring.util.CProxyUtils;

/**
 * <p>
 * Description: ICRealClass
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICRealClass} 为「真实业务类名」契约接口：</p>
 * <ul>
 *   <li>{@code getRealClassName()}：返回实现者所属的真实业务类全限定名（如
 *       {@code com.example.service.impl.OrderServiceImpl}）</li>
 *   <li>{@code getRealClass()}：返回实现者所属的真实业务类</li>
 *   <li>{@code getRealPackageName()}：返回实现者所属的真实业务类包名</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>解决什么问题</b></p>
 * <ul>
 *   <li>在基类/公共流程中需要「当前业务类的类名、类或包名」时，直接调用 {@code getClass()}
 *       在代理场景下会拿到代理生成类（CGLIB 的 {@code Xxx$$EnhancerBySpringCGLIB$$...}、
 *       JDK 动态代理的 {@code com.sun.proxy.$ProxyN}），类名与包名都不再是业务类，达不到预期。</li>
 *   <li>「当前实现是谁」这件事由业务基类自己知道（它就是被装配的那一层）：基类实现本接口，
 *       公共流程统一改调本接口方法，即可在代理形态下尽量拿到业务类。</li>
 * </ul>
 * <p><b>怎么用：实现接口即可，不用写方法体</b></p>
 * <ul>
 *   <li>业务基类只声明 {@code implements ICRealClass}，方法体由本接口的默认实现提供：
 *       默认实现委托 {@link CProxyUtils} 的 {@code getReal*} 系列解析，解析不到时退回代理对象自身类型
 *       （类名可能形如 {@code ...$$EnhancerBySpringCGLIB$$...}）。「实现即得」——业务侧无需写样板方法体。</li>
 *   <li>需要定制返回值时再覆盖（可选，不是使用本接口的前提）。</li>
 * </ul>
 * <p><b>与工具类的分工</b></p>
 * <ul>
 *   <li>能改业务代码（自己声明的基类）→ 实现本接口：公共流程统一从契约取值。</li>
 *   <li>改不了业务代码（第三方对象、只想临时解析某个代理对象）→ 用工具类
 *       （{@link CProxyUtils#getRealClassName(Object)} 等）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>解析不到真实类（非代理、或代理未携带目标元数据）</td>
 *     <td>退回代理对象自身类型——与直接用 {@code getClass()} 同级，不会更差</td>
 *   </tr>
 *   <tr>
 *     <td>对象为 null</td>
 *     <td>返回 null（实例方法，正常调用不会为 null）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>在代理场景下需要真实业务类/类名/包名的基类与公共流程；希望「实现契约即得正确值、不写样板方法体」的场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li><b>不承载 Bean 名语义</b>：Bean 名是容器内的实例标识、可由人工指定为任意字符串（无命名规则），
 *       与类名不是同一概念、也不能互相推导，故命名用 {@code RealClass} 而非 {@code BeanName}。</li>
 *   <li>改不了其代码的第三方/代理对象，本接口无能为力（改用工具类解析）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>`取舍` 默认实现依赖代理元数据解析能力：解析不到时退化为 {@code getClass()}，
 *       属「显式声明 + 可用则更好」的取舍。</li>
 *   <li>`已知限制` 携带不了目标元数据的代理（裸 {@code Proxy.newProxyInstance}、MyBatis Mapper 等）
 *       解析结果为代理类自身，不猜测业务类型——需要精确语义时由实现方覆盖本方法显式声明。</li>
 * </ul>
 * <h2>模块归属</h2>
 * <ul>
 *   <li>本接口放 `ctool4j-spring` 而非 `ctool4j-core`：默认实现依赖 {@code CProxyUtils}
 *       （代理解析基于 spring-core 的 {@code AopUtils}），与 Spring 生态同属一层，
 *       放 core 会把「代理」概念与 spring-core 依赖带入基础工具模块。</li>
 * </ul>
 *
 * @see CProxyUtils
 * @since 2026/9/12
 * @version 1.1
 */
public interface ICRealClass {

    /**
     * 获取真实业务类
     * <p>默认实现委托 {@link CProxyUtils#getRealClass(Object)}；解析不到时退回
     * {@code getClass()}（实现方被代理时为代理类）。实现方无需写方法体，需要定制返回值时再覆盖本方法。</p>
     *
     * @return 真实业务类
     */
    default Class<?> getRealClass() {
        return CProxyUtils.getRealClass(this);
    }

    /**
     * 获取真实业务类的全限定类名
     * <p>默认实现委托 {@link CProxyUtils#getRealClassName(Object)}；解析不到时退回
     * {@code getClass().getName()}（实现方被代理时为代理类名）。实现方无需写方法体，需要定制返回值时再覆盖本方法。</p>
     *
     * @return 真实业务类全限定名（如 {@code com.example.service.impl.OrderServiceImpl}）
     */
    default String getRealClassName() {
        return CProxyUtils.getRealClassName(this);
    }

    /**
     * 获取真实业务类的包名
     * <p>默认实现委托 {@link CProxyUtils#getRealPackageName(Object)}；解析不到或无包名时返回 null。</p>
     *
     * @return 真实业务类包名；无包名时返回 null
     */
    default String getRealPackageName() {
        return CProxyUtils.getRealPackageName(this);
    }

}
