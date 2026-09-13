# ctool4j-bom

> BOM：统一管理第三方依赖版本，并 import `ctool4j-dependencies` 管理自研构件版本（依赖清单见 [pom.xml](pom.xml)）。

## 简介

`ctool4j-bom` 是面向**使用方**的版本入口（packaging=pom）：import Spring Boot / Spring Cloud / Spring Cloud Alibaba
的官方 BOM，管理 MyBatis-Plus、Redisson、Hutool、Guava、EasyExcel、MinIO 等第三方依赖版本，并 import 自研的
`ctool4j-dependencies`。业务项目引入本 BOM 即可获得一致的依赖版本，无需手写版本号。

## 使用方式

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.c332030</groupId>
            <artifactId>ctool4j-bom</artifactId>
            <version>${ctool4j.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

随后按需引入组件模块（如 `ctool4j-web`、`ctool4j-redis`）即可，版本由本 BOM 决定。
模块间依赖关系见 [doc/dependency.adoc](../doc/dependency.adoc)。
