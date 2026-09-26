import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    api(lib("rocketmq"))

    api(project(":ctool4j-mq-base"))

}
