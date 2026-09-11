# 项目特有规范

以下为 ctool4j 项目**特有**规范（远端统一规范 `https://agent.c332030.com/AGENTS_COMMON.adoc` 未包含或仅通用化提及的部分）。与远端规范冲突时，以本项目特有规范为准。

## 命名前缀

- 命名前缀（适用范围：本仓库**新增、重构的主代码**类型；测试夹具、注解处理器测试样例、生成代码及既有存量不受约束，随变更迁移、不强制批量改名）：
  - 类（class）统一使用 `C` 固定前缀。
  - 接口（interface）按类型区分前缀：
    - **纯字段接口**（仅为数据对象提供属性/字段契约的接口，如 `ICId`、`ICName`、`ICCreateBy`）统一使用 `IC` 固定前缀。
    - **功能型接口**（定义行为/能力方法、作为组件或框架扩展点被实现，含 function 接口）**不要求** `IC` 前缀（非禁止，而是不强制）：新增的使用 `C` 前缀（如 `CFunction`、`CBiConsumer`、`CService`）；沿用 JDK/Hutool 等既有命名的函数式接口保持原名（如 `StringFunction`、`ToStringFunction`、`StartEndTimeConsumer`）。
  - **禁止新增裸 `I` 前缀**类型：不得仅以 `I` 开头（如 `ISecuritySession` 应为 `ICSecuritySession`、`IFoo` 应为 `ICFoo`）；既有裸 `I` 类型（如 `IGenericType`）随变更迁移。

## Spring 配置

- @Bean 的方法名以 `c` 作为固定前缀

## Lombok

- 项目在根目录 `lombok.config` 全局配置 `lombok.toString.callSuper=call`、`lombok.equalsAndHashCode.callSuper=call`：继承场景下 lombok 自动在 `toString`/`equals`/`hashCode` 中包含父类字段，子类**无需**再显式标注 `@ToString(callSuper=true)`/`@EqualsAndHashCode(callSuper=true)`（标注属冗余，禁止新增）
- 全局配置 `lombok.fieldDefaults.defaultPrivate=true`：字段默认 private（显式 `public`/`protected` 字段不受影响）

## Java 反射（项目自有方法句柄工具）

- 反射操作优先使用 `CMethodHandleUtils`：访问字段（getter/setter）、调用方法、构造对象等反射操作一律优先使用 `CMethodHandleUtils` 提供的现成方法句柄（字段 getter/setter、方法、构造器），避免直接使用 `Field#get/set`、`Method#invoke`、`Constructor#newInstance` 等原生反射调用；`CMethodHandleUtils` 没有现成的方法时，可在其中补充后再使用。识别特征：代码中出现 `field.get/set`、`method.invoke`、`constructor.newInstance` 等直接反射调用，应改用 `CMethodHandleUtils` 对应方法句柄（如 `getGetterHandle/getSetterHandle/getHandle`），再用 `handle.invoke/invokeExact` 执行

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

## 与远端规范的差异（项目覆盖）

以下为项目既有约定；与远端统一规范不一致处，按远端「引用方项目自身规范优先」以本约定执行。存量不做一次性批量改动，随代码变更逐步迁移：

- 测试类包路径：测试类置于对应源包之下并加一层 `test` 段（如源类 `com.c332030.ctool4j.core.util.CMapUtils`，其测试类 `CMapUtilsTests` 位于同层 `test` 包 `com.c332030.ctool4j.core.test.util`）；远端 `specs/stack/java-testing.adoc` 的「测试类与源类包路径一致、不额外加 `test` 段」不适用于本项目，新增测试类沿用本约定
- 文档：javadoc 即文档——类级细节归源码 javadoc 注释；「一个类一个 adoc」已废弃，禁止新增类级 adoc；规划性/总结性的模块级、功能级文档统一置于仓库/模块 `doc/` 目录；存量类级 adoc 不强制删除

## 语言目标

主代码与测试代码统一以 **Java 8** 为目标版本（项目事实）：禁用 `List.of`/`Map.of` 等目标版本不支持的新语法糖；语言层面的规则见远端 Java 相关规范（`specs/stack/java.adoc`、`specs/stack/java-syntax.adoc`），此处仅记录本项目的目标版本取值。
