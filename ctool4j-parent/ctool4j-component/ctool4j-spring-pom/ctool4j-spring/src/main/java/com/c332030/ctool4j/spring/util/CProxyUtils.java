package com.c332030.ctool4j.spring.util;

import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;

/**
 * <p>
 * Description: CProxyUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CProxyUtils} 为代理工具类，提供代理场景下的真实类解析：</p>
 * <ul>
 *   <li>{@code isProxy}：判定对象是否为代理（Spring AOP 代理 / JDK 动态代理 / CGLIB 代理）</li>
 *   <li>{@code getRealClass}：解析对象所属的真实业务类（能取到目标类时取目标类，否则取自身）</li>
 *   <li>{@code getRealClassName}：解析真实业务类全限定名</li>
 *   <li>{@code getRealPackageName}：解析真实业务类所在包名</li>
 *   <li>{@code getPackageName}：取类所在包名（取不到返回 null）</li>
 * </ul>
 * <h2>测试</h2>
 * <ul>
 *   <li>功能测试：{@code CProxyUtilsTests}（同包，代理判定 / 元数据解析 / 兜底 / 包名）</li>
 *   <li>性能对比：{@code CProxyUtilsPerfTests}（同包，{@code PerfTests} 为规范定式后缀，需显式触发，由根 {@code pom.xml} 排除规则挡在常规测试之外）</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>为什么不能直接用 {@code getClass()}</b></p>
 * <ul>
 *   <li>代理对象的类型是代理框架生成的类——CGLIB 为
 *       {@code com.example.OrderService$$EnhancerBySpringCGLIB$$1a2b3c4d}，JDK 动态代理为
 *       {@code com.sun.proxy.$Proxy12}（包名是代理框架的，不是业务包）。在基类/公共流程里用
 *       {@code getClass()} 取名，代理场景下拿到的既不是业务类名、也不是业务包名。</li>
 * </ul>
 * <p><b>解析路径（按目标元数据位置依次尝试）</b></p>
 * <ul>
 *   <li>1. 非 Spring 代理对象（不含 {@code Advised} 元数据）直接返回自身类型——这类对象不可能持有目标实例，
 *       无需任何解析；</li>
 *   <li>2. 惰性剥离多层代理：每层取 {@code TargetSource} 的目标实例，取到即返回其实例类型
 *       （实例可能被工厂换过实现，只有实例自带的信息最准），不在中间层做无用的类型解析；</li>
 *   <li>3. 没有目标实例时取静态目标类型（{@code AopUtils#getTargetClass}）；</li>
 *   <li>4. 兜底：都取不到时返回对象自身类型，不猜测业务类型
 *       （裸 JDK 动态代理返回代理类自身，CGLIB 子类代理返回可精确解析到的静态目标类型）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>{@code object} 为 null</td>
 *     <td>返回 null（getRealClass / getRealClassName / getRealPackageName / getPackageName）</td>
 *   </tr>
 *   <tr>
 *     <td>非代理对象</td>
 *     <td>返回对象自身类型（与 {@code getClass()} 等价）</td>
 *   </tr>
 *   <tr>
 *     <td>携带不了目标元数据的代理（裸 JDK 动态代理、MyBatis Mapper 等）</td>
 *     <td>返回代理类自身，不猜测业务类型；需要精确语义时由实现方显式声明（见 spring 模块的 {@code ICRealClass}）</td>
 *   </tr>
 *   <tr>
 *     <td>{@code clazz} 的 {@code Class#getPackage()} 与类加载器的包定义都取不到</td>
 *     <td>{@code getPackageName} 返回 null，{@code getRealPackageName} 返回 null
 *         （JDK 动态代理类由类加载器定义包名，可正常取到）</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要真实业务类/类名/包名的公共流程：日志、幂等键、路由、指标、权限等。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅解析类型归属，不做代理解包后的行为调用（需要调用目标方法请用 Spring AOP 提供的能力）；</li>
 *   <li>不作缓存：被解析对象在调用间不保证复用，缓存解析结果即内存泄漏（参照通用编码规范「缓存前先判断对象生命周期」）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>「真实类」来自代理对象自带的元数据：非 Spring 生成的代理携带不了这份元数据，返回代理类自身；
 *       需要精确语义时由实现方显式声明（见 spring 模块的 {@code ICRealClass}），属「显式声明优于魔法探测」的取舍。</li>
 *   <li>解析结果不缓存：被解析对象在调用间不保证复用，热路径如需缓存由调用方按其生命周期自行决策。</li>
 * </ul>
 *
 * @since 2025/11/22
 * @version 1.2
 */
@CustomLog
@UtilityClass
public class CProxyUtils {

    /**
     * 判定对象是否为代理
     * <p>先按 JDK {@code Proxy#isProxyClass} 判定（覆盖裸 {@code Proxy.newProxyInstance} 与
     * Spring 的 JDK 动态代理），再按 Spring {@code AopUtils#isAopProxy} 判定（覆盖 CGLIB 代理）；
     * 不用「类名包含 {@code $$}」之类的启发式规则。</p>
     *
     * @param object 待判定对象
     * @return 是否为代理对象；{@code object} 为 null 时返回 false
     */
    public boolean isProxy(Object object) {

        if (null == object) {
            return false;
        }

        val clazz = object.getClass();
        return Proxy.isProxyClass(clazz) || AopUtils.isAopProxy(object);
    }

    /**
     * 解析对象所属的真实业务类
     * <p>按「非代理快速返回 → 惰性剥离多层代理 → 静态目标类型 → 自身类型」解析：</p>
     * <ul>
     *   <li>1. 非 Spring 代理对象（不含 {@code Advised} 元数据、也不可能持有目标实例）直接返回自身类型，
     *       不做任何反射或类层次缓存查询；热路径常见形态（业务对象、裸 JDK 动态代理）走这一分支；</li>
     *   <li>2. 持有目标元数据的对象，惰性剥离多层代理：每层取 {@code Advised#getTargetSource#getTarget}，
     *       取到目标实例即停止（实例可能被工厂换过实现，实例自带的信息最准），不在中间层做无用的类型解析；</li>
     *   <li>3. 没有可用目标实例时取静态目标类型（CGLIB 子类代理等场景，由 {@code AopUtils#getTargetClass} 解析）；</li>
     *   <li>4. 都取不到时返回自身类型，不猜测业务类型。</li>
     * </ul>
     *
     * @param object 待解析对象
     * @return 真实业务类；{@code object} 为 null 时返回 null
     */
    public Class<?> getRealClass(Object object) {

        if (null == object) {
            return null;
        }

        // 1. 非 Spring 代理：不持有目标元数据，直接返回自身类型（等价于 org.springframework.util.ClassUtils#getUserClass）
        if (!(object instanceof Advised)) {
            return object.getClass();
        }

        // 2. 惰性剥离多层代理：有目标实例即返回它的实际类型，不在中间层做无用的类型解析
        Object current = object;
        while (current instanceof Advised) {

            val target = getTarget(current);
            if (null == target || target == current) {
                break;
            }

            current = target;
        }

        if (current != object) {
            return current.getClass();
        }

        // 3. 没有目标实例：取静态目标类型（如 CGLIB 子类代理的父类）
        val targetClass = AopUtils.getTargetClass(object);
        if (null != targetClass) {
            return targetClass;
        }

        // 4. 兜底：取不到目标业务类时返回自身类型，不猜测
        return object.getClass();
    }

    /**
     * 解析对象所属真实业务类的全限定类名
     *
     * @param object 待解析对象
     * @return 真实业务类全限定名；{@code object} 为 null 时返回 null
     */
    public String getRealClassName(Object object) {

        val realClass = getRealClass(object);
        if (null == realClass) {
            return null;
        }

        return realClass.getName();
    }

    /**
     * 解析对象所属真实业务类的包名
     *
     * @param object 待解析对象
     * @return 真实业务类包名；{@code object} 为 null 或无包名时返回 null
     */
    public String getRealPackageName(Object object) {

        val realClass = getRealClass(object);
        if (null == realClass) {
            return null;
        }

        return getPackageName(realClass);
    }

    /**
     * 获取类所在包名
     * <p>先取 {@code Class#getPackage()}（普通类、数组类均返回其元素类型的包）；为 null 时退回
     * 类加载器记录的包定义——动态生成的类（如 JDK 动态代理的 {@code com.sun.proxy.$ProxyN}）
     * 其 {@code getPackage()} 实测为 null，而其类名不含包名、类加载器仍记录着包定义。
     * 基础类型与数组类取不到包名时返回 null；不按类名截断反推（代理生成类与数组描述符的类名
     * 都不含包名，反推必然得到错误结果）。</p>
     *
     * @param clazz 类
     * @return 包名；{@code clazz} 为 null 或取不到包名时返回 null
     */
    public String getPackageName(Class<?> clazz) {

        if (null == clazz) {
            return null;
        }

        // 1. 先取 Class 自带的包（数组类取其元素类型的包）
        val pkg = clazz.getPackage();
        if (null != pkg) {
            return pkg.getName();
        }

        // 2. 退回类加载器记录的包定义（供 getPackage() 为 null 的动态生成类使用）
        val pkgName = getDeclaredPackageName(clazz);
        return null == pkgName || pkgName.isEmpty() ? null : pkgName;
    }

    /**
     * 取类加载器为类记录的包名
     * <p>仅适用于已由类加载器定义包的类；基础类型、数组类无包定义（数组类名为描述符形式，
     * 交给 {@code ClassUtils#getPackageName} 会得到描述符前缀而非包名），返回 null。</p>
     *
     * @param clazz 类
     * @return 包名；取不到时返回 null
     */
    private static String getDeclaredPackageName(Class<?> clazz) {

        if (clazz.isArray() || clazz.isPrimitive()) {
            return null;
        }

        val classLoader = clazz.getClassLoader();
        if (null == classLoader) {
            return null;
        }

        return ClassUtils.getPackageName(clazz.getName());
    }

    /**
     * 从代理对象元数据中取被代理的目标实例
     * <p>取最外层代理的 {@code Advised#getTargetSource#getTarget}（Spring 代理工厂织入的代理，
     * JDK 与 CGLIB 代理均适用）；非 {@code Advised} 对象、目标实例换取失败（{@code TargetSource}
     * 抛异常）或目标就是自身时返回 null，由调用方退回静态目标类型。</p>
     *
     * @param object 待解析对象
     * @return 目标实例；取不到时返回 null
     */
    private static Object getTarget(Object object) {

        if (!(object instanceof Advised)) {
            return null;
        }

        val targetSource = ((Advised)object).getTargetSource();
        if (null == targetSource) {
            return null;
        }

        try {
            return targetSource.getTarget();
        } catch (Exception e) {
            // 目标实例不可用（如 TargetSource 每次都重新获取且失败）时返回 null，退回静态目标类型
            log.debug("目标实例获取失败，退回静态目标类型，proxyClass: {}", object.getClass(), e);
            return null;
        }
    }

}
