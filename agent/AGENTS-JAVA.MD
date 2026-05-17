
# java 规范

## 编码

- 禁止移除 lombok 的使用
- 使用 lombok val、var
- 实体类使用 @Data、@SuperBuilder、@NoArgsConstructor、@AllArgsConstructor

## lambda

- e -> e 换成 CFunction.self
- (a, b) -> a 换成 CBiFunction.first()
