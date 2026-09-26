plugins {
    `java-library`
}

dependencies {

    api("org.springframework.boot:spring-boot-starter-security")

    api(project(":ctool4j-spring-base"))
    api(project(":ctool4j-definition"))

}
