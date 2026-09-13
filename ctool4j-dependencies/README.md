# ctool4j-dependencies

> BOM：统一管理 ctool4j 自研构件的版本（依赖清单见 [pom.xml](pom.xml)）。

## 简介

`ctool4j-dependencies` 是自研构件的版本清单（packaging=pom）。业务项目通常不直接引入本模块，而是引入
`ctool4j-bom` —— 由它 import 本模块并叠加第三方依赖版本，一次获得全项目一致的版本。

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

引入后声明 ctool4j 构件无需再写版本号。自研构件之间的依赖关系见 [doc/dependency.adoc](../doc/dependency.adoc)。
