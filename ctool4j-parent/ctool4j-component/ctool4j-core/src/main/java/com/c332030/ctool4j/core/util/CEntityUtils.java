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
 * @since 2025/12/18
 * @see "doc/design/core/CEntityUtils.adoc"
 * @see "doc/design/core/CEntityUtilsTests.adoc"
 */
@UtilityClass
public class CEntityUtils {

    /**
     * 清除方法句柄
     */
    private static final List<MethodHandle> CLEAR_METHODS =
        CReflectUtils.getAllMethodsByName(
                CEntityUtils.class, "clear"
            ).stream()
            .map(CMethodHandleUtils::getHandle)
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
