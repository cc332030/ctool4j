plugins {
    `java-library`
}

dependencies {

    api(project(":ctool4j-log-base"))

    api("org.springframework.boot:spring-boot-starter-logging")
    api("org.springframework.boot:spring-boot-starter-log4j2")

}
