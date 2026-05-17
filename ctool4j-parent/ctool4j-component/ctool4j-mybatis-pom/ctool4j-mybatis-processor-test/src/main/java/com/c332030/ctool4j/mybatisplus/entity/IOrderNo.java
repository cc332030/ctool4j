package com.c332030.ctool4j.mybatisplus.entity;

import com.c332030.ctool4j.definition.annotation.CBizId;
import com.c332030.ctool4j.mybatisplus.processor.CAutoBizService;

/**
 * <p>
 * Description: IOrderNo
 * </p>
 *
 * @author c332030
 * @since 2026/5/17
 */
@CAutoBizService
public interface IOrderNo {

    String getOrderNo();

    void setOrderNo(String orderNo);

}
