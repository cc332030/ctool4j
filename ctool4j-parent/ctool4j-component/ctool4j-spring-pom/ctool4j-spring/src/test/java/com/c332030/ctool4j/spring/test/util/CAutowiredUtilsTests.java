package com.c332030.ctool4j.spring.test.util;

import cn.hutool.core.lang.func.LambdaUtil;
import com.c332030.ctool4j.spring.bean.CSpringConfigBeans;
import com.c332030.ctool4j.spring.config.CSpringApplicationConfig;
import com.c332030.ctool4j.spring.util.CAutowiredUtils;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * <p>
 * Description: CAutowiredUtilsTests
 * </p>
 *
 * <p>
 * 是 {@link CAutowiredUtils} 的测试用例。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>验证字段映射与「显式指定注入源」的字段写入两条路径。</li>
 *   <li>注入源由调用方显式传入是本类的关键约束（不依赖全局静态上下文），因此用 mock 的 Bean 直接断言写入结果，
 *   无需启动容器。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对字段映射与注入源的约定。</li>
 *   <li>依据等价类/边界覆盖：有/无 CAutowired 字段、bean 为空。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：字段映射命中；显式注入源写入实例字段。</li>
 *   <li>未覆盖：真实容器的取 Bean 过程与包扫描（由 Spring 集成测试与 {@code CAutowiredUtilsBootTests} 覆盖）。</li>
 * </ul>
 * <h2>自动注入工具</h2>
 * <ul>
 *   <li>1.1 字段映射命中 CAutowired 字段（{@code getFieldMap}）</li>
 *   <li>1.2 显式注入源写入实例字段（{@code autowired_withExplicitBean_writesField}）</li>
 * </ul>
 *
 * @since 2025/12/28
 * @version 1.0
 * @see CAutowiredUtils
 */
public class CAutowiredUtilsTests {

    /**
     * 对应测试用例 1.1：字段映射命中 CAutowired 字段
     */
    @Test
    public void getFieldMap() {

        val fieldMap = CAutowiredUtils.getFieldMap(CSpringConfigBeans.class);
        val fieldName = LambdaUtil.getFieldName(CSpringConfigBeans::getSpringApplicationConfig);

        Assertions.assertNotNull(fieldMap.get(fieldName));

    }

    /**
     * 对应测试用例 1.2：显式注入源写入实例字段
     */
    @Test
    public void autowired_withExplicitBean_writesField() throws Exception {
        // 正例：注入源由调用方给出时，字段被写成该 Bean（不查全局上下文）
        val config = new CSpringApplicationConfig();
        val holder = new InstanceHolder();

        Field field = InstanceHolder.class.getDeclaredField("config");
        // 反射取到的字段默认不可访问，与 CReflectUtils 的调用前置条件一致
        field.setAccessible(true);
        CAutowiredUtils.autowired(config, InstanceHolder.class, holder, field);

        Assertions.assertSame(config, holder.getConfig());
    }

    /**
     * 实例字段注入的测试宿主：字段名与类型固定，便于按反射取字段
     */
    public static class InstanceHolder {

        /**
         * 注入目标字段（声明顺序与类型固定，便于按反射取字段断言）
         */
        private CSpringApplicationConfig config;

        CSpringApplicationConfig getConfig() {
            return config;
        }

    }

}
