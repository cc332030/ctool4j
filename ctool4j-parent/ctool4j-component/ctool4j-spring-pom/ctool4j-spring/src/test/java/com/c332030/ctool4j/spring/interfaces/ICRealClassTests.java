package com.c332030.ctool4j.spring.interfaces;

import com.c332030.ctool4j.spring.util.CProxyUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * <p>
 * Description: ICRealClassTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证核心承诺：**实现 {@code ICRealClass} 即得真实类/类名/包名，业务侧不写方法体**——
 *       默认实现直接委托 {@code CProxyUtils} 的同名方法。</li>
 *   <li>用真实代理（Spring {@code ProxyFactory} / JDK 动态代理）验证「基类实现接口 → 公共流程仍能取到业务类」这条业务主线。</li>
 *   <li>兜底单列：非代理对象退回 {@code getClass()}，不抛异常、不比不实现接口更差。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对「实现即得、解析不到退回 getClass()、不猜」的约定。</li>
 *   <li>依据测试方法（等价类/边界值）：实现形态分 接口默认实现 / 自定义覆盖 两类，
 *       对象形态分 普通 / CGLIB 代理 / JDK 代理 三类。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：默认实现返回真实类/类名/包名并与 {@code CProxyUtils} 一致；业务侧覆盖时以覆盖值为准；
 *       普通对象与代理对象的兜底行为。</li>
 *   <li>未覆盖：真实 Spring 容器织入的代理（依赖容器装配，本用例用 {@code ProxyFactory} 等价构造）。</li>
 * </ul>
 * <h2>默认实现（实现即得）</h2>
 * <ul>
 *   <li>1.1 getRealClass/getRealClassName/getRealPackageName：普通与 CGLIB 代理对象均取业务类信息（getRealClass）</li>
 *   <li>1.2 getRealClassName：业务侧覆盖默认实现时以覆盖后的返回值为准（getRealClassName）</li>
 * </ul>
 * <h2>兜底</h2>
 * <ul>
 *   <li>2.1 getRealClassName：非代理对象退回 getClass()，不抛异常（getRealClassName）</li>
 *   <li>2.2 getRealPackageName：与 {@code CProxyUtils} 结果一致（getRealPackageName）</li>
 * </ul>
 *
 * @see ICRealClass
 * @see CProxyUtils
 * @since 2026/9/12
 * @version 1.1
 */
public class ICRealClassTests {

    /**
     * 业务基类：实现 {@code ICRealClass} 但**不写任何方法体**，方法由接口默认实现提供
     */
    public static class OrderBase implements ICRealClass {

        /**
         * 业务方法（仅用于证明该类型可被代理）
         *
         * @return 服务名
         */
        public String getServiceName() {
            return "order";
        }

    }

    /**
     * 自定义覆盖默认实现的业务类
     */
    public static class CustomNameService implements ICRealClass {

        /**
         * 覆盖默认实现，按业务需要自定义返回值
         *
         * @return 自定义的真实类名
         */
        @Override
        public String getRealClassName() {
            return "com.example.custom.CustomName";
        }

    }

    /**
     * 创建 Spring CGLIB 代理（{@code ProxyFactory} + 目标实例，强制代理目标类）
     *
     * <p>与 Spring 容器装配出的代理形态一致：代理是业务类的子类，
     * {@code getRealClass()} 经代理元数据解析到目标业务类。</p>
     *
     * @param target 目标实例
     * @return CGLIB 代理对象
     */
    private OrderBase newCglibProxy(OrderBase target) {

        val factory = new ProxyFactory();
        factory.setTarget(target);
        factory.setProxyTargetClass(true);
        return (OrderBase)factory.getProxy();
    }

    /**
     * 创建 JDK 动态代理（handler 转发到目标对象）
     *
     * @param target 目标对象
     * @return JDK 动态代理对象
     */
    private Object newJdkProxy(OrderBase target) {
        return Proxy.newProxyInstance(
            ICRealClassTests.class.getClassLoader(),
            new Class<?>[]{ICRealClass.class},
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return method.invoke(target, args);
                }
            }
        );
    }

    /**
     * 测试默认实现（实现即得）
     * 对应测试用例 1.1：普通对象取到自身业务类信息，三个方法均与 CProxyUtils 一致
     */
    @Test
    public void defaultImplementation() {

        val base = new OrderBase();

        Assertions.assertEquals(OrderBase.class, base.getRealClass());
        Assertions.assertEquals(OrderBase.class.getName(), base.getRealClassName());
        Assertions.assertEquals(CProxyUtils.getRealPackageName(base), base.getRealPackageName());
        Assertions.assertEquals(OrderBase.class.getPackage().getName(), base.getRealPackageName());

    }

    /**
     * 测试默认实现（代理对象）
     * <p>补充覆盖 1.1 的代理形态：代理对象上三个方法均可正常调用、不抛异常，且与 {@code CProxyUtils} 口径一致。</p>
     */
    @Test
    public void defaultImplementationOnProxy() {

        val base = new OrderBase();

        // Spring CGLIB 代理：默认实现取到被代理的目标业务类（代理元数据里带目标实例）
        val cglibProxy = newCglibProxy(base);
        Assertions.assertNotEquals(OrderBase.class, cglibProxy.getClass());
        Assertions.assertEquals(OrderBase.class, cglibProxy.getRealClass());
        Assertions.assertEquals(OrderBase.class.getName(), cglibProxy.getRealClassName());
        Assertions.assertEquals(OrderBase.class.getPackage().getName(), cglibProxy.getRealPackageName());
        Assertions.assertEquals(CProxyUtils.getRealClass(cglibProxy), cglibProxy.getRealClass());
        Assertions.assertEquals(CProxyUtils.getRealClassName(cglibProxy), cglibProxy.getRealClassName());
        Assertions.assertEquals(CProxyUtils.getRealPackageName(cglibProxy), cglibProxy.getRealPackageName());

        // JDK 动态代理：对象里没有目标元数据，退回代理类自身（不猜测业务类型）；
        // 默认实现被 handler 转发到目标实例后，取到的是目标业务类
        val jdkProxy = newJdkProxy(base);
        Assertions.assertNotEquals(OrderBase.class, jdkProxy.getClass());
        Assertions.assertEquals(jdkProxy.getClass(), CProxyUtils.getRealClass(jdkProxy));
        Assertions.assertEquals(OrderBase.class, ((ICRealClass)jdkProxy).getRealClass());
        Assertions.assertEquals(OrderBase.class.getName(), ((ICRealClass)jdkProxy).getRealClassName());

    }

    /**
     * 测试业务侧覆盖默认实现
     * 对应测试用例 1.2：以覆盖后的返回值为准
     */
    @Test
    public void customImplementation() {

        Assertions.assertEquals("com.example.custom.CustomName", new CustomNameService().getRealClassName());

    }

    /**
     * 测试兜底
     * 对应测试用例 2.1、2.2：非代理对象退回 getClass()，不抛异常
     */
    @Test
    public void fallback() {

        val base = new OrderBase();

        Assertions.assertDoesNotThrow(base::getRealClass);
        Assertions.assertDoesNotThrow(base::getRealClassName);
        Assertions.assertDoesNotThrow(base::getRealPackageName);

        Assertions.assertNotNull(base.getRealClass());
        Assertions.assertNotNull(base.getRealClassName());

    }

}
