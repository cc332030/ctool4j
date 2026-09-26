package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.core.log.CLogUtils;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.definition.entity.base.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * <p>
 * Description: CClassUtilsTests
 * </p>
 * <p>{@code CClassUtils} 的测试用例</p>
 *
 * <p><b>JDK 版本约束（重要备注）</b>：本测试在最新 LTS 档位下编译运行（jdk8 档位见 src/test/java-jdk8 的同名文件）——样本含 {@code jdk.jfr.Event}
 * 等仅 JDK 8 存在的类型（JDK 9+ 已移除，测试代码在 JDK 9+ 无法编译）；本项目同样只允许 JDK 8 编译运行
 * （构建与 CI 均为 JDK 8），<b>不追求跨 JDK 版本通用</b>。若未来升级编译 JDK，须同步更换这些样本
 * 并更新本备注。</p>
 *
 * <p><b>用例设计思路</b>：按「字段对比 / 包名 / JDK 类判断 / 继承链 / 接口」五个维度组织，样本与断言口径如下：</p>
 * <ul>
 *   <li><b>样本只取 JDK 8 存在的类型</b>（见上「JDK 版本约束」）：包名前缀与 JDK 类判断覆盖
 *       {@code BASE_PACKAGES} 的 java/javax/jdk/sun 四段与 com.sun/com.oracle 的 com 首段，
 *       样本为 {@code jdk.jfr.Event}、{@code sun.misc.Unsafe}、{@code com.sun.net.httpserver.HttpServer}；</li>
 *   <li><b>字段对比按可观察输出断言</b>：{@code compareField} 是"打印差异表格"的诊断入口（无返回值），
 *       故断言其可观察结果与依赖的打印约定——日志门面按类型分派打印形态（表格载体 {@code StringBuilder}
 *       等 {@code CharSequence} 原样返回，保证对比表格不会被打成占位文本；业务类型按 {@code [类名]} 占位），
 *       并断言原对象未被 {@code compareField} 改动（只读诊断约定）；</li>
 *   <li><b>继承链/接口</b>：以实体基类真实继承链（CBaseEntity → CBaseTimeEntity → CBaseCreateTimeEntity → CId）
 *       验证顺序、去重保序与不递归接口继承（接口 Set 顺序与"父类链由子至父 + 每层声明顺序"逐一比对）。</li>
 * </ul>
 *
 * <p><b>设计依据</b>：依据 {@code CClassUtils} javadoc 对继承链顺序（由子至父、不含 Object）、接口去重保序、
 * JDK 类判断（基本类型或 {@code BASE_PACKAGES_START} 前缀）的约定；依据等价类/边界值覆盖（各类首段包名、
 * 多层继承、中间层、Object 自身、接口自身、无接口、非 JDK 包）。</p>
 *
 * <p><b>覆盖场景</b>：{@code compareField} 多类字段对比的日志入参形态与入参不可变；{@code getFirstPackage}
 * 六类包前缀；{@code isJdkClass} 六类包、基本类型与应用/第三方包边界；{@code getSuperClasses} 多层/中间层/
 * 父类为 Object/Object 自身/接口自身；{@code getInterfaces} 多层顺序与去重、接口继承不递归、单层、无接口。</p>
 * <p><b>未覆盖</b>：{@code isExistClass}/{@code getMap}/{@code findClasses}/{@code listSubClass}/{@code listAnnotatedClass}
 * （依赖 Spring 容器与类路径扫描，未在单测覆盖）；{@code compareField} 表格内容的逐列渲染（列宽填充、
 * 缺失占位属日志文本的排版细节，断言价值低，改为断言表格载体的打印形态与入参不可变）。</p>
 *
 * <p><b>用例编号索引</b>：1 字段对比（1.1-1.2）；2 包名（2.1）；3 JDK 类判断（3.1）；4 继承链（4.1）；5 接口（5.1）。
 * 各测试方法 javadoc 标注其编号与说明。</p>
 *
 * <h2>字段对比</h2>
 * <ul>
 *   <li>1.1 compareField：多类字段对比不抛异常且不改动入参（compareField）</li>
 *   <li>1.2 compareField：表格载体原样返回、业务字段值按类名占位（compareField_printAbleArgs）</li>
 * </ul>
 * <h2>包名</h2>
 * <ul>
 *   <li>2.1 getFirstPackage：java/javax/jdk/sun 与 com.sun 各包前缀（getFirstPackage）</li>
 * </ul>
 * <h2>JDK 类判断</h2>
 * <ul>
 *   <li>3.1 isJdkClass：六类包与基本类型为 true、应用自有包与第三方包为 false（isJdkClass）</li>
 * </ul>
 * <h2>继承链</h2>
 * <ul>
 *   <li>4.1 getSuperClasses：多层/中间层/父类为 Object/Object 自身/接口自身 各场景顺序正确（getSuperClasses）</li>
 * </ul>
 * <h2>接口</h2>
 * <ul>
 *   <li>5.1 getInterfaces：多层接口顺序去重保序、接口继承不递归、单层、无接口（getInterfaces）</li>
 * </ul>
 *
 * @since 2025/12/12
 * @version 1.2
 * @see CClassUtils
 */
public class CClassUtilsTests {

    /**
     * 测试类字段对比不抛异常且不改动入参
     * 对应测试用例 1.1：多类字段对比不抛异常且不改动入参
     * <p>测试意图：{@code compareField} 以变长参数接收多个类并打印字段差异表格；其契约是只读诊断——
     * 调用后类对象（含声明字段集合）必须与调用前一致，避免"诊断入口反向改动被诊断对象"。</p>
     */
    @Test
    public void compareField() {

        val fieldsBefore = declaredFieldNames(CBaseEntity.class);

        CClassUtils.compareField(CId.class, CBaseTimeEntity.class, CBaseEntity.class);

        Assertions.assertEquals(fieldsBefore, declaredFieldNames(CBaseEntity.class));

    }

    /**
     * 测试字段对比的表格载体与业务字段值的打印结果
     * 对应测试用例 1.2：表格载体原样返回、业务字段值按 {@code [类名]} 占位
     * <p>测试意图：{@code compareField} 的表格用 {@code StringBuilder} 拼接（属 {@code CharSequence}，
     * 日志门面判定可打印、原样输出，保证表格不被打成无信息的占位文本）；而对比表格里"类型名"一列由
     * {@code Field#getType().getSimpleName()} 取出、本身就是字符串。自定义业务类型默认按
     * {@code [类名]} 占位（判定结果按类缓存，故用 {@code CLogUtils#setJsonLog} 显式置为非 JSON 化后断言；
     * 该替身类仅本用例使用，覆盖不影响同 JVM 的其他用例）。</p>
     */
    @Test
    public void compareField_printAbleArgs() {

        val table = new StringBuilder("c332030");

        // 可打印类型（CharSequence）：原样返回，不按 [类名] 占位
        Assertions.assertSame(table, CLogUtils.getPrintAble(table));
        Assertions.assertSame("c332030", CLogUtils.getPrintAble("c332030"));

        // 业务类型（显式置为非 JSON 化）：按 [类名] 占位
        val notPrintAble = new BeanFactoryProxy();
        val typeName = notPrintAble.getClass().getName();

        CLogUtils.setJsonLog(BeanFactoryProxy.class, false);

        Assertions.assertEquals(
            CStrUtils.concat("", "[", typeName, "]"),
            CLogUtils.getPrintAble(notPrintAble)
        );

    }

    /**
     * 测试获取类所在包的首段名称
     * 对应测试用例 2.1：java/javax/jdk/sun/com.sun/com.oracle 各包前缀
     * <p>样本：{@code BASE_PACKAGES} 各段取一个 JDK 8 存在的类型
     * （java/javax/jdk/sun 四段各取一个，com.sun/com.oracle 两段同属 {@code com} 首段、取 com.sun 样本），
     * 仅保证 JDK 8 下可编译（jdk 段的 {@code jdk.jfr.Event} 为 JDK 8 专有类型，见类级「JDK 版本约束」）。</p>
     *
     * <p>{@code sun} 段样本经 {@link Class#forName(String)} 按名取类（而非直接引用 {@code sun.misc.Unsafe}）：
     * 直接引用会触发 javac 的 {@code Unsafe is internal proprietary API} 警告，而该警告在 JDK 8 下<b>无法用
     * {@code @SuppressWarnings} 抑制</b>（实测 {@code sunapi}/{@code all} 等键均无效），按名取类既不产生警告、
     * 也仍以真实的 {@code sun.*} 类作为样本。</p>
     */
    @Test
    public void getFirstPackage() throws ClassNotFoundException {

        Assertions.assertEquals("java", CClassUtils.getFirstPackage(String.class));
        Assertions.assertEquals("javax", CClassUtils.getFirstPackage(DataSource.class));
        Assertions.assertEquals("jdk", CClassUtils.getFirstPackage(jdk.jfr.Event.class));
        Assertions.assertEquals("sun", CClassUtils.getFirstPackage(Class.forName("sun.misc.Unsafe")));
        Assertions.assertEquals("com", CClassUtils.getFirstPackage(com.sun.net.httpserver.HttpServer.class));

    }

    /**
     * 测试是否为 JDK 类
     * 对应测试用例 3.1：六类包与基本类型为 true、应用自有包与第三方包为 false
     * <p>边界：应用自有包（本测试类）、第三方包（Jackson、Spring）不属于 {@code BASE_PACKAGES}，须为 false；
     * 基本类型（{@code int}）按 {@code BASE_CLASSES} 判定为 true。</p>
     *
     * <p>同 {@code getFirstPackage}：{@code sun} 段样本经 {@link Class#forName(String)} 按名取类，
     * 避免引用 {@code sun.misc.Unsafe} 触发 JDK 8 下无法抑制的 javac 警告。</p>
     */
    @Test
    public void isJdkClass() throws ClassNotFoundException {

        Assertions.assertTrue(CClassUtils.isJdkClass(String.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(DataSource.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(jdk.jfr.Event.class));
        Assertions.assertTrue(CClassUtils.isJdkClass(Class.forName("sun.misc.Unsafe")));
        Assertions.assertTrue(CClassUtils.isJdkClass(com.sun.net.httpserver.HttpServer.class));

        // 基本类型走 BASE_CLASSES
        Assertions.assertTrue(CClassUtils.isJdkClass(int.class));

        // 应用自有包 / 第三方包：非 JDK 类
        Assertions.assertFalse(CClassUtils.isJdkClass(CClassUtilsTests.class));
        Assertions.assertFalse(CClassUtils.isJdkClass(ObjectMapper.class));
        Assertions.assertFalse(CClassUtils.isJdkClass(BeanFactory.class));

    }

    /**
     * 测试获取类及其所有父类（不含 Object）
     * <p>顺序约定：类本身在前，沿继承链由子至父，直至顶层父类（Object 除外）</p>
     * 对应测试用例 4.1：多层/中间层/父类为 Object/Object 自身/接口自身 各场景顺序正确
     */
    @Test
    public void getSuperClasses() {

        // 多层继承链：CBaseEntity -> CBaseTimeEntity -> CBaseCreateTimeEntity -> CId，顺序由子至父，不含 Object
        Assertions.assertEquals(
            Arrays.asList(CBaseEntity.class, CBaseTimeEntity.class, CBaseCreateTimeEntity.class, CId.class),
            CClassUtils.getSuperClasses(CBaseEntity.class)
        );

        // 中间层级继承链
        Assertions.assertEquals(
            Arrays.asList(CBaseTimeEntity.class, CBaseCreateTimeEntity.class, CId.class),
            CClassUtils.getSuperClasses(CBaseTimeEntity.class)
        );

        // 父类为 Object 时仅返回类本身
        Assertions.assertEquals(
            Collections.singletonList(CId.class),
            CClassUtils.getSuperClasses(CId.class)
        );

        // 边界：Object 自身（do-while 至少执行一次，返回仅含自身）
        Assertions.assertEquals(
            Collections.singletonList(Object.class),
            CClassUtils.getSuperClasses(Object.class)
        );

        // 接口：无父类（getSuperclass 为 null），仅返回接口自身
        Assertions.assertEquals(
            Collections.singletonList(ICId.class),
            CClassUtils.getSuperClasses(ICId.class)
        );

    }

    /**
     * 测试获取类及其父类实现的所有接口
     * <p>顺序约定：按父类链由子至父遍历，每层按 getInterfaces 声明顺序，LinkedHashSet 去重保序；
     * 只取各类直接实现的接口，不递归接口继承（如 ICCreateUpdateBy 是 ICCreateUpdateByAndTime 的父接口，不会出现）</p>
     * 对应测试用例 5.1：多层接口顺序去重保序、接口继承不递归、单层、无接口
     */
    @Test
    public void getInterfaces() {

        // 各层直接接口按父类链由子至父顺序：CBaseEntity->ICCreateUpdateByAndTime、
        // CBaseTimeEntity->ICCreateUpdateTime、CBaseCreateTimeEntity->ICCreateTime、CId->ICId
        val expected = Arrays.asList(
            ICCreateUpdateByAndTime.class,
            ICCreateUpdateTime.class,
            ICCreateTime.class,
            ICId.class
        );

        val interfaces = CClassUtils.getInterfaces(CBaseEntity.class);
        Assertions.assertEquals(expected.size(), interfaces.size());

        // 顺序：遍历顺序须与"父类链由子至父 + 每层声明顺序"一致
        val actual = new ArrayList<Class<?>>(interfaces);
        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertEquals(expected.get(i), actual.get(i));
        }
        Assertions.assertEquals(new HashSet<Class<?>>(expected), new HashSet<Class<?>>(actual));

        // 接口继承不递归：ICCreateUpdateBy 仅作为 ICCreateUpdateByAndTime 的父接口存在，不应被收集
        Assertions.assertFalse(interfaces.contains(ICCreateUpdateBy.class));

        // 单层实现
        Assertions.assertEquals(
            Collections.singleton(ICId.class),
            CClassUtils.getInterfaces(CId.class)
        );

        // 无接口：Object 自身、接口本身均返回空集合
        Assertions.assertTrue(CClassUtils.getInterfaces(Object.class).isEmpty());
        Assertions.assertTrue(CClassUtils.getInterfaces(ICId.class).isEmpty());

    }

    /**
     * 不可打印类型的测试替身（仅用于验证 {@code [类名]} 占位，不实现任何行为）
     */
    private static class BeanFactoryProxy implements BeanFactory {

        @Override
        public Object getBean(String name) throws BeansException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getBean(String name, Object... args) throws BeansException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T getBean(Class<T> requiredType) throws BeansException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
            throw new UnsupportedOperationException();
        }

        // Spring 7 新增重载
        @Override
        public <T> ObjectProvider<T> getBeanProvider(ParameterizedTypeReference<T> requiredType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsBean(String name) {
            return false;
        }

        @Override
        public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
            return false;
        }

        @Override
        public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
            return false;
        }

        @Override
        public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
            return false;
        }

        @Override
        public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
            return false;
        }

        @Override
        public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
            return null;
        }

        @Override
        public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
            return null;
        }

        @Override
        public String[] getAliases(String name) {
            return new String[0];
        }

    }

    /**
     * 取类的实例字段名集合（用于入参不可变断言）
     *
     * @param type 类
     * @return 字段名集合
     */
    private HashSet<String> declaredFieldNames(Class<?> type) {

        val names = new HashSet<String>();
        for (Field field : type.getDeclaredFields()) {
            names.add(field.getName());
        }
        return names;
    }

}
