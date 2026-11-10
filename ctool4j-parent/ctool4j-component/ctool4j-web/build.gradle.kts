import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    api("org.springframework.boot:spring-boot-starter-web")
    // 校验实现（hibernate-validator）留给使用方，此处只为 auto-config 提供实现
    api("org.springframework.boot:spring-boot-starter-validation")
    // Bean Validation API：jdk8 → `javax.validation:validation-api`，其余 → `jakarta.validation:jakarta.validation-api`；
    // 只声明 API，供 `@CRequired` / `CRequiredValidator`（java-jdk8 / java-latest 各一份同包同名实现）使用
    api(lib("validation-api"))

    api(project(":ctool4j-spring"))
    // `CRequiredValidator` 复用 core 的 `CValidUtils` 做按类型分发的非空判定
    api(project(":ctool4j-core"))

}
