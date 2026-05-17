package com.c332030.ctool4j.definition.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * <p>
 * Description: ICMsgAdapter
 * </p>
 *
 * @since 2025/12/30
 */
public interface ICMsgAdapter extends ICMsg, ICMessage{

    @JsonIgnore
    @Override
    default String getMessage() {
        return getMsg();
    }

}
