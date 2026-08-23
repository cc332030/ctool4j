# ctool4j-job-pom

> 定时任务聚合模块：`ctool4j-job-base`（时间计算）、`ctool4j-xxl-job`（xxl-job 开箱即用封装）。

## 简介

`ctool4j-job-pom` 是聚合 pom，包含 2 个子模块。核心是 `ctool4j-xxl-job`：通过 Spring 自动配置创建 xxl-job 执行器、用切面增强 `@XxlJob` 处理方法（参数注入、耗时打印、错误记录），并提供任务参数解析工具。

---

## 子模块一：ctool4j-job-base

> 定时任务时间计算工具。

### 功能特性

- `CJobUtils`：计算每日 / 多日定时任务的开始、结束时间

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CJobUtils` | 工具类 | 每日/多日任务时间段计算 |

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` / `ctool4j-definition` | 工具与函数式接口 |

---

## 子模块二：ctool4j-xxl-job

> xxl-job 分布式任务调度集成。

### 功能特性

- **自动配置**：`CXxlJobConfiguration` 条件装配 `XxlJobSpringExecutor` 执行器 Bean
- **任务切面**：`CXxlJobAspect` 拦截 `@XxlJob` 方法，自动注入任务参数、打印执行耗时、捕获并记录错误
- **任务接口**：`ICTask` 任务执行接口（自代理模式）
- **参数工具**：`CXxlJobUtils` 获取 / 解析 / 分割任务参数
- **配置属性**：调度中心（admin）、执行器（executor）、日志开关分组配置

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CXxlJobConfiguration` | 配置 | 条件装配 XxlJobSpringExecutor |
| `CXxlJobAspect` | 切面 | `@XxlJob` 增强：参数注入 / 耗时 / 错误记录 |
| `ICTask` | 接口 | 任务执行接口 |
| `CXxlJobUtils` | 工具类 | 任务参数获取 / 解析 / 分割 |
| `CXxlJobAdminConfig` | 配置 | 调度中心地址 / token / 超时（前缀 `xxl.job.admin`） |
| `CXxlJobExecutorConfig` | 配置 | 执行器 appname / ip / port / logpath（前缀 `xxl.job.executor`） |
| `CXxlJobExecutorLogConfig` | 配置 | 耗时打印 / 错误捕获开关（前缀 `xxl.job.executor.log`） |
| `CXxlJobConfig` | 配置 | 总开关（前缀 `xxl.job`） |

### 使用示例

```java
// 任务实现（ICTask 自代理，保证切面生效）
@Component
public class OrderJob implements ICTask {

    @XxlJob("order-stat")
    public void stat(String param) {
        Map<String, String> params = CXxlJobUtils.parseJobParam(param);
        // 业务处理
    }
}
```

### 配置项

| 配置前缀 | 说明 |
|----------|------|
| `xxl.job.enabled` | 是否启用 xxl-job |
| `xxl.job.admin.*` | 调度中心配置 |
| `xxl.job.executor.*` | 执行器配置 |
| `xxl.job.executor.log.*` | 日志行为开关 |

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` / `ctool4j-definition` | 工具与类型 |
| `ctool4j-spring` | 切面、代理 |
| `xxl-job-core` | xxl-job 客户端 |
