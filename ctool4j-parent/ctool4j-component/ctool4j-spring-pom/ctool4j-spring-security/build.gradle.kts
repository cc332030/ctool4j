import com.c332030.ctool4j.gradle.buildsrc.util.cOptional

plugins {
    `java-library`
}

/**
 * Spring Security 契约模块：模型、服务契约、工具与 Jackson 支持。
 * 自动装配（`CSecurityConfiguration` 及 `config/`、`configuration/`）已抽离至同级的
 * `ctool4j-spring-security-autoconfigure`（追加型按档位承载 SS 5.7 / SS 7 的 API 差异），
 * 依赖方向单向（autoconfigure → 本模块），需要装配的使用方显式引入该模块。
 */

dependencies {

    api("org.springframework.boot:spring-boot-starter-security")
    cOptional("org.springframework.boot:spring-boot-starter-data-redis")
    cOptional("org.springframework.session:spring-session-data-redis")

    api(project(":ctool4j-web"))
    // 容器侧适配模块：`ctool4j-spring-security-servlet` 内在 `src/main/java`（jakarta）与 `src/main/java-javax`
    // 两套同包同名源码间按档位自动切换（见根 `build.gradle.kts`），故依赖模块恒定、不再按侧拼接模块名
    api(project(":ctool4j-spring-security-servlet"))

}
