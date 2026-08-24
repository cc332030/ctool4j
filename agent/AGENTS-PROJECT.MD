# 项目特有规范

以下为 ctool4j 项目**特有**规范（远端统一规范 `http://agent.c332030.com/AGENTS.adoc` 未包含或仅通用化提及的部分）。与远端规范冲突时，以本项目特有规范为准。

## 命名前缀

- 命名前缀：当前项目所有类（class）统一使用 `C` 固定前缀，所有接口（interface）统一使用 `IC` 固定前缀；新增、重构代码均须遵循

## Spring 配置

- @Bean 的方法名以 `c` 作为固定前缀

## Lombok

- 项目在根目录 `lombok.config` 全局配置 `lombok.toString.callSuper=call`、`lombok.equalsAndHashCode.callSuper=call`：继承场景下 lombok 自动在 `toString`/`equals`/`hashCode` 中包含父类字段，子类**无需**再显式标注 `@ToString(callSuper=true)`/`@EqualsAndHashCode(callSuper=true)`（标注属冗余，禁止新增）
- 全局配置 `lombok.fieldDefaults.defaultPrivate=true`：字段默认 private（显式 `public`/`protected` 字段不受影响）

## Java 反射（项目自有方法句柄工具）

- 反射操作优先使用 `CMethodHandleUtils`：访问字段（getter/setter）、调用方法、构造对象等反射操作一律优先使用 `CMethodHandleUtils` 提供的现成方法句柄（字段 getter/setter、方法、构造器），避免直接使用 `Field#get/set`、`Method#invoke`、`Constructor#newInstance` 等原生反射调用；`CMethodHandleUtils` 没有现成的方法时，可在其中补充后再使用。识别特征：代码中出现 `field.get/set`、`method.invoke`、`constructor.newInstance` 等直接反射调用，应改用 `CMethodHandleUtils` 对应方法句柄（如 `getGetterHandle/getSetterHandle/getHandle`），再用 `handle.invoke/invokeExact` 执行

## 语法糖（C 工具类）

使用本项目提供的 C 工具类替代原生重复写法，统一代码风格。以下均为通用规则。

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

## 测试（项目细化）

- 测试代码语言层面保持 Java 8 目标（项目目标版本），禁用 `List.of`/`Map.of` 等新语法糖（见远端统一规范的 Java 规范与本文件「语法糖（C 工具类）」）；语法糖（C 工具类）使用按主代码标准执行
