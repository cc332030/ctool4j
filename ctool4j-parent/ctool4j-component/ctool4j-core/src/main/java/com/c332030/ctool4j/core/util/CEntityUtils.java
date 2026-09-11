package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CClassUtils;
import com.c332030.ctool4j.core.classes.CMethodHandleUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.definition.entity.base.*;
import com.c332030.ctool4j.definition.function.CConsumer;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: CEntityUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CEntityUtils} 为实体清理工具类，提供按具体实体类型清空其公共字段的能力：</p>
 * <ul>
 *   <li>ICUpdateTime、ICCreateUpdateTime、ICCreateBy、ICUpdateBy、ICCreateUpdateBy、</li>
 *   <li>ICCreateUpdateByAndTime、CBaseTimeEntity、CBaseEntity）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>clear(Object) 类型无匹配清除方法</td>
 *     <td>调用空函数，无操作（不抛异常）</td>
 *   </tr>
 *   <tr>
 *     <td>无公共字段的普通对象</td>
 *     <td>clear 为空函数，无操作</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>对实体公共字段统一清空（如数据脱敏、对象复用前清理）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅清空框架约定的公共字段（id/createBy/updateBy/createTime/updateTime 等），业务私有字段</li>
 *   <li>不受影响。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>按继承距离查找而非方法声明顺序，可确定性命中"最近" clear，不依赖反射顺序（核心取舍）。</li>
 *   <li>清除函数按类缓存无失效机制：接口/基类静态不变，风险低。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>清除方法句柄与映射</b></p>
 * <ul>
 *   <li>启动时收集本类所有名为 {@code clear} 的方法句柄（{@code CReflectUtils.getAllMethodsByName} →</li>
 *   <li>{@code CMethodHandleUtils.getHandle}），排除 {@code Object} 参数重载，构建「参数类型 → 清除函数」映射</li>
 *   <li>{@code CLEAR_METHOD_MAP}。</li>
 * </ul>
 * <p><b>最近清除查找（findNearestClear）</b></p>
 * <ul>
 *   <li>按继承距离（自身、父类、父接口由近及远）在 {@code CLEAR_METHOD_MAP} 中查找 type 最近的清除函数。</li>
 *   <li>优先查父类链（{@code CClassUtils.getSuperClasses}），再查接口链（{@code CClassUtils.getInterfaces}，</li>
 *   <li>按继承距离由近及远且去重）。</li>
 *   <li>未匹配时返回空函数 {@code CConsumer.empty()}。</li>
 * </ul>
 * <p><b>按类缓存</b></p>
 * <ul>
 *   <li>清除函数按类缓存于 {@code CLEAN_ENTITY_CONSUMER}（{@code CClassValue}，基于 {@code java.lang.ClassValue}，</li>
 *   <li>线程安全、按类弱关联），避免每次查找。</li>
 * </ul>
 * <p><b>重载级联</b></p>
 * <ul>
 *   <li>{@code CBaseEntity.clear} 级联 {@code ICId} + {@code ICCreateUpdateByAndTime}；{@code ICCreateUpdateByAndTime.clear}</li>
 *   <li>级联 {@code ICCreateUpdateBy} + {@code ICCreateUpdateTime}，逐级清空。</li>
 * </ul>
 *
 * @since 2025/12/18
 * @version 1.0
 */
@UtilityClass
public class CEntityUtils {

    /**
     * 清除方法句柄
     */
    private static final List<MethodHandle> CLEAR_METHODS =
        CReflectUtils.getAllMethods(CEntityUtils.class)
            .stream()
            .filter(method -> "clear".equals(method.getName()))
            .map(CMethodHandleUtils::toHandle)
            .collect(CCollectors.toUnmodifiableList());

    /**
     * clear 方法参数类型 → 清除函数 映射（不含 {@code Object} 参数）
     */
    private final Map<Class<?>, CConsumer<Object>> CLEAR_METHOD_MAP = buildClearMethodMap();

    private Map<Class<?>, CConsumer<Object>> buildClearMethodMap() {

        val map = new HashMap<Class<?>, CConsumer<Object>>();
        for (val handle : CLEAR_METHODS) {

            val param0 = handle.type().parameterType(0);
            if (param0 != Object.class) {
                map.put(param0, handle::invoke);
            }
        }
        return map;
    }

    /**
     * 按继承距离（自身、父类、父接口由近及远）查找 type 最近的清除函数
     * <p>相比按方法声明顺序匹配，可确定性命中"最近"的 clear 重载，不依赖反射顺序</p>
     *
     * @param type 实体类型
     * @return 最近的清除函数，未匹配时返回空函数
     */
    private CConsumer<Object> findNearestClear(Class<?> type) {

        for (val superClass : CClassUtils.getSuperClasses(type)) {

            val consumer = CLEAR_METHOD_MAP.get(superClass);
            if (null != consumer) {
                return consumer;
            }
        }

        // getInterfaces 按继承距离由近及远且去重，顺序即"最近优先"
        for (val iface : CClassUtils.getInterfaces(type)) {

            val consumer = CLEAR_METHOD_MAP.get(iface);
            if (null != consumer) {
                return consumer;
            }
        }
        return CConsumer.empty();
    }

    /**
     * 各实体类清除方法缓存
     */
    private static final CClassValue<CConsumer<Object>> CLEAN_ENTITY_CONSUMER = CClassValue.of(
        CEntityUtils::findNearestClear
    );

    /**
     * 清空实体
     * <ul>
     *   <li>{@code clear(Object entity)}：按实体类型动态查找最近的清除方法并清空</li>
     *   <li>其余 {@code clear(...)} 重载：分别清空各类接口/基类声明的公共字段（ICId、ICCreateTime、</li>
     * </ul>
     *
     * @param entity 实体
     */
    public void clear(Object entity) {
        CLEAN_ENTITY_CONSUMER.get(entity.getClass())
            .accept(entity);
    }

    /**
     * 清空实体
     *
     * @param entity 实体
     */
    public void clear(ICId<?> entity) {
        entity.setId(null);
    }

    /**
     * 清空实体
     *
     * @param entity 实体
     */
    public void clear(ICCreateTime entity) {
        entity.setCreateTime(null);
    }

    /**
     * 清空实体
     *
     * @param entity 实体
     */
    public void clear(ICUpdateTime entity) {
        entity.setUpdateTime(null);
    }

    /**
     * 清空实体
     *
     * @param entity 实体
     */
    public void clear(ICCreateUpdateTime entity) {
        clear((ICCreateTime) entity);
        clear((ICUpdateTime) entity);
    }

    /**
     * 清空实体
     *
     * @param entity 实体
     */
    public void clear(ICCreateBy entity) {
        entity.setCreateBy(null);
        entity.setCreateById(null);
    }

    /**
     * 清空实体
     *
     * @param entity 实体
     */
    public void clear(ICUpdateBy entity) {
        entity.setUpdateBy(null);
        entity.setUpdateById(null);
    }

    /**
     * 清空实体
     *
     * @param entity 实体
     */
    public void clear(ICCreateUpdateBy entity) {
        clear((ICCreateBy) entity);
        clear((ICUpdateBy) entity);
    }

    /**
     * 清空实体
     *
     * @param entity 实体
     */
    public void clear(ICCreateUpdateByAndTime entity) {
        clear((ICCreateUpdateBy) entity);
        clear((ICCreateUpdateTime) entity);
    }

    /**
     * 清空实体
     *
     * @param entity 实体
     */
    public void clear(CBaseTimeEntity<?> entity) {
        clear((ICId<?>) entity);
        clear((ICCreateUpdateTime) entity);
    }

    /**
     * 清空实体
     *
     * @param entity 实体
     */
    public void clear(CBaseEntity<?> entity) {
        clear((ICId<?>) entity);
        clear((ICCreateUpdateByAndTime) entity);
    }

}
