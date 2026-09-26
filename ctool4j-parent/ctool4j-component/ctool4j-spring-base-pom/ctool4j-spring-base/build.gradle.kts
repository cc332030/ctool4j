plugins {
    `java-library`
}

dependencies {

    api("org.springframework.boot:spring-boot")

    api(project(":ctool4j-http-base"))
    api(project(":ctool4j-core"))
    api(project(":ctool4j-autowired-processor"))

}
