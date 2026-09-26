package com.c332030.ctool4j.minio.configuration;

import com.c332030.ctool4j.minio.config.CMinioConfig;
import com.c332030.ctool4j.minio.config.CMinioOkHttpConfig;
import io.minio.MinioClient;
import lombok.val;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * <p>
 * Description: CMinioConfigurationTests
 * </p>
 * <p>
 * 验证 {@link CMinioConfiguration} 两个 {@code @Bean} 的装配契约：{@code cMinioOkHttpClient} 把
 * {@code CMinioOkHttpConfig} 的超时映射到 OkHttpClient，{@code cMinioClient} 按 {@code CMinioConfig}
 * 与共享 OkHttpClient 构建 MinioClient。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>两个 {@code @Bean} 的行为都是"按配置构建并暴露 Bean"，故用 {@code ApplicationContextRunner}
 *   起最小上下文，断言 Bean 的存在与装配产物；不引入业务启动类（本模块无启动类）。</li>
 *   <li>配置对象以普通 Bean 提供：{@code CMinioOkHttpConfig}/{@code CMinioConfig} 是
 *   {@code @ConfigurationProperties} 类，测试里直接注册实例即可驱动 {@code @Bean} 的参数注入。</li>
 *   <li>超时映射是本类唯一的"可精确读取"产物（OkHttpClient 的
 *   {@code connectTimeoutMillis}/{@code writeTimeoutMillis}/{@code readTimeoutMillis}），故作为主断言点，
 *   默认值与自定义值各一例。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计（类 javadoc）：OkHttpClient 按配置的连接/写/读超时构建，MinioClient 按
 *   endpoint、credentials、httpClient 构建。</li>
 *   <li>依据 {@link CMinioOkHttpConfig} 的默认值约定：连接 1s、写 3s、读 3s。</li>
 *   <li>依据黑盒原则：只断言可观测结果（Bean 存在、超时取值），不假设内部字段。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：OkHttpClient 默认超时与自定义超时的映射；MinioClient 在容器内装配成立。</li>
 *   <li>未覆盖：与真实 MinIO 服务的连接与调用（需外部环境，属端到端测试）。</li>
 * </ul>
 * <h2>设计取舍</h2>
 * <ul>
 *   <li>{@code cMinioClient} 的断言只到"Bean 存在且类型正确"：MinioClient 不暴露 endpoint/credentials
 *   的读取入口（已核实 minio 7.1.4 的公开 API 只有构造器与业务方法），要读到实例化参数只能依赖反射
 *   读内部字段，属白盒、随库版本漂移，故不采用；该 {@code @Bean} 的关键取舍（配置传参）改由
 *   {@code cMinioOkHttpClient} 一侧的精确断言与"容器装配成立"共同覆盖。</li>
 * </ul>
 * <h2>MinIO 装配</h2>
 * <ul>
 *   <li>1.1 默认超时按 {@code CMinioOkHttpConfig} 约定映射（{@code cMinioOkHttpClient_defaultTimeouts}）</li>
 *   <li>1.2 自定义超时按配置映射（{@code cMinioOkHttpClient_configuredTimeouts}）</li>
 *   <li>2.1 MinioClient 与 OkHttpClient 均在容器内装配成立（{@code cMinioClient_registeredWithSharedHttpClient}）</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 * @see CMinioConfiguration
 */
class CMinioConfigurationTests {

    /**
     * 对应测试用例 1.1：默认超时（1/3/3 秒）按配置约定映射到 OkHttpClient
     */
    @Test
    void cMinioOkHttpClient_defaultTimeouts() {
        runner(new CMinioOkHttpConfig()).run(context -> {
            // 正例：未显式配置时取 CMinioOkHttpConfig 的默认值
            val client = context.getBean(OkHttpClient.class);

            Assertions.assertNotNull(client);
            Assertions.assertEquals(1000, client.connectTimeoutMillis());
            Assertions.assertEquals(3000, client.writeTimeoutMillis());
            Assertions.assertEquals(3000, client.readTimeoutMillis());
        });
    }

    /**
     * 对应测试用例 1.2：自定义超时按配置映射到 OkHttpClient
     */
    @Test
    void cMinioOkHttpClient_configuredTimeouts() {
        val okHttpConfig = new CMinioOkHttpConfig();
        okHttpConfig.setConnectTimeout(5);
        okHttpConfig.setWriteTimeout(6);
        okHttpConfig.setReadTimeout(7);

        runner(okHttpConfig).run(context -> {
            // 正例：三个超时各自独立生效（秒 → 毫秒），证明配置项被逐项读取而非部分遗漏
            val client = context.getBean(OkHttpClient.class);

            Assertions.assertEquals(5000, client.connectTimeoutMillis());
            Assertions.assertEquals(6000, client.writeTimeoutMillis());
            Assertions.assertEquals(7000, client.readTimeoutMillis());
        });
    }

    /**
     * 对应测试用例 2.1：MinioClient 与 OkHttpClient 均在容器内装配成立
     */
    @Test
    void cMinioClient_registeredWithSharedHttpClient() {
        runner(new CMinioOkHttpConfig()).run(context -> {
            // 正例：MinioClient 依赖 OkHttpClient 参数能解析——两个 @Bean 的装配链路成立
            Assertions.assertTrue(context.containsBean("cMinioOkHttpClient"));
            Assertions.assertTrue(context.containsBean("cMinioClient"));
            Assertions.assertNotNull(context.getBean(OkHttpClient.class));
            Assertions.assertNotNull(context.getBean(MinioClient.class));
        });
    }

    /**
     * 构造只加载 {@link CMinioConfiguration} 与给定 OkHttp 配置的上下文运行器
     *
     * @param okHttpConfig OkHttp 超时配置
     * @return 上下文运行器
     */
    private static ApplicationContextRunner runner(CMinioOkHttpConfig okHttpConfig) {
        return new ApplicationContextRunner()
            .withUserConfiguration(CMinioConfiguration.class)
            .withBean(CMinioOkHttpConfig.class, () -> okHttpConfig)
            .withBean(CMinioConfig.class, CMinioConfigurationTests::minioConfig);
    }

    /**
     * 构造合法的 MinIO 连接配置（endpoint 非空即可，本类不断言其取值）
     *
     * @return MinIO 连接配置
     */
    private static CMinioConfig minioConfig() {
        val config = new CMinioConfig();
        config.setEndpoint("http://127.0.0.1:9000");
        config.setAccessKey("test-access-key");
        config.setSecretKey("test-secret-key");
        return config;
    }

}
