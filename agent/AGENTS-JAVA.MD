
# java 规范

## 编码

- 禁止移除 lombok 的使用
- 使用 lombok val、var
- 实体类使用 @Data、@SuperBuilder、@NoArgsConstructor、@AllArgsConstructor

## lambda

- e -> e 换成 CFunction.self
- (a, b) -> a 换成 CBiFunction.first()

## javadoc

注释需满足发布私服 / Maven 中央仓库的 javadoc 检查（doclint）要求：

- 所有 public / protected 的类、方法、字段必须编写 javadoc 注释
- 多行注释永远写多行：`/**` 单独一行、注释内容逐行书写、`*/` 单独一行，禁止单行写法 `/** xxx */`
- javadoc 使用完整多行块，包含 `@param`、`@return`、`@throws`、`@since`：

```java
/**
 * 功能描述
 *
 * @param xxx 参数说明
 * @return 返回值说明
 */
```
- `@param` 须与参数一一对应且名称一致；泛型参数使用 `@param <T> 泛型描述`
- 非 void 方法必须标注 `@return`；抛出受检异常时标注 `@throws`
- 特殊字符必须转义：`< > &` 使用 `&lt; &gt; &amp;`，或使用 `{@code ...}` 包裹（如 `{@code <=0}`、`{@code <T>}`）
- HTML 标签必须闭合：`<p>` 换行、`<ul><li>` 列表正确嵌套闭合
- 字段注释须说明含义、默认值及配置方式
- 构建器 setter 方法注释须标注 `@param`、`@return this`
