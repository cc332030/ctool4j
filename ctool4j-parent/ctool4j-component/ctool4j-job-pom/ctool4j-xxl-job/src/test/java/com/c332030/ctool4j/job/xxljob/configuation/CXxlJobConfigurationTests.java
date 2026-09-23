package com.c332030.ctool4j.job.xxljob.configuation;

import com.c332030.ctool4j.core.classes.CMethodHandleUtils;
import com.c332030.ctool4j.job.xxljob.config.CXxlJobAdminConfig;
import com.c332030.ctool4j.job.xxljob.config.CXxlJobConfig;
import com.c332030.ctool4j.job.xxljob.config.CXxlJobExecutorConfig;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Description: CXxlJobConfigurationTests
 * </p>
 * <p>
 * 验证 {@link CXxlJobConfiguration#cXxlJobExecutor} 的装配契约：受类级开关 {@code xxl.job.enable} 控制、
 * 受 {@code @ConditionalOnMissingBean} 让位于使用方实现、并按 admin/executor 配置完成初始化。
 * </p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li><b>条件分支用容器、配置映射脱离容器</b>：{@code XxlJobSpringExecutor} 实现
 *   {@code SmartInitializingSingleton}，一旦被容器实例化即调用 {@code start()}——会创建日志目录、启动
 *   心跳/回调线程并<b>绑定内嵌服务端口</b>。故只对"不实例化该 Bean"的路径（开关关闭、使用方已提供）起
 *   容器；"缺省注册"路径改为<b>直调 {@code @Bean} 方法</b>（同 {@code CAbstractAuthConfigurationTests}
 *   的取舍：起容器会连带装配与待验证契约无关的副作用）。</li>
 *   <li>配置映射是本方法的主体（9 个 setter 顺序排列、漏一行在运行期才表现为连错地址/端口），
 *   而 {@code XxlJobExecutor} 的字段全为 {@code private} 且无 getter（已核实 xxl-job-core 2.3.0），
 *   故用 {@code CMethodHandleUtils} 的字段 getter 句柄读取实例状态做精确断言——比"仅断言非空"
 *   才能真正捕获"漏映射"的缺陷。</li>
 *   <li>条件在 bean 定义注册阶段评估，故"使用方配置类"必须排在 {@link CXxlJobConfiguration} 之前。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计（类 javadoc）：类级 {@code @ConditionalOnProperty(xxl.job.enable, matchIfMissing=true)}；
 *   方法级 {@code @ConditionalOnMissingBean(XxlJobExecutor.class)}；按 admin/executor 配置初始化。</li>
 *   <li>依据 {@link CXxlJobAdminConfig}/{@link CXxlJobExecutorConfig} 的默认值约定：timeout 3 秒、
 *   port 9999、logpath {@code ./logs/xxl-job/jobhandler}、logretentiondays 30。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：类级开关关闭时不注册；使用方已提供 {@code XxlJobExecutor} 时默认实现让位；直调时 admin 与
 *   executor 的全部配置项映射到执行器。</li>
 *   <li>未覆盖：执行器真实启动后的注册心跳与端口服务（需外部调度中心与端口资源，属端到端测试）。</li>
 * </ul>
 * <h2>XxlJob 装配</h2>
 * <ul>
 *   <li>1.1 {@code xxl.job.enable=false} 时整类跳过、不注册执行器（{@code cXxlJobExecutor_disabledByProperty}）</li>
 *   <li>1.2 使用方提供执行器时默认实现让位（{@code cXxlJobExecutor_userBeanWins}）</li>
 *   <li>1.3 缺省路径把 admin/executor 配置逐项映射到执行器（{@code cXxlJobExecutor_mapsAdminAndExecutorConfig}）</li>
 * </ul>
 *
 * @since 2026/9/23
 * @version 1.0
 * @see CXxlJobConfiguration
 */
class CXxlJobConfigurationTests {

    /**
     * 对应测试用例 1.1：{@code xxl.job.enable=false} 时整类跳过、不注册执行器
     */
    @Test
    void cXxlJobExecutor_disabledByProperty() {
        new ApplicationContextRunner()
            .withUserConfiguration(CXxlJobConfiguration.class)
            .withBean(CXxlJobConfig.class)
            .withPropertyValues("xxl.job.enable=false")
            .run(context -> {
                // 正例：开关显式关闭——本地的关闭手段，必须一个 Bean 都不注册
                Assertions.assertFalse(context.containsBean("cXxlJobExecutor"));
                Assertions.assertTrue(context.getBeansOfType(XxlJobExecutor.class).isEmpty());
            });
    }

    /**
     * 对应测试用例 1.2：使用方提供执行器时默认实现让位
     */
    @Test
    void cXxlJobExecutor_userBeanWins() {
        // 使用方配置类排在前面：条件评估时其 bean 定义已存在 → 默认实现跳过
        new ApplicationContextRunner()
            .withUserConfiguration(UserXxlJobExecutorConfig.class, CXxlJobConfiguration.class)
            .withBean(CXxlJobConfig.class)
            .withBean(CXxlJobAdminConfig.class)
            .withBean(CXxlJobExecutorConfig.class)
            .run(context -> {
                val names = context.getBeanNamesForType(XxlJobExecutor.class);

                Assertions.assertArrayEquals(new String[] {"userXxlJobExecutor"}, names);
            });
    }

    /**
     * 对应测试用例 1.3：缺省路径把 admin/executor 配置逐项映射到执行器
     */
    @Test
    @SneakyThrows
    void cXxlJobExecutor_mapsAdminAndExecutorConfig() {
        val adminConfig = new CXxlJobAdminConfig();
        adminConfig.setAddresses("http://127.0.0.1:8080/xxl-job-admin");
        adminConfig.setAccessToken("test-access-token");
        adminConfig.setTimeout(7);

        val executorConfig = new CXxlJobExecutorConfig();
        executorConfig.setAppname("test-app");
        executorConfig.setAddress("http://127.0.0.1:9998");
        executorConfig.setIp("127.0.0.1");
        executorConfig.setPort(9998);
        executorConfig.setLogpath("./test-logs");
        executorConfig.setLogretentiondays(10);

        // 直调 @Bean 方法：不启动容器，避免触发执行器 start（端口绑定与线程）
        val executor = new CXxlJobConfiguration()
            .cXxlJobExecutor(new CXxlJobConfig(), adminConfig, executorConfig);

        // 正例：缺省实现是 Spring 环境专用的执行器
        Assertions.assertInstanceOf(XxlJobSpringExecutor.class, executor);

        // 正例：admin 侧三项
        Assertions.assertEquals("http://127.0.0.1:8080/xxl-job-admin", readField(executor, "adminAddresses"));
        Assertions.assertEquals("test-access-token", readField(executor, "accessToken"));
        Assertions.assertEquals(7, readField(executor, "timeout"));

        // 正例：executor 侧六项（逐项断言，漏映射任何一项都会失败）
        Assertions.assertEquals("test-app", readField(executor, "appname"));
        Assertions.assertEquals("http://127.0.0.1:9998", readField(executor, "address"));
        Assertions.assertEquals("127.0.0.1", readField(executor, "ip"));
        Assertions.assertEquals(9998, readField(executor, "port"));
        Assertions.assertEquals("./test-logs", readField(executor, "logPath"));
        Assertions.assertEquals(10, readField(executor, "logRetentionDays"));
    }

    /**
     * 读取执行器实例的字段值
     * <p>{@code XxlJobExecutor} 的配置字段为 {@code private} 且无 getter（xxl-job-core 2.3.0），
     * 只能按字段名读取</p>
     *
     * @param executor 执行器实例
     * @param name     字段名
     * @return 字段值
     */
    @SneakyThrows
    private static Object readField(XxlJobExecutor executor, String name) {
        val field = XxlJobExecutor.class.getDeclaredField(name);
        return CMethodHandleUtils.getGetterHandleAsType(field).invoke(executor);
    }

    /**
     * 使用方自建执行器的配置（验证条件不覆盖使用方实现）
     */
    @Configuration
    static class UserXxlJobExecutorConfig {

        /**
         * 使用方执行器
         *
         * @return 执行器
         */
        @Bean
        XxlJobExecutor userXxlJobExecutor() {
            return new XxlJobExecutor() {

            };
        }

    }

}
