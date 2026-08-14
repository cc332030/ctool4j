
# 测试规范

以下规范适用于本项目所有模块的单元测试（JUnit 5）。

- 测试目标：优先覆盖"容易出错或出错后难发现"的方法，含正常路径、边界与异常路径；异常断言用 `assertThrowsExactly` 精确匹配异常类型，不用 `assertThrows`（避免父类异常掩盖子类语义）
- 测试代码与主代码遵循相同的语言规范（Java 8 目标，禁用 `List.of`/`Map.of` 等新语法糖，见 AGENTS-JAVA.MD、AGENTS-SYNTAX.MD）
- 只新增测试文件，不修改 `src/main` 源码；测试中发现主代码真实行为与直觉不符时，以实际行为修正断言并在注释中说明，不改主代码
- 验证以 Maven 结果为准：Windows cmd 下编译失败 exit code 可能为 0，必须读取 `target/surefire-reports` 的 Tests run/Failures/Errors/Skipped 判断是否通过
- 同一 Maven 模块禁止并行构建；多 agent 协作时同一模块分阶段执行（先写测试文件，再串行执行验证），避免构建冲突
- 涉及 lombok 等语法糖时注意实际展开结果（如 `val` 的类型推断），避免用例预期与实际不符
- 含 null 元素的集合字面量需显式泛型（如 `Arrays.<Integer>asList`），避免类型推断歧义
- 依赖反射/序列化（Jackson、hutool 等）的测试注意资源释放（如 generator 需 close）与构造器可见性（反射默认只取 public 构造器）
