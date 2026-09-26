plugins {
    `java-library`
}

dependencies {

    api("org.springframework.boot:spring-boot-starter-amqp")

    api(project(":ctool4j-mq-base"))

}
