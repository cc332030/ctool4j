plugins {
    `java-library`
}

dependencies {

    api("org.springframework.cloud:spring-cloud-starter-bootstrap")
    api("org.springframework.cloud:spring-cloud-commons")

    api(project(":ctool4j-spring"))

}
