plugins {
    `java-library`
}

dependencies {

    api("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")

    api(project(":ctool4j-spring-cloud"))

}
