package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.core.enums.CDataTypeEnum;
import com.c332030.ctool4j.core.util.CStrUtils;
import com.c332030.ctool4j.definition.interfaces.ICEnumName;
import com.c332030.ctool4j.definition.interfaces.ICText;

/**
 * <p>
 * Description: ICRequestHeader
 * </p>
 *
 * @author c332030
 * @since 2024/3/21
 * @see doc/design/core/ICRequestHeader.adoc
 * @see doc/design/core/ICRequestHeaderTests.adoc
 */
public interface ICRequestHeader extends ICText, ICEnumName {

    /**
     * 数据类型
     * @return 数据类型
     */
    default CDataTypeEnum getDataType() {
        return CDataTypeEnum.STRING;
    }

    /**
     * 是否必输
     * @return 是否必输
     */
    default boolean isRequired() {
        return false;
    }

    /**
     * 报文头名
     * @return 报文头名
     */
    default String getHeaderName() {
        return CStrUtils.upperUnderscoreToHeaderName(name());
    }

}
