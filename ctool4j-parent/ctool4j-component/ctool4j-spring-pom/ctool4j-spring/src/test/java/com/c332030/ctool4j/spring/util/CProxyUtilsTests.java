package com.c332030.ctool4j.spring.util;

import com.c332030.ctool4j.spring.util.CProxyUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * <p>
 * Description: CProxyUtilsTests
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按「代理判定 / Spring 元数据解析 / 兜底 / 普通对象与 null / 包名」几个维度组织。</li>
 *   <li>用**真实代理**而非 mock：JDK 动态代理由 {@code Proxy.newProxyInstance} 生成、
 *       Spring JDK / CGLIB 代理由 {@code ProxyFactory} 织入。</li>
 *   <li>能力边界单列：携带不了目标元数据的代理（裸 JDK 动态代理）按设计返回代理类自身，用正例固化，
 *       避免后续被误判为缺陷。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对「能解析到目标类时取目标类、否则取自身、null 返回 null」的约定。</li>
 *   <li>依据测试方法（等价类/边界值/分支覆盖）：对象形态分 普通 / JDK 代理 / 有元数据无目标实例的 CGLIB 代理 / Spring 代理 / null，
 *       取值分 类 / 全限定名 / 包名 三类。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：isProxy 对普通对象 / JDK 动态代理 / CGLIB 形态代理 / Spring 代理 / null 的判定；getRealClass 对普通对象、
 *       裸 JDK 动态代理、有元数据无目标实例的 CGLIB 代理、Spring 代理的取值；getRealClassName / getRealPackageName /
 *       getPackageName 的取值与无包名兜底。</li>
 *   <li>未覆盖：真实 Spring 容器织入的代理（属 ctool4j-spring 的用例）、MyBatis Mapper 等依赖外部框架的实例。</li>
 * </ul>
 * <h2>代理判定</h2>
 * <ul>
 *   <li>1.1 isProxy：普通对象 false、JDK 动态代理 true、Spring 代理 true、CGLIB 形态 Spring 代理 true、null false（isProxy）</li>
 * </ul>
 * <h2>真实类解析</h2>
 * <ul>
 *   <li>2.1 getRealClass：Spring 代理取目标来源的目标类（getRealClass）</li>
 *   <li>2.2 getRealClass/getRealClassName：裸 JDK 动态代理携带不了目标元数据，返回代理类自身；有元数据无目标实例的 CGLIB 代理退回静态目标类型（getRealClass）
 *       （对照用例 2.3：有元数据且能取到目标实例的 CGLIB 代理，取目标实例的实际类型）</li>
 * </ul>
 * <h2>普通对象与 null</h2>
 * <ul>
 *   <li>3.1 三个解析方法：null 入参返回 null、普通对象返回自身（getRealClass / getRealClassName / getRealPackageName）</li>
 * </ul>
 * <h2>包名</h2>
 * <ul>
 *   <li>4.1 getPackageName：普通类返回包名，基础类型、数组类与 null 返回 null，JDK 代理类返回包名（getPackageName）</li>
 * </ul>
 *
 * @see CProxyUtils
 * @since 2026/9/12
 * @version 1.1
 */
public class CProxyUtilsTests {

    /**
     * 业务接口（JDK 动态代理的目标接口）
     */
    public interface IOrderService {

        /**
         * 获取服务名
         *
         * @return 服务名
         */
        String getServiceName();

    }

    /**
     * 业务实现：既是 JDK 动态代理的目标，也是 Spring CGLIB 代理的父类
     */
    public static class OrderService implements IOrderService {

        /**
         * 获取服务名
         *
         * @return 服务名
         */
        @Override
        public String getServiceName() {
            return "order";
        }

    }

    /**
     * 创建 CGLIB 代理（Spring {@code ProxyFactory} 按类代理，回调调用父类实现）
     * <p>回调自带 Spring 的 {@code DynamicAdvisedInterceptor}，但未关联目标实例，
     * 属「有 CGLIB 形态、无目标实例元数据」的形态。</p>
     *
     * @return CGLIB 代理对象（类型为业务类的子类，故以 {@code Object} 返回）
     */
    private Object newCglibProxy() {

        val proxyFactory = new ProxyFactory();
        proxyFactory.setTargetClass(OrderService.class);
        proxyFactory.setProxyTargetClass(true);
        return proxyFactory.getProxy();
    }

    /**
     * 创建 JDK 动态代理（无目标元数据）
     *
     * @return JDK 动态代理对象
     */
    private IOrderService newJdkProxy() {
        return (IOrderService)Proxy.newProxyInstance(
            CProxyUtilsTests.class.getClassLoader(),
            new Class<?>[]{IOrderService.class},
            (proxy, method, args) -> null
        );
    }

    /**
     * 测试代理判定
     * 对应测试用例 1.1：普通对象 false、JDK 动态代理 true、Spring 代理 true、CGLIB 形态 Spring 代理 true、null false
     */
    @Test
    public void isProxy() {

        Assertions.assertFalse(CProxyUtils.isProxy(null));
        Assertions.assertFalse(CProxyUtils.isProxy(new OrderService()));

        val jdkProxy = newJdkProxy();
        Assertions.assertNotEquals(OrderService.class, jdkProxy.getClass());
        Assertions.assertTrue(CProxyUtils.isProxy(jdkProxy));

        val springProxy = newSpringProxy(new OrderService());
        Assertions.assertNotEquals(OrderService.class, springProxy.getClass());
        Assertions.assertTrue(CProxyUtils.isProxy(springProxy));

        // CGLIB 形态的 Spring 代理（按类代理）同样携带 Advised 元数据，isProxy 判定为 true
        val cglibProxy = newCglibProxy();
        Assertions.assertNotEquals(OrderService.class, cglibProxy.getClass());
        Assertions.assertTrue(CProxyUtils.isProxy(cglibProxy));

    }

    /**
     * 创建 Spring 代理（{@code ProxyFactory} 织入，携带目标来源元数据）
     *
     * @param target 目标对象
     * @return Spring 代理对象
     */
    private Object newSpringProxy(Object target) {

        val proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(IOrderService.class);
        proxyFactory.setTarget(target);

        return proxyFactory.getProxy();
    }

    /**
     * 测试 Spring 元数据解析
     * 对应测试用例 2.1：Spring 代理取目标来源的目标类
     */
    @Test
    public void springProxy() {

        val service = new OrderService();
        val proxy = newSpringProxy(service);

        Assertions.assertNotEquals(OrderService.class, proxy.getClass());
        Assertions.assertTrue(CProxyUtils.isProxy(proxy));
        Assertions.assertEquals(OrderService.class, CProxyUtils.getRealClass(proxy));
        Assertions.assertEquals(OrderService.class.getName(), CProxyUtils.getRealClassName(proxy));
        Assertions.assertEquals(
            OrderService.class.getPackage().getName(),
            CProxyUtils.getRealPackageName(proxy)
        );

    }

    /**
     * 测试能力边界
     * 对应测试用例 2.2：裸 JDK 动态代理携带不了目标元数据，返回代理类自身（不猜测业务类型）
     */
    @Test
    public void proxyWithoutTargetMetadata() {

        val jdkProxy = newJdkProxy();
        Assertions.assertEquals(jdkProxy.getClass(), CProxyUtils.getRealClass(jdkProxy));
        Assertions.assertEquals(jdkProxy.getClass().getName(), CProxyUtils.getRealClassName(jdkProxy));

        // 有元数据、但无目标实例的 CGLIB 代理：退回静态目标类型
        val cglibProxy = newCglibProxy();
        Assertions.assertNotEquals(OrderService.class, cglibProxy.getClass());
        Assertions.assertEquals(OrderService.class, CProxyUtils.getRealClass(cglibProxy));

    }

    /**
     * 测试普通对象与 null
     * 对应测试用例 3.1：null 返回 null、普通对象返回自身
     */
    @Test
    public void normalAndNull() {

        Assertions.assertNull(CProxyUtils.getRealClass(null));
        Assertions.assertNull(CProxyUtils.getRealClassName(null));
        Assertions.assertNull(CProxyUtils.getRealPackageName(null));

        val service = new OrderService();
        Assertions.assertEquals(OrderService.class, CProxyUtils.getRealClass(service));
        Assertions.assertEquals(OrderService.class.getName(), CProxyUtils.getRealClassName(service));
        Assertions.assertEquals(
            OrderService.class.getPackage().getName(),
            CProxyUtils.getRealPackageName(service)
        );

    }

    /**
     * 测试包名
     * 对应测试用例 4.1：普通类返回包名、null 与基础类型/数组类返回 null、JDK 代理类正常返回包名
     */
    @Test
    public void getPackageName() {

        Assertions.assertNull(CProxyUtils.getPackageName(null));
        Assertions.assertEquals(
            OrderService.class.getPackage().getName(),
            CProxyUtils.getPackageName(OrderService.class)
        );

        // 基础类型、数组类无 Package 定义（类名为描述符形式），返回 null
        Assertions.assertNull(CProxyUtils.getPackageName(int.class));
        Assertions.assertNull(CProxyUtils.getPackageName(OrderService[].class));

        // JDK 动态代理生成类由类加载器定义包名，可正常取到
        val jdkProxy = newJdkProxy();
        Assertions.assertEquals("com.sun.proxy", CProxyUtils.getPackageName(jdkProxy.getClass()));

    }

}
