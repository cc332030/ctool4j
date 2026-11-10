plugins {
    `java-library`
}

/**
 * Spring Security 自动装配模块：`CSecurityConfiguration` 及其配套的 `config/`（配置属性与
 * 放行路径契约）、`configuration/`（`CSecurityInit`）。
 *
 * 独立成模块而不是留在 `ctool4j-spring-security` 内，是为了让后者的主源集保持单源：
 * `CSecurityConfiguration` 的实质 API 差异是 `antMatchers`（SS 5.7，jdk8 档位）与
 * `requestMatchers`（SS 5.8 才有、SS 7 沿用）——同包同名两份按档位追加，故取「追加型」目录：
 * `src/main/java` 放公共类，`src/main/java-jdk8` / `src/main/java-latest` 各放一份
 * `CSecurityConfiguration`，与主源集无同名类冲突，根脚本自动追加，无需 `cExclusiveJdk8SourceDirs`。
 *
 * 依赖方向单向：本模块 → `ctool4j-spring-security`（契约层）；后者不反向依赖本模块，
 * 需要装配的使用方（如 `ctool4j-auth-spring`）显式引入本模块。
 */
dependencies {

    api("org.springframework.boot:spring-boot-starter-security")
    api(project(":ctool4j-spring-security"))
    // 直接使用 servlet 适配层的处理器（CAccessDeniedHandler / CAuthenticationEntryPoint）与
    // web 层的 CAbstractWebAuthFilter，均经 `ctool4j-spring-security` 传递可得，此处显式声明以明示直接依赖
    api(project(":ctool4j-spring-security-servlet"))
    api(project(":ctool4j-web"))
    api("org.springframework.cloud:spring-cloud-context")

}
