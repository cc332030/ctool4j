package com.c332030.ctool4j.mybatisplus.processor;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.c332030.ctool4j.mybatisplus.service.ICBizService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: IOrderService - 自动生成的 Service 接口示例
 * </p>
 *
 * <p>此文件为手动编写的示例，实际使用时会由注解处理器自动生成</p>
 *
 * @since 2025/05/16
 */
public interface IOrderService<ENTITY extends Order> extends ICBizService<ENTITY, Order> {

    String getOrderNo(Order entity);

    SFunction<ENTITY, String> getOrderNoColumn();

    default Order getByOrderNo(String orderNo) {
        return getByBizId(orderNo);
    }

    default List<Order> listByOrderNo(String orderNo) {
        return listByBizId(orderNo);
    }

    default Long countByOrderNo(String orderNo) {
        return countByBizId(orderNo);
    }

    default boolean removeByOrderNo(String orderNo) {
        return removeByBizId(orderNo);
    }

    default List<Order> listByOrderNos(Collection<String> orderNos) {
        return listByBizIds(orderNos);
    }

    default Long countByOrderNos(Collection<String> orderNos) {
        return countByBizIds(orderNos);
    }

    default boolean removeByOrderNos(Collection<String> orderNos) {
        return removeByBizIds(orderNos);
    }

    default Map<String, Order> listMapByOrderNos(Collection<String> orderNos) {
        return listMapByBizIds(orderNos);
    }

}
