plugins {
    `java-library`
}

dependencies {

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")

    api(project(":ctool4j-spring"))

}
