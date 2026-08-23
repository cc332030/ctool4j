# ctool4j-file-pom

> 文件处理聚合模块：CSV 读写（`ctool4j-csv`）、Excel 读写（`ctool4j-excel`）、MinIO 对象存储（`ctool4j-minio`）。

## 简介

`ctool4j-file-pom` 是聚合 pom，包含 3 个相互独立的子模块，覆盖文件导入导出与对象存储接入场景。

---

## 子模块一：ctool4j-csv

> 基于 Apache Commons CSV 的 CSV 读写工具。

### 功能特性

- `CCsvHelper`：Builder 式读写 CSV（Reader / 流 / 文件 / Bean 映射）
- `CCsvUtils`：单元格 trim 与退格字符清理

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CCsvHelper` | 工具类 | CSV 读写（Reader/流/文件/Bean） |
| `CCsvUtils` | 工具类 | CSV 单元格清理 |

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` | 工具 |
| `commons-csv` | CSV 解析 |

---

## 子模块二：ctool4j-excel

> 基于 EasyExcel 的 Excel 读写工具。

### 功能特性

- `CExcelHelper`：Builder 式读写 Excel（Bean 映射，支持导出/导入）
- `CExcelUtils`：预留

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CExcelHelper` | 工具类 | Excel 读写（Bean 映射） |
| `CExcelUtils` | 工具类 | 预留 |

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` | 工具 |
| `easyexcel` | Excel 处理 |

---

## 子模块三：ctool4j-minio

> MinIO 对象存储：Spring 自动配置 + 对象操作服务。

### 功能特性

- **自动配置**：`CMinioConfiguration` 装配 OkHttpClient 与 MinioClient Bean
- **对象操作**：`CMinioService` 提供元数据查询、下载、上传操作
- **配置属性**：连接信息（endpoint / accessKey / secretKey）、OkHttp 超时分组配置

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CMinioConfiguration` | 配置 | 装配 OkHttpClient / MinioClient |
| `CMinioService` | 服务 | MinIO 元数据 / 下载 / 上传 |
| `CMinioUtils` | 工具类 | 从响应提取内容长度 |
| `CMinioConfig` | 配置 | 连接信息（前缀 `minio`） |
| `CMinioOkHttpConfig` | 配置 | OkHttp 超时（前缀 `minio.okhttp`） |

### 使用示例

```yaml
minio:
  endpoint: http://127.0.0.1:9000
  access-key: admin
  secret-key: admin123
```

```java
@Autowired
private CMinioService minioService;

// 上传
minioService.upload(bucket, objectName, inputStream, contentType);
```

### 配置项

| 配置前缀 | 说明 |
|----------|------|
| `minio.*` | 连接信息 |
| `minio.okhttp.*` | OkHttp 超时 |

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` | 工具 |
| `minio` / `okhttp` | 对象存储客户端 |
