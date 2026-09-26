plugins {
    `java-library`
}

dependencies {

    api("org.springframework.boot:spring-boot-starter-logging")

    api(project(":ctool4j-log-base"))

}
