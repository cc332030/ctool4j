package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.core.enums.CDataTypeEnum;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.definition.interfaces.ICEnumName;
import com.c332030.ctool4j.definition.interfaces.ICText;

/**
 * <p>
 * Description: IRequestHeader
 * </p>
 *
 * @author c332030
 * @since 2024/3/21
 */
public interface ICRequestHeader extends ICText, ICEnumName {

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
