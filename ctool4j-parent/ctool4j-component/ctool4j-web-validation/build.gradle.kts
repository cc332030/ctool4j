import com.c332030.ctool4j.gradle.buildsrc.util.cExclusiveJdk8SourceDirs
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

/**
 * Bean Validation 适配模块：`@CRequired` 必填注解与其校验器。
 *
 * `CRequired` / `CRequiredValidator` 依赖 Bean Validation API，而该 API 在 jdk8 档位是 `javax.validation`、
 * 最新 LTS 档位是 `jakarta.validation`——同一个全限定名必须有两份实现，故 `src/main/java` 承 jakarta
 * （默认侧）、`src/main/java-javax` 承 javax（jdk8 侧），按档位整体替换（追加会重复）。
 *
 * 独立成模块而不是留在 `ctool4j-web` 内，是为了让 `ctool4j-web` 的主源集保持单源：
 * `ctool4j-web` 其余 46 个类与 Bean Validation 容器无关，留在原处会让整个模块被迫按档位拆主源目录。
 * 也不放容器适配层（`ctool4j-http-servlet`）：那一层的契约是"只做容器类型转换、零业务依赖"。
 */
dependencies {

    // Bean Validation API：jdk8 → `javax.validation:validation-api`，其余 → `jakarta.validation:jakarta.validation-api`
    // 只声明 API，实现（hibernate-validator）由使用方的 `spring-boot-starter-validation` 提供
    api(lib("validation-api"))

    // `CRequiredValidator` 复用 `CValidUtils` 做按类型分发的非空判定
    api(project(":ctool4j-core"))

    // 容器注入的真实 Validator 才是被测对象（不手动 new 校验器），故测试取 Spring 容器与 CTool4jSpringBootTest
    testImplementation(project(":ctool4j-spring"))

}

/**
 * 本模块的类按**两侧同包同名、全量镜像**维护：`src/main/java` 承 jakarta（默认侧）、`src/main/java-javax`
 * 承 javax（jdk8 侧），两目录的类集合完全一致（`CRequired` / `CRequiredValidator` 仅 Bean Validation
 * 包名不同，`CNotRequired` 两侧逐字节一致——它是纯标记注解，与 Bean Validation 无关，跟着成对放置是为了
 * 让本模块只有一种源码形态）。追加会重复，故 jdk8 档位用 `java-javax` 整体替换主源集与测试源集。
 */
cExclusiveJdk8SourceDirs("java-javax")
cExclusiveJdk8SourceDirs("java-javax", "test")
