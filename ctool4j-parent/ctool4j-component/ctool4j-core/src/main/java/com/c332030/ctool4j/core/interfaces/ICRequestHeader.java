package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.definition.enums.CDataTypeEnum;
import com.c332030.ctool4j.definition.interfaces.IEnumName;
import com.c332030.ctool4j.definition.interfaces.IText;

/**
 * <p>
 * Description: IRequestHeader
 * </p>
 *
 * @author c332030
 * @since 2024/3/21
 */
public interface ICRequestHeader extends IText, IEnumName {

    /**
     * 数据类型
     */
    CDataTypeEnum getDataType();

    /**
     * 是否必输
     */
    boolean isRequired();

    /**
     * 报文头名
     */
    default String getHeaderName() {
        return CStrUtils.upperUnderscoreToHeaderName(name());
    }

}
