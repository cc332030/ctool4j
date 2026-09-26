plugins {
    `java-library`
}

dependencies {

    api(project(":ctool4j-log-base"))

    // 覆盖父级继承的 spring-boot-starter-logging（provided），改为 compile 传递到使用项目；
    // 排除 logback 与 log4j-to-slf4j（反向桥接），避免与 log4j-slf4j-impl 冲突（对齐迁移前 Maven pom）
    api("org.springframework.boot:spring-boot-starter-logging") {
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }

    api("org.springframework.boot:spring-boot-starter-log4j2")

}
