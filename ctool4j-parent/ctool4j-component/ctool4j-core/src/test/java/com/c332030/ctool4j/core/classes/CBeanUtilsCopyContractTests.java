package com.c332030.ctool4j.core.test.classes;

import cn.hutool.core.date.DateUtil;
import com.c332030.ctool4j.core.classes.CBeanUtils;
import com.c332030.ctool4j.core.util.CList;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * Description: CBeanUtilsCopyContractTests
 * </p>
 *
 * <p>固化 {@code CBeanUtils} 的<b>单属性复制契约</b>：一个属性"一次决议 + 一次执行 + 一次写入"。</p>
 * <p>决议只由 (值的运行时类型, 目标字段声明类型) 决定，实现上只有一个决议点与一个执行点；
 * 旧实现"先 {@code convertOpt} 写一次、再深拷贝覆盖一次"的双流程已删除，
 * 深拷贝是决议的结果之一，而非后置补写步骤。</p>
 *
 * <h2>决议表（本类逐行固化）</h2>
 * <table border="1">
 *   <caption>决议表</caption>
 *   <tr><th>值的运行时类型</th><th>目标声明类型</th><th>决议</th><th>用例</th></tr>
 *   <tr><td>不可变（String/包装类/Number/时间/枚举/Class 等）</td><td>可赋值</td><td>共享</td>
 *       <td>1.1 immutable_assignable_shared</td></tr>
 *   <tr><td>不可变</td><td>不可赋值 + 有转换器</td><td>转换</td>
 *       <td>1.2 immutable_unassignable_convert</td></tr>
 *   <tr><td>不可变</td><td>不可赋值 + 无转换器</td><td>跳过</td>
 *       <td>1.3 immutable_unassignable_skip</td></tr>
 *   <tr><td>可变（集合/Map/数组/Bean/Date 等）</td><td>可赋值</td><td>深拷贝</td>
 *       <td>2.1 mutable_assignable_deepCopy</td></tr>
 *   <tr><td>可变</td><td>不可赋值 + 有转换器</td><td>转换（转换器优先，不被深拷贝覆盖）</td>
 *       <td>2.2 mutable_unassignable_converterFirst</td></tr>
 *   <tr><td>可变</td><td>不可赋值 + 无转换器 + 目标不可变</td><td>跳过</td>
 *       <td>2.3 mutable_unassignable_immutableTarget_skip</td></tr>
 *   <tr><td>可变</td><td>不可赋值 + 无转换器 + 目标可变（自定义类）</td><td>跨类型结构深拷贝</td>
 *       <td>2.4 mutable_unassignable_mutableTarget_crossTypeDeepCopy</td></tr>
 *   <tr><td>可变</td><td>不可赋值 + 目标为 JDK 非拷贝协议类型</td><td>跳过</td>
 *       <td>2.5 mutable_unassignable_jdkTarget_skip</td></tr>
 *   <tr><td>可变（JDK 非拷贝协议，如 StringBuilder）</td><td>可赋值（同型）</td><td>共享（不做 JDK 结构拷贝）</td>
 *       <td>2.6 mutable_jdkSameType_shared</td></tr>
 *   <tr><td>Class</td><td>可赋值</td><td>共享（Class 不参与拷贝）</td>
 *       <td>3.1 classField_shared</td></tr>
 * </table>
 *
 * <h2>入口一致性（同一决议、同一执行）</h2>
 * <ul>
 *   <li>4.1 objectSourceAndMapSource_sameResolution：对象源与 Map 源对同一字段决议一致（深拷贝字段均为副本、共享字段为同一引用）</li>
 *   <li>4.2 supplierEntry_sameAsDirect：supplier 入口与直连入口一致（不再经 Map 中转）</li>
 * </ul>
 *
 * <h2>本类覆盖不到的</h2>
 * <ul>
 *   <li>“只写一次”无法通过外部观察断言（写入走字段句柄，不经过 setter 方法），
 *   由实现结构保证（单一决议点 + 单一执行点 + 每循环一处写入），并在类 javadoc 中说明；</li>
 *   <li>深拷贝的元素/键值/环状引用/容器实现等细节见 {@code CBeanUtilsDeepCopyTests}。</li>
 * </ul>
 *
 * @since 2026/9/17
 * @version 1.0
 */
class CBeanUtilsCopyContractTests {

    /**
     * <p>对应测试用例 1.1～1.3：不可变值（String）侧的四类决议</p>
     */
    @Test
    void immutable_assignable_shared() {

        val from = new ScalarSource();
        from.setName("n");
        from.setAge(18);

        val to = CBeanUtils.copy(from, ScalarTarget.class);

        Assertions.assertSame(from.getName(), to.getName(), "不可变值可赋值 ⇒ 共享引用");
        Assertions.assertEquals(18, to.getAge());
    }

    /**
     * <p>对应测试用例 1.2：不可变值不可赋值 ⇒ 转换器</p>
     */
    @Test
    void immutable_unassignable_convert() {

        val from = new StringSource();
        from.setValue("123");

        val to = CBeanUtils.copy(from, IntTarget.class);

        Assertions.assertEquals(123, to.getValue(), "String→int 应命中转换器");
    }

    /**
     * <p>对应测试用例 1.3：不可变值不可赋值且无转换器 ⇒ 跳过（不抛异常）</p>
     */
    @Test
    void immutable_unassignable_skip() {

        val from = new StringSource();
        from.setValue("abc");

        val to = CBeanUtils.copy(from, StringBufferTarget.class);

        Assertions.assertNull(to.getValue(), "String→StringBuffer 无转换器 ⇒ 跳过");
    }

    /**
     * <p>对应测试用例 2.1：可变值可赋值 ⇒ 深拷贝（集合与 Bean 均副本）</p>
     */
    @Test
    void mutable_assignable_deepCopy() {

        val from = new MutableSource();
        from.setRoles(CList.of("r1"));
        from.setInner(newInner("inner"));

        val to = CBeanUtils.copy(from, MutableTarget.class);

        Assertions.assertNotSame(from.getRoles(), to.getRoles(), "集合字段应深拷贝");
        Assertions.assertEquals(from.getRoles(), to.getRoles());
        Assertions.assertNotSame(from.getInner(), to.getInner(), "Bean 字段应深拷贝");
        Assertions.assertEquals("inner", to.getInner().getName());
    }

    /**
     * <p>对应测试用例 2.2：可变值不可赋值 ⇒ 显式转换器优先（一次决议，不再被深拷贝覆盖）</p>
     */
    @Test
    void mutable_unassignable_converterFirst() {

        val date = new Date(1700000000000L);
        val from = new DateSource();
        from.setDate(date);

        // 目标 String：命中 Date→String 格式化转换（不是 toString，也不是深拷贝）
        val toStr = CBeanUtils.copy(from, DateStrTarget.class);
        Assertions.assertEquals(DateUtil.formatDateTime(date), toStr.getDate());

        // 目标 Long：命中 Date→Long 毫秒转换
        val toMills = CBeanUtils.copy(from, DateMillisTarget.class);
        Assertions.assertEquals(date.getTime(), toMills.getDate());
    }

    /**
     * <p>对应测试用例 2.3：可变值不可赋值、无转换器、目标不可变 ⇒ 跳过</p>
     */
    @Test
    void mutable_unassignable_immutableTarget_skip() {

        val from = new ListSource();
        from.setRoles(CList.of("r1"));

        val to = CBeanUtils.copy(from, ListToStrTarget.class);

        Assertions.assertNull(to.getRoles(), "集合→String：容器源不参与转换且目标装不下 ⇒ 跳过");
    }

    /**
     * <p>对应测试用例 2.4：可变值不可赋值、无转换器、目标可变（自定义类）⇒ 跨类型结构深拷贝</p>
     */
    @Test
    void mutable_unassignable_mutableTarget_crossTypeDeepCopy() {

        val from = new InnerSource();
        from.setBean(newInner("inner"));

        val to = CBeanUtils.copy(from, InnerVoSource.class);

        Assertions.assertNotSame(from.getBean(), to.getBean(), "跨类型 Bean 字段应按目标声明类型结构拷贝");
        Assertions.assertEquals("inner", to.getBean().getName());
    }

    /**
     * <p>对应测试用例 2.5：可变值不可赋值、目标为 JDK 非拷贝协议类型 ⇒ 跳过</p>
     */
    @Test
    void mutable_unassignable_jdkTarget_skip() {

        val from = new StringBuilderSource();
        from.setSb(new StringBuilder("sb"));

        val to = CBeanUtils.copy(from, StringBufferSbTarget.class);

        Assertions.assertNull(to.getSb(), "StringBuilder→StringBuffer 不做 JDK 结构拷贝 ⇒ 跳过（需转换器）");
    }

    /**
     * <p>对应测试用例 2.6：JDK 非拷贝协议类型同型 ⇒ 共享（不做 JDK 内部字段拷贝）</p>
     */
    @Test
    void mutable_jdkSameType_shared() {

        val sb = new StringBuilder("sb");
        val from = new StringBuilderSource();
        from.setSb(sb);

        val to = CBeanUtils.copy(from, StringBuilderTarget.class);

        Assertions.assertSame(sb, to.getSb());
    }

    /**
     * <p>对应测试用例 3.1：Class 字段按不可变处理（共享，不参与拷贝）</p>
     */
    @Test
    void classField_shared() {

        val from = new ClassSource();
        from.setType(String.class);

        val to = CBeanUtils.copy(from, ClassTarget.class);

        Assertions.assertSame(String.class, to.getType());
    }

    /**
     * <p>对应测试用例 4.1：对象源与 Map 源对同一字段决议一致</p>
     */
    @Test
    void objectSourceAndMapSource_sameResolution() {

        val from = new MutableSource();
        from.setRoles(CList.of("r1"));
        from.setInner(newInner("inner"));
        from.setSb(new StringBuilder("sb"));

        val byObject = CBeanUtils.copy(from, MutableTarget.class);
        val byMap = CBeanUtils.copy(CBeanUtils.toMap(from), MutableTarget.class);

        Assertions.assertEquals(byObject.getRoles(), byMap.getRoles());
        Assertions.assertNotSame(from.getRoles(), byMap.getRoles(), "两个入口都应深拷贝集合字段");
        Assertions.assertEquals(byObject.getInner().getName(), byMap.getInner().getName());
        Assertions.assertNotSame(from.getInner(), byMap.getInner(), "两个入口都应深拷贝 Bean 字段");
        Assertions.assertSame(from.getSb(), byMap.getSb(), "两个入口对 JDK 非拷贝协议类型都应共享");
    }

    /**
     * <p>对应测试用例 4.2：supplier 入口与直连入口一致（同一流程，不经 Map 中转）</p>
     */
    @Test
    void supplierEntry_sameAsDirect() {

        val from = new MutableSource();
        from.setRoles(CList.of("r1"));
        from.setInner(newInner("inner"));

        val direct = CBeanUtils.copy(from, MutableTarget.class);
        val bySupplier = CBeanUtils.copy(from, MutableTarget::new);

        Assertions.assertNotSame(direct, bySupplier);
        Assertions.assertEquals(direct.getRoles(), bySupplier.getRoles());
        Assertions.assertNotSame(from.getRoles(), bySupplier.getRoles());
        Assertions.assertEquals(direct.getInner().getName(), bySupplier.getInner().getName());
        Assertions.assertNotSame(from.getInner(), bySupplier.getInner());
    }

    private static Inner newInner(String name) {
        val inner = new Inner();
        inner.setName(name);
        return inner;
    }

    @Data
    static class ScalarSource {

        private String name;

        private Integer age;
    }

    @Data
    static class ScalarTarget {

        private String name;

        private int age;
    }

    @Data
    static class StringSource {

        private String value;
    }

    @Data
    static class IntTarget {

        private int value;
    }

    @Data
    static class StringBufferTarget {

        private StringBuffer value;
    }

    @Data
    static class Inner {

        private String name;
    }

    @Data
    static class InnerVo {

        private String name;
    }

    @Data
    static class MutableSource {

        private List<String> roles;

        private Inner inner;

        private StringBuilder sb;
    }

    @Data
    static class MutableTarget {

        private List<String> roles;

        private Inner inner;

        private StringBuilder sb;
    }

    @Data
    static class DateSource {

        private Date date;
    }

    @Data
    static class DateStrTarget {

        private String date;
    }

    @Data
    static class DateMillisTarget {

        private Long date;
    }

    @Data
    static class ListSource {

        private List<String> roles;
    }

    @Data
    static class ListToStrTarget {

        private String roles;
    }

    @Data
    static class InnerSource {

        private Inner bean;
    }

    @Data
    static class InnerVoSource {

        private InnerVo bean;
    }

    @Data
    static class StringBuilderSource {

        private StringBuilder sb;
    }

    @Data
    static class StringBuilderTarget {

        private StringBuilder sb;
    }

    @Data
    static class StringBufferSbTarget {

        private StringBuffer sb;
    }

    @Data
    static class ClassSource {

        private Class<?> type;
    }

    @Data
    static class ClassTarget {

        private Class<?> type;
    }

}
