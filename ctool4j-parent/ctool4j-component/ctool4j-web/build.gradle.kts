plugins {
    `java-library`
}

dependencies {

    api("org.springframework.boot:spring-boot-starter-web")
    // 校验实现（hibernate-validator）留给使用方：注解与校验器在 ctool4j-web-validation，此处只为 auto-config 提供实现
    api("org.springframework.boot:spring-boot-starter-validation")

    api(project(":ctool4j-spring"))
    // `@CRequired` / `@CNotRequired` 必填校验：Bean Validation API 的包名按档位不同，故独立成模块
    api(project(":ctool4j-web-validation"))

}
