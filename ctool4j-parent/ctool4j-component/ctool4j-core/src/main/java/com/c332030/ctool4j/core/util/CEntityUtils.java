package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.core.cache.impl.CClassValue;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.definition.entity.base.*;
import com.c332030.ctool4j.definition.function.CConsumer;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: CEntityUtils
 * </p>
 *
 * @since 2025/12/18
 */
@UtilityClass
public class CEntityUtils {

    /**
     * 清除方法
     */
    private static final List<Method> CLEAR_METHODS = CReflectUtils.getAllMethodsByName(
            CEntityUtils.class, "clear"
    );

    /**
     * clear 方法参数类型 → 清除函数 映射（不含 {@code Object} 参数）
     */
    private final Map<Class<?>, CConsumer<Object>> CLEAR_METHOD_MAP = buildClearMethodMap();

    private Map<Class<?>, CConsumer<Object>> buildClearMethodMap() {

        val map = new HashMap<Class<?>, CConsumer<Object>>();
        for (val method : CLEAR_METHODS) {

            val param0 = method.getParameterTypes()[0];
            if (param0 != Object.class) {
                map.put(param0, e -> invokeClear(method, e));
            }
        }
        return map;
    }

    @SneakyThrows
    private void invokeClear(Method method, Object entity) {
        method.invoke(null, entity);
    }

    /**
     * 按继承距离（自身、父类、父接口由近及远）查找 type 最近的清除函数
     * <p>相比按方法声明顺序匹配，可确定性命中"最近"的 clear 重载，不依赖反射顺序</p>
     *
     * @param type 实体类型
     * @return 最近的清除函数，未匹配时返回空函数
     */
    private CConsumer<Object> findNearestClear(Class<?> type) {

        val queue = new ArrayDeque<Class<?>>();
        val visited = new HashSet<Class<?>>();
        queue.add(type);
        visited.add(type);

        while (!queue.isEmpty()) {

            val current = queue.poll();
            val consumer = CLEAR_METHOD_MAP.get(current);
            if (null != consumer) {
                return consumer;
            }

            val superClass = current.getSuperclass();
            if (null != superClass && superClass != Object.class && visited.add(superClass)) {
                queue.add(superClass);
            }
            for (val iface : current.getInterfaces()) {
                if (visited.add(iface)) {
                    queue.add(iface);
                }
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
     * @param entity 实体
     */
    public void clear(Object entity) {
        CLEAN_ENTITY_CONSUMER.get(entity.getClass())
                .accept(entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICId<?> entity) {
        entity.setId(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICCreateTime entity) {
        entity.setCreateTime(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICUpdateTime entity) {
        entity.setUpdateTime(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICCreateUpdateTime entity) {
        clear((ICCreateTime)entity);
        clear((ICUpdateTime)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICCreateBy entity) {
        entity.setCreateBy(null);
        entity.setCreateById(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICUpdateBy entity) {
        entity.setUpdateBy(null);
        entity.setUpdateById(null);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICCreateUpdateBy entity) {
        clear((ICCreateBy)entity);
        clear((ICUpdateBy)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(ICCreateUpdateByAndTime entity) {
        clear((ICCreateUpdateBy)entity);
        clear((ICCreateUpdateTime)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(CBaseTimeEntity<?> entity) {
        clear((ICId<?>) entity);
        clear((ICCreateUpdateTime)entity);
    }

    /**
     * 清空实体
     * @param entity 实体
     */
    public void clear(CBaseEntity<?> entity) {
        clear((ICId<?>) entity);
        clear((ICCreateUpdateByAndTime)entity);
    }

}
