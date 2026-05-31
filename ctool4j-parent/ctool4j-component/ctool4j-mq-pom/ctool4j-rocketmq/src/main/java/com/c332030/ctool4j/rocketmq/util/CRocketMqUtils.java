package com.c332030.ctool4j.rocketmq.util;

import com.c332030.ctool4j.core.util.CJsonUtils;
import com.c332030.ctool4j.definition.entity.base.ICId;
import com.c332030.ctool4j.rocketmq.interfaces.ICRocketMQ;
import com.c332030.ctool4j.spring.annotation.CAutowired;
import com.c332030.ctool4j.spring.annotation.CAutowiredScan;
import lombok.CustomLog;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;

/**
 * <p>
 * Description: CRocketMQUtils
 * </p>
 *
 * @author c332030
 * @since 2026/5/31
 */
@CustomLog
@UtilityClass
@CAutowiredScan
public class CRocketMQUtils {

    @Setter
    @CAutowired
    RocketMQTemplate rocketMQTemplate;

    public void send(ICRocketMQ rocketMQ, ICId<?> entity) {
        send(rocketMQ, entity.getId().toString(), entity);
    }

    public void send(ICRocketMQ rocketMQ, String key, Object body) {

        val destination = rocketMQ.getTopic() + ":" + rocketMQ.getTag();
        log.info("destination: {}, key: {}, body: {}", destination, key, body);

        val message = MessageBuilder.withPayload(body)
                .setHeader(MessageConst.PROPERTY_KEYS, key)
                .build();

        val sendResult = rocketMQTemplate.syncSendOrderly(destination, message, key);
        log.info("sendResult: {}", CJsonUtils.toJson(sendResult));

    }

}
