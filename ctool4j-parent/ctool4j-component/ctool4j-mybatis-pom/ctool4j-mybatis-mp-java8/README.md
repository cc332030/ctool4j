# ctool4j-mybatis-mp-java8

> MyBatis-Plus 版本侧**自适应中间层**：把 `IService` / `ServiceImpl` 的版本侧差异（3.5.17 起由
> `com.baomidou.mybatisplus.extension.service` 迁到 `com.baomidou.mybatisplus.spring.service`）
> 收敛到本模块，使上层公共契约（`ICCheckService` → `ICService` 一族）与版本完全无关。

## 简介

命名沿用 Kotlin 的 `kotlin-stdlib-jdk7/jdk8` 范式：**同一 API 的两个上游坐标各写一份、由引入方按需取用**。
本模块与 `ctool4j-mybatis-base` 一起构成"版本无关的公共面"：

```
MP 上游 IService（坐标随版本迁移）        MP 上游 ServiceImpl（坐标随版本迁移）
        ↑ 继承                                  ↑ 继承
spi/IMybatisPlusSpi（自适应层，两侧各一份）  spi/CMybatisPlusServiceImpl（两侧各一份）
        ↑ 继承                                  ↑ 继承
service/ICCheckService（公共，本模块 jar）    service/impl/CBaseServiceImpl（两侧各一份）
        ↑ 继承
ICBizIdService → ICService（公共，落在 ctool4j-mybatis-base）
```

对应关系（用户口径）：`IService → IxxxService → ICService`，`ServiceImpl 同理`。

## 源目录布局

`src/main/` 下四个目录，**同组的两个目录之间代码完全互斥**（互为镜像、同包同名、只挂其一）：

| 目录 | 承载 | 编译/挂载者 |
|------|------|-------------|
| `java` | **版本无关公共面**：`service/ICCheckService`、`CMybatisPlusSide` | 本模块（随 jar 发布） |
| `java-adaptive-spring` | **3.5.x 侧自适应层**：`spi/IMybatisPlusSpi`、`spi/IService`、`spi/CMybatisPlusServiceImpl`（承 `spring.service`） | 本模块（默认侧）+ `ctool4j-mybatis` |
| `java-adaptive-extension` | **3.3.x/3.4.x 侧自适应层**（承 `extension.service`），相对路径集合与 `java-adaptive-spring` 完全镜像 | `ctool4j-mybatis-33` / `-34`（`srcDir`） |
| `java-base-spring` / `java-base-extension` | 两侧实现基底 `service/impl/CBaseServiceImpl` | 各版本模块（`srcDir`） |

## 自适应机制

一次 Gradle 构建同时产出 3.5.x 侧与 3.3.x/3.4.x 侧的使用方，同一个中间模块工程无法在一次构建里同时是两侧，
故侧差异表达为**两个同结构、互斥的目录**，由各消费模块按侧挂载其一：

- 默认侧（3.5.x）随本模块一起编译、随 jar 发布，`ctool4j-mybatis` 直接依赖 jar 即可；
- 另一侧由消费模块 `srcDir` 挂载 **同全限定名** 的源码 —— **源码优先于 classpath 上的 jar**，同名类被替换为该侧实现。

这与本仓库既有的 `java-javax` ↔ `java-jakarta` 成对适配（按 JDK 档位二选一）同构。

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `ICCheckService` | 接口 | 全部 service 契约的根，**不引用任何版本侧类型** |
| `IMybatisPlusSpi` | 接口 | 自适应层：承接 MP 上游 `IService` + 统一空安全语义，两侧各一份 |
| `IService` | 接口 | 中性名承接（对外沿用的 `spi/IService`），两侧各一份 |
| `CMybatisPlusServiceImpl` | 抽象类 | 自适应实现基底：承接 MP 上游 `ServiceImpl`，两侧各一份 |
| `CBaseServiceImpl` | 抽象类 | 实现基底：桥接自适应基底与公共 `ICService`，两侧各一份 |
| `CMybatisPlusSide` | 常量类 | 记录需两侧各一份的符号数（复核用） |

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `mybatis-plus-boot-starter` | api | 自适应层直接继承 MP 上游 `IService`/`ServiceImpl`；版本由使用方/BOM 决定 |
| `ctool4j-core` | api | `CList`、`CValidUtils` 等核心工具（经其传递 hutool） |

## 依赖方

`ctool4j-mybatis`（3.5.x 侧）、`ctool4j-mybatis-33` / `-34`（3.3.x/3.4.x 侧）。
