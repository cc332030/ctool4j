
# java 规范

## 编码

- 禁止移除 lombok 的使用
- 使用 lombok val、var
- 实体类使用 @Data、@SuperBuilder、@NoArgsConstructor、@AllArgsConstructor
- JSON 处理全局统一使用 Jackson，禁止引入其他 JSON 库（如 Gson、fastjson）

## 健壮性

- 公共方法对边界输入显式处理：null 入参、无匹配结果（如 map get 不到、反射找不到对应构造器）必须返回空集合/默认值或快速失败（requireNonNull/断言），不得让调用方遇到隐式 NPE
- 禁止修改调用方传入的可变入参（数组、集合、对象内容）；确需就地处理时，API 契约必须显式声明
- 消费输入流必须保证后续可用：读取后重新缓冲再返回，异常路径不得返回已消费且未重缓冲的对象
- 资源生命周期归属一致：static 线程池等全局资源不得由实例 @PreDestroy 关闭，防止多上下文互相影响
- 字节数组转字符串必须显式指定字符集；HTTP 文本内容优先从 Content-Type 解析 charset，未声明才使用默认值
- 链式字符串替换使用占位符或一次性正则，避免替换产物被后续规则二次改写；缓存键命名不得与输入空间可能出现的值冲突
- 按标准协议（如 HTTP 报文）拼接日志或输出时，结构须符合协议规范（首行即协议行，不得以空行开头、不得插入非协议内容）
- 需要内存回收（弱引用/软引用语义）的缓存统一使用 Caffeine 的 weakKeys/weakValues/softValues，禁止手写 WeakHashMap/软引用自实现，避免弱引用语义与并发安全踩坑

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
