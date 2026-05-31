package com.c332030.ctool4j.rocketmq.interfaces;

/**
 * <p>
 * Description: ICRocketMQ
 * </p>
 *
 * @author c332030
 * @since 2026/5/31
 */
public interface ICRocketMQ {

    /**
     * 消息主题
     * @return 主题
     */
    String getTopic();

    /**
     * 消息标签
     * @return 标签
     */
    String getTag();

}
