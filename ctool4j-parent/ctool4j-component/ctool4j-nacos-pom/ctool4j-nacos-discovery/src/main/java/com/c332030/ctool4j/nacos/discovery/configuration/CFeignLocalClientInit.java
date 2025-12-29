package com.c332030.ctool4j.nacos.discovery.configuration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.c332030.ctool4j.definition.function.CBiConsumer;
import com.c332030.ctool4j.spring.lifecycle.ICSpringInit;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CFeignLocalClientInit
 * </p>
 *
 * @since 2025/1/13
 */
@CustomLog
@Component
@ConditionalOnProperty(prefix = "feign.client.local-instance", value = "enabled", havingValue = "true")
public class CFeignLocalClientInit implements ICSpringInit, AutoCloseable {

    @Autowired
    CFeignLocalClientConfig clientConfig;

    final NamingService namingService;

    @SneakyThrows
    public CFeignLocalClientInit(NacosDiscoveryProperties discoveryProperties) {
        namingService = NamingFactory.createNamingService(discoveryProperties.getNacosProperties());
    }

    @Override
    public void onInit() {
        doForClient("注册", namingService::registerInstance);
    }

    @Override
    public void close() {
        doForClient("取消注册", namingService::deregisterInstance);
    }

    private void doForClient(String operateName, CBiConsumer<String, Instance> consumer) {

        val successInstances = clientConfig.getUrls().entrySet().stream().map(entry -> {

            val serviceName = entry.getKey();
            val ipPort = entry.getValue();

            Instance instance = null;
            try {

                val ipPortArr = ipPort.split(":");
                instance = new Instance();
                instance.setClusterName(serviceName);
                instance.setIp(ipPortArr[0]);
                instance.setPort(Integer.parseInt(ipPortArr[1]));

                consumer.accept(serviceName, instance);

                return instance;
            } catch (Throwable e ){
                log.error("{} 失败，serviceName: {}, instance: {}, ipPort: {}",
                        operateName, serviceName, instance, ipPort, e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        log.info("{} 成功：\n{}", operateName, successInstances);

    }

}
