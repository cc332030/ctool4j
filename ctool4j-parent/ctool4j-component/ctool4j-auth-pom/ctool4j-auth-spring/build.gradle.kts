plugins {
    `java-library`
}

dependencies {

    api(project(":ctool4j-auth-base"))
    api(project(":ctool4j-spring-security"))
    // 装配在 autoconfigure：CSecurityConfiguration 抽离后按依赖方向（autoconfigure → spring-security）
    // 显式引入，维持原有组件扫描行为
    api(project(":ctool4j-spring-security-autoconfigure"))

}
