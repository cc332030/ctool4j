package com.c332030.ctool4j.rocketmq.interfaces;

/**
 * <p>
 * Description: ICRocketMQConsumer
 * </p>
 *
 * @author c332030
 * @since 2026/5/31
 */
public interface ICRocketMQConsumer<ROCKET_MQ extends ICRocketMQ, KEY, BODY> {

    /**
     * RockeTMQ 消费者
     * @param key 键
     * @param body 消息主体
     */
    void consume(KEY key, BODY body);

}
