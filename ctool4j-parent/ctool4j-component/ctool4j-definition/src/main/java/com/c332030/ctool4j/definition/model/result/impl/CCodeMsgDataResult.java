package com.c332030.ctool4j.definition.model.result.impl;

import com.c332030.ctool4j.definition.model.result.ICCodeMsgDataResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CCodeMessageDataResult
 * </p>
 *
 * @author c332030
 * @since 2025/12/9
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CCodeMsgDataResult<CODE, DATA> implements ICCodeMsgDataResult<CODE, DATA> {

    CODE code;

    String msg;

    DATA data;

}
