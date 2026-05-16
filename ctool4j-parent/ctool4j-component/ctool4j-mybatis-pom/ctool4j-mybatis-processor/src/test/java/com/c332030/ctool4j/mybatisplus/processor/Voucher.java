package com.c332030.ctool4j.mybatisplus.processor;

import com.c332030.ctool4j.definition.entity.base.CBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Description: Voucher 测试实体
 * </p>
 *
 * @since 2025/05/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoBizService(bizIdField = "voucherNo", bizIdColumn = "voucher_no", generateImpl = true)
public class Voucher extends CBaseEntity<Long> {

    private String voucherNo;

    private String voucherName;

    private Integer amount;
}
