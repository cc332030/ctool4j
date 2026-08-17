
# spring 规范

## 配置

- @Bean 的方法名以 'c' 作为固定前缀

## 注入

- 依赖注入默认使用构造器注入；无注入字段时添加 @AllArgsConstructor 属正常写法，不作问题提醒
