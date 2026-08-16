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

    /**
     * 获取消息（委托给 getMsg）
     * @return 消息
     */
    @JsonIgnore
    @Override
    default String getMessage() {
        return getMsg();
    }

}
