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
 * <h2>能力目录</h2>
 * <p>{@code CFeignLocalClientInit}（{@code @Component} + {@code @ConditionalOnProperty}）实现 {@code ICSpringInit}、{@code AutoCloseable}，将本地客户端实例注册到 Nacos：</p>
 * <ul>
 *   <li>构造时基于 Nacos 发现配置创建 {@code NamingService}。</li>
 *   <li>{@code onInit}：Spring 启动时将配置的本地实例注册到 Nacos。</li>
 *   <li>{@code close}（{@code @PreDestroy}）：取消注册并关闭命名服务，防重入。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>单个实例注册/注销失败</td>
 *     <td>捕获记录 error 日志，返回 null 被过滤</td>
 *   </tr>
 *   <tr>
 *     <td>ip:port 格式错误/缺端口</td>
 *     <td>抛 IllegalArgumentException，外层 catch 记录</td>
 *   </tr>
 *   <tr>
 *     <td>close 重复触发</td>
 *     <td>{@code closed} 防重入直接返回</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>本地开发/联调时将本地 Feign 客户端注册到 Nacos。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>依赖 Nacos、Feign 客户端相关组件。</li>
 *   <li>注册失败不阻断其他实例（过滤后继续）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>注册/注销</b></p>
 * <ul>
 *   <li>{@code doForClient(operateName, consumer)} 遍历 {@code clientConfig.getUrls()}（服务名→ip:port）。</li>
 *   <li>以最后一个冒号拆分 host:port（兼容 IPv6 多冒号）；缺端口或格式错误抛异常由外层 catch 记录。</li>
 *   <li>注册成功的实例过滤 null 后收集，记录成功日志。</li>
 * </ul>
 * <p><b>生命周期</b></p>
 * <ul>
 *   <li>构造时 {@code NamingFactory.createNamingService(nacosProperties)}。</li>
 *   <li>Spring 不会调用 {@code AutoCloseable.close}，须经 {@code @PreDestroy} 在容器销毁时触发。</li>
 *   <li>{@code close} 防重入：{@code closed} volatile 标记，重复触发直接返回。</li>
 * </ul>
 *
 * @since 2025/1/13
 * @version 1.0
 */
@CustomLog
@Component
@ConditionalOnProperty(prefix = "feign.client.local-instance", value = "enabled", havingValue = "true")
public class CFeignLocalClientInit implements ICSpringInit, AutoCloseable {

    @Autowired
    CFeignLocalClientConfig clientConfig;

    final NamingService namingService;

    volatile boolean closed;

    /**
     * 构造方法，根据 Nacos 发现配置创建命名服务
     *
     * @param discoveryProperties Nacos 发现配置
     */
    @SneakyThrows
    public CFeignLocalClientInit(NacosDiscoveryProperties discoveryProperties) {
        namingService = NamingFactory.createNamingService(discoveryProperties.getNacosProperties());
    }

    /**
     * Spring 启动初始化回调：将本地客户端实例注册到 Nacos
     */
    @Override
    public void onInit() {
        doForClient("注册", namingService::registerInstance);
    }

    /**
     * 关闭：取消注册本地实例并关闭命名服务，防重入
     */
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
