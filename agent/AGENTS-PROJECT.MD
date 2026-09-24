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
- **公共内容的落点判定**：只与 Servlet 公共面有关（请求、响应、过滤器、转发器、跨包异常）→ `ctool4j-http-base`；属 Spring 扩展点体系（拦截器、响应体增强、请求工具、cors 配置与工具、错误响应组装）→ `ctool4j-spring-base`（**不引 servlet-api**，只用 `ctool4j-http-base` 的抽象类型）；安全侧（未认证、访问拒绝）→ `ctool4j-spring-security-base`；跨模块通用常量与返回体 → `ctool4j-definition`。
- **命名分层**（本仓库固定的继承方向）：
  - 基层契约用正名 `ICXxx`，如 `ICHandlerInterceptor`、`ICResponseBodyAdvice`。
  - 两侧适配层与基层契约**同包**时必须另取名字避免重名，此时统一加 `Spring` 限定：`ICSpringHandlerInterceptor`、`ICSpringResponseBodyAdvice`（仍是**接口**，桥接方法用 `default` 一次给全）。不构成重名的两侧同名类（如 `ICFilter`、`CRequestUtils`、`CAuthenticationEntryPoint`、`CErrorController`）**沿用原名**，靠模块切换生效，不为加限定而改名——两侧同名与否由「是否存在同包同名的基层契约」决定，不由"是否在两侧模块"决定。
  - 业务实现用 `C` 前缀，如 `CCorsInterceptor`。
  - **禁止 `C 继承 IC 继承 C`**：`IC` 只可继承 `IC`，`C` 只可继承 `C` 或实现 `IC`。

### 使用约束

- 子模块**不要直接使用两侧模块的类**：优先用 base 的接口与类型；对外方法签名、接口参数也暴露 base 类型。
- 业务模块里把容器对象转成抽象层对象是**允许且必要**的边界用法（如 `CHttpServletResponse.of(response)`），但出现位置越少越好；base 已提供的能力不要再走两侧（例：取客户端 IP 用 `CHttpRequest#getClientIp`，不要解包适配器）。
- **不需要**为了实现两侧兼容而把业务类搬到两侧模块：类留在原模块、把签名改成抽象层类型即可；只有签名被框架锁死（Spring 扩展点、`@RequestMapping` 参数）时，才落两侧同名接口/基类。
- 无容器依赖的类、常量、注解一律放 base（例：`CConfigurationProperties`、`CResourceUrlConstants`、cors 配置与工具）。
- 两侧同名类**只留必须接触容器的部分**（包装容器对象、被框架锁死的签名、两侧取值不同的常量）；不接触容器的算法与分支抽到 base 的公共类
  （按职责命名，如 `CHttpRequestUtils`、`CErrorUtils`、`CResourceUrlUtils`），两侧同名类**按需调用**它，公共实现只存一份、两侧不复制。
- base 公共类**按职责语义命名，不用 `CBaseXxx`**：它们多是 `@UtilityClass` 纯静态工具，`Base` 会被读成"抽象基类"（名副其实的错位）。
  例：`CHttpRequestUtils` 整族方法第一参即 `CHttpRequest`，比 `CBaseRequestUtils` 自解释。
- base 公共类**不得与两侧同名类重名**：base 与 `-javax`/`-jakarta` 同时在同一 classpath 上，同名同包的两份 class 会让 JVM 取先命中者且**不报错**，属不可控行为。
  故两侧已占用的主名（如 `CRequestUtils`）不能被 base 层再占用——这也意味着"把 base 实现改叫主名"的方案不可取。
- 两侧同名类的**标准形态**（既有做法：`CRequestUtils` ↔ `CHttpRequestUtils`）：base 公共类收「以抽象层对象为入参」的纯逻辑；两侧同名类保留**同名对外方法**，方法体只做「取当前容器对象 → 包装 → 委托 base」两件事。这样公共实现单份、两侧不复制，调用方换模块也零改动。
- **测试夹具可直接使用两侧适配器**（如 `CHttpServletRequest.of(mockRequest)`）：测试要构造容器场景，属允许的转换边界；「少用两侧」的约束针对业务主代码。
- **严禁工具类继承工具类**：工具类（`@UtilityClass` 或纯静态工具）之间只用**调用（组合）**复用，不建立继承关系（工具类保持 final，不因复用而开继承口）。
- **编排也算逻辑**：多步调用序与放行判定（如「先设置跨域头 → 再判 OPTIONS 预检 → 返回是否继续」）下沉 base 只写一份（`CCorsUtils.handleAndContinue`）；两侧同名类只剩「包装容器对象 + 按 base 的返回值放行」。
- **容器逃生口唯一**：抽象层表达不了的能力（二进制输出流、会话、Cookie）只在 base 留**一个**取底层对象的入口（如 `CHttpRequestUtils#getServletRequestAttributes`）；调用处用 `val` 承接、不 import 容器包，其余场景一律走抽象层方法。
- **容器选型点集中**：两侧适配模块的直接依赖只出现在要选边的模块（`ctool4j-spring`、`ctool4j-spring-security`）；业务模块不因取容器类型而声明它，需要时经依赖链传递即可。
- **唯一例外（容器无关方法仍留两侧）**：某抽象层方法是子类的**覆写扩展点**、必须保持 `protected` 时（如 `CAbstractWebAuthFilter#doFilterInternal(CHttpRequest, CHttpResponse, CFilterChain)`）不能上移到 base 的接口——接口方法只能是 `public`，加宽可见性会破坏既有子类的覆写；此情形允许两侧各留一份默认实现，并在类 javadoc 的「已知限制与取舍」写明理由。
- 两侧**取值不同**的常量不能放 base（例：`RequestDispatcher.ERROR_EXCEPTION` 两侧分别是 `javax.servlet.error.exception` 与 `jakarta.servlet.error.exception`），只能落在两侧同名类上；下游引用同名常量，切模块即切值。
- 抽象层方法只声明容器无关的异常（`CServletException`、`IOException`），不引入 `ServletException`。
- 两侧模块若使用日志门面（`@CustomLog`），须显式声明 `ctool4j-core` 依赖（`ctool4j-http-base` 不传递 core）。
- **抽象层加能力的落点**：抽象层需要新能力时，加到 `ctool4j-http-base` 的接口（`CHttpRequest`/`CHttpResponse`/`CFilter` 等）并在**两侧适配器同步实现**；能用既有抽象层方法表达的，不要为省事去解包底层对象。
- **迁移既有类的手法**（本仓库实际做法）：类需要随 Servlet 面归位时用 `git mv` 保留历史、**包名与类名保持不变**——调用方 import 与调用一行不改，只靠模块依赖切换生效；测试随实现一起迁；迁移原因与验证结论落 `tmp/plan` 记录。

### 构建与验证

- `-pl <聚合 pom>` **不会**递归构建子模块；构建整棵子树须用 `-f <聚合 pom>/pom.xml`（或 `-amd`）。用错会得到"假通过"。
- **改动后必须真验证**：用 `-am install` 覆盖受影响模块及其上游（同时验证编译、单测与 javadoc 生成）；只跑 `compile` 或只跑目标模块会漏掉跨模块调用方与 javadoc 错误。日志落 `tmp/build/mvn-*.log` 便于回溯，验证结论（含失败用例数、跳过的检查）如实报告，不得把"未验证"说成"已通过"。
- **新增模块要两处登记**：（1）父聚合 pom 的 `<modules>`；（2）`ctool4j-dependencies` 的构件登记（版本由父管理，使用处不写 `<version>`）。漏第 2 处会让下游依赖解析失败，漏第 1 处则该模块根本不参与构建。暂不纳入当前矩阵的模块（如 `-jakarta` 侧）须在 pom 内写明**原因与回挂方式**。
- 只登记当前依赖矩阵下可编译的模块：`-jakarta` 侧需要 Spring 6 的 Servlet 支持，在本仓库 Spring 5.3 / Boot 2.x 矩阵下**不登记**进 `<modules>`（`ctool4j-http-jakarta` 不依赖 Spring，登记构建），`ctool4j-spring-base-pom/pom.xml` 内已注明回挂方式。**未登记的侧模块不参与构建**，其正确性只能靠「两侧同构 + 自检」保证——故不得在其中写 javax 侧没有的内容。
- **改 `-javax` 侧必须同步 `-jakarta` 侧**：两侧同名文件除 `javax`/`jakarta` 字样外应**完全一致**。副本靠人工维护会漂移（本仓库已出现过 7 个副本滞后：`http` 侧适配器 5 个、spring 侧 2 个）。改完后按下式自查，差异应为空：

  ```powershell
  # 归一化 javax/jakarta 字样后逐文件比对，输出两侧不一致的文件
  # 注意：$jv/$jk 必须是【绝对路径】——脚本用 FullName 裁剪前缀，传相对路径会把每个文件都误报为 DIFF
  $jv='<绝对路径 ...-javax 模块目录>'; $jk='<绝对路径 ...-jakarta 模块目录>'
  Get-ChildItem "$jv\src\main" -Recurse -Filter *.java | ForEach-Object {
    $rel=$_.FullName.Substring($jv.Length+1); $other=Join-Path $jk $rel
    $a=([IO.File]::ReadAllText($_.FullName)).Replace('javax','X').Replace('jakarta','X')
    $b=if(Test-Path $other){([IO.File]::ReadAllText($other)).Replace('javax','X').Replace('jakarta','X')}else{''}
    if($a -ne $b){ "DIFF: $rel" }
  }
  ```

- **两侧同文的判定口径（自检的适用边界）**：
  - **侧别专属**字样（`javax.servlet` 一类的 import、「（本份承接 javax 侧）」一类措辞）随侧别替换；**两侧成对提及**（同一处同时出现两个词，如「javax 与 jakarta」「`RequestDispatcher.ERROR_EXCEPTION` 两侧分别是 …」）在两侧保持**同一份原文**——用"整体互换/全量替换"的脚本同步会把双提及写成「jakarta 与 jakarta」，而归一化自检恰好看不出这种坏损。
  - 两侧文件**行尾须一致**（按 `.gitattributes` 一律 LF）：行尾差异会被归一化比对报成"内容漂移"（本仓库实测：2 个 spring 侧文件因 javax 侧 CRLF / jakarta 侧 LF 而误报）。
  - 自查范围是 **`src/main`**；测试只落在 `-javax` 侧，不要求两侧同名。
- **两侧 pom 依赖同构**：`-javax` 与 `-jakarta` 的依赖清单保持一致（只差 servlet-api 坐标与模块名）；两侧用到 `@CustomLog` 时都须显式声明 `ctool4j-core`（见「使用约束」）。不对称不易被发现（未登记的侧模块不参与构建，只能逐条核对）——本仓库实测：`ctool4j-spring-jakarta` 曾漏 `ctool4j-core`。
- **下沉/迁移方法时用例同迁**：方法下沉 base 后，其单元测试一并迁到该 base 模块的 `src/test`；base 模块没有 servlet-api，用例只 mock 抽象层类型（`CHttpRequest`/`CHttpResponse`），接口的 `default` 方法要用 `thenCallRealMethod()` 才会跑到真实实现（mock 默认不执行默认方法）；容器相关的用例留在 `-javax` 侧。

- **两侧桥接方法一律走动态分派**：桥接里调用抽象层方法时写普通调用（`preHandle(...)`），**不得**写 `IXxx.super.preHandle(...)`——后者直接调基层契约的默认实现、**绕过实现类的覆写**，下游覆写失效且不会报错。唯一例外是「Spring 侧为抽象方法、必须给默认值」的场景（如 `ResponseBodyAdvice#supports`）。

## 测试约定

- 命名 `被测类名 + Tests`（复数 `s`，如 `CCorsUtilsTests`；全仓 `*Tests` 269 个 vs `*Test` 2 个），置于 `src/test/java` 下**与被测类相同的包**。
- 框架统一 JUnit 5（`org.junit.jupiter`，269 个测试类全覆盖），断言用 **JUnit 自带的 `Assertions.*`**（`assertNotNull`/`assertEquals`/`assertThrows`/`assertArrayEquals`…）——**不引入 AssertJ / Hamcrest**（全仓 0 使用）；Mockito 仅在确需打桩处按需使用。
- Spring 容器 / Web 场景用 Spring Mock（`MockHttpServletRequest`/`MockHttpServletResponse` 等，22 处）；需要走抽象层时经两侧适配器包装（`CHttpServletRequest.of(...)`）。
- 测试里出现的静态状态（工具类静态配置等）必须在 `@AfterEach` 还原，避免用例间相互污染。
- 实现类迁移时**测试随实现一起迁**（`git mv`），不做"实现搬走、测试留在原模块"。

## 代码注释与版本

- 主代码类/接口的 javadoc 统一按六小节组织（全仓既有风格，**按类实际情况取舍，无内容的小节不留空标题**）：
  `能力目录`、`设计要点`、`兜底设计`、`适用范围`、`不适用与边界场景`、`已知限制与取舍`。
- 每个主代码类/接口**必须**有 `@since` 与 `@version`：新增类从 `1.0` 起；**改动既有类时递增 `@version`**（如 `1.2 → 1.3`），未改动的类保持原值、不批量刷版本。
- 类 javadoc 首段用 `<p>Description: 类名</p>`，其后按需接概述段。方法 javadoc 的 `@param`/`@return`/`@throws` 与签名一致，不写与实现重复的废话注释。

## 统一响应结构

- 对外返回统一响应结构时**优先使用 `CStrResult`**（`com.c332030.ctool4j.definition.model.result.impl`，实现 `ICStrResult`）：
  签名暴露 `ICStrResult` 等 `IC*Result` 契约，构造用实现类（`CStrResult`）。
- **错误响应体一律走 `CStrResult`**（如 `CStrResult.error(HttpStatus, message)`）：异常处理器、安全处理器（未认证/访问拒绝）、过滤器与拦截器的写出处，全项目保持同一种错误体结构，**不新造响应 DTO**。
- 需要非字符串数据时用同族实现（`CIntResult`、`CIntMsgResult`），不新增结果基类、不绕开 result 包自建返回结构。

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
