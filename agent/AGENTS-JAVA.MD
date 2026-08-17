
# java 规范

## 编码

- 强制优先使用 lombok：能用 lombok 代替的绝不手写——getter/setter（@Getter/@Setter/@Data）、构造器（@NoArgsConstructor/@RequiredArgsConstructor/@AllArgsConstructor）、builder（@SuperBuilder）、toString/equals/hashCode（@ToString/@EqualsAndHashCode）等一律用 lombok 生成，反例：手写 get/set；禁止移除 lombok 的使用
- 局部变量强制使用 lombok val、var（硬性要求）：局部变量声明必须且一律使用 `val`/`var`（类型由初始化表达式推断，`val` 不可变、`var` 可变），不得为显式写出类型而放弃 val/var（如 `String name = ...` 必须写 `val name = ...`，`StringBuilder sb = ...` 必须写 `var sb = ...`）；val/var 仅适用于局部变量，字段不得使用；唯一例外：类型推断结果不清晰（复杂泛型、联合类型推断等）等确需显式类型提示的场景可写显式类型，此类例外须克制使用
- lombok.config 实时读取：lombok.config 内容可能调整，规范不固化具体配置项；使用 lombok 或涉及 lombok 默认行为时，实时读取项目根目录 `lombok.config`，按当前配置执行（如字段默认访问级别、@ToString/@EqualsAndHashCode 的 callSuper、@NonNull 异常类型、自定义日志注解等），不得在规范或代码注释中固化其具体配置内容
- 实体类使用 @Data、@SuperBuilder（优先 SuperBuilder 而非 @Builder，支持继承场景）、@NoArgsConstructor、@AllArgsConstructor
- 严禁手写构造方法：无参、必参、全参构造方法一律使用 lombok 注解生成（@NoArgsConstructor/@RequiredArgsConstructor/@AllArgsConstructor），禁止手写任何构造器；禁止 `new Xxx(参数...)` 创建对象，创建一律使用 builder（优先 @SuperBuilder 而非 @Builder，生成的 builder 链式赋值，如 `CClassConverter.<From, To>builder().fromClass(...).build()`）；@AllArgsConstructor 等 lombok 构造注解仅用于框架注入或序列化等特殊场景（如 Spring 构造器注入，见 AGENTS-SPRING.MD），业务创建对象不得依赖其生成的构造器
- 定义方法时参数换行：多参数且整行宽度（含缩进）临近或超过 80 字符时，参数需换行——左括号 `(` 紧跟方法名后换行，每个参数单独一行，右括号 `)` 单独一行收尾；参数较少或整行宽度未达阈值时保持单行

```java
public void doSomething(
        String firstParam,
        Integer secondParam,
        int thirdParam
) {
    // ...
}
```
- JSON 处理全局统一使用 Jackson，禁止引入其他 JSON 库（如 Gson、fastjson）
- 反射操作优先使用 MethodHandle：访问字段（getter/setter）、调用方法、构造对象等反射操作一律优先使用 `CMethodHandleUtils` 提供的现成方法句柄（字段 getter/setter、方法、构造器），避免直接使用 `Field#get/set`、`Method#invoke`、`Constructor#newInstance` 等原生反射调用；`CMethodHandleUtils` 没有现成的方法时，可在其中补充后再使用。识别特征：代码中出现 `field.get/set`、`method.invoke`、`constructor.newInstance` 等直接反射调用，应改用 `CMethodHandleUtils` 对应方法句柄（如 `getGetterHandle/getSetterHandle/getHandle`），再用 `handle.invoke/invokeExact` 执行
- 日志打印：静态文本（无参）如 `log.info("初始化完成")` 不属占位符参数化约束范围，合规；异常日志不得拼 `e.getMessage()`，须将 throwable 作为最后一个参数传入（如 `log.error("msg: {}", arg, e)`）

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
