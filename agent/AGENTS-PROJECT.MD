# 项目特有规范

以下为 ctool4j 项目**特有**规范（远端统一规范 `https://agent.c332030.com/AGENTS_COMMON.adoc` 未包含或仅通用化提及的部分）。与远端规范冲突时，以本项目特有规范为准。

## 命名前缀

- 命名前缀（适用范围：本仓库**新增、重构的主代码**类型；测试夹具、注解处理器测试样例、生成代码及既有存量不受约束，随变更迁移、不强制批量改名）：
  - 类（class）统一使用 `C` 固定前缀。
  - 接口（interface）按类型区分前缀：
    - **纯字段接口**（仅为数据对象提供属性/字段契约的接口，如 `ICId`、`ICName`、`ICCreateBy`）统一使用 `IC` 固定前缀。
    - **纯字段接口（`IC` 开头的接口）不声明 set 方法**（除非确有必要）：接口只暴露读取契约，写入能力由实现类承担（lombok `@Data`/`@Setter`、构造器、Builder）。原因是 `void setXxx(...)` 与 lombok 链式访问器不兼容：`@Accessors(chain = true)` 生成的 setter 返回 `this`，而返回值类型无法重写 `void` 方法，实现类无法同时满足「链式访问器」与「接口契约」——即"接口里一个 void setter 会挡掉所有想用链式 setter 的实现"，实现类编译失败。确有必要时（如需框架按接口写入）必须同时确认实现类不使用链式访问器，并在接口 javadoc 写明该约束。
    - **功能型接口**（定义行为/能力方法、作为组件或框架扩展点被实现，含 function 接口）**不要求** `IC` 前缀（非禁止，而是不强制）：新增的使用 `C` 前缀（如 `CFunction`、`CBiConsumer`、`CService`）；沿用 JDK/Hutool 等既有命名的函数式接口保持原名（如 `StringFunction`、`ToStringFunction`、`StartEndTimeConsumer`）。
  - **禁止新增裸 `I` 前缀**类型：不得仅以 `I` 开头（如 `ISecuritySession` 应为 `ICSecuritySession`、`IFoo` 应为 `ICFoo`）；既有裸 `I` 类型（如 `IGenericType`）随变更迁移。
  - **接口继承方向**：接口只可继承接口（`IC` → `IC`）；实现类继承实现类或实现接口（`C` → `C`/`IC`），**禁止出现 `C 继承 IC 继承 C`** 这种交叉。多栈（javax/jakarta）场景下"基层契约用正名、两侧适配层加 `Spring` 限定"的具体取值见下文「javax/jakarta 双栈（抽象层 + 两侧同名适配）」。

## javax/jakarta 双栈（抽象层 + 两侧同名适配）

背景：`javax.servlet` 与 `jakarta.servlet` 的类型出现在 Spring 扩展点的方法签名里，业务模块无法只靠类型替换同时支持两套容器。项目按「抽象层（容器无关）+ 两侧同名适配（容器相关）」落地，规则如下。

### 模块分层

- **base 模块（抽象契约，容器无关）**：`ctool4j-http-base`、`ctool4j-spring-base`、`ctool4j-spring-security-base`。只放不依赖任何 Servlet 包的接口、常量与工具。
- **两侧适配模块（`-javax` / `-jakarta`）**：性质是**底层桥接**（类比 JDK 的 `sun.*`），只做「容器对象 ↔ 抽象层对象」的类型转换，不放业务逻辑。两侧**同包同名**，切换容器只需把依赖模块从 `-javax` 换成 `-jakarta`，业务代码与 import 不变。
- **命名分层**（本仓库固定的继承方向）：
  - 基层契约用正名 `ICXxx`，如 `ICHandlerInterceptor`、`ICResponseBodyAdvice`。
  - 两侧适配层与基层契约同包，必须另取名字，统一加 `Spring` 限定：`ICSpringHandlerInterceptor`、`ICSpringResponseBodyAdvice`（仍是**接口**，桥接方法用 `default` 一次给全）。
  - 业务实现用 `C` 前缀，如 `CCorsInterceptor`。
  - **禁止 `C 继承 IC 继承 C`**：`IC` 只可继承 `IC`，`C` 只可继承 `C` 或实现 `IC`。

### 使用约束

- 子模块**不要直接使用两侧模块的类**：优先用 base 的接口与类型；对外方法签名、接口参数也暴露 base 类型。
- 业务模块里把容器对象转成抽象层对象是**允许且必要**的边界用法（如 `CHttpServletResponse.of(response)`），但出现位置越少越好；base 已提供的能力不要再走两侧（例：取客户端 IP 用 `CHttpRequest#getClientIp`，不要解包适配器）。
- **不需要**为了实现两侧兼容而把业务类搬到两侧模块：类留在原模块、把签名改成抽象层类型即可；只有签名被框架锁死（Spring 扩展点、`@RequestMapping` 参数）时，才落两侧同名接口/基类。
- 无容器依赖的类、常量、注解一律放 base（例：`CConfigurationProperties`、`CResourceUrlConstants`、cors 配置与工具）。
- 两侧**取值不同**的常量不能放 base（例：`RequestDispatcher.ERROR_EXCEPTION` 两侧分别是 `javax.servlet.error.exception` 与 `jakarta.servlet.error.exception`），只能落在两侧同名类上；下游引用同名常量，切模块即切值。
- 抽象层方法只声明容器无关的异常（`CServletException`、`IOException`），不引入 `ServletException`。
- 两侧模块若使用日志门面（`@CustomLog`），须显式声明 `ctool4j-core` 依赖（`ctool4j-http-base` 不传递 core）。

### 构建与验证

- `-pl <聚合 pom>` **不会**递归构建子模块；构建整棵子树须用 `-f <聚合 pom>/pom.xml`（或 `-amd`）。用错会得到"假通过"。
- 只登记当前依赖矩阵下可编译的模块：`-jakarta` 侧需要 Spring 6 的 Servlet 支持，在本仓库 Spring 5.3 / Boot 2.x 矩阵下**不登记**进 `<modules>`（`ctool4j-http-jakarta` 不依赖 Spring，登记构建），`ctool4j-spring-base-pom/pom.xml` 内已注明回挂方式。
- **改 `-javax` 侧必须同步 `-jakarta` 侧**：两侧同名文件除 `javax`/`jakarta` 字样外应**完全一致**。副本靠人工维护会漂移（本仓库已出现过 7 个副本滞后：`http` 侧适配器 5 个、spring 侧 2 个）。改完后按下式自查，差异应为空：

  ```powershell
  # 归一化 javax/jakarta 字样后逐文件比对，输出两侧不一致的文件
  $jv='<...-javax 模块目录>'; $jk='<...-jakarta 模块目录>'
  Get-ChildItem "$jv\src\main" -Recurse -Filter *.java | ForEach-Object {
    $rel=$_.FullName.Substring($jv.Length+1); $other=Join-Path $jk $rel
    $a=([IO.File]::ReadAllText($_.FullName)).Replace('javax','X').Replace('jakarta','X')
    $b=if(Test-Path $other){([IO.File]::ReadAllText($other)).Replace('javax','X').Replace('jakarta','X')}else{''}
    if($a -ne $b){ "DIFF: $rel" }
  }
  ```

- **两侧桥接方法一律走动态分派**：桥接里调用抽象层方法时写普通调用（`preHandle(...)`），**不得**写 `IXxx.super.preHandle(...)`——后者直接调基层契约的默认实现、**绕过实现类的覆写**，下游覆写失效且不会报错。唯一例外是「Spring 侧为抽象方法、必须给默认值」的场景（如 `ResponseBodyAdvice#supports`）。

## Spring 配置

- @Bean 的方法名以 `c` 作为固定前缀
- 配置属性（`@ConfigurationProperties`）的 Bean 由 **ctool4j-spring 全局注册**：其 `@ConfigurationPropertiesScan` 扫描 `com.c332030.ctool4j`（`@CSpringBootApplication` 亦已开启该扫描），故各模块的属性类只声明 `@ConfigurationProperties` 即可，**不重复声明** `@EnableConfigurationProperties`（依赖链中已含 ctool4j-spring，属性绑定天然生效）

## Lombok

- 项目在根目录 `lombok.config` 全局配置 `lombok.toString.callSuper=call`、`lombok.equalsAndHashCode.callSuper=call`：继承场景下 lombok 自动在 `toString`/`equals`/`hashCode` 中包含父类字段，子类**无需**再显式标注 `@ToString(callSuper=true)`/`@EqualsAndHashCode(callSuper=true)`（标注属冗余，禁止新增）
- 全局配置 `lombok.fieldDefaults.defaultPrivate=true`：字段默认 private（显式 `public`/`protected` 字段不受影响）
- 全局配置 `lombok.addLombokGeneratedAnnotation=true`：lombok 在其生成的成员（getter/setter、构造器、`equals`/`hashCode`/`toString`、Builder 等）上标注 `@lombok.Generated`（**`CLASS` 保留**），供 JaCoCo/PIT 等按字节码分析的工具识别并**排除自动生成代码**，覆盖率/变异统计只计手写逻辑（对齐远端 `specs/general/testing.adoc`「覆盖率统计须排除自动生成的代码（lombok 生成…）」）。要点：
  - 标记写在 `.class` 字节码里，**不要求**源码引用 lombok、也**不要求**注解类出现在运行时（lombok 为 `provided`）：工具按注解**简单名** `Generated` 匹配即可排除；反之**运行时反射读该注解读不到**（`CLASS` 保留），需要运行时读注解的场景不适用。
  - 按简单名匹配 ⇒ **同名即排除**：手写方法不得标注任何简单名为 `Generated` 的注解（含 `javax.annotation.Generated`、`javax.annotation.processing.Generated`、自定义 `@Generated`），否则会被一并排除在统计之外。
  - 接入 JaCoCo/PIT 时无需再手工排除 lombok 生成代码（本配置已提供标记）；未接入覆盖率/变异工具时，该配置不产生统计效果。
- 接口中声明 `void setXxx(...)` 会与链式访问器（`@Accessors(chain = true)`）冲突：生成的 setter 返回 `this`，无法重写 `void` 方法，实现类编译失败。约束与例外见「命名前缀 - 纯字段接口」。

## Java 反射（项目自有方法句柄工具）

- 反射操作优先使用 `CMethodHandleUtils`：访问字段（getter/setter）、调用方法、构造对象等反射操作一律优先使用 `CMethodHandleUtils` 提供的现成方法句柄（字段 getter/setter、方法、构造器），避免直接使用 `Field#get/set`、`Method#invoke`、`Constructor#newInstance` 等原生反射调用；`CMethodHandleUtils` 没有现成的方法时，可在其中补充后再使用。识别特征：代码中出现 `field.get/set`、`method.invoke`、`constructor.newInstance` 等直接反射调用，应改用 `CMethodHandleUtils` 对应方法句柄（如 `getGetterHandle/getSetterHandle/getHandle`），再用 `handle.invoke/invokeExact` 执行
- 方法句柄获取按调用频次选择（判定口径）：**一次性**（获取语句只执行一次，句柄由调用方长期持有、存入自身缓存或闭包）用生成版 `toXxxHandle`/`toHandle`，**不进句柄缓存**；**多次访问**（同一 `Field`/`Method`/`Constructor` 会被反复获取且句柄不被调用方持有）用缓存版 `getXxxHandle`/`getHandle`；频次不可知（对外公共 API）默认缓存版。判定细则与 `getXxxHandleAsType` 的缓存边界见 `CMethodHandleUtils` 的「API 选用（一次性 / 多次访问）」

## 语法糖（C 工具类）

使用本项目提供的 C 工具类替代原生重复写法，统一代码风格。以下为本项目约定的 C 工具类统一用法（主代码与测试代码同等适用）。

### 集合（List / Set）

- 创建集合：
  - 空集合用 `CList.of()` / `CSet.of()` / `CMap.of()`
  - 指定大小用 `CCollUtils.newList/newSet/newLinkedSet/newMap/newLinkedMap`
  - 可变参数创建用 `CList.of(...)` / `CSet.of(...)`（自动过滤 null，返回不可变集合）
- 过滤：`collection.stream().filter(predicate).collect(...)` 换成 `CCollUtils.filter(collection, predicate)`
  - 过滤 null → `CCollUtils.filterNull(collection)`
  - 过滤非空字符串 → `CCollUtils.filterString(collection)`
  - 按转换后的 key 过滤 → `CCollUtils.filterKey(collection, convert)`
- 转换（map）：`stream().map(f).filter(p).collect(toList()/toSet())` 换成 `CCollUtils.convert(...)` / `convertSet(...)`（默认过滤 null）
- List 转 Map：`stream().collect(Collectors.toMap(...))` 换成 `CCollUtils.toMap(coll, toKey[, toValue][, mergeFunction])`
- 分组：`Collectors.groupingBy(...)` 换成 `CCollUtils.groupingBy(coll, keyFn)`
- 拼接：`stream().collect(...)` 合并元素换成 `CCollUtils.concat/concatOne`，逐个判空 add 换成 `addIgnoreNull/addIgnoreEmpty/addIgnoreBlank`、`addAllIgnoreNull`
- 判空保护：`CCollUtils.defaultEmpty`、`first`、`last`、`get`、`size`、`contains`
- 按 key 去重：`CStreamUtils.distinctByKey(keyFn)`（作 filter 参数）

### Map

- 键/值转换：`entrySet().stream().collect(...)` 换成
  - 只转键 → `CMapUtils.mapKey(map, keyMapper)`
  - 只转值 → `CMapUtils.mapValue(map, valueMapper)`
  - 键值都转 → `CMapUtils.map(map, keyMapper, valueMapper)`
- 过滤：`entrySet().stream().filter(...).collect(...)` 换成 `CMapUtils.filter(filterKey/filterValue)(map, predicate)`
- 合并多个 Map：`CMapUtils.merge(maps...)`（默认取 first）或 `CMapUtils.merge(mergeFunction, maps...)`
- 判空写入：`CMapUtils.put(map, k, v)`、`computeIfAbsent(map, key, supplier/function)`

### Optional

- 空安全链式：`COpt.ofNullable(x).map(f).orElse(d)`
- 集合/字符串判空：`COpt.ofEmptyAble(...)`；空白字符串判断：`COpt.ofBlankAble(...)`

### 参数校验（有效性判断）

- 参数校验（判 null / 判空白 / 判空集合、Map、数组）统一使用 `CValidUtils`（`com.c332030.ctool4j.core.validation.CValidUtils`，`ctool4j-core`）的 `isValid` / `isNotValid`，按类型自动分发：字符串按 blank、集合/`Iterable`/Map/数组按 empty、其他对象按 null；有效性判断不再手写 `x == null`、`StringUtils.isBlank`、`CollUtil.isEmpty` 等逐类型判空，也不再引入其他有效性判断工具（本条范围限有效性判断，不含断言式校验等场景）
- 调用注意：传入具体类型变量（由重载自动匹配）；传 `null` 字面量会因多重载歧义而编译失败
- `CValidateUtils` 已废弃（`@Deprecated`）：新代码不得使用，一律改用 `CValidUtils`
- 存量代码不做一次性全量替换：随代码变更（改动到相关代码时）逐步迁移；未改动处的既有写法不作为问题处理

## 已知问题登记与 review 豁免

- **登记落点**：影响依赖升级的已知不兼容问题（当前面向 Spring Boot 2.x → 3.x / JDK 17）统一登记在 `doc/compatibility.adoc`——该文件是这类**已知问题的唯一登记处**，条目须带可执行的处置（或处理方案）与状态（`待处理` / `已修复` / `待评估`）。
- **review 豁免**：已在 `doc/compatibility.adoc` 登记的条目，code review 时**不再作为问题提出**（依远端 code review 规范的 `KNOWN` 归类：仅留痕、不进入待处理问题序列）。这类问题受当前基线版本约束、无法立即修复，反复提出只挤占注意力而不产生行动。
- **豁免边界**：豁免仅限「已登记」的条目——未登记的新问题须正常提出；已登记条目的影响面或结论有变化时，先在同一处更新该条目，再按新状态 review。

## 与远端规范的差异（项目覆盖）

- **变更日志仅在用户明确要求时登记**（覆盖远端 `specs/general/doc-design.adoc`「版本与变更记录 - 同提交同步与完成定义」）：远端要求"改动源文件时其 changelog 条目须在本提交中一并更新，只改一侧视为未完成"；本项目调整为——**仅在用户明确要求时追加 `CHANGELOG.adoc` 条目，日常改动不写本文件**，不因"改了代码"顺手补一行。
  - 覆盖理由：日常改动逐条登记与源文件 javadoc、`git log` 大量重复，条目噪音会掩盖真正的对外变更。
- **类间互引只做「测试 → 源」单向**（覆盖远端 `specs/general/doc-design.adoc`「类间关系与双向引用」中"源类标注其各类测试类"一项）：主代码类注释**不得**用 `@see`/`{@link}` 指向测试类——javadoc 的类路径不含测试源，未解析的 `@see` 会使 javadoc 构建失败（已实测：退出码 1）；主代码需要提及时用 `{@code 测试类名}` 纯文本。反向（测试类用 `@see` 标注源类）照常执行。

## 语言目标

主代码与测试代码统一以 **Java 8** 为目标版本（项目事实）：禁用 `List.of`/`Map.of` 等目标版本不支持的新语法糖；语言层面的规则见远端 Java 相关规范（`specs/stack/java.adoc`、`specs/stack/java-syntax.adoc`），此处仅记录本项目的目标版本取值。
