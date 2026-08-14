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

import javax.annotation.PreDestroy;
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

    volatile boolean closed;

    @SneakyThrows
    public CFeignLocalClientInit(NacosDiscoveryProperties discoveryProperties) {
        namingService = NamingFactory.createNamingService(discoveryProperties.getNacosProperties());
    }

    @Override
    public void onInit() {
        doForClient("注册", namingService::registerInstance);
    }

    @Override
    @PreDestroy
    @SneakyThrows
    public void close() {
        // Spring 不会调用 AutoCloseable.close，须经 @PreDestroy 在容器销毁时触发；
        // 防重入：@PreDestroy 与显式调用可能重复触发
        if(closed) {
            return;
        }
        closed = true;
        doForClient("取消注册", namingService::deregisterInstance);
        namingService.shutDown();
    }

    private void doForClient(String operateName, CBiConsumer<String, Instance> consumer) {

        val successInstances = clientConfig.getUrls().entrySet().stream().map(entry -> {

            val serviceName = entry.getKey();
            val ipPort = entry.getValue();

            Instance instance = null;
            try {

                // 以最后一个冒号拆分 host:port，兼容 IPv6 地址（含多个冒号）；
                // 缺端口或格式错误时抛异常，由外层 catch 记录，避免静默吞掉注册失败
                val lastColonIndex = ipPort.lastIndexOf(":");
                if(lastColonIndex <= 0) {
                    throw new IllegalArgumentException("ip:port 格式错误: " + ipPort);
                }
                instance = new Instance();
                instance.setClusterName(serviceName);
                instance.setIp(ipPort.substring(0, lastColonIndex));
                instance.setPort(Integer.parseInt(ipPort.substring(lastColonIndex + 1)));

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
