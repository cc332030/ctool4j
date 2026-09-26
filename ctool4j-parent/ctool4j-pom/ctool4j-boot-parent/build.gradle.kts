// 无源码的聚合/父 pom：pom 由根 build.gradle.kts 发布。
// 不注入 `<parent>`——对外版本管理由 pom 内 `<dependencyManagement>` 的 BOM import 提供；
// boot-parent 另外注入 spring-boot-maven-plugin 配置（见根脚本 cAddSpringBootPlugin）。
