
# cnb 规范（构建与运行环境）

> 适用范围：本规范仅适用于 cnb 云开发环境下的 ctool4j 项目构建/运行验证，供 AI 参考；不属于通用项目规范，不必遵循通用规范管理流程。

本规范记录本项目（ctool4j）在构建/运行验证时已确认的环境事实与踩坑结论，避免重复踩坑。

### 运行环境

- JDK：本项目只允许 JDK 8 编译运行（Java 8 目标）；当前环境 JDK 8 位于 `/opt/jdk8`（Temurin 8u502），运行 maven 时须设置 `JAVA_HOME=/opt/jdk8`；系统 apt 源仅提供 JDK 21，不作为替代
- Maven：优先使用 `/opt/maven/apache-maven-3.9.16/bin/mvn`（`mvnw` 内嵌下载 Maven 分发可能因网络失败）；环境路径配置不得写入项目文件、不随项目提交
- 依赖镜像：`repo.maven.apache.org` 对当前网络返回 429 限流，依赖经 `~/.m2/settings.xml` 配置的阿里云镜像（`maven.aliyun.com/repository/central`，`mirrorOf=*`）拉取；镜像属环境级配置，不得写入项目 pom、不随项目提交

### 构建命令

- `mvnw` 位于仓库根 `/workspace`（不在 `ctool4j-parent` 子目录下）；`mvn`/`mvnw` 命令一律从仓库根目录执行
- 模块构建用 `-pl` 指定相对仓库根的模块路径（如 `ctool4j-parent/ctool4j-component/ctool4j-core`），并加 `-am` 一并构建上游依赖模块
- 多模块构建指定 `-Dtest=xxx` 时，`-am` 构建的上游模块无匹配测试类会报 `No tests matching pattern` 失败，须加 `-Dsurefire.failIfNoSpecifiedTests=false`
- 测试通过标准：从构建日志中确认对应模块出现 `Tests run: N, Failures: 0, Errors: 0` 且整体 `BUILD SUCCESS`

### 结果验证

- 命令输出重定向与日志文件处置遵循 AGENTS.md"编译验证""临时产物统一目录"条目（重定向到 `tmp/` 日志、不保留不提交），此处不重复展开
- `execute_command` 的终端 stdout 可能被 IDE 附加输出干扰或截断，不以终端输出判断结果——先确认日志文件已生成（如 `wc -l`），再 `grep` 关键行（`Tests run`、`BUILD SUCCESS/FAILURE`、`[ERROR]`）得出结论
